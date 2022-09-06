package com.audiowaveforms

import android.animation.FloatEvaluator
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.DecelerateInterpolator
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.graphics.toRect
import java.util.*
import kotlin.math.*

class AudioWaveSeekBarView : View {
    enum class Thumb {
        MIN, MAX
    }

    private var mActivePointerId = INVALID_POINTER_ID
    private var mDownMotionX = 0f
    private var mIsDragging = false
    private var mCallback: OnSeekBarRangedChangeListener? =
        null
    private var mPaint: Paint? = null
    private var mPaintThumb: Paint? = null
    private var mBackgroundLineRectLeft: RectF? = null
    private var mBackgroundLineRectRight: RectF? = null
    private var mProgressLineRect: RectF? = null
    private var mPressedThumb: Thumb? = null
    private var mThumbImage: Bitmap? = null
    private var mThumbImage2: Bitmap? = null
    private var mThumbImageRight: Drawable? = null
    private var mThumbPressedImage: Bitmap? = null
    private var mThumbHalfWidth = 0f
    private var mThumbHalfHeight = 0f
    private var mThumbPressedHalfWidth = 0f
    private var mThumbPressedHalfHeight = 0f
    private var mPadding = 0f
    private var mBackgroundLineHeight = 0f
    private var mProgressLineHeight = 0f
    private var mProgressBackgroundColor = DEFAULT_BACKGROUND_COLOR
    private var mProgressColor = DEFAULT_COLOR
    private var mMinValueAnimator: ValueAnimator? = null
    private var mMaxValueAnimator: ValueAnimator? = null
    private var minValue = 0f
    private var maxValue = 0f
    private var mNormalizedMinValue = 0f
    private var mNormalizedMaxValue = 1f
    private var mRounded = false
    private var mStepProgressEnable = false
    private val mProgressStepList: MutableList<Float> =
        ArrayList()
    private var mStepRadius = DEFAULT_STEP_RADIUS.toFloat()

    private var mCanvasWidth = 0
    private var mCanvasHeight = 0
    private var onProgressChanged: SeekBarOnProgressChanged? = null
    private val mWavePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mWaveRect = RectF()
    private val mProgressCanvas = Canvas()
    private var mMaxValue = dp(context, 2).toInt()
    private var mScaledTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
    var sample: IntArray? = null
        set(value) {
            field = value
            invalidate()
        }

    var progress: Int = 0
        set(value) {
            field = value
            invalidate()

            if (onProgressChanged != null)
                onProgressChanged!!.onProgressChanged(this, progress, false)
        }

    private var waveBackgroundColor: Int = Color.LTGRAY
        set(value) {
            field = value
            invalidate()
        }

    private var waveProgressColor: Int = Color.WHITE
        set(value) {
            field = value
            invalidate()
        }

    var waveGap: Float = dp(context, 2)
        set(value) {
            field = value
            invalidate()
        }

    var waveWidth: Float = dp(context, 5)
        set(value) {
            field = value
            invalidate()
        }

    private var waveMinHeight: Float = waveWidth
        set(value) {
            field = value
            invalidate()
        }

    private var waveCornerRadius: Float = dp(context, 2)
        set(value) {
            field = value
            invalidate()
        }

    interface OnSeekBarRangedChangeListener {
        fun onStopTouch(
            view: AudioWaveSeekBarView,
            minValue: Float,
            maxValue: Float,
            thumb: Thumb
        )

        fun onProgressChanged(
            view: AudioWaveSeekBarView,
            minValue: Float,
            maxValue: Float,
            thumb: Thumb
        )

        fun onStartTouch(view: AudioWaveSeekBarView)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : super(context, attrs) {
        setupAttrs(context, attrs)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        setupAttrs(context, attrs)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setupAttrs(
        context: Context,
        attrs: AttributeSet?
    ) {
        val a =
            context.theme.obtainStyledAttributes(attrs, R.styleable.AudioWaveView, 0, 0)
        val min: Float
        val currentMin: Float
        val max: Float
        val currentMax: Float
        val progressHeight: Int
        val bgProgressHeight: Int
        try {
            min = a.getFloat(
                R.styleable.AudioWaveView_min,
                DEFAULT_MIN_PROGRESS
            )
            currentMin = a.getFloat(R.styleable.AudioWaveView_currentMin, min)
            max = a.getFloat(
                R.styleable.AudioWaveView_max,
                DEFAULT_MAX_PROGRESS
            )
            currentMax = a.getFloat(R.styleable.AudioWaveView_currentMax, max)
            progressHeight = a.getDimensionPixelSize(
                R.styleable.AudioWaveView_progresssHeight,
                DEFAULT_PROGRESS_HEIGHT
            )
            bgProgressHeight = a.getDimensionPixelSize(
                R.styleable.AudioWaveView_backgrounddHeight,
                DEFAULT_PROGRESS_HEIGHT
            )
            mRounded = a.getBoolean(R.styleable.AudioWaveView_rounded, false)
            mProgressColor = a.getColor(
                R.styleable.AudioWaveView_progresssColor,
                DEFAULT_COLOR
            )
            val thumbColor = a.getColor(
                R.styleable.AudioWaveView_thumbsColor,
                DEFAULT_COLOR
            )
            mPaintThumb = Paint(Paint.ANTI_ALIAS_FLAG)
            mPaintThumb!!.color = thumbColor

            mProgressBackgroundColor = a.getColor(
                R.styleable.AudioWaveView_backgrounddColor,
                DEFAULT_BACKGROUND_COLOR
            )
            if (a.hasValue(R.styleable.AudioWaveView_thumbsResource)) {
                setThumbsImageResource(
                    a.getResourceId(
                        R.styleable.AudioWaveView_thumbsResource,
                        R.drawable.background_thumb
                    )
                )
            } else {
                if (a.hasValue(R.styleable.AudioWaveView_thumbNormalResource)) {
                    setThumbNormalImageResource(
                        a.getResourceId(
                            R.styleable.AudioWaveView_thumbNormalResource,
                            R.drawable.background_thumb
                        )
                    )
                }
                if (a.hasValue(R.styleable.AudioWaveView_thumbPressedResource)) {
                    setThumbPressedImageResource(
                        a.getResourceId(
                            R.styleable.AudioWaveView_thumbPressedResource,
                            R.drawable.background_thumb
                        )
                    )
                }
            }
            waveWidth = a.getDimension(R.styleable.AudioWaveView_wavee_width, waveWidth)
            waveGap = a.getDimension(R.styleable.AudioWaveView_wavee_gap, waveGap)
            waveCornerRadius =
                a.getDimension(R.styleable.AudioWaveView_wavee_corner_radius, waveCornerRadius)
            waveMinHeight =
                a.getDimension(R.styleable.AudioWaveView_wavee_min_height, waveMinHeight)
            waveBackgroundColor =
                a.getColor(R.styleable.AudioWaveView_wavee_background_color, waveBackgroundColor)
            waveProgressColor =
                a.getColor(R.styleable.AudioWaveView_wavee_progress_color, waveProgressColor)
            progress = a.getInteger(R.styleable.AudioWaveView_wavee_progress, progress)
        } finally {
            a.recycle()
        }
        init(min, currentMin, max, currentMax, progressHeight, bgProgressHeight)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun init(
        min: Float,
        currentMin: Float,
        max: Float,
        currentMax: Float,
        progressHeight: Int,
        bgProgressHeight: Int
    ) {
        mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mBackgroundLineRectLeft = RectF()
        mBackgroundLineRectRight = RectF()
        mProgressLineRect = RectF()
        if (mThumbImage == null && mThumbPressedImage == null) {
            mThumbImageRight = context.getDrawable( R.drawable.background_thumb)
            mThumbImageRight = mThumbImageRight!!.constantState!!.newDrawable()
            mThumbImageRight!!.layoutDirection = LAYOUT_DIRECTION_RTL
            mThumbImageRight!!.isAutoMirrored = true
            val d = mThumbImageRight
            mThumbImage2 = Bitmap.createBitmap(
                d!!.intrinsicWidth,
                d.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            d.setBounds(0, 0, d.intrinsicWidth, d.intrinsicHeight)
            d.draw(Canvas(mThumbImage2!!))
            setThumbNormalImageResource(R.drawable.background_thumb)
            setThumbPressedImageResource(R.drawable.background_thumb)
        } else if (mThumbImage == null) {
            setThumbNormalImageResource(R.drawable.background_thumb)
        } else if (mThumbPressedImage == null) {
            setThumbPressedImageResource(R.drawable.background_thumb)
        }
        measureThumb()
        measureThumbPressed()
        updatePadding()
        setBackgroundHeight(bgProgressHeight.toFloat())
        setProgressHeight(progressHeight.toFloat())
        minValue = min
        maxValue = max
        selectedMinValue = currentMin
        selectedMaxValue = currentMax

        // This solves focus handling issues in case EditText widgets are being used along with the RangeSeekBar within ScrollViews.
        isFocusable = true
        isFocusableInTouchMode = true
        if (!isInEditMode) {
            mScaledTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
        }
    }

    fun setOnSeekBarRangedChangeListener(listener: OnSeekBarRangedChangeListener?) {
        mCallback = listener
    }

    /**
     * This method will change the min value to a desired value. Note that if Progress by Steps is enabled, min will stay as default.
     *
     * @param value new min value
     * @return true if changed
     */
    fun setMinValue(value: Float): Boolean {
        if (mStepProgressEnable) {
            return false
        }
        minValue = value
        selectedMinValue = selectedMinValue
        return true
    }

    /**
     * This method will change the max value to a desired value. Note that if Progress by Steps is enabled, max will stay as default.
     *
     * @param value new max value
     * @return true if changed
     */
    fun setMaxValue(value: Float): Boolean {
        if (mStepProgressEnable) {
            return false
        }
        maxValue = value
        setSelectedMaxVal(selectedMaxValue)
        return true
    }

    private fun setSelectedMinValue(value: Float, animate: Boolean) {
        setSelectedMinValue(value, animate, DEFAULT_ANIMATE_DURATION)
    }

    private fun setSelectedMinValue(
        value: Float,
        animate: Boolean,
        duration: Long
    ) {
        if (animate) {
            if (mMinValueAnimator != null) {
                mMinValueAnimator!!.cancel()
            }
            mMinValueAnimator = getAnimator(
                selectedMinValue,
                value,
                duration,
                AnimatorUpdateListener { valueAnimator -> setSelectedMinVal(valueAnimator.animatedValue as Float) })
            mMinValueAnimator!!.start()
        } else {
            setSelectedMinVal(value)
        }
    }

    private fun setSelectedMinVal(value: Float) {
        // in case mMinValue == mMaxValue, avoid division by zero when normalizing.
        if (maxValue - minValue == 0f) {
            setNormalizedMinValue(0f)
        } else {
            setNormalizedMinValue(valueToNormalized(value))
        }
        onChangedValues()
    }

    var selectedMinValue: Float
        get() = normalizedToValue(mNormalizedMinValue)
        set(value) {
            setSelectedMinValue(value, false)
        }

    private fun setSelectedMaxValue(value: Float, animate: Boolean) {
        setSelectedMaxValue(value, animate, DEFAULT_ANIMATE_DURATION)
    }

    private fun setSelectedMaxValue(
        value: Float,
        animate: Boolean,
        duration: Long
    ) {
        if (animate) {
            if (mMaxValueAnimator != null) {
                mMaxValueAnimator!!.cancel()
            }
            mMaxValueAnimator = getAnimator(
                selectedMaxValue,
                value,
                duration,
                AnimatorUpdateListener { valueAnimator -> setSelectedMaxVal(valueAnimator.animatedValue as Float) })
            mMaxValueAnimator!!.start()
        } else {
            setSelectedMaxVal(value)
        }
    }

    private fun setSelectedMaxVal(value: Float) {
        // in case mMinValue == mMaxValue, avoid division by zero when normalizing.
        if (maxValue - minValue == 0f) {
            setNormalizedMaxValue(1f)
        } else {
            setNormalizedMaxValue(valueToNormalized(value))
        }
        onChangedValues()
    }

    var selectedMaxValue: Float
        get() = normalizedToValue(mNormalizedMaxValue)
        set(value) {
            setSelectedMaxValue(value, false)
        }

    fun getPressedThumb() = mPressedThumb

    private fun getAnimator(
        current: Float,
        next: Float,
        duration: Long,
        updateListener: AnimatorUpdateListener
    ): ValueAnimator {
        val animator = ValueAnimator()
        animator.interpolator = DecelerateInterpolator()
        animator.duration = duration
        animator.setObjectValues(current, next)
        animator.setEvaluator(object : FloatEvaluator() {
            fun evaluate(
                fraction: Float,
                startValue: Float,
                endValue: Float
            ): Int {
                return (startValue + (endValue - startValue) * fraction).roundToInt()
            }
        })
        animator.addUpdateListener(updateListener)
        return animator
    }

    /**
     * Sets normalized min value to value so that 0 <= value <= normalized max value <= 1. The View will get invalidated when calling this method.
     *
     * @param value The new normalized min value to set.
     */
    private fun setNormalizedMinValue(value: Float) {
        mNormalizedMinValue = max(
            0f,
            min(1f, min(value, mNormalizedMaxValue))
        )
        invalidate()
    }

    /**
     * Sets normalized max value to value so that 0 <= normalized min value <= value <= 1. The View will get invalidated when calling this method.
     *
     * @param value The new normalized max value to set.
     */
    private fun setNormalizedMaxValue(value: Float) {
        mNormalizedMaxValue = max(
            0f,
            min(1f, max(value, mNormalizedMinValue))
        )
        invalidate()
    }

    fun setRounded(rounded: Boolean) {
        mRounded = rounded
        invalidate()
    }

    /**
     * Set progress bar background height
     *
     * @param height is given in pixels
     */
    private fun setBackgroundHeight(height: Float) {
        mBackgroundLineHeight = height
    }

    /**
     * Set progress bar progress height
     *
     * @param height is given in pixels
     */
    private fun setProgressHeight(height: Float) {
        mProgressLineHeight = height
    }

    private fun setBackgroundTrackColor(resId: Int) {
        setBackgroundColor(ContextCompat.getColor(context, resId))
    }

    override fun setBackgroundColor(color: Int) {
        mProgressBackgroundColor = color
        invalidate()
    }

    private fun setProgressTrackColor(resId: Int) {
        mProgressColor = ContextCompat.getColor(context, resId)
        invalidate()
    }

    private fun setThumbsImageResource(
        @DrawableRes resId: Int
    ) {
        setThumbNormalImageResource(resId)
        setThumbPressedImageResource(resId)
    }

    private fun setThumbNormalImage(bitmap: Bitmap?) {
        setThumbNormalImage(bitmap, true)
    }

    private fun setThumbNormalImage(bitmap: Bitmap?, requestLayout: Boolean) {
        mThumbImage = bitmap
        mThumbPressedImage = if (mThumbPressedImage == null) mThumbImage else mThumbPressedImage
        measureThumb()
        updatePadding()
        if (requestLayout) {
            requestLayout()
        }
    }

    private fun setThumbNormalImageResource(
        @DrawableRes resId: Int
    ) {
        val d = ContextCompat.getDrawable(context, resId)!!
        mThumbImage = Bitmap.createBitmap(
            d.intrinsicWidth,
            d.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        d.setBounds(0, 0, d.intrinsicWidth, d.intrinsicHeight)
        d.draw(Canvas(mThumbImage!!))
        setThumbNormalImage(mThumbImage)
    }

    private fun setThumbPressedImage(bitmap: Bitmap?) {
        setThumbPressedImage(bitmap, true)
    }

    private fun setThumbPressedImage(bitmap: Bitmap?, requestLayout: Boolean) {
        mThumbPressedImage = bitmap
        mThumbImage = if (mThumbImage == null) mThumbPressedImage else mThumbImage
        measureThumbPressed()
        updatePadding()
        if (requestLayout) {
            requestLayout()
        }
    }

    private fun setThumbPressedImageResource(
        @DrawableRes resId: Int
    ) {
        val d = ContextCompat.getDrawable(context, resId)!!
        mThumbPressedImage = Bitmap.createBitmap(
            d.intrinsicWidth,
            d.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        d.setBounds(0, 0, d.intrinsicWidth, d.intrinsicHeight)
        d.draw(Canvas(mThumbPressedImage!!))
        setThumbPressedImage(mThumbPressedImage)
    }

    private fun onChangedValues() {
        if (mCallback != null && mPressedThumb != null) {
            mCallback!!.onStopTouch(this, selectedMinValue, selectedMaxValue, mPressedThumb!!)
        }
    }

    private fun onChangingValues() {
        if (mCallback != null && mPressedThumb != null) {
            mCallback!!.onProgressChanged(this, selectedMinValue, selectedMaxValue, mPressedThumb!!)
        }
    }

    private fun measureThumb() {
        mThumbHalfWidth = 0.5f * mThumbImage!!.width
        mThumbHalfHeight = 0.5f * mThumbImage!!.height
    }

    private fun measureThumbPressed() {
        mThumbPressedHalfWidth = 0.5f * mThumbPressedImage!!.width
        mThumbPressedHalfHeight = 0.5f * mThumbPressedImage!!.height
    }

    private fun updatePadding() {
        val thumbWidth = max(mThumbHalfWidth, mThumbPressedHalfWidth)
        val thumbHeight =
            max(mThumbHalfHeight, mThumbPressedHalfHeight)
        mPadding = max(min(thumbWidth, thumbHeight), mStepRadius)
    }

    /**
     * Converts a normalized value to a value space between absolute minimum and maximum.
     *
     * @param normalized The value to "de-normalize".
     * @return The "de-normalized" value.
     */
    private fun normalizedToValue(normalized: Float): Float {
        return minValue + normalized * (maxValue - minValue)
    }

    /**
     * Converts the given value to a normalized value.
     *
     * @param value The value to normalize.
     * @return The normalized value.
     */
    private fun valueToNormalized(value: Float): Float {
        return if (0f == maxValue - minValue) {
            // prevent division by zero, simply return 0.
            0f
        } else (value - minValue) / (maxValue - minValue)
    }

    /**
     * Converts a normalized value into screen space.
     *
     * @param normalizedCoordinate The normalized value to convert.
     * @return The converted value in screen space.
     */
    private fun normalizedToScreen(normalizedCoordinate: Float): Float {
        return mPadding + normalizedCoordinate * (width - 2 * mPadding)
    }

    /**
     * Converts screen space x-coordinates into normalized values.
     *
     * @param screenCoordinate The x-coordinate in screen space to convert.
     * @return The normalized value.
     */
    private fun screenToNormalized(screenCoordinate: Float): Float {
        val width = width
        return if (width <= 2 * mPadding) {
            // prevent division by zero, simply return 0.
            0f
        } else {
            val result = (screenCoordinate - mPadding) / (width - 2 * mPadding)
            min(1f, max(0f, result))
        }
    }

    private fun trackTouchEvent(event: MotionEvent) {
        val pointerIndex = event.findPointerIndex(mActivePointerId)
        var x = event.getX(pointerIndex)
        if (mStepProgressEnable) {
            x = getClosestStep(screenToNormalized(x))
        }
        if (Thumb.MIN == mPressedThumb) {
            setNormalizedMinValue(if (mStepProgressEnable) x else screenToNormalized(x))
        } else if (Thumb.MAX == mPressedThumb) {
            setNormalizedMaxValue(if (mStepProgressEnable) x else screenToNormalized(x))
        }
    }

    private fun onSecondaryPointerUp(ev: MotionEvent) {
        val pointerIndex =
            ev.action and ACTION_POINTER_INDEX_MASK shr ACTION_POINTER_INDEX_SHIFT
        val pointerId = ev.getPointerId(pointerIndex)
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose
            // a new active pointer and adjust accordingly.
            val newPointerIndex = if (pointerIndex == 0) 1 else 0
            mDownMotionX = ev.getX(newPointerIndex)
            mActivePointerId = ev.getPointerId(newPointerIndex)
        }
    }

    /**
     * Tries to claim the user's drag motion, and requests disallowing any ancestors from stealing events in the drag.
     */
    private fun attemptClaimDrag() {
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(true)
        }
    }

    private fun onStartTrackingTouch() {
        mIsDragging = true
    }

    private fun onStopTrackingTouch() {
        mIsDragging = false
    }

    /**
     * Decides which (if any) thumb is touched by the given x-coordinate.
     *
     * @param touchX The x-coordinate of a touch event in screen space.
     * @return The pressed thumb or null if none has been touched.
     */
    private fun evalPressedThumb(touchX: Float): Thumb? {
        var result: Thumb? = null
        val minThumbPressed = isInThumbRange(touchX, mNormalizedMinValue)
        val maxThumbPressed = isInThumbRange(touchX, mNormalizedMaxValue)
        if (minThumbPressed && maxThumbPressed) {
            // if both thumbs are pressed (they lie on top of each other), choose the one with more room to drag. this avoids "stalling" the thumbs in a
            // corner, not being able to drag them apart anymore.
            result =
                if (touchX / width > 0.5f) Thumb.MIN else Thumb.MAX
        } else if (minThumbPressed) {
            result = Thumb.MIN
        } else if (maxThumbPressed) {
            result = Thumb.MAX
        }
        return result
    }

    /**
     * Decides if given x-coordinate in screen space needs to be interpreted as "within" the normalized thumb x-coordinate.
     *
     * @param touchX               The x-coordinate in screen space to check.
     * @param normalizedThumbValue The normalized x-coordinate of the thumb to check.
     * @return true if x-coordinate is in thumb range, false otherwise.
     */
    private fun isInThumbRange(
        touchX: Float,
        normalizedThumbValue: Float
    ): Boolean {
        return abs(touchX - normalizedToScreen(normalizedThumbValue)) <= mThumbHalfWidth
    }

    /**
     * When enabled, min and max are set to 0 and 100 (default values) and cannot be changed
     *
     * @param enable if true, enables Progress by Step
     */
    fun enableProgressBySteps(enable: Boolean) {
        mStepProgressEnable = enable
        if (enable) {
            setMinValue(DEFAULT_MIN_PROGRESS)
            setMaxValue(DEFAULT_MAX_PROGRESS)
        }
        invalidate()
    }

    /**
     * Note: 0 and 100 will automatically be added as min and max respectively, you don't need to add it again.
     *
     * @param steps values for each step
     */
    fun setProgressSteps(vararg steps: Float) {
        if (steps.isNotEmpty()) {
            val res: MutableList<Float> =
                ArrayList()
            for (step in steps) {
                res.add(step)
            }
            setProgressSteps(res)
        }
    }

    /**
     * Note: 0 and 100 will automatically be added as min and max respectively, you don't need to add it again.
     *
     * @param steps values for each step
     */
    private fun setProgressSteps(steps: List<Float>?) {
        if (steps != null) {
            mProgressStepList.clear()
            mProgressStepList.add(valueToNormalized(DEFAULT_MIN_PROGRESS))
            for (step in steps) {
                mProgressStepList.add(valueToNormalized(step))
            }
            mProgressStepList.add(valueToNormalized(DEFAULT_MAX_PROGRESS))
            invalidate()
        }
    }

    /**
     * @param radius in pixels
     */
    fun setProgressStepRadius(radius: Float) {
        mStepRadius = radius
        updatePadding()
        invalidate()
    }

    val progressSteps: List<Float>
        get() {
            val res: MutableList<Float> = ArrayList()
            for (step in mProgressStepList) {
                res.add(normalizedToValue(step))
            }
            return res
        }

    private fun getClosestStep(value: Float): Float {
        var min = abs(mProgressStepList[0] - value)
        var currentMin: Float
        var colesest = 0f
        for (step in mProgressStepList) {
            currentMin = abs(step - value)
            if (currentMin < min) {
                colesest = step
                min = currentMin
            }
        }
        return colesest
    }

    @Synchronized
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var width = 200
        if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.UNSPECIFIED) {
            width = MeasureSpec.getSize(widthMeasureSpec)
        }
        val maxThumb =
            max(mThumbImage!!.height, mThumbPressedImage!!.height)
        val maxHeight = max(mProgressLineHeight, mBackgroundLineHeight).toInt()
        var height =
            max(max(maxThumb, maxHeight), dpToPx(mStepRadius))
        if (MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.UNSPECIFIED) {
            height = min(height, MeasureSpec.getSize(heightMeasureSpec))
        }
        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvasWidth = w
        mCanvasHeight = h
    }

    @Synchronized
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawWaves(canvas)


        mPaint!!.style = Paint.Style.FILL
        mPaint!!.isAntiAlias = true



        // draw seek bar background line
        val corners: Float = max(
            mBackgroundLineHeight,
            mProgressLineHeight
        ) * if (mRounded) 0.5f else 0f

        // draw seek bar progress line
        mProgressLineRect!![mPadding, 0.5f * (height - mProgressLineHeight), width - mPadding] = 0.5f * (height + mProgressLineHeight)
        mProgressLineRect!!.left = normalizedToScreen(mNormalizedMinValue)
        mProgressLineRect!!.right = normalizedToScreen(mNormalizedMaxValue)
        mPaint!!.color = mProgressColor
//        mPaint!!.color = Color.BLACK
        canvas.drawRoundRect(mProgressLineRect!!, corners, corners, mPaint!!)

        mBackgroundLineRectLeft!![mPadding, 0.5f * (height - mBackgroundLineHeight), mProgressLineRect!!.left] =
            0.5f * (height + mBackgroundLineHeight)
        mBackgroundLineRectRight!![mProgressLineRect!!.right, 0.5f * (height - mBackgroundLineHeight), width - mPadding] =
            0.5f * (height + mBackgroundLineHeight)
        mPaint!!.color = mProgressBackgroundColor
        canvas.drawRoundRect(mBackgroundLineRectLeft!!, corners, corners, mPaint!!)
        canvas.drawRoundRect(mBackgroundLineRectRight!!, corners, corners, mPaint!!)
        mBackgroundLineRectLeft!!.left = normalizedToScreen(mNormalizedMinValue)
//        mBackgroundLineRect!!.right = normalizedToScreen(mNormalizedMaxValue)
        mBackgroundLineRectLeft!!.right = mProgressLineRect!!.left
        mBackgroundLineRectRight!!.left = mProgressLineRect!!.right
        mBackgroundLineRectRight!!.right = normalizedToScreen(mNormalizedMaxValue)


        val minX = normalizedToScreen(mNormalizedMinValue)
        val maxX = normalizedToScreen(mNormalizedMaxValue)

        // draw progress steps, if enabled
        if (mStepProgressEnable) {
            var stepX: Float
            for (step in mProgressStepList) {
                Log.w("TAG", "onDraw:"+mPaint!!.color )
                stepX = normalizedToScreen(step)
                mPaint!!.color =
                    if (stepX > maxX || stepX < minX) mProgressBackgroundColor else mProgressColor
                Log.w("TAG", "onDraw1:"+mPaint!!.color )
                drawStep(canvas, normalizedToScreen(step), mStepRadius, mPaint)
            }
        }

        mTextPaint.textSize = AudioWaveSeekView.dpToPx(context, 14f).toFloat()
        mTextPaint.color = Color.WHITE
        mTextPaint.getTextBounds("A", 0, 1, mWaveRect.toRect())
        canvas.drawText(
            "Thunder",
            40f,
            50f,
            mTextPaint
        )

        // draw thumbs
        drawThumb(
            canvas,
            minX,
            Thumb.MIN == mPressedThumb
        )
        drawThumb2(
            canvas,
            maxX,
            Thumb.MAX == mPressedThumb
        )
    }

    /**
     * @param canvas           The canvas to draw upon.
     * @param screenCoordinate The x-coordinate in screen space where to draw the image.
     * @param pressed          Is the thumb currently in "pressed" state?
     */
    private fun drawThumb(
        canvas: Canvas,
        screenCoordinate: Float,
        pressed: Boolean
    ) {
        canvas.drawBitmap(
            (if (pressed) mThumbPressedImage else mThumbImage)!!,
            screenCoordinate - if (pressed) mThumbPressedHalfWidth else mThumbHalfWidth,
            0.5f * height - if (pressed) mThumbPressedHalfHeight else mThumbHalfHeight,
            mPaintThumb
        )
    }

    private fun drawThumb2(
        canvas: Canvas,
        screenCoordinate: Float,
        pressed: Boolean
    ) {
        canvas.drawBitmap (
            mThumbImage2!!,
            screenCoordinate - if (pressed) mThumbPressedHalfWidth else mThumbHalfWidth,
            0.5f * height - if (pressed) mThumbPressedHalfHeight else mThumbHalfHeight,
            mPaintThumb
        )
    }


    /**
     * @param canvas           The canvas to draw upon.
     * @param screenCoordinate The x-coordinate in screen space where to draw the step.
     * @param radius           Step circle radius
     * @param paint            Paint to color the steps
     */
    private fun drawStep(
        canvas: Canvas,
        screenCoordinate: Float,
        radius: Float,
        paint: Paint?
    ) {
        canvas.drawCircle(screenCoordinate, 0.5f * height, radius, paint!!)
    }

    private fun drawWaves(canvas: Canvas) {
        if (sample == null || sample!!.isEmpty())
            return

        sample!!.maxOrNull()?.let {
            mMaxValue = it
        }
        Log.e("drawWaves", "drawWaves>>: "+sample!!.size )

        val step = (getAvailableWidth() / (waveGap + waveWidth)) / sample!!.size

        var i = 0F
        var lastWaveRight = paddingLeft.toFloat() + mThumbHalfWidth
//        var lastWaveRight = mBackgroundLineRect!!.left
        while (i < sample!!.size) {

            var waveHeight = getAvailableHeight() * (sample!![i.toInt()].toFloat() / mMaxValue)

            if (waveHeight < waveMinHeight)
                waveHeight = waveMinHeight

            val top: Float = paddingTop + getAvailableHeight() / 2F - waveHeight / 2F

//            when(waveGravity){
//                WaveGravity.TOP -> paddingTop.toFloat()
//                WaveGravity.CENTER -> paddingTop+getAvailableHeight()/2F - waveHeight/2F
//                WaveGravity.BOTTOM -> mCanvasHeight - paddingBottom - waveHeight
//            }

            mWaveRect.set(lastWaveRight, top, lastWaveRight + waveWidth, top + waveHeight)

            when {
                mWaveRect.contains(getAvailableWidth() * progress / 100F, mWaveRect.centerY()) -> {
                    var bitHeight = mWaveRect.height().toInt()
                    if (bitHeight <= 0)
                        bitHeight = waveWidth.toInt()

                    val bitmap =
                        Bitmap.createBitmap(getAvailableWidth(), bitHeight, Bitmap.Config.ARGB_8888)
                    mProgressCanvas.setBitmap(bitmap)

                    val fillWidth = (getAvailableWidth() * progress / 100F)
                    Log.e("fillWidth", "drawWaves: $fillWidth")

                    mWavePaint.color = waveProgressColor
                    mProgressCanvas.drawRect(0F, 0F, fillWidth, mWaveRect.bottom, mWavePaint)

                    mWavePaint.color = waveBackgroundColor
                    mProgressCanvas.drawRect(
                        fillWidth,
                        0F,
                        getAvailableWidth().toFloat(),
                        mWaveRect.bottom,
                        mWavePaint
                    )

                    val shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
                    mWavePaint.shader = shader
                }
                mWaveRect.right <= getAvailableWidth() * progress / 100F -> {
                    mWavePaint.color = waveProgressColor
                    mWavePaint.shader = null
                }
                else -> {
                    mWavePaint.color = waveBackgroundColor
                    mWavePaint.shader = null
                }
            }

            canvas.drawRoundRect(mWaveRect, waveCornerRadius, waveCornerRadius, mWavePaint)

            lastWaveRight = mWaveRect.right + waveGap

            if (lastWaveRight + waveWidth > getAvailableWidth() + paddingLeft)
                break

            i += 1 / step
        }
    }

    private fun getAvailableWidth() =
        (width - mThumbHalfWidth).toInt()//mCanvasWidth-paddingLeft-paddingRight

    private fun getAvailableHeight() =
        mProgressLineHeight//mCanvasHeight - paddingTop - paddingBottom

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }
        val pointerIndex: Int
        val action = event.action
        when (action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                // Remember where the motion event started
                mActivePointerId = event.getPointerId(event.pointerCount - 1)
                pointerIndex = event.findPointerIndex(mActivePointerId)
                mDownMotionX = event.getX(pointerIndex)
                mPressedThumb = evalPressedThumb(mDownMotionX)

                // Only handle thumb presses.
                if (mPressedThumb == null) {
                    return super.onTouchEvent(event)
                }
                isPressed = true
                mCallback!!.onStartTouch(this)
                invalidate()
                onStartTrackingTouch()
                trackTouchEvent(event)
                attemptClaimDrag()
            }
            MotionEvent.ACTION_MOVE -> if (mPressedThumb != null) {
                if (mIsDragging) {
                    trackTouchEvent(event)
                } else {
                    // Scroll to follow the motion event
                    pointerIndex = event.findPointerIndex(mActivePointerId)
                    val x = event.getX(pointerIndex)
                    if (abs(x - mDownMotionX) > mScaledTouchSlop) {
                        isPressed = true
                        invalidate()
                        onStartTrackingTouch()
                        trackTouchEvent(event)
                        attemptClaimDrag()
                    }
                }
                onChangingValues()
            }
            MotionEvent.ACTION_UP -> {
                if (mIsDragging) {
                    trackTouchEvent(event)
                    onStopTrackingTouch()
                    isPressed = false
                } else {
                    // Touch up when we never crossed the touch slop threshold
                    // should be interpreted as a tap-seek to that location.
                    onStartTrackingTouch()
                    trackTouchEvent(event)
                    onStopTrackingTouch()
                }
                onChangedValues()
                mPressedThumb = null
                invalidate()
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                val index = event.pointerCount - 1
                // final int index = ev.getActionIndex();
                mDownMotionX = event.getX(index)
                mActivePointerId = event.getPointerId(index)
                invalidate()
            }
            MotionEvent.ACTION_POINTER_UP -> {
                onSecondaryPointerUp(event)
                invalidate()
            }
            MotionEvent.ACTION_CANCEL -> {
                if (mIsDragging) {
                    onStopTrackingTouch()
                    isPressed = false
                }
                invalidate() // see above explanation
            }
            else -> {
            }
        }
        return true
    }

    override fun onSaveInstanceState(): Parcelable? {
        val bundle = Bundle()
        bundle.putParcelable("SUPER", super.onSaveInstanceState())
        bundle.putFloat("MIN", mNormalizedMinValue)
        bundle.putFloat("MAX", mNormalizedMaxValue)
        bundle.putFloat("MIN_RANGE", minValue)
        bundle.putFloat("MAX_RANGE", maxValue)
        return bundle
    }

    override fun onRestoreInstanceState(parcel: Parcelable) {
        val bundle = parcel as Bundle
        super.onRestoreInstanceState(bundle.getParcelable("SUPER"))
        mNormalizedMinValue = bundle.getFloat("MIN")
        mNormalizedMaxValue = bundle.getFloat("MAX")
        minValue = bundle.getFloat("MIN_RANGE")
        maxValue = bundle.getFloat("MAX_RANGE")
        onChangedValues()
        onChangingValues()
    }

    private fun dpToPx(dp: Float): Int {
        return ceil(
            dp * Resources.getSystem().displayMetrics.density.toDouble()
        ).toInt()
    }

    companion object {
        private const val INVALID_POINTER_ID = 255
        private const val ACTION_POINTER_INDEX_MASK = 0x0000ff00
        private const val ACTION_POINTER_INDEX_SHIFT = 8
        private val DEFAULT_COLOR = Color.argb(0xFF, 0x33, 0xB5, 0xE5)
        private val DEFAULT_BACKGROUND_COLOR =
            Color.argb(0xFF, 0xC0, 0xC0, 0xC0)
        private const val DEFAULT_PROGRESS_HEIGHT = 10
        private const val DEFAULT_STEP_RADIUS = DEFAULT_PROGRESS_HEIGHT + 2
        private const val DEFAULT_MIN_PROGRESS = 0f
        private const val DEFAULT_MAX_PROGRESS = 100f
        private const val DEFAULT_ANIMATE_DURATION: Long = 1000
    }

    private fun dp(context: Context?, dp: Int): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            context!!.resources.displayMetrics
        )
    }

    interface SeekBarOnProgressChanged {

        fun onProgressChanged(
            waveformSeekBar: AudioWaveSeekBarView,
            progress: Int,
            fromUser: Boolean
        )
    }
}