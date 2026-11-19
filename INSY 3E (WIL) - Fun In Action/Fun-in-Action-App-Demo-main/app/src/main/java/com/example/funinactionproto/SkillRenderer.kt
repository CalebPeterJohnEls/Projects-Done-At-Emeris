package com.example.funinactionproto

import android.content.Context
import android.graphics.Typeface
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView

// Object responsible for rendering UI elements, instead of manually creating a display for each skill
object SkillRenderer {

    fun render(
        context: Context,
        container: LinearLayout,
        skill: Skill
    ) {

        container.removeAllViews()

        // Main title
        addText(
            context = context,
            container = container,
            text = skill.title,
            textSize = 28f,
            bold = true,
            bottomMargin = 16
        )

        // Technique name
        skill.techniqueName?.let {

            addText(
                context = context,
                container = container,
                text = it,
                textSize = 20f,
                bold = true,
                bottomMargin = 8
            )
        }

        // Description
        skill.description?.let {

            addText(
                context = context,
                container = container,
                text = it,
                textSize = 16f,
                bottomMargin = 24
            )
        }

        // Sections
        for (section in skill.sections) {

            // Section heading
            addText(
                context = context,
                container = container,
                text = section.title,
                textSize = 20f,
                bold = true,
                topMargin = 12,
                bottomMargin = 8
            )

            // Bullet points
            for (point in section.points) {

                addText(
                    context = context,
                    container = container,
                    text = "• $point",
                    textSize = 16f,
                    bottomMargin = 8
                )
            }
        }
    }

    private fun addText(
        context: Context,
        container: LinearLayout,
        text: String,
        textSize: Float,
        bold: Boolean = false,
        topMargin: Int = 0,
        bottomMargin: Int = 0
    ) {

        val textView = TextView(context)

        textView.text = text
        textView.textSize = textSize

        if (bold) {
            textView.setTypeface(
                null,
                Typeface.BOLD
            )
        }

        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        params.topMargin =
            dpToPx(context, topMargin)

        params.bottomMargin =
            dpToPx(context, bottomMargin)

        textView.layoutParams = params

        container.addView(textView)
    }

    private fun dpToPx(
        context: Context,
        dp: Int
    ): Int {

        return (
                dp *
                        context.resources.displayMetrics.density
                ).toInt()
    }
}

