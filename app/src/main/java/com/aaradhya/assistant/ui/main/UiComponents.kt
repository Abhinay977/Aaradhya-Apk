package com.aaradhya.assistant.ui.main

import android.animation.ValueAnimator
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.*
import android.net.TrafficStats
import android.os.BatteryManager
import android.os.SystemClock
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.aaradhya.assistant.R
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.sin

// ══════════════════════════════════════════════════════════════════════════════
// WaveformView
// ══════════════════════════════════════════════════════════════════════════════

class WaveformView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val BAR_COUNT = 20
    private val barHeights = FloatArray(BAR_COUNT) { 0.1f }
    private val targetHeights = FloatArray(BAR_COUNT) { 0.1f }

    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var isAnimating = false
    private var tick = 0f

    // Gold theme color
    private val colorGold = ContextCompat.getColor(context, R.color.aaradhya_gold)
    private val colorGoldDim = ContextCompat.getColor(context, R.color.aaradhya_gold_dim)

    private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 16
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener {
            tick += 0.1f
            updateBars()
            invalidate()
        }
    }

    fun startAnimation() {
        isAnimating = true
        if (!animator.isRunning) animator.start()
    }

    fun stopAnimation() {
        isAnimating = false
        // Do not reset heights immediately to let them animate down, but switch to idle sine wave
    }

    fun setAmplitude(rms: Float) {
        val amp = rms.coerceIn(0f, 1f)
        
        // Auto-switch to active mode if audio is detected
        if (amp > 0.05f) {
            isAnimating = true
        } else if (amp < 0.02f) {
            isAnimating = false
        }

        val base = 0.05f + amp * 0.15f
        for (i in 0 until BAR_COUNT) {
            val randomFactor = 0.5f + Math.random().toFloat() * amp
            targetHeights[i] = (base + randomFactor * amp * 0.5f).coerceIn(0.05f, 1f)
        }
    }

    private fun updateBars() {
        if (!isAnimating) {
            // Idle animation: sine wave
            for (i in 0 until BAR_COUNT) {
                targetHeights[i] = 0.1f + (sin(tick * 0.5f + i * 0.5f).toFloat() * 0.05f)
            }
        }
        for (i in 0 until BAR_COUNT) {
            barHeights[i] += (targetHeights[i] - barHeights[i]) * 0.3f
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val barWidth = width.toFloat() / (BAR_COUNT * 1.5f)
        val gap = barWidth * 0.5f
        val centerY = height / 2f

        for (i in 0 until BAR_COUNT) {
            val h = barHeights[i] * height * 0.9f
            val left = i * (barWidth + gap) + gap / 2f
            val right = left + barWidth
            val top = centerY - h / 2f
            val bottom = centerY + h / 2f

            // Color based on height/activity
            if (isAnimating && barHeights[i] > 0.3f) {
                barPaint.color = colorGold
            } else {
                barPaint.color = colorGoldDim
            }
            
            canvas.drawRoundRect(left, top, right, bottom, barWidth / 2f, barWidth / 2f, barPaint)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!animator.isRunning) animator.start()
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        if (visibility == View.VISIBLE) {
            if (!animator.isRunning) animator.start()
        } else {
            animator.cancel()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator.cancel()
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// SystemVitalsView
// ══════════════════════════════════════════════════════════════════════════════

class SystemVitalsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.MONOSPACE
        textSize = 24f
    }
    private val boldTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.defaultFromStyle(Typeface.BOLD)
        textSize = 24f
    }

    private val colorText = ContextCompat.getColor(context, R.color.aaradhya_text)
    private val colorText2 = ContextCompat.getColor(context, R.color.aaradhya_text2)
    private val colorBorder = ContextCompat.getColor(context, R.color.aaradhya_border)
    private val colorAmber = ContextCompat.getColor(context, R.color.aaradhya_amber)
    private val colorGold = ContextCompat.getColor(context, R.color.aaradhya_gold)
    private val colorGoldDim = ContextCompat.getColor(context, R.color.aaradhya_gold_dim)
    private val colorTeal = ContextCompat.getColor(context, R.color.aaradhya_teal)

    private var tick = 0f

    // Values to display
    var cpuLoad = 0
    var ramUsage = 0
    var batteryLevel = 0
    var netTx = 0f
    var netRx = 0f
    var uptimeStr = "--"
    var procCount = 0

    private var isSpeaking = false

    private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 50
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener {
            tick += 0.5f
            invalidate()
        }
    }

    fun setIsSpeaking(speaking: Boolean) {
        isSpeaking = speaking
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!animator.isRunning) animator.start()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator.cancel()
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        if (visibility == View.VISIBLE) {
            if (!animator.isRunning) animator.start()
        } else {
            animator.cancel()
        }
    }

    fun updateVitals(
        cpu: Int, ram: Int, bat: Int, 
        tx: Float, rx: Float, 
        uptime: String, procs: Int
    ) {
        this.cpuLoad = cpu
        this.ramUsage = ram
        this.batteryLevel = bat
        this.netTx = tx
        this.netRx = rx
        this.uptimeStr = uptime
        this.procCount = procs
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        if (w < 10 || h < 10) return

        val padX = 30f
        var currentY = 40f

        val cw = w - padX * 2
        val barH = 16f
        val blockH = 80f

        // Stats array
        val statsData = listOf(
            Triple("CPU LOAD", cpuLoad, colorAmber),
            Triple("RAM USAGE", ramUsage, colorGold),
            Triple("BATTERY", batteryLevel, colorTeal)
        )

        for ((label, value, color) in statsData) {
            // Label
            boldTextPaint.color = colorText2
            boldTextPaint.textAlign = Paint.Align.LEFT
            canvas.drawText(label, padX, currentY, boldTextPaint)

            // Value
            boldTextPaint.color = color
            boldTextPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText("$value%", padX + cw, currentY, boldTextPaint)

            currentY += 20f

            // Bar background
            paint.style = Paint.Style.FILL
            paint.color = Color.argb(60, Color.red(color), Color.green(color), Color.blue(color))
            canvas.drawRect(padX, currentY, padX + cw, currentY + barH, paint)
            
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2f
            paint.color = colorBorder
            canvas.drawRect(padX, currentY, padX + cw, currentY + barH, paint)

            // Bar fill
            val pct = (value / 100f).coerceIn(0f, 1f)
            val fillW = cw * pct
            if (fillW > 2f) {
                val shimmer = if (isSpeaking) (10 * sin(tick * 0.5f)).toInt() else 0
                val actualW = (fillW + shimmer).coerceIn(0f, cw)
                paint.style = Paint.Style.FILL
                paint.color = color
                canvas.drawRect(padX, currentY, padX + actualW, currentY + barH, paint)
            }

            // Ticks
            paint.style = Paint.Style.STROKE
            paint.color = colorBorder
            paint.strokeWidth = 2f
            for (step in 10 until 100 step 10) {
                val tx = padX + (cw * step / 100f)
                canvas.drawLine(tx, currentY, tx, currentY + barH, paint)
            }

            currentY += blockH
        }

        // ── Network info ──
        currentY += 20f
        paint.color = colorBorder
        canvas.drawLine(padX, currentY, w - padX, currentY, paint)
        currentY += 40f

        boldTextPaint.color = colorText2
        boldTextPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("NET I/O", padX, currentY, boldTextPaint)
        
        currentY += 35f
        textPaint.color = colorTeal
        canvas.drawText("↑ ${String.format("%.1f", netTx)} MB", padX, currentY, textPaint)
        
        currentY += 35f
        val colorGold2 = ContextCompat.getColor(context, R.color.aaradhya_gold2)
        textPaint.color = colorGold2
        canvas.drawText("↓ ${String.format("%.1f", netRx)} MB", padX, currentY, textPaint)

        // ── Uptime ──
        currentY += 60f
        boldTextPaint.color = colorText2
        canvas.drawText("UPTIME", padX, currentY, boldTextPaint)
        boldTextPaint.color = colorText
        boldTextPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText(uptimeStr, padX + cw, currentY, boldTextPaint)

        // ── Process count ──
        currentY += 40f
        boldTextPaint.color = colorText2
        boldTextPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("PROCESSES", padX, currentY, boldTextPaint)
        boldTextPaint.color = colorText
        boldTextPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText(procCount.toString(), padX + cw, currentY, boldTextPaint)

        // ── Mini waveform at bottom ──
        val wy = h - 60f
        paint.color = colorBorder
        paint.strokeWidth = 2f
        canvas.drawLine(padX, wy, w - padX, wy, paint)
        
        paint.style = Paint.Style.FILL
        for (xi in 0 until cw.toInt() step 12) {
            val amp = if (isSpeaking) kotlin.random.Random.nextInt(4, 28) else (4 + sin(tick * 0.2f + xi * 0.1f) * 6).toInt()
            paint.color = if (isSpeaking) colorGold else colorGoldDim
            canvas.drawRect(padX + xi, wy + 20f - amp, padX + xi + 6f, wy + 20f, paint)
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// ChatMessage & ChatAdapter
// ══════════════════════════════════════════════════════════════════════════════

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

class ChatAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val messages = mutableListOf<ChatMessage>()
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    companion object {
        private const val TYPE_USER    = 0
        private const val TYPE_AARADHYA = 1
    }

    fun addMessage(msg: ChatMessage) {
        if (!msg.isUser && messages.lastOrNull { !it.isUser }?.text == msg.text) return
        messages.add(msg)
        notifyItemInserted(messages.size - 1)
    }

    fun appendToLastMessage(text: String, isUser: Boolean) {
        if (messages.isEmpty() || messages.last().isUser != isUser) {
            messages.add(ChatMessage(text, isUser))
            notifyItemInserted(messages.size - 1)
        } else {
            val last = messages.removeAt(messages.size - 1)
            messages.add(last.copy(text = last.text + text))
            notifyItemChanged(messages.size - 1)
        }
    }

    override fun getItemViewType(position: Int) =
        if (messages[position].isUser) TYPE_USER else TYPE_AARADHYA

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_USER) {
            UserViewHolder(inflater.inflate(R.layout.item_chat_user, parent, false))
        } else {
            AaradhyaViewHolder(inflater.inflate(R.layout.item_chat_aaradhya, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = messages[position]
        when (holder) {
            is UserViewHolder    -> holder.bind(msg)
            is AaradhyaViewHolder -> holder.bind(msg)
        }
    }

    override fun getItemCount() = messages.size

    inner class UserViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(R.id.chatUserText)
        private val timeView: TextView = itemView.findViewById(R.id.chatUserTime)
        fun bind(msg: ChatMessage) {
            textView.text = msg.text
            timeView.text = timeFormat.format(Date(msg.timestamp))
        }
    }

    inner class AaradhyaViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(R.id.chatAaradhyaText)
        private val timeView: TextView = itemView.findViewById(R.id.chatAaradhyaTime)
        fun bind(msg: ChatMessage) {
            textView.text = msg.text
            timeView.text = timeFormat.format(Date(msg.timestamp))
        }
    }
}
