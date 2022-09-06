package com.videoframeview

import android.net.Uri
import android.util.Log
import io.reactivex.disposables.Disposable
import java.io.File
import java.util.*
import kotlin.math.ceil

class VideoFrameHelper(
    private var videoFrameView: VideoFrameView,
    private val position: Int,
    private val onProgressChanged: (startTime: Long, endTime: Long, action: Int) -> Unit,
    private val onDidStartDragging: () -> Unit,
    private val onDidStopDragging: (startTime: Long, endTime: Long) -> Unit
) {
    private var videoUri: Uri? = null
    private var videoFile: File? = null
    private var videoDuration = 0f
    private var estimatedDuration: Long = 0
    private var trimStartTime: Long = 0
    private var trimEndTime: Long = 0
    private var originalSize: Long = 0
    private var estimatedSize: Long = 0
    private val trimTask: Disposable? = null
    private val convertTask: Disposable? = null
    private var path = ""
    private val videoMinDurationMs = 1000
    private val videoMaxDurationMs = 60000
    private val videoMaxSize = 10 * 1024 * 1024.toLong()

    companion object {
        const val PROGRESS_LEFT = 1
        const val PROGRESS_RIGHT = 2
        const val PROGRESS_PLAY = 3
    }

    fun onDestroy() {
        clearFrames()
        trimTask?.dispose()
        convertTask?.dispose()
    }

    //    fun updatePlayProgress(currentPosition: Long) {
    fun updatePlayProgress(currentPosition: Long, videoDuration: Long) {
        var progress =
            if (videoDuration > 0) currentPosition.toFloat() / videoDuration else 0f
        progress -= videoFrameView.leftProgress
        if (progress < 0) {
            progress = 0f
        }
        progress /= videoFrameView.rightProgress - videoFrameView.leftProgress
        if (progress > 1) {
            progress = 1f
        }
        videoFrameView.progress = progress
    }

    fun initialize(
        videoFile: Uri?,
        videoDuration: Long,
        startTime: Long,
        endTime: Long,
        isTrimmed: Boolean,
        isSpeed: Boolean
    ) {

        videoUri = videoFile
        path = videoFile!!.path!!
        this.videoFile = File(path)
        videoFrameView.setPosition(position)
        this.videoDuration =
            if (isTrimmed || isSpeed) videoDuration.toFloat() else (endTime - startTime).toFloat()
        if (startTime.toFloat() == this.videoDuration || videoDuration > startTime && videoDuration - startTime < 50 || startTime > videoDuration && startTime - videoDuration < 50) {
            trimStartTime = 0
            trimEndTime = this.videoDuration.toLong()
        } else {
            trimStartTime = startTime
            trimEndTime = endTime
        }
        val isEdit = isTrimmed || this.videoDuration != (trimEndTime - trimStartTime).toFloat()
        if (isEdit)
            videoFrameView.setStartEndTime(startTime, endTime)
//        videoFrameView.setSplit(isSplit, startTime)
        initVideo(isEdit)
    }

    private fun updateVideoInfo() {
        trimStartTime =
            ceil(videoFrameView.leftProgress * videoDuration.toDouble()).toLong()
        trimEndTime =
            ceil(videoFrameView.rightProgress * videoDuration.toDouble()).toLong()
        estimatedDuration = trimEndTime - trimStartTime
        estimatedSize =
            (originalSize * (estimatedDuration.toFloat() / videoDuration)).toLong()
    }

    private fun initVideo(isEdit: Boolean) {
        originalSize = videoFile!!.length()
        initVideoFrameView(isEdit)
    }

    private fun initVideoFrameView(isEdit: Boolean) {
        if (videoDuration >= videoMinDurationMs + 1000) {
            val minProgressDiff =
                videoMinDurationMs / videoDuration
            Log.d("minProgressDiff", "minProgressDiff $minProgressDiff")
            videoFrameView.setMinProgressDiff(minProgressDiff)
        }
        if (videoDuration >= videoMaxDurationMs + 1000) {
//            val maxProgressDiff =
//                videoMaxDurationMs / videoDuration
//            videoFrameView.setMaxProgressDiff(maxProgressDiff)
            videoFrameView.setMaxProgressDiff(videoDuration)
        }
        videoFrameView.setMaxVideoSize(videoMaxSize, originalSize)
        videoFrameView.setDelegate(object :
            VideoFrameView.VideoTimelineViewDelegate {
            override fun onLeftProgressChanged(progress: Float) {
                videoFrameView.progress = 0f
                updateVideoInfo()
                onProgressChanged.invoke(
                    trimStartTime, trimEndTime, PROGRESS_LEFT
                )
            }

            override fun onRightProgressChanged(progress: Float) {
                videoFrameView.progress = 0f
                updateVideoInfo()
                onProgressChanged.invoke(
                    trimStartTime, trimEndTime, PROGRESS_RIGHT
                )
            }

            override fun onPlayProgressChanged(progress: Float) {
//                onProgressChanged.invoke(
//                    trimStartTime, trimEndTime,
//                    PROGRESS_PLAY
//                )
            }

            override fun onDidStartDragging() {
                onDidStartDragging.invoke()
            }

            override fun onDidStopDragging() {
                Log.e("onDidStopDragging", "trimStartTime ${getMinuteSeconds(trimStartTime)}")
                Log.e("onDidStopDragging", "trimEndTime ${getMinuteSeconds(trimEndTime)}")
                onDidStopDragging.invoke(trimStartTime, trimEndTime)
            }
        })
        videoFrameView.setVideoPath( videoDuration.toLong(), isEdit)
    }

    fun getMinuteSeconds(millis: Long): String {
        val minutes = (millis / 1000 / 60).toInt()
        val seconds = (millis / 1000.toDouble()).toInt() - (minutes * 60)
        val milliSecond = millis.toDouble().toInt() - (seconds * 1000)
        return String.format(Locale.US, "%d:%02d:%2d", minutes, seconds, milliSecond)
    }



    private fun clearFrames() {
        videoFrameView.clearFrames()
    }
}