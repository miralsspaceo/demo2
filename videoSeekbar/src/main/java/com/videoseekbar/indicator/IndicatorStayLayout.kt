package com.videoseekbar.indicator

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout

class IndicatorStayLayout @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    override fun onFinishInflate() {
        val childCount = childCount
        for (i in childCount - 1 downTo 0) {
            layoutIndicator(getChildAt(i), i)
        }
        super.onFinishInflate()
    }
    /**
     * If you want to initial seek bar by java code to make
     * indicator stay always,call this.
     *
     * @param seekBar the direct child in indicatorStayLayout
     * @param index   the child index you wanted indicatorSeekBar to attach to IndicatorStayLayout;
     */
    /**
     * If you want to initial seek bar by java code to make
     * indicator stay always,call this.
     *
     * @param seekBar the direct child in indicatorStayLayout
     */
    @JvmOverloads
    fun attachTo(seekBar: IndicatorSeekBar?, index: Int = -2) {
        if (seekBar == null) {
            throw NullPointerException(
                "the seek bar wanna attach to IndicatorStayLayout " +
                        "can not be null value."
            )
        }
        layoutIndicator(seekBar, index)
        addView(seekBar, index + 1)
    }

    /**
     * layout each indicator
     *
     * @param child the indicatorSeekBar which should hava a indicator content view.
     * @param index the index you want the seek bar to located in IndicatorStayLayout.
     */
    private fun layoutIndicator(child: View, index: Int) {
        if (child is IndicatorSeekBar) {
            val seekBar = child
            seekBar.setIndicatorStayAlways(true)
            val contentView = seekBar.indicatorContentView
                ?: throw IllegalStateException(
                    "Can not find any indicator in the IndicatorSeekBar, please " +
                            "make sure you have called the attr: SHOW_INDICATOR_TYPE for IndicatorSeekBar and the value is not IndicatorType.NONE."
                )
            check(contentView !is IndicatorSeekBar) { "IndicatorSeekBar can not be a contentView for Indicator in case this inflating loop." }
            val params = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            val layoutParams = MarginLayoutParams(params)
            layoutParams.setMargins(
                layoutParams.leftMargin,
                layoutParams.topMargin,
                layoutParams.rightMargin,
                0 //dp2px(seekBar.context, 2f) -
            )
            addView(contentView, index, layoutParams)
            seekBar.showStayIndicator()
        }
    }

    override fun setOrientation(orientation: Int) {
        require(orientation == VERTICAL) {
            ("IndicatorStayLayout is always vertical and does"
                    + " not support horizontal orientation")
        }
        super.setOrientation(orientation)
    }

    init {
        orientation = VERTICAL
    }
}