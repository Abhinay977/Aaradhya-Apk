package com.aaradhya.assistant.ai

import android.media.*
import android.util.Log
import kotlinx.coroutines.*
import java.util.concurrent.LinkedBlockingQueue
import kotlin.math.sqrt

/**
 * Manages PCM audio I/O:
 * - AudioRecord: 16kHz, Mono, PCM-16 (mic input → WebSocket)
 * - AudioTrack: 24kHz, Mono, PCM-16 (WebSocket audio → speaker)
 *
 * Echo suppression: while isSpeaking=true, mic data is discarded (not sent to WS)
 *
 * FIXES:
 * - Bounded playbackQueue (500 chunks max) to prevent OOM crash after 1-2 min
 * - @Volatile + AtomicBoolean for isSpeaking to fix multi-thread race conditions
 * - recordJob finally block no longer calls stopRecording() to prevent self-deadlock
 * - Local snapshot of audioRecord reference inside coroutine to prevent null race
 */
class AudioEngine {
    companion object {
        private const val TAG = "AudioEngine"

        // Mic
        const val MIC_SAMPLE_RATE  = 16000
        const val MIC_CHANNEL_IN   = AudioFormat.CHANNEL_IN_MONO
        const val MIC_ENCODING     = AudioFormat.ENCODING_PCM_16BIT

        // Speaker
        const val SPK_SAMPLE_RATE  = 24000
        const val SPK_CHANNEL_OUT  = AudioFormat.CHANNEL_OUT_MONO
        const val SPK_ENCODING     = AudioFormat.ENCODING_PCM_16BIT

        const val CHUNK_SIZE = 1024

        // FIX #1: Bounded queue — prevents OOM crash when Gemini floods audio faster than playback
        private const val MAX_QUEUE_CAPACITY = 500
    }

    // ── Callbacks ──────────────────────────────────────────────────────────────
    /** Called with each PCM chunk ready to send to WebSocket */
    var onAudioChunkReady: ((ByteArray) -> Unit)? = null
    /** Called with RMS amplitude 0..1 for waveform display */
    var onAmplitudeChanged: ((Float) -> Unit)? = null
    /** Called when audio playback starts */
    var onSpeakingStarted: (() -> Unit)? = null
    /** Called when audio playback queue drains */
    var onSpeakingStopped: (() -> Unit)? = null

    // ── State ──────────────────────────────────────────────────────────────────
    @Volatile var isMuted    = false

    // FIX #2: Use java.util.concurrent.atomic for isSpeaking to prevent race conditions
    private val _isSpeaking = java.util.concurrent.atomic.AtomicBoolean(false)
    var isSpeaking: Boolean
        get() = _isSpeaking.get()
        set(value) { _isSpeaking.set(value) }

    // FIX #3: Use lock objects to guard audioRecord and audioTrack access across threads
    private val recordLock = Any()
    private val trackLock = Any()
    private var audioRecord: AudioRecord? = null
    private var audioTrack:  AudioTrack?  = null

    // FIX #1: Bounded queue to prevent memory exhaustion
    private val playbackQueue = LinkedBlockingQueue<ByteArray>(MAX_QUEUE_CAPACITY)

    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        Log.e(TAG, "AudioEngine Coroutine Exception: ${exception.message}")
    }
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob() + exceptionHandler)
    private var recordJob:   Job? = null
    private var playbackJob: Job? = null

    // ── Recording ──────────────────────────────────────────────────────────────

    fun startRecording() {
        if (recordJob?.isActive == true) return

        val minBuf = AudioRecord.getMinBufferSize(MIC_SAMPLE_RATE, MIC_CHANNEL_IN, MIC_ENCODING)
        val bufSize = maxOf(minBuf, CHUNK_SIZE * 4)

        try {
            val recorder = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                MIC_SAMPLE_RATE,
                MIC_CHANNEL_IN,
                MIC_ENCODING,
                bufSize
            )

            if (recorder.state != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "AudioRecord failed to initialize")
                recorder.release()
                return
            }

            synchronized(recordLock) {
                audioRecord = recorder
            }

            recorder.startRecording()

            recordJob = scope.launch {
                val buffer = ByteArray(CHUNK_SIZE)
                // FIX #4: Capture a local reference to avoid null race with stopRecording()
                val localRecorder = recorder
                try {
                    while (isActive && localRecorder.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                        val read = localRecorder.read(buffer, 0, buffer.size)
                        if (read > 0) {
                            val rms = computeRms(buffer, read)
                            onAmplitudeChanged?.invoke(rms)

                            // Echo suppression: don't send mic audio while speaking
                            if (!isMuted && !isSpeaking) {
                                onAudioChunkReady?.invoke(buffer.copyOf(read))
                            }
                        } else if (read < 0) {
                            Log.e(TAG, "AudioRecord read error: $read")
                            break
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error during audio recording: ${e.message}")
                }
                // FIX #5: Do NOT call stopRecording() here — it would cancel this job itself
                // Just release the local recorder reference safely
                try {
                    if (localRecorder.state == AudioRecord.STATE_INITIALIZED) {
                        localRecorder.stop()
                    }
                    localRecorder.release()
                } catch (e: Exception) {
                    Log.e(TAG, "Error releasing recorder in coroutine: ${e.message}")
                }
                synchronized(recordLock) {
                    if (audioRecord === localRecorder) audioRecord = null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start AudioRecord: ${e.message}")
            synchronized(recordLock) {
                audioRecord?.release()
                audioRecord = null
            }
        }
    }

    fun stopRecording() {
        recordJob?.cancel()
        recordJob = null
        val recorder = synchronized(recordLock) {
            val r = audioRecord
            audioRecord = null
            r
        }
        try {
            if (recorder?.state == AudioRecord.STATE_INITIALIZED) {
                recorder.stop()
            }
            recorder?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping AudioRecord: ${e.message}")
        }
    }

    // ── Playback ───────────────────────────────────────────────────────────────

    fun startPlayback() {
        if (playbackJob?.isActive == true) return

        val minBuf = AudioTrack.getMinBufferSize(SPK_SAMPLE_RATE, SPK_CHANNEL_OUT, SPK_ENCODING)
        val bufSize = maxOf(minBuf, CHUNK_SIZE * 8)

        try {
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANT)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setSampleRate(SPK_SAMPLE_RATE)
                        .setChannelMask(SPK_CHANNEL_OUT)
                        .setEncoding(SPK_ENCODING)
                        .build()
                )
                .setBufferSizeInBytes(bufSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            synchronized(trackLock) {
                audioTrack = track
                track.play()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize or play AudioTrack: ${e.message}")
        }

        playbackJob = scope.launch {
            while (isActive) {
                val chunk = try {
                    playbackQueue.poll(100, java.util.concurrent.TimeUnit.MILLISECONDS)
                } catch (e: InterruptedException) { break }

                if (chunk != null) {
                    // FIX #2: Use compareAndSet for thread-safe isSpeaking transition
                    if (_isSpeaking.compareAndSet(false, true)) {
                        withContext(Dispatchers.Main) { onSpeakingStarted?.invoke() }
                    }
                    try {
                        synchronized(trackLock) {
                            if (audioTrack?.state == AudioTrack.STATE_INITIALIZED) {
                                audioTrack?.write(chunk, 0, chunk.size)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "AudioTrack write error: ${e.message}")
                    }
                } else {
                    // Queue is empty
                    if (isSpeaking) {
                        // Wait a bit to see if more audio arrives
                        delay(150)
                        if (playbackQueue.isEmpty()) {
                            if (_isSpeaking.compareAndSet(true, false)) {
                                withContext(Dispatchers.Main) { onSpeakingStopped?.invoke() }
                            }
                        }
                    }
                }
            }
        }
    }

    /** Queue PCM audio from WebSocket for playback */
    fun queueAudio(pcmBytes: ByteArray) {
        // FIX #1: offer() drops the chunk if queue is full instead of blocking/crashing
        val offered = playbackQueue.offer(pcmBytes)
        if (!offered) {
            Log.w(TAG, "Playback queue full — dropping audio chunk to prevent OOM")
        }
    }

    /** Interrupt playback — clears queue and stops current audio immediately */
    fun interruptPlayback() {
        playbackQueue.clear()
        try {
            synchronized(trackLock) {
                if (audioTrack?.state == AudioTrack.STATE_INITIALIZED) {
                    audioTrack?.pause()
                    audioTrack?.flush()
                    audioTrack?.play()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "AudioTrack interrupt error: ${e.message}")
        }
        if (_isSpeaking.compareAndSet(true, false)) {
            scope.launch(Dispatchers.Main) { onSpeakingStopped?.invoke() }
        }
    }

    fun release() {
        scope.cancel()
        stopRecording()
        playbackJob?.cancel()
        synchronized(trackLock) {
            try {
                if (audioTrack?.state == AudioTrack.STATE_INITIALIZED) {
                    audioTrack?.stop()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping AudioTrack: ${e.message}")
            }
            try {
                audioTrack?.release()
            } catch (e: Exception) {
                Log.e(TAG, "Error releasing AudioTrack: ${e.message}")
            }
            audioTrack = null
        }
        playbackQueue.clear()
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private fun computeRms(buffer: ByteArray, length: Int): Float {
        if (length < 2) return 0f
        var sum = 0.0
        var i = 0
        while (i + 1 < length) {
            // PCM-16: little-endian short
            val sample = (buffer[i].toInt() and 0xFF) or (buffer[i + 1].toInt() shl 8)
            sum += sample.toDouble() * sample.toDouble()
            i += 2
        }
        val samplesCount = length / 2
        if (samplesCount == 0) return 0f

        val rms = sqrt(sum / samplesCount)
        // Normalize to 0..1 (max PCM-16 value = 32768)
        val normalized = (rms / 32768.0).toFloat()
        return if (normalized.isNaN()) 0f else normalized.coerceIn(0f, 1f)
    }
}
