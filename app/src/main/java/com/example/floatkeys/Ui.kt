package com.example.floatkeys

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.TextView

object Ui {
    fun dp(c: Context, v: Int) = (v * c.resources.displayMetrics.density).toInt()
    fun rounded(color: Int, r: Int, c: Context) = GradientDrawable().apply { shape = GradientDrawable.RECTANGLE; setColor(color); cornerRadius = dp(c,r).toFloat() }
    fun circle(color: Int) = GradientDrawable().apply { shape = GradientDrawable.OVAL; setColor(color) }
    fun actionButton(c: Context, text: String) = TextView(c).apply { this.text=text; textSize=16f; setTextColor(Color.rgb(25,25,28)); gravity=Gravity.CENTER_VERTICAL; setPadding(dp(c,20),0,dp(c,20),0); background=rounded(Color.WHITE,24,c); elevation=dp(c,1).toFloat(); minHeight=dp(c,64); isClickable=true; isFocusable=true }
    fun label(c: Context, text: String, size: Float=14f) = TextView(c).apply { this.text=text; textSize=size; setTextColor(Color.rgb(85,85,92)) }
    fun title(c: Context, text: String) = TextView(c).apply { this.text=text; textSize=34f; setTextColor(Color.rgb(20,20,22)); setTypeface(typeface,Typeface.BOLD) }
}
