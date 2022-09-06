package com.videoframeview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.AsyncTask
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import java.util.concurrent.TimeUnit
import kotlin.math.ceil


class VideoFrameViewCutter: View {
    private var videoLength: Long = 0
    var leftProgress = 0f
    var playX = 0f
        private set
    var rightProgress = 1f
        private set
    private var paint: Paint? = null
    private var paintCursor: Paint? = null
    private var paint2: Paint? = null
    private var pressedLeft = false
    private var pressedPlay = false
    private var pressedRight = false
    var playProgress = 0.0f
    private var pressDx = 0f
    private var mediaMetadataRetriever: MediaMetadataRetriever? = null
    private var delegate: VideoTimelineViewDelegate? =
        null
    private val frames = ArrayList<Bitmap?>()
    private var currentTask: AsyncTask<Int?, Int?, Bitmap?>? = null
    private var frameTimeOffset: Long = 0
    private var frameWidth = 0
    private var frameHeight = 0
    private var framesToLoad = 0
    private var maxProgressDiff = 1.0f
    private var minProgressDiff = 0.0f
    private var isRoundFrames = false
    private var rect1: Rect? = null
    private var rect2: Rect? = null
    private val rect3 = RectF()
        private var drawableLeft: Drawable? = null
    private var drawableRight: Drawable? = null
    private var maxVideoSizeProgressDiff = -1f
    private var position = 0
    private var startTime = 0L
    private var endTime = 0L
    private var isDrawableSet = false
    private var isThumbDisplay = false
    private var isSplit = false
    private var splitStartTime = 0L

    interface VideoTimelineViewDelegate {
        fun onLeftProgressChanged(progress: Float)
        fun onRightProgressChanged(progress: Float)
        fun onPlayProgressChanged(progress: Float,startprogress   : Float)
        fun onDidStartDragging()
        fun onDidStopDragging()
    }

    constructor(context: Context?) : super(context) {
        initialize(null)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context,
        attrs
    ) {
        initialize(attrs)
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        initialize(attrs)
    }

    private fun initialize(attrs: AttributeSet?) {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.VideoFrameView, 0, 0)
        drawableLeft = if (a.hasValue(R.styleable.VideoFrameView_thumbsDrawable)) {
            ContextCompat.getDrawable(
                context, a.getResourceId(
                    R.styleable.VideoFrameView_thumbsDrawable,
                    R.drawable.ic_frame_bar
                )
            )
        } else
            ContextCompat.getDrawable(context, R.drawable.ic_vertical_line)

        drawableRight = if (a.hasValue(R.styleable.VideoFrameView_thumbsDrawable)) {
            ContextCompat.getDrawable(
                context, a.getResourceId(
                    R.styleable.VideoFrameView_thumbsDrawable,
                    R.drawable.ic_right
                )
            )
        } else

        ContextCompat.getDrawable(context, R.drawable.ic_right)
        isDrawableSet = a.getBoolean(R.styleable.VideoFrameView_isDrawableSet, isDrawableSet)
        isThumbDisplay = a.getBoolean(R.styleable.VideoFrameView_isThumbDisplay, isThumbDisplay)
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint!!.color = -0x1
        paint2 = Paint()
        paint2!!.color = 0x7f000000
        paintCursor = Paint()
        paintCursor!!.color = ContextCompat.getColor(context, android.R.color.white)

        setColor(ContextCompat.getColor(context, android.R.color.black))
    }

    fun setPosition(position: Int) {
        this.position = position
    }

    var progress: Float
        get() = playProgress
        set(value) {
            playProgress = value
            postInvalidate()
        }

    fun setMinProgressDiff(value: Float) {
        minProgressDiff = value
    }

    fun setMaxProgressDiff(value: Float) {
        maxProgressDiff = value
        if (rightProgress - leftProgress > maxProgressDiff) {
            rightProgress = leftProgress + maxProgressDiff
            postInvalidate()
        }
    }

    /**
     * Pass 0 or below to disable max video size
     */
    fun setMaxVideoSize(maxVideoSize: Long, videoOriginalSize: Long) {
        if (maxVideoSize > 0) {
            maxVideoSizeProgressDiff = maxVideoSize.toFloat() / videoOriginalSize
        }
    }

    fun setStartEndTime(startTime: Long, endTime: Long) {
        this.startTime = startTime
        this.endTime = endTime
    }

    fun setSplit(isSplit: Boolean, splitStartTime: Long) {
        this.isSplit = isSplit
        this.splitStartTime = splitStartTime
    }

/*    fun setRoundFrames(value: Boolean) {
        isRoundFrames = value
        if (isRoundFrames) {
            rect1 = Rect(
                dp(context, 14f),
                dp(context, 14f),
                dp(context, 14 + 28.toFloat()),
                dp(context, 14 + 28.toFloat())
            )
            rect2 = Rect()
        }
    }*/

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        Log.e("onTouchEvent", "Called")
        val x = event.x
        val y = event.y
        val width = measuredWidth - dp(context, 32f)
        var startX = (width * leftProgress).toInt() + dp(context, 16f)
         playX = ((width ) * (leftProgress + ((rightProgress ) - leftProgress) * playProgress)) + dp(
             context,16f)
        var endX = (width * rightProgress).toInt() + dp(context, 16f)
        if (event.action == MotionEvent.ACTION_DOWN) {
            parent.requestDisallowInterceptTouchEvent(true)
            if (mediaMetadataRetriever == null) {
                return false
            }
            val additionWidth = dp(context, 12f)
            val additionWidthPlay = dp(context, 8f)
            if (playX - additionWidthPlay <= x && x <= playX + additionWidthPlay && y >= 0 && y <= measuredHeight ) {
                delegate?.onDidStartDragging()
                pressedPlay = true
                pressDx =  (x - playX )
                postInvalidate()
                return true
            } else
            if (startX - additionWidth <= x && x <= startX + additionWidth && y >= 0 && y <= measuredHeight) {
                if (delegate != null) {
                    delegate!!.onDidStartDragging()
                }
                pressedLeft = true
                pressDx = (x - startX)
                postInvalidate()
                return true
            }
            else if (endX - additionWidth <= x && x <= endX + additionWidth && y >= 0 && y <= measuredHeight) {
                if (delegate != null) {
                    delegate!!.onDidStartDragging()
                }
                pressedRight = true
                pressDx = (x - endX)
                postInvalidate()
                return true
            } else
                return false
        } else if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
            if (pressedLeft) {
                if (delegate != null) {
                    delegate!!.onDidStopDragging()
                }
                pressedLeft = false
                return true
            } else if (pressedRight) {
                if (delegate != null) {
                    delegate!!.onDidStopDragging()
                }
                pressedRight = false
                return true
            }
            else if (pressedPlay) {
                delegate?.onDidStopDragging()
                pressedPlay = false
                return true
            }
            return false
        } else if (event.action == MotionEvent.ACTION_MOVE) {
            if (pressedPlay) {
                playX = (x - pressDx)
                playProgress = (playX - dp(context,20f)) /  width

                if (playProgress < leftProgress) {
                    playProgress = leftProgress
                }
                else if (playProgress > rightProgress) {
//                    playProgress = rightProgress - 0.01000f
                    playProgress = rightProgress
                }
                Log.e("onTouchEvent", "playProgress: "+playProgress )
                Log.e("onTouchEvent", "rightProgress: "+rightProgress )
                Log.e("onTouchEvent", "leftProgress: "+leftProgress )

                delegate?.onPlayProgressChanged(leftProgress + (rightProgress - leftProgress) * playProgress,leftProgress)
                postInvalidate()

                return true
            }
            else if (pressedLeft) {
                startX = (x - pressDx).toInt()
                if (startX < dp(context, 16f)) {
                    startX = dp(context, 16f)
                } else if (startX > endX) {
                    startX = endX
                }
                leftProgress =
                    (startX - dp(context, 16f)).toFloat() / width.toFloat()
                if (maxVideoSizeProgressDiff != -1f && rightProgress - leftProgress > maxVideoSizeProgressDiff) { // Higher than max video size limit
                    rightProgress = leftProgress + maxVideoSizeProgressDiff
                } else if (rightProgress - leftProgress > maxProgressDiff) {
                    rightProgress = leftProgress + maxProgressDiff
                } else if (minProgressDiff != 0f && rightProgress - leftProgress < minProgressDiff) {
                    leftProgress = rightProgress - minProgressDiff
                    if (leftProgress < 0) {
                        leftProgress = 0f
                    }
                }
                if (delegate != null) {
                    delegate!!.onLeftProgressChanged(leftProgress)
                }
                postInvalidate()
                return true
            }
            else if (pressedRight) {
                endX = (x - pressDx).toInt()
                if (endX < startX) {
                    endX = startX
                } else if (endX > width + dp(context, 16f)) {
                    endX = width + dp(context, 16f)
                }
                rightProgress =
                    (endX - dp(context, 16f)).toFloat() / width.toFloat()
                if (maxVideoSizeProgressDiff != -1f && rightProgress - leftProgress > maxVideoSizeProgressDiff) { // Higher than max video size limit
                    leftProgress = rightProgress - maxVideoSizeProgressDiff
                } else if (rightProgress - leftProgress > maxProgressDiff) {
                    leftProgress = rightProgress - maxProgressDiff
                } else if (minProgressDiff != 0f && rightProgress - leftProgress < minProgressDiff) {
                    rightProgress = leftProgress + minProgressDiff
                    if (rightProgress > 1.0f) {
                        rightProgress = 1.0f
                    }
                }
                if (delegate != null) {
                    delegate!!.onRightProgressChanged(rightProgress)
                }
                postInvalidate()
                return true
            }

        }
        return false
    }

    private fun setColor(color: Int) {
        paint!!.color = color
    }

    fun setVideoPath(path: Uri?, videoDuration: Long, isEdit: Boolean) {
        destroy()
        mediaMetadataRetriever = MediaMetadataRetriever()
        if (isEdit) {
            leftProgress = (startTime.toFloat() / videoDuration.toFloat())
            rightProgress = (endTime.toFloat() / videoDuration.toFloat())
            if (rightProgress > 1f)
                rightProgress = 1.0f
            if (leftProgress >= 1f)
                leftProgress = 0f
//            leftProgress = startTime.toFloat()
//            rightProgress = endTime.toFloat()
        } else {
            leftProgress = 0.0f
            rightProgress = 1.0f
//            leftProgress = 0.0f
//            rightProgress = videoDuration.toFloat()
        }
        try {
            mediaMetadataRetriever!!.setDataSource(context, path)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        videoLength = videoDuration
        postInvalidate()
    }

    fun setDelegate(delegate: VideoTimelineViewDelegate?) {
        this.delegate = delegate
    }

    private fun reloadFrames(frameNum: Int) {
        if (mediaMetadataRetriever == null) {
            return
        }
        if (frameNum == 0) {
            if (isRoundFrames) {
                frameWidth = dp(context, 56f)
                frameHeight = frameWidth
                framesToLoad = ceil(
                    (measuredWidth - dp(
                        context,
                        16f
                    )) / (frameHeight / 2.0f).toDouble()
                ).toInt()
            } else {
                frameHeight = dp(context, 60f)
                framesToLoad = (measuredWidth - dp(context, 16f)) / frameHeight
                frameWidth = ceil(
                    (measuredWidth.toDouble() - dp(
                        context,
                        16f
                    )) / framesToLoad.toFloat()
                ).toInt()
            }
            frameTimeOffset = videoLength / framesToLoad
//            Log.e("VideoFrameView", "videoLength $videoLength framesToLoad $framesToLoad")
        }
        currentTask = @SuppressLint("StaticFieldLeak")
        object : AsyncTask<Int?, Int?, Bitmap?>() {
            private var numFrame = 0

            override fun onPostExecute(bitmap: Bitmap?) {
                if (!isCancelled) {
                    frames.add(bitmap)
                    postInvalidate()
                    if (numFrame < framesToLoad) {
                        reloadFrames(numFrame + 1)
                    }
                }
            }

            override fun doInBackground(vararg params: Int?): Bitmap? {
                numFrame = params[0]!!
                var bitmap: Bitmap? = null
                if (isCancelled) {
                    return null
                }
                try {
                    var time = frameTimeOffset * numFrame * 1000
                    if (isSplit) time += (splitStartTime * 1000)
//                    Log.e("VideoFrameView", "time $time frameTimeOffset $frameTimeOffset numFrame $numFrame")
                    bitmap = mediaMetadataRetriever!!.getFrameAtTime(
                        time,
                        MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                    )
                    if (isCancelled) {
                        return null
                    }
                    if (bitmap != null) {
                        val result =
                            Bitmap.createBitmap(frameWidth, frameHeight, bitmap.config)
                        val canvas = Canvas(result)
                        val scaleX =
                            frameWidth.toFloat() / bitmap.width.toFloat()
                        val scaleY =
                            frameHeight.toFloat() / bitmap.height.toFloat()
                        val scale = if (scaleX > scaleY) scaleX else scaleY
                        val w = (bitmap.width * scale).toInt()
                        val h = (bitmap.height * scale).toInt()
                        val srcRect =
                            Rect(0, 0, bitmap.width, bitmap.height)
                        val destRect =
                            Rect((frameWidth - w) / 2, (frameHeight - h) / 2, w, h)
                        canvas.drawBitmap(bitmap, srcRect, destRect, null)
                        bitmap.recycle()
                        bitmap = result
                    }
                } catch (e: Exception) { //FileLog.e(e)
                    e.printStackTrace()
                }
                return bitmap
            }
        }
        currentTask!!.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, frameNum, null, null)
    }

    private fun destroy() {
        synchronized(sync) {
            try {
                if (mediaMetadataRetriever != null) {
                    mediaMetadataRetriever!!.release()
                    mediaMetadataRetriever = null
                }
            } catch (e: Exception) { //FileLog.e(e)
                e.printStackTrace()
            }
        }
        for (a in frames.indices) {
            val bitmap = frames[a]
            bitmap?.recycle()
        }
        frames.clear()
        if (currentTask != null) {
            currentTask!!.cancel(true)
            currentTask = null
        }
    }

    fun clearFrames() {
        for (a in frames.indices) {
            val bitmap = frames[a]
            bitmap?.recycle()
        }
        frames.clear()
        if (currentTask != null) {
            currentTask!!.cancel(true)
            currentTask = null
        }
        postInvalidate()
    }

    /*  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
          super.onMeasure(widthMeasureSpec, heightMeasureSpec)
          val widthSize = MeasureSpec.getSize(widthMeasureSpec)
          if (lastWidth != widthSize) {
              clearFrames()
              lastWidth = widthSize
          }
      }*/

    @SuppressLint("ResourceAsColor")
    override fun onDraw(canvas: Canvas) {
        val width = measuredWidth - dp(context, 36f)
        val startX = (width * leftProgress).toInt() + dp(context, 16f)
        val endX = (width * rightProgress).toInt() + dp(context, 16f)
        canvas.save()
        canvas.clipRect(
            dp(context, 16f),
            dp(context, 4f),
            width + dp(context, 20f),
            dp(context, 56f)
        )
        if (frames.isEmpty() && currentTask == null) {
            reloadFrames(0)
        } else {
            for ((offset, a) in frames.indices.withIndex()) {
                val bitmap = frames[a]
                if (bitmap != null) {
                    var x = dp(
                        context,
                        16f
                    ) + offset * if (isRoundFrames) frameWidth / 2 else frameWidth
                    if (a > 0) { // Add frame space 2dp
                        x += dp(context, 0f) * offset
                    }
                    val y = dp(context, 2 + 4.toFloat())
                    if (isRoundFrames) {
                        rect2!![x, y, x + dp(context, 28f)] = y + dp(context, 28f)
                        canvas.drawBitmap(bitmap, rect1, rect2!!, null)
                    } else {
                        canvas.drawBitmap(bitmap, x.toFloat(), y.toFloat(), null)
                    }
                }
            }
        }
        val top = dp(context, 2 + 4.toFloat())
        val end = dp(context, 56f)

//        canvas.drawRect(
//            dp(context, 16f).toFloat(),
//            top.toFloat(),
//            startX.toFloat(),
//            dp(context, 46f).toFloat(),
//            paint2!!
//        )
//        canvas.drawRect(
//            endX + dp(context, 4f).toFloat(),
//            top.toFloat(),
//            dp(context, 16f) + width + dp(context, 4f).toFloat(),
//            dp(context, 46f).toFloat(),
//            paint2!!
//        )
        /**
         * Draw top line
         */


        canvas.drawRect(
            startX.toFloat(),
            dp(context, 4f).toFloat(),
            startX + dp(context, 2f).toFloat(),
            end.toFloat(),
            paint!!
        )
//        /**
//         * Draw bottom line
//         */
//        canvas.drawRect(
//            endX + dp(context, 2f).toFloat(),
//            dp(context, 4f).toFloat(),
//            endX + dp(context, 4f).toFloat(),
//            end.toFloat(),
//            paint!!
//        )
        /**
         * Draw top line
         */

        canvas.drawRect(
            startX + dp(context, 2f).toFloat(),
            dp(context, 4f).toFloat(),
            endX + dp(context, 4f).toFloat(),
            top.toFloat(),
            paint!!
        )

        /**
         * Draw bottom line
         */

        canvas.drawRect(
            startX + dp(context, 2f).toFloat(),
            end - dp(context, 2f).toFloat(),
            endX + dp(context, 4f).toFloat(),
            end.toFloat(),
            paint!!
        )
        //Reactagle middle

        // rectangle positions
        val left = startX + dp(context, 2f)
        val right = endX + dp(context, 4f)

        // draw rectangle shape to canvas
        val shapeDrawable: ShapeDrawable = ShapeDrawable(RectShape())
        shapeDrawable.setBounds( left, top, right, end)
        shapeDrawable.paint.color = Color.parseColor("#54F32677")
        shapeDrawable.draw(canvas)
        canvas.restore()

        // Draw left line and the drawable
       if (isDrawableSet) {
            drawableLeft!!.setBounds(
                startX - dp(context, 10f),
                (dp(context, 52f) - dp(context, 18f)) / 2 - dp(context, 18.toFloat()) + dp(
                    context,
                    5f
                ),
                startX + dp(context, 4f),
                (dp(context, 52f) + dp(context, 18f)) / 2 + dp(context, 18 + 3.toFloat())
            )
            drawableLeft!!.draw(canvas)
        }
     else {
            rect3.set(
                (startX - dp(context, 8f)).toFloat(),
                dp(context, 4f).toFloat(),
                (startX + dp(context, 2f)).toFloat(),
                end.toFloat(),
            )

            canvas.drawRoundRect(
                rect3,
                dp(context, 4f).toFloat(),
                dp(context, 4f).toFloat(),
                paint!!
            )
            drawableLeft!!.setBounds(
                startX - dp(context, 8f), dp(context, 4f) + (dp(context, 52f) - dp(
                    context, 18f
                )) / 2, startX + dp(context, 2f), (dp(context, 52f) - dp(context, 18f)) / 2 + dp(
                    context, 18f + 4
                )
            )
            drawableLeft!!.draw(canvas)
        }

        /*rect3.set(startX, dp(getContext(),4), startX + dp(getContext(),1), end)
        canvas.drawRoundRect(rect3, dp(getContext(),4), dp(getContext(),4), paint)
        rect3.set(startX - dp(getContext(),2f), dp(getContext(),16), startX + dp(getContext(),4), dp(getContext(),36.2f))
        canvas.drawRoundRect(rect3, dp(getContext(),4), dp(getContext(),4), paint)*/
         // Draw right line and the drawable

        if (isDrawableSet) {
            drawableRight!!.setBounds(
                endX + dp(context, 0f),
                (dp(context, 52f) - dp(context, 18f)) / 2 - dp(context, 18.toFloat()) + dp(
                    context,
                    5f
                ),
                endX + dp(context, 14f),
                (dp(context, 52f) + dp(context, 18f)) / 2 + dp(context, 18 + 3.toFloat())
            )
            drawableRight!!.draw(canvas)
        } else {
            rect3.set(
                (endX + dp(context, 2f)).toFloat(),
                dp(context, 4f).toFloat(), (endX + dp(context, 12f)).toFloat(), end.toFloat()
            )
            canvas.drawRoundRect(
                rect3,
                dp(context, 4f).toFloat(),
                dp(context, 4f).toFloat(),
                paint!!
            )
            drawableRight!!.setBounds(
                endX + dp(context, 2f),
                dp(context, 4f) + (dp(context, 52f) - dp(context, 18f)) / 2,
                endX + dp(context, 12f),
                (dp(context, 52f) - dp(context, 18f)) / 2 + dp(context, 18f + 4)
            )
            drawableRight!!.draw(canvas)
        }
        /*rect3.set(endX + dp(getContext(),2), dp(getContext(),4), endX + dp(getContext(),3), end)
        canvas.drawRoundRect(rect3, dp(getContext(),4), dp(getContext(),4), paint)
        rect3.set(endX - dp(getContext(),0), dp(getContext(),16), endX + dp(getContext(),6), dp(getContext(),36.2f))
        canvas.drawRoundRect(rect3, dp(getContext(),4), dp(getContext(),4), paint)*/
// Draw seek bar to seek frames

        // h and w are height and width of the screen
        // h and w are height and width of the screen
              var cx = dp(
            context,
            20f
        ) + width * (leftProgress + (rightProgress - leftProgress) * playProgress)

//         cx = dp(context,18f) + width * (leftProgress + (rightProgress - leftProgress) * playProgress)
//        rect3[cx - dp(context, 1.5f), dp(context, 2f).toFloat(), cx + dp(context, 1.5f)] = dp(
//            context, 50f
//        ).toFloat()
//        canvas.drawRoundRect(
//            rect3,
//            dp(context, 1f).toFloat(),
//            dp(context, 1f).toFloat(),
//            paint2!!
//        )
//       paint2!!.color = android.R.color.black
//        canvas.drawCircle(
//            cx,
//            dp(context, 5f).toFloat(),
//            dp(context, 6.6f).toFloat(),
//            paint2!!
//        )

        if (!isDrawableSet && isThumbDisplay) {
            rect3[cx + dp(context, 1f), dp(context, 2f).toFloat(), cx + dp(context, 4f)] =
                dp(context, 70f).toFloat()
            canvas.drawRoundRect(
                rect3,
                dp(context, 1f).toFloat(),
                dp(context, 1f).toFloat(),
                paintCursor!!
            )

        }
    }

    private fun dp(context: Context, value: Float): Int {
        return if (value == 0f) {
            0
        } else ceil(context.resources.displayMetrics.density * value.toDouble()).toInt()
    }

    companion object {
        private val sync = Any()
    }
}