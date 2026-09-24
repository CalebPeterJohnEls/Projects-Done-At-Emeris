package com.example.betterbudget_poe

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.abs

class BubbleBackgroundView @JvmOverloads constructor( // (JetBrains, 2024 ; Lerpoux, 2021)
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    data class Bubble( // (Android Developers, 2026a)
        var x: Float, var y: Float,
        var radius: Float,
        var dx: Float, var dy: Float,
        var color: Int
    )

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val bubbles = mutableListOf<Bubble>()
    private var bgColor = Color.BLACK
    private var initialized = false
    private val animator = object : Runnable { // (Android Developers, 2026b)
        override fun run() {
            update()
            invalidate()
            postDelayed(this, 16L) // ~60fps
        }
    }

    // Four themes: default, light, ocean, sunset
    companion object {
        val THEMES = mapOf(
            "default" to Triple(
                Color.parseColor("#000000"),
                listOf("#1B5E20", "#2E7D32", "#4DC9A9"),
                listOf(180f, 130f, 100f)
            ),
            "light" to Triple(
                Color.parseColor("#E0E0E0"),
                listOf("#66BB6A", "#81C784", "#A5D6A7"),
                listOf(180f, 130f, 100f)
            ),
            "ocean" to Triple(
                Color.parseColor("#0D1B2A"),
                listOf("#0077B6", "#00B4D8", "#90E0EF"),
                listOf(180f, 130f, 100f)
            ),
            "sunset" to Triple(
                Color.parseColor("#1A0A00"),
                listOf("#BF360C", "#E64A19", "#FF8A65"),
                listOf(180f, 130f, 100f)
            )
        )
    }

    fun applyTheme(themeKey: String) {
        val theme = THEMES[themeKey] ?: THEMES["default"]!!
        bgColor = theme.first
        setBackgroundColor(bgColor)

        if (width > 0 && height > 0) {
            initBubbles(theme.second, theme.third)
        } else {
            post {
                initBubbles(theme.second, theme.third)
            }
        }
    }

    private fun initBubbles(colors: List<String>, radii: List<Float>) { // (Android Developers, 2026a)
        bubbles.clear()
        colors.forEachIndexed { i, hex ->
            val r = radii[i]
            bubbles.add(
                Bubble(
                    x = (r + Math.random().toFloat() * (width - 2 * r)),
                    y = (r + Math.random().toFloat() * (height - 2 * r)),
                    radius = r,
                    dx = (1.5f + Math.random().toFloat() * 1.5f) * if (Math.random() > 0.5) 1 else -1,
                    dy = (1.5f + Math.random().toFloat() * 1.5f) * if (Math.random() > 0.5) 1 else -1,
                    color = Color.parseColor(hex)
                )
            )
        }
        initialized = true
    }

    private fun update() {
        if (!initialized) return
        for (b in bubbles) {
            b.x += b.dx
            b.y += b.dy
            if (b.x - b.radius <= 0 || b.x + b.radius >= width) b.dx = -b.dx
            if (b.y - b.radius <= 0 || b.y + b.radius >= height) b.dy = -b.dy

            // Bounce bubbles off each other
            for (other in bubbles) {
                if (b === other) continue
                val distX = b.x - other.x
                val distY = b.y - other.y
                val dist = Math.sqrt((distX * distX + distY * distY).toDouble()).toFloat()
                if (dist < b.radius + other.radius) {
                    b.dx = -b.dx
                    b.dy = -b.dy
                }
            }
        }
    }

    override fun onDraw(canvas: Canvas) { // (Android Developers, 2026a)
        super.onDraw(canvas)
        canvas.drawColor(bgColor)
        for (b in bubbles) {
            paint.color = b.color
            paint.alpha = 180
            canvas.drawCircle(b.x, b.y, b.radius, paint)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        post(animator)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeCallbacks(animator)
    }
}

/*Reference List:
Android Developers, 2026a. Canvas and Drawables. [online] Available at: https://developer.android.com/develop/ui/views/graphics/drawables [Accessed 1 June 2026].
Android Developers, 2026b. View.postDelayed. [online] Available at: https://developer.android.com/reference/android/view/View#postDelayed(java.lang.Runnable,%20long) [Accessed 1 June 2026]
JetBrains, 2024. IntelliJ Platform Threading. [online] Available at: https://jb.gg/ij-platform-threading [Accessed 1 June 2026].
Lerpoux, G., 2021. Simple ball collision detection in Android Canvas. [online] Stack Overflow. Available at: https://stackoverflow.com/questions/345838/ball-to-ball-collision-detection-and-handling [Accessed 1 June 2026].
*/
