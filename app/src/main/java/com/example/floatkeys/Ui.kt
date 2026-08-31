package com.example.floatkeys

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.TextView

object Ui {
    fun dp(context: Context, value: Int): Int =
        (value * context.resources.displayMetrics.density).toInt()

    fun rounded(color: Int, radiusDp: Int, context: Context): GradientDrawable =
        GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(color)
            cornerRadius = dp(context, radiusDp).toFloat()
        }

    fun circle(color: Int): GradientDrawable =
        GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(color)
        }

    fun actionButton(context: Context, text: String): TextView =
        TextView(context).apply {
            this.text = text
            textSize = 16f
            setTextColor(Color.rgb(25, 25, 28))
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(context, 20), 0, dp(context, 20), 0)
            background = rounded(Color.WHITE, 24, context)
            elevation = dp(context, 1).toFloat()
            minHeight = dp(context, 64)
            isClickable = true
            isFocusable = true
        }

    fun label(context: Context, text: String, size: Float = 14f): TextView =
        TextView(context).apply {
            this.text = text
            textSize = size
            setTextColor(Color.rgb(85, 85, 92))
        }

    fun title(context: Context, text: String): TextView =
        TextView(context).apply {
            this.text = text
            textSize = 34f
            setTextColor(Color.rgb(20, 20, 22))
            setTypeface(typeface, Typeface.BOLD)
        }
}
