package com.aaradhya.assistant.ai;

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
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000j\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\u0010\u0007\n\u0002\u0010\u0002\n\u0002\b\u0005\n\u0002\u0010\u0012\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\b\n\u0002\b\t\u0018\u0000 <2\u00020\u0001:\u0001<B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0018\u00101\u001a\u00020\u00152\u0006\u00102\u001a\u00020\u001c2\u0006\u00103\u001a\u000204H\u0002J\u0006\u00105\u001a\u00020\u0016J\u000e\u00106\u001a\u00020\u00162\u0006\u00107\u001a\u00020\u001cJ\u0006\u00108\u001a\u00020\u0016J\u0006\u00109\u001a\u00020\u0016J\u0006\u0010:\u001a\u00020\u0016J\u0006\u0010;\u001a\u00020\u0016R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0005\u001a\u0004\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0007\u001a\u0004\u0018\u00010\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u000b\u001a\u00020\fX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u000b\u0010\r\"\u0004\b\u000e\u0010\u000fR$\u0010\u0011\u001a\u00020\f2\u0006\u0010\u0010\u001a\u00020\f8F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\b\u0011\u0010\r\"\u0004\b\u0012\u0010\u000fR(\u0010\u0013\u001a\u0010\u0012\u0004\u0012\u00020\u0015\u0012\u0004\u0012\u00020\u0016\u0018\u00010\u0014X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0017\u0010\u0018\"\u0004\b\u0019\u0010\u001aR(\u0010\u001b\u001a\u0010\u0012\u0004\u0012\u00020\u001c\u0012\u0004\u0012\u00020\u0016\u0018\u00010\u0014X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u001d\u0010\u0018\"\u0004\b\u001e\u0010\u001aR\"\u0010\u001f\u001a\n\u0012\u0004\u0012\u00020\u0016\u0018\u00010 X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b!\u0010\"\"\u0004\b#\u0010$R\"\u0010%\u001a\n\u0012\u0004\u0012\u00020\u0016\u0018\u00010 X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b&\u0010\"\"\u0004\b\'\u0010$R\u0010\u0010(\u001a\u0004\u0018\u00010)X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010*\u001a\b\u0012\u0004\u0012\u00020\u001c0+X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010,\u001a\u0004\u0018\u00010)X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010-\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010.\u001a\u00020/X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u00100\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006="}, d2 = {"Lcom/aaradhya/assistant/ai/AudioEngine;", "", "()V", "_isSpeaking", "Ljava/util/concurrent/atomic/AtomicBoolean;", "audioRecord", "Landroid/media/AudioRecord;", "audioTrack", "Landroid/media/AudioTrack;", "exceptionHandler", "Lkotlinx/coroutines/CoroutineExceptionHandler;", "isMuted", "", "()Z", "setMuted", "(Z)V", "value", "isSpeaking", "setSpeaking", "onAmplitudeChanged", "Lkotlin/Function1;", "", "", "getOnAmplitudeChanged", "()Lkotlin/jvm/functions/Function1;", "setOnAmplitudeChanged", "(Lkotlin/jvm/functions/Function1;)V", "onAudioChunkReady", "", "getOnAudioChunkReady", "setOnAudioChunkReady", "onSpeakingStarted", "Lkotlin/Function0;", "getOnSpeakingStarted", "()Lkotlin/jvm/functions/Function0;", "setOnSpeakingStarted", "(Lkotlin/jvm/functions/Function0;)V", "onSpeakingStopped", "getOnSpeakingStopped", "setOnSpeakingStopped", "playbackJob", "Lkotlinx/coroutines/Job;", "playbackQueue", "Ljava/util/concurrent/LinkedBlockingQueue;", "recordJob", "recordLock", "scope", "Lkotlinx/coroutines/CoroutineScope;", "trackLock", "computeRms", "buffer", "length", "", "interruptPlayback", "queueAudio", "pcmBytes", "release", "startPlayback", "startRecording", "stopRecording", "Companion", "app_debug"})
public final class AudioEngine {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String TAG = "AudioEngine";
    public static final int MIC_SAMPLE_RATE = 16000;
    public static final int MIC_CHANNEL_IN = android.media.AudioFormat.CHANNEL_IN_MONO;
    public static final int MIC_ENCODING = android.media.AudioFormat.ENCODING_PCM_16BIT;
    public static final int SPK_SAMPLE_RATE = 24000;
    public static final int SPK_CHANNEL_OUT = android.media.AudioFormat.CHANNEL_OUT_MONO;
    public static final int SPK_ENCODING = android.media.AudioFormat.ENCODING_PCM_16BIT;
    public static final int CHUNK_SIZE = 1024;
    private static final int MAX_QUEUE_CAPACITY = 500;
    
    /**
     * Called with each PCM chunk ready to send to WebSocket
     */
    @org.jetbrains.annotations.Nullable()
    private kotlin.jvm.functions.Function1<? super byte[], kotlin.Unit> onAudioChunkReady;
    
    /**
     * Called with RMS amplitude 0..1 for waveform display
     */
    @org.jetbrains.annotations.Nullable()
    private kotlin.jvm.functions.Function1<? super java.lang.Float, kotlin.Unit> onAmplitudeChanged;
    
    /**
     * Called when audio playback starts
     */
    @org.jetbrains.annotations.Nullable()
    private kotlin.jvm.functions.Function0<kotlin.Unit> onSpeakingStarted;
    
    /**
     * Called when audio playback queue drains
     */
    @org.jetbrains.annotations.Nullable()
    private kotlin.jvm.functions.Function0<kotlin.Unit> onSpeakingStopped;
    @kotlin.jvm.Volatile()
    private volatile boolean isMuted = false;
    @org.jetbrains.annotations.NotNull()
    private final java.util.concurrent.atomic.AtomicBoolean _isSpeaking = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.Object recordLock = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.Object trackLock = null;
    @org.jetbrains.annotations.Nullable()
    private android.media.AudioRecord audioRecord;
    @org.jetbrains.annotations.Nullable()
    private android.media.AudioTrack audioTrack;
    @org.jetbrains.annotations.NotNull()
    private final java.util.concurrent.LinkedBlockingQueue<byte[]> playbackQueue = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.CoroutineExceptionHandler exceptionHandler = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.CoroutineScope scope = null;
    @org.jetbrains.annotations.Nullable()
    private kotlinx.coroutines.Job recordJob;
    @org.jetbrains.annotations.Nullable()
    private kotlinx.coroutines.Job playbackJob;
    @org.jetbrains.annotations.NotNull()
    public static final com.aaradhya.assistant.ai.AudioEngine.Companion Companion = null;
    
    public AudioEngine() {
        super();
    }
    
    /**
     * Called with each PCM chunk ready to send to WebSocket
     */
    @org.jetbrains.annotations.Nullable()
    public final kotlin.jvm.functions.Function1<byte[], kotlin.Unit> getOnAudioChunkReady() {
        return null;
    }
    
    /**
     * Called with each PCM chunk ready to send to WebSocket
     */
    public final void setOnAudioChunkReady(@org.jetbrains.annotations.Nullable()
    kotlin.jvm.functions.Function1<? super byte[], kotlin.Unit> p0) {
    }
    
    /**
     * Called with RMS amplitude 0..1 for waveform display
     */
    @org.jetbrains.annotations.Nullable()
    public final kotlin.jvm.functions.Function1<java.lang.Float, kotlin.Unit> getOnAmplitudeChanged() {
        return null;
    }
    
    /**
     * Called with RMS amplitude 0..1 for waveform display
     */
    public final void setOnAmplitudeChanged(@org.jetbrains.annotations.Nullable()
    kotlin.jvm.functions.Function1<? super java.lang.Float, kotlin.Unit> p0) {
    }
    
    /**
     * Called when audio playback starts
     */
    @org.jetbrains.annotations.Nullable()
    public final kotlin.jvm.functions.Function0<kotlin.Unit> getOnSpeakingStarted() {
        return null;
    }
    
    /**
     * Called when audio playback starts
     */
    public final void setOnSpeakingStarted(@org.jetbrains.annotations.Nullable()
    kotlin.jvm.functions.Function0<kotlin.Unit> p0) {
    }
    
    /**
     * Called when audio playback queue drains
     */
    @org.jetbrains.annotations.Nullable()
    public final kotlin.jvm.functions.Function0<kotlin.Unit> getOnSpeakingStopped() {
        return null;
    }
    
    /**
     * Called when audio playback queue drains
     */
    public final void setOnSpeakingStopped(@org.jetbrains.annotations.Nullable()
    kotlin.jvm.functions.Function0<kotlin.Unit> p0) {
    }
    
    public final boolean isMuted() {
        return false;
    }
    
    public final void setMuted(boolean p0) {
    }
    
    public final boolean isSpeaking() {
        return false;
    }
    
    public final void setSpeaking(boolean value) {
    }
    
    public final void startRecording() {
    }
    
    public final void stopRecording() {
    }
    
    public final void startPlayback() {
    }
    
    /**
     * Queue PCM audio from WebSocket for playback
     */
    public final void queueAudio(@org.jetbrains.annotations.NotNull()
    byte[] pcmBytes) {
    }
    
    /**
     * Interrupt playback — clears queue and stops current audio immediately
     */
    public final void interruptPlayback() {
    }
    
    public final void release() {
    }
    
    private final float computeRms(byte[] buffer, int length) {
        return 0.0F;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\b\n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000e"}, d2 = {"Lcom/aaradhya/assistant/ai/AudioEngine$Companion;", "", "()V", "CHUNK_SIZE", "", "MAX_QUEUE_CAPACITY", "MIC_CHANNEL_IN", "MIC_ENCODING", "MIC_SAMPLE_RATE", "SPK_CHANNEL_OUT", "SPK_ENCODING", "SPK_SAMPLE_RATE", "TAG", "", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}