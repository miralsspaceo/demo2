package com.app.personas_social.stickerview

import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.Gravity


object ViewHelper {
    fun revertGravity(gravity: Int): Int {
        val rightGravity: Int
        val verticalG = gravity and Gravity.VERTICAL_GRAVITY_MASK
        val horizontalG = gravity and Gravity.HORIZONTAL_GRAVITY_MASK
        rightGravity = if (horizontalG == Gravity.RIGHT) {
            verticalG or Gravity.LEFT
        } else if (horizontalG == Gravity.LEFT) {
            verticalG or Gravity.RIGHT
        } else {
            gravity
        }
        return rightGravity
    }

    fun getDefiningColor(drawable: Drawable?): Int {
        if (drawable is GradientDrawable) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val colorList = drawable.color
                if (colorList != null) {
                    return colorList.defaultColor
                }
            }
        } else if (drawable is ColorDrawable) {
            return drawable.color
        }
        return -1
    }
}
