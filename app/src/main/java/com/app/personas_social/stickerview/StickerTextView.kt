package com.app.personas_social.stickerview

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import com.app.personas_social.R
import kotlin.math.max
import kotlin.math.min

class StickerTextView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = -1
    ) :
        View(context, attrs, defStyleAttr) {
        private val prefferedTextHeight: Int
        private val moveOnTouch: Boolean
        var boxRect = RectF()
        var handleSize: Int
        var thumbPadding: Int
        var top = 0f
        var bottom = 0f
        var currentX = 0f
        private var timeLineChangeListener: TimeLineChangeListener? = null
        private var handleDrawable: Drawable? = null
        private var handleRightDrawable: Drawable? = null
        private var borderDrawable: Drawable? = null
        private val leftGravity: Int
        private val rightGravity: Int
        private val rangeSpans: MutableList<Span> = ArrayList()
        private var activeSpan: Span? = null
        private val spanTextPaint: Paint
        private val gestureDetector: GestureDetector
        private val tempRect = Rect()
        private var textPad = 35
        private var minimumSize = 100

        fun setInfoPadding(textPad: Int) {
            this.textPad = textPad
        }

        fun setMinimumSize(minimumSize: Int) {
            this.minimumSize = minimumSize
        }

        fun setTextColor(color: Int) {
            spanTextPaint.color = color
        }

        fun setTextSize(size: Float) {
            spanTextPaint.textSize = size
//        spanTextPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD);

        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            if (MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY) {
                val height = handleSize + paddingBottom + paddingTop
                setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), height)
            } else {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            }
        }

        override fun onTouchEvent(ev: MotionEvent): Boolean {
            if (!isEnabled) return false
            gestureDetector.onTouchEvent(ev)
            when (ev.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    currentX = ev.x
                    activeSpan = findActiveSpanUnder(currentX)
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    val newX = ev.getX(0)
                    val newY = ev.getY(0)
                    val dx = newX - currentX
                    currentX = newX
                    val dxInt = dx.toInt()
                    var hasUpdate = false
                    if (activeSpan != null) {
                        if (activeSpan!!.isSelected) {
                            hasUpdate = true
                            if (activeSpan!!.leftDragging) {
                                handleLeftMovement(activeSpan!!, dxInt)
                            } else if (activeSpan!!.rightDragging) {
                                handleRightMovement(activeSpan!!, dxInt)
                            } else if (activeSpan!!.translateDragging) {
                                val amount = computeActualDistance(activeSpan!!, dxInt)
                                if (amount != 0) {
                                    activeSpan!!.move(amount)
                                } else {
                                    hasUpdate = false
                                }
                            } else {
                                val leftThumb = bound(activeSpan!!.offset, leftGravity).contains(
                                    newX.toInt(), newY.toInt()
                                )
                                val rightThumb = bound(activeSpan!!.end(), rightGravity).contains(
                                    newX.toInt(), newY.toInt()
                                )
                                if (leftThumb) {
                                    handleLeftMovement(activeSpan!!, dxInt)
                                    activeSpan!!.leftDragging = true
                                } else if (rightThumb) {
                                    handleRightMovement(activeSpan!!, dxInt)
                                    activeSpan!!.rightDragging = true
                                } else if (moveOnTouch) {
                                    val amount = computeActualDistance(activeSpan!!, dxInt)
                                    if (amount != 0) {
                                        activeSpan!!.translateDragging = true
                                        activeSpan!!.move(amount)
                                    } else {
                                        hasUpdate = false
                                    }
                                }
                            }
                        }
                        if (hasUpdate) {
                            invalidate()
                            parent.requestDisallowInterceptTouchEvent(true)
                        }
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (activeSpan != null && timeLineChangeListener != null) {
                        timeLineChangeListener!!.onRangeChanged(
                            activeSpan!!.tag,
                            activeSpan!!.offset * 1f ,
                            activeSpan!!.end() * 1f
                        )
                    }
                    if (activeSpan != null) {
                        activeSpan!!.translateDragging = false
                        activeSpan!!.rightDragging = activeSpan!!.translateDragging
                        activeSpan!!.leftDragging = activeSpan!!.rightDragging
                        activeSpan = null
                    }
                }
            }
            return super.onTouchEvent(ev)
        }

        private fun handleLeftMovement(span: Span, dx: Int) {
            if (dx < 0) {
                val newDx = computeActualDistance(span, dx)
                if (newDx != 0) {
                    span.shrinkLeft(newDx)
                }
            } else {
                span.shrinkLeft(min(dx, span.length - minimumSize))
            }
        }

        private fun handleRightMovement(span: Span, dx: Int) {
            if (dx > 0) {
                val newDx = computeActualDistance(span, dx)
                if (newDx != 0) {
                    span.length += newDx
                }
            } else {
                span.length = max(span.length + dx, minimumSize)
            }
        }

        private fun computeActualDistance(target: Span, dx: Int): Int {
            var canMove = dx <= 0 && target.offset + dx > 0 || dx > 0 && target.end() + dx < width
            if (!canMove) {
                return if (dx <= 0) -target.offset else width - target.end() // compensate
            }
            for (child in rangeSpans) {
                if (child === target) {
                    continue
                }
                if (dx > 0) {
                    if (child.offset > target.offset) { // check childs right off
                        canMove = child.offset >= target.end() + dx
                    }
                } else {
                    if (child.end() <= target.offset) { // check childs left off
                        canMove = child.end() <= target.offset + dx
                    }
                }
                if (!canMove) {
                    // requested dx can be bigger: compensate
                    return if (dx > 0) child.offset - target.end() else child.end() - target.offset
                }
            }
            return dx
        }

        private fun extraBasedOnThumbGravity(): Int {
            var gravityPadding = 0
            val mask = leftGravity and Gravity.HORIZONTAL_GRAVITY_MASK
            if (mask == Gravity.LEFT) {
                gravityPadding = handleSize
            } else if (mask == Gravity.CENTER_HORIZONTAL) {
                gravityPadding = handleSize / 2
            }
            return gravityPadding
        }

        private fun findActiveSpanUnder(x: Float): Span? {
            val extraPadding = extraBasedOnThumbGravity()
            for (range in rangeSpans) {
                if (range.isSelected && range.offset - extraPadding < x && x < range.end() + extraPadding) {
                    return range
                }
            }
            return null
        }

        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            top = paddingTop.toFloat()
            bottom = (h - paddingBottom).toFloat()
        }

        @JvmOverloads
        fun addSpan(offset: Int, length: Int, info: String? = null, obj: Any? = null) {
            val newSpan = Span(offset, length, info, obj)
            if (verifyBoundsAreCorrect(newSpan)) {
                rangeSpans.add(newSpan)
            }
        }

        private fun verifyBoundsAreCorrect(item: Span): Boolean {
            for (range in rangeSpans) {
                if (overlap(item, range)) {
                    Log.d(TAG, "Cannot have overlapping bars")
                    return false
                }
            }
            return true
        }

        fun addSpan(span: Span) {
            if (verifyBoundsAreCorrect(span)) {
                rangeSpans.add(span)
            }
        }

        fun removeSpan(tag: Any) {
            var i: Int
            i = 0
            while (i < rangeSpans.size) {
                if (rangeSpans[i].tag === tag) {
                    break
                }
                i++
            }
            if (i < rangeSpans.size) {
                rangeSpans.removeAt(i)
                invalidate()
            }
        }

        fun updateSpan(tag: Any, newInfo: String?) {
            val span = findSpanByTag(tag)
            if (span != null) {
                span.info = newInfo
                invalidate()
            }
        }

        fun updateSpan(tag: Any, selected: Boolean) {
            val span = findSpanByTag(tag)
            if (span != null) {
                span.isSelected = selected
                invalidate()
            }
        }

        fun updateSpanRange(tag: Any, offset: Int, length: Int) {
            val span = findSpanByTag(tag)
            if (span != null) {
                val oldOffset = span.offset
                val oldLen = span.length
                span.offset = offset
                span.length = length
                for (target in rangeSpans) {
                    if (target !== span && overlap(span, target)) {
                        span.offset = oldOffset
                        span.length = oldLen
                        Log.d(TAG, "Property update fail: Overlapping")
                    }
                }
                invalidate()
            }
        }

        private fun findSpanByTag(tag: Any): Span? {
            for (span in rangeSpans) {
                if (span.tag === tag) {
                    return span
                }
            }
            return null
        }

        private fun overlap(target: Span, source: Span): Boolean {
            return target.offset < source.end() && source.offset < target.end()
        }

        override fun onDraw(canvas: Canvas) {
            if (width > 0 && height > 0) {
                var selectedSpan: Span? = null
                for (item in rangeSpans) {
                    boxRect[item.offset.toFloat(), top, item.end().toFloat()] = bottom
                    if (!item.draw(canvas, boxRect)) {
                        if (borderDrawable != null) {
                            borderDrawable!!.setBounds(
                                boxRect.left.toInt(),
                                boxRect.top.toInt(), boxRect.right.toInt(), boxRect.bottom.toInt()
                            )
                            borderDrawable!!.state = if (item.isSelected) SELECTED_STATE_SET else EMPTY_STATE_SET
                            borderDrawable!!.draw(canvas)
                        }
                        val txt = item.info
                        if (!TextUtils.isEmpty(txt)) {
                            val leftPad =
                                if (leftGravity and Gravity.HORIZONTAL_GRAVITY_MASK == Gravity.RIGHT) handleSize + thumbPadding else textPad
                            drawInfo(canvas, item, leftPad, boxRect)
                        }
                    }
                    if (item.isSelected) {
                        selectedSpan = item
                    }
                }

                if (selectedSpan != null) {
                    if (handleDrawable != null) {
                        val leftBounds = bound(selectedSpan.offset, leftGravity)
                        val rightBounds = bound(selectedSpan.end()-32, rightGravity)
                        if (leftBounds.right < rightBounds.left) {
                            handleDrawable!!.bounds = leftBounds
                            handleDrawable!!.draw(canvas)
                            handleRightDrawable!!.bounds = rightBounds
                            handleRightDrawable!!.draw(canvas)
                        }
                    }
                }
            }
        }

        private fun drawInfo(canvas: Canvas, span: Span, leftPadding: Int, boxRect: RectF) {
            var txt = span.info
            spanTextPaint.getTextBounds(txt, 0, span.info!!.length, tempRect)
            if (boxRect.width() - 2 * textPad < tempRect.width()) {
                val newEnd = (txt!!.length * (boxRect.width()  + - 2 * textPad) / tempRect.width()).toInt()
                txt =
                    if (newEnd <= 0) "" else if (newEnd < txt.length) txt.substring(0, newEnd) else txt
            }
            canvas.drawText(
                txt!!,
                boxRect.left + leftPadding,
                boxRect.bottom - (boxRect.height() - prefferedTextHeight) / 2,
                spanTextPaint
            )
        }

        private fun bound(anchor: Int, gravity: Int): Rect {
            val height :Int =   handleDrawable!!.intrinsicHeight
            //        val height = handleSize
            val rect = Rect()

            when (gravity and Gravity.VERTICAL_GRAVITY_MASK) {
                Gravity.CENTER_VERTICAL -> {
                    Log.e(TAG, "CENTER_VERTICAL: ",)
                    rect.top = (getHeight() - height) / 2
                    rect.bottom = (getHeight() + height) / 2
                }
                Gravity.BOTTOM -> {

                    rect.top = getHeight() - height
                    rect.bottom = getHeight()
                }
                Gravity.TOP -> {
                    rect.top = 0
                    rect.bottom = height
                }
                else -> {
                    Log.e(TAG, "ELSE: ",)

                    rect.top = 0
                    rect.bottom = height
                }
            }

            when (gravity and Gravity.HORIZONTAL_GRAVITY_MASK) {
                Gravity.CENTER_HORIZONTAL -> rect.left = anchor -5- 0 / 2
                Gravity.END -> rect.left = anchor -0
                Gravity.START -> rect.left = anchor - 0 - 0
                else -> rect.left = anchor - 0 - 0
            }
            Log.e(TAG, "left: "+rect.left )
            rect.right = rect.left + handleSize
            Log.e(TAG, "right: "+rect.right )
            return rect
        }

        fun addIndicatorChangeListener(timeLineChangeListener: TimeLineChangeListener?) {
            this.timeLineChangeListener = timeLineChangeListener
        }

        private fun singleTapConfirmed(e: MotionEvent): Boolean {
            val x = e.x
            var spanToSelect: Span? = null
            var spanToDeselect: Span? = null
            val gravityPadding = extraBasedOnThumbGravity()
            for (item in rangeSpans) {
                if (item.isSelected) {
                    if (x < item.offset - gravityPadding || x > item.end() + gravityPadding) { // is out ?
                        item.isSelected = false
                        spanToDeselect = item
                        Log.d(TAG, "UNSelect " + spanToDeselect.hashCode())
                    } else {
                        if (x < item.offset - gravityPadding + handleSize) {
                            if (timeLineChangeListener != null) {
                                timeLineChangeListener!!.onThumbClicked(item.tag, 0)
                            }
                        } else if (x > item.end() + gravityPadding - handleSize) {
                            if (timeLineChangeListener != null) {
                                timeLineChangeListener!!.onThumbClicked(item.tag, 1)
                            }
                        }
                        if (spanToSelect != null) {
                            spanToSelect.isSelected = false
                            spanToSelect = null
                        }
                        break
                    }
                } else if (item.offset < x && x < item.end()) {
                    item.isSelected = true
                    spanToSelect = item
                }
            }
            if (spanToSelect != null) {
                notifySelectionChange(spanToSelect.tag, true)
            } else if (spanToDeselect != null) {
                notifySelectionChange(spanToDeselect.tag, false)
            }
            invalidate()
            return false
        }

        private fun notifySelectionChange(tag: Any?, `val`: Boolean) {
            if (timeLineChangeListener != null) {
                timeLineChangeListener!!.onSelectionChange(tag, `val`)
            }
        }

        interface TimeLineChangeListener {
            fun onRangeChanged(tag: Any?, startFraction: Float, endFraction: Float)
            fun onSelectionChange(tag: Any?, selected: Boolean)
            fun onThumbClicked(tag: Any?, thumbId: Int)
        }

        class Span(var offset: Int, var length: Int, var info: String?, var tag: Any?) {
            var isSelected = false
            var leftDragging = false
            var rightDragging = false
            var translateDragging = false
            fun draw(canvas: Canvas?, bound: RectF?): Boolean {
                return false
            }

            fun end(): Int {
                return offset + length
            }

            fun shrinkLeft(dx: Int) {
                val oldOffset = offset
                offset += dx
                length -= (offset - oldOffset)
            }

            fun move(dx: Int) {
                offset += dx
            }
        }

        companion object {
            private val TAG = StickerTextView::class.java.simpleName
            fun dpToPx(context: Context, dp: Float): Int {
                val density = context.resources.displayMetrics.density
                return (dp * density + 0.5f).toInt()
            }
        }

        init {
            gestureDetector =
                GestureDetector(getContext(), object : GestureDetector.SimpleOnGestureListener() {
                    override fun onSingleTapUp(e: MotionEvent): Boolean {
                        return singleTapConfirmed(e)
                    }
                })
            val typedArray = context.theme.obtainStyledAttributes(
                attrs, R.styleable.TextRangeView,
                0, 0
            )
            handleSize = typedArray.getDimension(
                R.styleable.TextRangeView_thumb_size,
                dpToPx(context, 20f).toFloat()
            )
                .toInt()

            thumbPadding =
                typedArray.getDimensionPixelSize(R.styleable.TextRangeView_thumb_padding, 0)
            moveOnTouch = typedArray.getBoolean(R.styleable.TextRangeView_move_on_touch, false)
            leftGravity = typedArray.getInt(R.styleable.TextRangeView_thumbGravity, Gravity.CENTER)
            rightGravity = ViewHelper.revertGravity(leftGravity)
            var id = typedArray.getResourceId(R.styleable.TextRangeView_thumbSrc, -1)
            if (id != -1) {
                handleDrawable = AppCompatResources.getDrawable(getContext(), id)
            }
            if (handleDrawable != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && handleDrawable!!.constantState != null) {
                    handleRightDrawable = handleDrawable!!.constantState!!.newDrawable()
                    handleRightDrawable!!.layoutDirection = LAYOUT_DIRECTION_RTL
                    handleRightDrawable!!.isAutoMirrored = true
                } else {
                    handleRightDrawable = handleDrawable
                }
                if (typedArray.hasValue(R.styleable.TextRangeView_srcTint)) {
                    val tint = typedArray.getColorStateList(R.styleable.TextRangeView_srcTint)
                    handleDrawable = handleDrawable!!.mutate()
                    DrawableCompat.setTintList(handleDrawable!!, tint)
                    handleRightDrawable = handleRightDrawable!!.mutate()
                    DrawableCompat.setTintList(handleRightDrawable!!, tint)
                }
            }
            id =
                typedArray.getResourceId(R.styleable.TextRangeView_background, R.drawable.ic_launcher_background)
            if (id != -1) {
                borderDrawable = AppCompatResources.getDrawable(getContext(), id)
            }
            typedArray.recycle()
            spanTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            spanTextPaint.textSize = dpToPx(getContext(), 12f).toFloat()
            spanTextPaint.color = Color.BLACK
            spanTextPaint.getTextBounds("A", 0, 1, tempRect)
            prefferedTextHeight = tempRect.height()
        }

    }