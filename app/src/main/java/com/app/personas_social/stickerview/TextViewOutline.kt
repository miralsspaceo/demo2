package com.app.personas_social.stickerview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.text.TextPaint
import androidx.appcompat.widget.AppCompatTextView

class TextViewOutline(
    context: Context?,
    var sticker: TextSticker
) :
    AppCompatTextView(context!!) {
    protected override fun onDraw(canvas: Canvas?) {
        //draw standard text
        super.onDraw(canvas)
        //draw outline
        val paint: TextPaint = paint
        paint.style =Paint.Style.STROKE
        paint.strokeWidth =  20f
        val color_tmp: Int =sticker.borderColor
        paint.color = color_tmp
        super.onDraw(canvas)
        //restore
        setTextColor(sticker.color)
        paint.style = Paint.Style.FILL;
        paint.strokeWidth =  0f
        paint.color = sticker.color

    }
}