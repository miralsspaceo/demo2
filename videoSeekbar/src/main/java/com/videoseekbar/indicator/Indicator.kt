package com.videoseekbar.indicator

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.videoseekbar.FormatUtils.dp2px
import com.videoseekbar.FormatUtils.px2sp
import com.videoseekbar.R

class Indicator(
    private val mContext: Context,
    private val mSeekBar: IndicatorSeekBar,
    private val mIndicatorColor: Int,
    private var mIndicatorType: Int,
    private val mIndicatorTextSize: Int,
    private val mIndicatorTextColor: Int,
    private var mIndicatorCustomView: View?,
    private val mIndicatorCustomTopContentView: View?
) {
    private val mLocation = IntArray(2)
    private var mProgressTextView: TextView? = null
    private var mIndicatorPopW: PopupWindow? = null
    private var mTopContentView: LinearLayout? = null
    private val mGap: Int = dp2px(mContext, 2f)
    var insideContentView: View? = null

    private fun initIndicator() {
        if (mIndicatorType == IndicatorType.CUSTOM) {
            if (mIndicatorCustomView != null) {
                insideContentView = mIndicatorCustomView
                //for the custom indicator view, if progress need to show when seeking ,
                // need a TextView to show progress and this textView 's identify must be progress;
                val progressTextViewId = mContext.resources.getIdentifier(
                    "isb_progress",
                    "id",
                    mContext.applicationContext.packageName
                )
                if (progressTextViewId > 0) {
                    val view =
                        insideContentView!!.findViewById<View>(progressTextViewId)
                    if (view != null) {
                        if (view is TextView) {
                            //progressText
                            mProgressTextView = view
                            mProgressTextView!!.text = mSeekBar.indicatorTextString
                            mProgressTextView!!.textSize = px2sp(
                                mContext,
                                mIndicatorTextSize.toFloat()
                            ).toFloat()
                            mProgressTextView!!.setTextColor(mIndicatorTextColor)
                        } else {
                            throw ClassCastException("the view identified by isb_progress in indicator custom layout can not be cast to TextView")
                        }
                    }
                }
            } else {
                throw IllegalArgumentException("the attr：indicator_custom_layout must be set while you set the indicator type to CUSTOM.")
            }
        } else {

            insideContentView = View.inflate(mContext, R.layout.isb_indicator, null)
            //container
            mTopContentView =
                insideContentView!!.findViewById<View>(R.id.indicator_container) as LinearLayout
            //progressText
            mProgressTextView =
                insideContentView!!.findViewById<View>(R.id.isb_progress) as TextView
            mProgressTextView!!.text = mSeekBar.indicatorTextString
            mProgressTextView!!.textSize = px2sp(mContext, mIndicatorTextSize.toFloat()).toFloat()
            mProgressTextView!!.setTextColor(mIndicatorTextColor)
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//                    mTopContentView!!.background = gradientDrawable
//                } else {
//                    mTopContentView!!.setBackgroundDrawable(gradientDrawable)
//                }
            //custom top content view
            if (mIndicatorCustomTopContentView != null) {
                //for the custom indicator top content view, if progress need to show when seeking ,
                //need a TextView to show progress and this textView 's identify must be progress;
                val progressTextViewId = mContext.resources.getIdentifier(
                    "isb_progress",
                    "id",
                    mContext.applicationContext.packageName
                )
                val topContentView: View = mIndicatorCustomTopContentView
                if (progressTextViewId > 0) {
                    val tv =
                        topContentView.findViewById<View>(progressTextViewId)
                    if (tv != null && tv is TextView) {
                        setTopContentView(topContentView, tv)
                    } else {
                        setTopContentView(topContentView)
                    }
                } else {
                    setTopContentView(topContentView)
                }
            }
        }
    }

    @get:NonNull
    private val gradientDrawable: GradientDrawable
        private get() {
            val tvDrawable: GradientDrawable
            tvDrawable = if (mIndicatorType == IndicatorType.ROUNDED_RECTANGLE) {
                mContext.resources
                    .getDrawable(R.drawable.isb_indicator_rounded_corners) as GradientDrawable
            } else {
                mContext.resources
                    .getDrawable(R.drawable.isb_indicator_square_corners) as GradientDrawable
            }
            tvDrawable.setColor(mIndicatorColor)
            return tvDrawable
        }

    private val windowWidth: Int
        private get() {
            val wm =
                mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            return wm.defaultDisplay?.width ?: 0
        }

    private val indicatorScreenX: Int
        private get() {
            mSeekBar.getLocationOnScreen(mLocation)
            return mLocation[0]
        }

    private fun adjustArrow(touchX: Float) {
        if (mIndicatorType == IndicatorType.CUSTOM || mIndicatorType == IndicatorType.CIRCULAR_BUBBLE) {
            return
        }
        val indicatorScreenX = indicatorScreenX
//        if (indicatorScreenX + touchX < mIndicatorPopW!!.contentView.measuredWidth / 2) {
//            setMargin(
//                mArrowView,
//                (-(mIndicatorPopW!!.contentView
//                    .measuredWidth / 2 - indicatorScreenX - touchX)).toInt(),
//                -1,
//                -1,
//                -1
//            )
//        } else if (mWindowWidth - indicatorScreenX - touchX < mIndicatorPopW!!.contentView
//                .measuredWidth / 2
//        ) {
//            setMargin(
//                mArrowView,
//                (mIndicatorPopW!!.contentView
//                    .measuredWidth / 2 - (mWindowWidth - indicatorScreenX - touchX)).toInt(),
//                -1,
//                -1,
//                -1
//            )
//        } else {
//            setMargin(mArrowView, 0, 0, 0, 0)
//        }
    }

    private fun setMargin(
        view: View?,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ) {
        if (view == null) {
            return
        }
        if (view.layoutParams is MarginLayoutParams) {
            val layoutParams = view.layoutParams as MarginLayoutParams
            layoutParams.setMargins(
                if (left == -1) layoutParams.leftMargin else left,
                if (top == -1) layoutParams.topMargin else top,
                if (right == -1) layoutParams.rightMargin else right,
                if (bottom == -1) layoutParams.bottomMargin else bottom
            )
            view.requestLayout()
        }
    }

    fun iniPop() {
        if (mIndicatorPopW != null) {
            return
        }
        if (mIndicatorType != IndicatorType.NONE && insideContentView != null) {
            insideContentView!!.measure(0, 0)
            mIndicatorPopW = PopupWindow(
                insideContentView,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                false
            )
        }
    }

    fun setProgressTextView(text: String?) {
        if (mProgressTextView != null) {
            mProgressTextView!!.text = text
        }
    }

    fun updateIndicatorLocation(offset: Int) {
        setMargin(insideContentView, offset, -1, -1, -1)
    }

//    fun updateArrowViewLocation(offset: Int) {
//        setMargin(mArrowView, offset, -1, -1, -1)
//    }

    /**
     * update the indicator position
     *
     * @param touchX the x location you touch without padding left.
     */
    fun update(touchX: Float) {
        if (!mSeekBar.isEnabled || mSeekBar.visibility != View.VISIBLE) {
            return
        }
        refreshProgressText()
        if (mIndicatorPopW != null) {
            mIndicatorPopW!!.contentView.measure(0, 0)
            mIndicatorPopW!!.update(
                mSeekBar,
                (touchX - mIndicatorPopW!!.contentView.measuredWidth / 2).toInt(),
                -(mSeekBar.measuredHeight + mIndicatorPopW!!.contentView
                    .measuredHeight - mSeekBar.paddingTop /*- mSeekBar.getTextHeight() */ + mGap),
                -1,
                -1
            )
            adjustArrow(touchX)
        }
    }

    /**
     * call this method to show the indicator.
     *
     * @param touchX the x location you touch, padding left excluded.
     */
    fun show(touchX: Float) {
        if (!mSeekBar.isEnabled || mSeekBar.visibility != View.VISIBLE) {
            return
        }
        refreshProgressText()
        if (mIndicatorPopW != null) {
            mIndicatorPopW!!.contentView.measure(0, 0)
            mIndicatorPopW!!.showAsDropDown(
                mSeekBar, (touchX - mIndicatorPopW!!.contentView.measuredWidth / 2f).toInt(),
                -(mSeekBar.measuredHeight + mIndicatorPopW!!.contentView
                    .measuredHeight - mSeekBar.paddingTop /*- mSeekBar.getTextHeight()*/ + mGap)
            )
            adjustArrow(touchX)
        }
    }

    fun refreshProgressText() {
        val tickTextString = mSeekBar.indicatorTextString
        if (mProgressTextView != null) {
            mProgressTextView!!.text = tickTextString
        }
    }

    /**
     * call this method hide the indicator
     */
    fun hide() {
        if (mIndicatorPopW == null) {
            return
        }
        mIndicatorPopW!!.dismiss()
    }

    val isShowing: Boolean
        get() = mIndicatorPopW != null && mIndicatorPopW!!.isShowing
    /*----------------------API START-------------------*/
    /**
     * get the indicator content view.
     *
     * @return the view which is inside indicator.
     */
    /**
     * call this method to replace the current indicator with a new indicator view , indicator arrow will be replace ,too.
     *
     * @param customIndicatorView a new content view for indicator.
     */
    var contentView: View?
        get() = insideContentView
        set(customIndicatorView) {
            mIndicatorType = IndicatorType.CUSTOM
            mIndicatorCustomView = customIndicatorView
            initIndicator()
        }

    /**
     * call this method to replace the current indicator with a new indicator view, indicator arrow will be replace ,too.
     *
     * @param customIndicatorView a new content view for indicator.
     * @param progressTextView    this TextView will show the progress or tick text, must be found in @param customIndicatorView
     */
    fun setContentView(
        @NonNull customIndicatorView: View?,
        progressTextView: TextView?
    ) {
        mProgressTextView = progressTextView
        mIndicatorType = IndicatorType.CUSTOM
        mIndicatorCustomView = customIndicatorView
        initIndicator()
    }

    /**
     * get the indicator top content view.
     * if indicator type [IndicatorType] is CUSTOM or CIRCULAR_BUBBLE, call this method will get a null value.
     *
     * @return the view which is inside indicator's top part, not include arrow
     */
    val topContentView: View?
        get() = mTopContentView

    /**
     * set the View to the indicator top container, not influence indicator arrow ;
     * if indicator type [IndicatorType] is CUSTOM or CIRCULAR_BUBBLE, call this method will be not worked.
     *
     * @param topContentView the view is inside the indicator TOP part, not influence indicator arrow;
     */
    fun setTopContentView(@NonNull topContentView: View) {
        setTopContentView(topContentView, null)
    }

    /**
     * set the  View to the indicator top container, and show the changing progress in indicator when seek;
     * not influence indicator arrow;
     * if indicator type is custom , this method will be not work.
     *
     * @param topContentView   the view is inside the indicator TOP part, not influence indicator arrow;
     * @param progressTextView this TextView will show the progress or tick text, must be found in @param topContentView
     */
    fun setTopContentView(
        @NonNull topContentView: View,
        @Nullable progressTextView: TextView?
    ) {
        mProgressTextView = progressTextView
        mTopContentView!!.removeAllViews()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            topContentView.background = gradientDrawable
        } else {
            topContentView.setBackgroundDrawable(gradientDrawable)
        }
        mTopContentView!!.addView(topContentView)
    } /*----------------------API END-------------------*/

    init {
        initIndicator()
    }
}