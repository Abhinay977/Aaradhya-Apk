package com.aaradhya.assistant.ui.main

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import com.aaradhya.assistant.R
import kotlin.math.*
import kotlin.random.Random

class OrbAnimationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    enum class State { IDLE, LISTENING, SPEAKING, THINKING, PROCESSING, MUTED }

    private var currentState = State.IDLE
    
    // Theme colors
    private val colorBg = ContextCompat.getColor(context, R.color.aaradhya_bg)
    private val colorGold = ContextCompat.getColor(context, R.color.aaradhya_gold)
    private val colorGold2 = ContextCompat.getColor(context, R.color.aaradhya_gold2)
    private val colorGoldDim = ContextCompat.getColor(context, R.color.aaradhya_gold_dim)
    private val colorAmber = ContextCompat.getColor(context, R.color.aaradhya_amber)
    private val colorTeal = ContextCompat.getColor(context, R.color.aaradhya_teal)
    private val colorTealDim = ContextCompat.getColor(context, R.color.aaradhya_teal_dim)
    private val colorRed = ContextCompat.getColor(context, R.color.aaradhya_red)

    // Animators & Time
    private val loopAnimator = ValueAnimator.ofFloat(0f, 1f)
    private var tick = 0L
    private var lastTime = System.currentTimeMillis()

    // Dynamic state variables
    private var currentScale = 1.0f
    private var targetScale = 1.0f
    private var haloAlpha = 60f
    private var targetHaloAlpha = 60f
    
    private var scanAngle = 0f
    private var scan2Angle = 90f
    private var scan3Angle = 180f
    
    private val ringsSpin = floatArrayOf(0f, 45f, 90f, 135f)
    private val pulseRadii = mutableListOf(0f)
    
    private class Particle(
        var angle: Float,
        var r: Float,
        var speed: Float,
        var size: Float,
        var life: Float,
        var maxLife: Float
    )
    private val particles = mutableListOf<Particle>()
    
    // Paints
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.MONOSPACE
        isFakeBoldText = true
    }

    init {
        setupAnimator()
        spawnInitialParticles()
    }

    fun setState(state: State) {
        if (currentState == state) return
        currentState = state
        invalidate()
    }

    private fun isSpeaking() = currentState == State.SPEAKING
    private fun isMuted() = currentState == State.MUTED

    private fun setupAnimator() {
        loopAnimator.duration = 1000
        loopAnimator.repeatCount = ValueAnimator.INFINITE
        loopAnimator.interpolator = LinearInterpolator()
        loopAnimator.addUpdateListener {
            animateTick()
            invalidate()
        }
    }

    private fun spawnInitialParticles() {
        particles.clear()
        for (i in 0 until 18) {
            spawnParticle(randomLife = true)
        }
    }

    private fun spawnParticle(randomLife: Boolean = false) {
        // We defer actual radius calculation to onDraw time based on view size,
        // so r here is a fractional ratio (0.38 to 0.52)
        val angle = Random.nextDouble(0.0, 2 * PI).toFloat()
        val speed = Random.nextDouble(0.2, 0.9).toFloat() * if (Random.nextBoolean()) 1f else -1f
        val rFrac = Random.nextDouble(0.38, 0.52).toFloat()
        val size = Random.nextDouble(1.0, 2.5).toFloat() * 2f // scaled up slightly for hdpi
        val maxLife = Random.nextDouble(80.0, 200.0).toFloat()
        val life = if (randomLife) Random.nextDouble(0.0, 100.0).toFloat() else 0f
        
        particles.add(Particle(angle, rFrac, speed, size, life, maxLife))
    }

    private fun updateParticles() {
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            p.angle += p.speed * 0.012f * (if (isSpeaking()) 2.5f else 1.0f)
            p.life += 1f
            if (p.life >= p.maxLife) {
                iterator.remove()
            }
        }
        while (particles.size < 18) {
            spawnParticle()
        }
    }

    private fun animateTick() {
        tick++
        val now = System.currentTimeMillis()
        val dt = (now - lastTime) / 1000f
        
        // Update targets every ~0.12s or ~0.5s
        if (now - lastTime > (if (isSpeaking()) 120 else 500)) {
            if (isSpeaking()) {
                targetScale = Random.nextDouble(1.06, 1.13).toFloat()
                targetHaloAlpha = Random.nextDouble(150.0, 200.0).toFloat()
            } else if (isMuted()) {
                targetScale = 1.0f
                targetHaloAlpha = 20f
            } else {
                targetScale = Random.nextDouble(1.001, 1.006).toFloat()
                targetHaloAlpha = Random.nextDouble(55.0, 75.0).toFloat()
            }
            lastTime = now
        }

        val sp = if (isSpeaking()) 0.32f else 0.14f
        currentScale += (targetScale - currentScale) * sp
        haloAlpha += (targetHaloAlpha - haloAlpha) * sp

        val ringSpeeds = if (isSpeaking()) floatArrayOf(1.4f, -0.9f, 2.1f, -1.3f) 
                         else floatArrayOf(0.55f, -0.33f, 0.88f, -0.5f)
        
        for (i in 0..3) {
            ringsSpin[i] = (ringsSpin[i] + ringSpeeds[i]) % 360f
        }

        scanAngle = (scanAngle + (if (isSpeaking()) 3.2f else 1.3f)) % 360f
        scan2Angle = (scan2Angle + (if (isSpeaking()) -2.0f else -0.9f)) % 360f
        scan3Angle = (scan3Angle + (if (isSpeaking()) 1.5f else 0.6f)) % 360f

        val pspd = if (isSpeaking()) 4.2f else 1.9f
        // limit is relative to view size, will calculate in draw. Just increment radius.
        for (i in pulseRadii.indices) {
            pulseRadii[i] += pspd
        }
        
        // Remove pulses that are too large (assume max radius ~600 for removal, will clamp in draw)
        pulseRadii.removeAll { it > 800f }
        
        if (pulseRadii.size < 4 && Random.nextDouble() < (if (isSpeaking()) 0.07 else 0.025)) {
            pulseRadii.add(0f)
        }

        updateParticles()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        loopAnimator.start()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        loopAnimator.cancel()
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        if (visibility == VISIBLE) {
            if (!loopAnimator.isRunning) loopAnimator.start()
        } else {
            loopAnimator.cancel()
        }
    }

    // Helper to get Color with modified alpha
    private fun ac(baseColor: Int, alpha: Int): Int {
        val a = alpha.coerceIn(0, 255)
        return Color.argb(a, Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor))
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        val cx = w / 2f
        val cy = h / 2f
        val fw = min(w, h) - 20f // base width
        val FW = fw

        // ── Subtle radial glow behind orb ──
        val glowR = (FW * 0.72f).toInt()
        val haInt = haloAlpha.toInt()
        
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        
        for (r in glowR downTo (FW * 0.25f).toInt() step 30) {
            val frac = (r - FW * 0.25f) / (glowR - FW * 0.25f)
            val ga = if (isMuted()) {
                max(0, min(40, (haInt * 0.12f * (1f - frac)).toInt()))
            } else {
                max(0, min(55, (haInt * 0.15f * (1f - frac)).toInt()))
            }
            val col = if (isMuted()) ac(colorRed, ga) else ac(colorGold, ga)
            paint.color = col
            canvas.drawCircle(cx, cy, r.toFloat(), paint)
        }

        // ── Pulse rings ──
        val limit = FW * 0.68f
        for (pr in pulseRadii) {
            if (pr > limit) continue
            val pa = max(0, (200f * (1.0f - pr / limit)).toInt())
            val col = if (isMuted()) ac(colorRed, pa / 4) else ac(colorGold, pa)
            paint.color = col
            canvas.drawCircle(cx, cy, pr, paint)
        }

        // ── Rotating arc rings ──
        // (r_frac, w_ring, arc_l, gap)
        val ringDefs = arrayOf(
            floatArrayOf(0.44f, 6f, 100f, 80f),
            floatArrayOf(0.36f, 4f, 70f, 60f),
            floatArrayOf(0.29f, 2f, 55f, 45f),
            floatArrayOf(0.22f, 2f, 40f, 35f)
        )
        
        for (idx in ringDefs.indices) {
            val def = ringDefs[idx]
            val ringR = FW * def[0]
            val wRing = def[1]
            val arcL = def[2]
            val gap = def[3]
            
            val baseA = ringsSpin[idx]
            val aVal = max(0, min(255, (haloAlpha * (1.0f - idx * 0.15f)).toInt()))
            
            val col = if (isMuted()) {
                ac(colorRed, aVal)
            } else {
                if (idx % 2 == 0) ac(colorGold, aVal)
                else ac(colorTeal, (aVal * 0.65f).toInt())
            }
            
            paint.color = col
            paint.strokeWidth = wRing
            val rect = RectF(cx - ringR, cy - ringR, cx + ringR, cy + ringR)
            
            val step = (arcL + gap).toInt()
            if (step > 0) {
                for (s in 0..360/step) {
                    val start = (baseA + s * step) % 360f
                    canvas.drawArc(rect, start, arcL, false, paint)
                }
            }
        }

        // ── Scan sweepers ──
        val sr = FW * 0.47f
        val scanA = min(255, (haloAlpha * 1.5f).toInt())
        val scanRect = RectF(cx - sr, cy - sr, cx + sr, cy + sr)
        paint.strokeWidth = 4f
        
        // Scan 1
        val ext1 = if (isSpeaking()) 60f else 38f
        paint.color = ac(colorGold, scanA)
        canvas.drawArc(scanRect, scanAngle, ext1, false, paint)
        
        // Scan 2
        val ext2 = if (isSpeaking()) 45f else 28f
        paint.color = ac(colorTeal, (scanA * 0.7f).toInt())
        canvas.drawArc(scanRect, scan2Angle, ext2, false, paint)
        
        // Scan 3
        val ext3 = if (isSpeaking()) 30f else 20f
        paint.color = ac(colorAmber, (scanA * 0.5f).toInt())
        canvas.drawArc(scanRect, scan3Angle, ext3, false, paint)

        // ── Tick marks ──
        val tOut = FW * 0.48f
        val tInBase = FW * 0.462f
        for (deg in 0 until 360 step 6) {
            val rad = Math.toRadians(deg.toDouble())
            val inn = if (deg % 30 == 0) tInBase 
                      else if (deg % 10 == 0) tInBase + 8f 
                      else tInBase + 14f
            val wLine = if (deg % 30 == 0) 4f else 2f
            val col = ac(colorGold, if (deg % 30 == 0) 130 else 60)
            
            paint.color = col
            paint.strokeWidth = wLine
            
            val x1 = cx + tOut * cos(rad).toFloat()
            val y1 = cy - tOut * sin(rad).toFloat()
            val x2 = cx + inn * cos(rad).toFloat()
            val y2 = cy - inn * sin(rad).toFloat()
            canvas.drawLine(x1, y1, x2, y2, paint)
        }

        // ── Crosshair ──
        val chR = FW * 0.49f
        val chGap = FW * 0.12f
        paint.color = ac(colorGold, (haloAlpha * 0.45f).toInt())
        paint.strokeWidth = 2f
        
        canvas.drawLine(cx - chR, cy, cx - chGap, cy, paint)
        canvas.drawLine(cx + chGap, cy, cx + chR, cy, paint)
        canvas.drawLine(cx, cy - chR, cx, cy - chGap, paint)
        canvas.drawLine(cx, cy + chGap, cx, cy + chR, paint)

        // ── Corner brackets ──
        val blen = 40f
        val bc = ac(colorGold, 180)
        val hl = cx - FW / 2f
        val hr = cx + FW / 2f
        val ht = cy - FW / 2f
        val hb = cy + FW / 2f
        
        paint.color = bc
        paint.strokeWidth = 4f
        paint.style = Paint.Style.STROKE
        
        val bracketPts = arrayOf(
            floatArrayOf(hl, ht, 1f, 1f),
            floatArrayOf(hr, ht, -1f, 1f),
            floatArrayOf(hl, hb, 1f, -1f),
            floatArrayOf(hr, hb, -1f, -1f)
        )
        
        for (bp in bracketPts) {
            val bx = bp[0]
            val by = bp[1]
            val sdx = bp[2]
            val sdy = bp[3]
            
            canvas.drawLine(bx, by, bx + sdx * blen, by, paint)
            canvas.drawLine(bx, by, bx, by + sdy * blen, paint)
            
            // corner dot
            paint.style = Paint.Style.FILL
            paint.color = colorGold2
            canvas.drawCircle(bx - sdx * 4f, by - sdy * 4f, 4f, paint)
            paint.style = Paint.Style.STROKE
            paint.color = bc
        }

        // ── Particles ──
        paint.style = Paint.Style.FILL
        for (p in particles) {
            val orbitR = FW * p.r
            val px = cx + orbitR * cos(p.angle)
            val py = cy + orbitR * sin(p.angle)
            val lifeFrac = p.life / p.maxLife
            val fade = (180f * (1f - abs(lifeFrac * 2f - 1f))).toInt().coerceIn(0, 255)
            
            val col = if (isMuted()) {
                ac(colorRed, fade)
            } else {
                if (lifeFrac < 0.5f) ac(colorGold, fade) else ac(colorTeal, fade)
            }
            
            paint.color = col
            canvas.drawCircle(px, py, p.size, paint)
        }

        // ── Face / Orb ──
        val orbR = FW * 0.24f * currentScale
        
        val layers = arrayOf(
            floatArrayOf(orbR + 56f, 20f,  200f, 100f, 0f),
            floatArrayOf(orbR + 32f, 45f,  220f, 130f, 0f),
            floatArrayOf(orbR + 16f, 80f,  240f, 160f, 0f),
            floatArrayOf(orbR,       140f, 255f, 200f, 0f),
            floatArrayOf(orbR * 0.65f, 200f, 255f, 220f, 40f),
            floatArrayOf(orbR * 0.35f, 255f, 255f, 240f, 120f)
        )
        
        val mutedLayers = arrayOf(
            floatArrayOf(orbR + 56f, 20f,  200f, 20f, 40f),
            floatArrayOf(orbR + 32f, 45f,  200f, 20f, 40f),
            floatArrayOf(orbR + 16f, 80f,  200f, 20f, 40f),
            floatArrayOf(orbR,       140f, 200f, 20f, 40f),
            floatArrayOf(orbR * 0.65f, 200f, 200f, 20f, 40f),
            floatArrayOf(orbR * 0.35f, 255f, 200f, 20f, 40f)
        )
        
        val activeLayers = if (isMuted()) mutedLayers else layers
        
        for (l in activeLayers) {
            val r2 = l[0]
            val a2 = l[1].toInt()
            val rr = l[2].toInt()
            val gg = l[3].toInt()
            val bb = l[4].toInt()
            
            paint.color = Color.argb(a2, rr, gg, bb)
            canvas.drawCircle(cx, cy, r2, paint)
        }

        // JARVIS text (fallback to AARADHYA)
        val textAlpha = min(255, (haloAlpha * 2.2f).toInt())
        textPaint.color = Color.argb(textAlpha, 240, 180, 0)
        textPaint.textSize = FW * 0.05f
        
        canvas.drawText("AARADHYA", cx, cy + textPaint.textSize * 0.35f, textPaint)
    }
}
