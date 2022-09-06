package com.videoseekbar

import android.content.Context
import android.util.TypedValue
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

internal object FormatUtils {
    private val LEADING_DECIMALS = arrayOf(
        "0.".toCharArray(), "0.0".toCharArray(),
        "0.00".toCharArray(), "0.000".toCharArray(), "0.0000".toCharArray(),
        "0.00000".toCharArray(),
        "0.000000".toCharArray(), "0.0000000".toCharArray(), "0.00000000".toCharArray(),
        "0.000000000".toCharArray(), "0.0000000000".toCharArray(), "0.00000000000".toCharArray(),
        "0.000000000000".toCharArray(), "0.0000000000000".toCharArray(),
        "0.00000000000000".toCharArray(),
        "0.000000000000000".toCharArray()
    )

    /**
     * format a double value quickly, will remove the suffix:0
     */
    @JvmStatic
    fun fastFormat(d: Double, precision: Int): String {
        val posPrecision = Math.abs(precision)
        val roundUpVal =
            Math.abs(d) * Math.pow(10.0, posPrecision.toDouble()) + 0.5
        if (roundUpVal > 999999999999999.0 || posPrecision > 16) { // double has max 16 precisions
            return bigDecFormat(d, posPrecision)
        }
        val longPart = Math.nextUp(roundUpVal).toLong()
        if (longPart < 1) {
            return "0"
        }
        val longPartChars = java.lang.Long.toString(longPart).toCharArray()
        val formatChars: CharArray
        if (longPartChars.size > posPrecision) {
            var end = longPartChars.size - 1
            val decIndex = longPartChars.size - posPrecision
            while (end >= decIndex && longPartChars[end] == '0') {
                end--
            }
            if (end >= decIndex) {
                formatChars = CharArray(end + 2)
                System.arraycopy(longPartChars, 0, formatChars, 0, decIndex)
                formatChars[decIndex] = '.'
                System.arraycopy(
                    longPartChars, decIndex, formatChars,
                    decIndex + 1, end - decIndex + 1
                )
            } else {
                formatChars = CharArray(decIndex)
                System.arraycopy(longPartChars, 0, formatChars, 0, decIndex)
            }
        } else {
            var end = longPartChars.size - 1
            while (end >= 0 && longPartChars[end] == '0') {
                end--
            }
            val leadings =
                LEADING_DECIMALS[posPrecision - longPartChars.size]
            formatChars = Arrays.copyOf(leadings, leadings.size + end + 1)
            System.arraycopy(longPartChars, 0, formatChars, leadings.size, end + 1)
        }
        return if (Math.signum(d) > 0) String(formatChars) else "-" + String(formatChars)
    }

    private fun bigDecFormat(d: Double, precision: Int): String {
        var formatStr = BigDecimal(java.lang.Double.toString(d))
            .setScale(Math.abs(precision), RoundingMode.HALF_UP)
            .toString()
        if (precision == 0) {
            return formatStr
        }
        var end = formatStr.length - 1
        while (end >= 0 && formatStr[end] == '0') {
            end--
        }
        formatStr = formatStr.substring(0, end + 1)
        if (formatStr[formatStr.length - 1] == '.') {
            formatStr = formatStr.substring(0, formatStr.length - 1)
        }
        return formatStr
    }

    fun dp2px(context: Context, dpValue: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dpValue,
            context.resources.displayMetrics
        ).toInt()
    }

    fun sp2px(context: Context, spValue: Float): Int {
        return (spValue * context.resources.displayMetrics.scaledDensity + 0.5f).toInt()
    }

    fun px2sp(context: Context, pxValue: Float): Int {
        return (pxValue / context.resources.displayMetrics.scaledDensity + 0.5f).toInt()
    }
}