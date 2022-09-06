package com.videoframeview

import android.net.Uri
import android.util.Log
import io.reactivex.disposables.Disposable
import java.io.File
import java.util.*
import kotlin.math.ceil
import kotlin.math.roundToInt


class VideoFrameCutterHelper(
    private var videoFrameView: VideoFrameViewCutter,
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
    private var mtrimStartTime: Long = 0
    private var trimEndTime: Long = 0
    private var originalSize: Long = 0
    private var estimatedSize: Long = 0
    private val trimTask: Disposable? = null
    private val convertTask: Disposable? = null
    private var path = ""
    var progress = 0f
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

    fun onUpdate(){
        videoFrameView.invalidate()
    }

    //    fun updatePlayProgress(currentPosition: Long) {
    fun updatePlayProgress(startTime: Long, currentPosition: Long, videoDuration: Long) {
        progress =
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

       if (currentPosition > videoDuration){
            trimStartTime = 0
            trimEndTime = 0
            videoFrameView.progress = 0.0f
//            updateVideoInfo()
        }

        Log.e("updatePlayProgress", "updatePlayProgressprogress: ${videoFrameView.progress.toFloat()}" )
        Log.e("updatePlayProgress", "updatePlayProgress: ${videoFrameView.playProgress * videoDuration.toDouble()} trimStartTime >> $trimStartTime  trimEndTime $trimEndTime" )
        Log.e("updatePlayProgress", "updatestart: ${videoFrameView.leftProgress* videoDuration.toDouble()}" )
        Log.e("updatePlayProgress", "updatePlayemd: ${videoFrameView.rightProgress* videoDuration.toDouble()} ")



    }

    fun initialize(
        videoFile: Uri?,
        videoDuration: Long,
        startTime: Long,
        endTime: Long,
        isTrimmed: Boolean,
        isSplit: Boolean,
        isSpeed: Boolean
    ) {
        videoUri = videoFile
        videoFrameView.setPosition(position)
        path = videoFile!!.path!!
        this.videoFile = File(path)
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
        videoFrameView.setSplit(isSplit, startTime)
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
        initVideoFrameViewCutter(isEdit)
    }

    private fun initVideoFrameViewCutter(isEdit: Boolean) {
        if (videoDuration >= videoMinDurationMs + 1000) {
            val minProgressDiff =
                videoMinDurationMs / videoDuration
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
            VideoFrameViewCutter.VideoTimelineViewDelegate {
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

            override fun onPlayProgressChanged(progress: Float,startprogress:Float) {
                Log.e("onPlayProgressChanged", "onPlayProgressChanged: "+progress  + "STARTPROGRRES >>>>> $startprogress")
                if (videoFrameView.playProgress == 0f){
                    trimStartTime =
                        ceil(videoFrameView.leftProgress * videoDuration.toDouble()).toLong()
                }else{

                 trimStartTime =
                    ceil(videoFrameView.playProgress * videoDuration.toDouble()).toLong()
                }

                trimEndTime =
                    ceil(videoFrameView.rightProgress * videoDuration.toDouble()).toLong()

                Log.e("onPlayProgressChanged", "rightProgress: "+ videoFrameView.rightProgress * videoDuration.toDouble())
                Log.e("onPlayProgressChanged", "leftProgress: "+videoFrameView.leftProgress * videoDuration.toDouble() )
                Log.e("onPlayProgressChanged", "playProgress: "+ videoFrameView.playProgress * videoDuration.toDouble())

                estimatedDuration = trimEndTime - trimStartTime
                estimatedSize =
                    (originalSize * (estimatedDuration.toFloat() / videoDuration)).toLong()
//                updateVideoInfo()


                if (trimStartTime <= trimEndTime){
                      if (trimStartTime == trimEndTime ){
                    trimStartTime =
                        ceil(videoFrameView.leftProgress * videoDuration.toDouble()).toLong()
                    onProgressChanged.invoke(
                        trimStartTime, trimEndTime,
                        PROGRESS_PLAY
                    )
                }else if (trimStartTime == (Math.round((videoFrameView.playProgress * videoDuration.toDouble())) + 1).toLong()){
                    trimStartTime =
                        ceil(videoFrameView.leftProgress * videoDuration.toDouble()).toLong()
                    Log.e("onPlayProgressChanged", "trimStartTime: "+trimStartTime  )
                    onProgressChanged.invoke(
                        trimStartTime, trimEndTime,
                        PROGRESS_PLAY
                    )
                }
                else{
                    onProgressChanged.invoke(
                        trimStartTime, trimEndTime,
                        PROGRESS_PLAY
                    )

                }
            }
            }

            override fun onDidStartDragging() {
                onDidStartDragging.invoke()
            }

            override fun onDidStopDragging() {
                onDidStopDragging.invoke(trimStartTime, trimEndTime)
            }
        })
        videoFrameView.setVideoPath(videoUri, videoDuration.toLong(), isEdit)
    }

    fun getMinuteSeconds(millis: Long): String {
        val minutes = (millis / 1000 / 60).toInt()
        val seconds = (millis / 1000.toDouble()).toInt() - (minutes * 60)
        val milliSecond = millis.toDouble().toInt() - (seconds * 1000)
        return String.format(Locale.US, "%d:%02d:%2d", minutes, seconds, milliSecond)
    }

    /*@JvmOverloads
    fun formatFileSizeInBMBGB(size: Long, removeZero: Boolean = false): String {
        return if (size < 1024) {
            String.format("%d B", size)
        } else if (size < 1024 * 1024) {
            val value = size / 1024.0f
            if (removeZero && (value - value.toInt()) * 10 == 0f) {
                String.format("%d KB", value.toInt())
            } else {
                String.format("%.1f KB", value)
            }
        } else if (size < 1024 * 1024 * 1024) {
            val value = size / 1024.0f / 1024.0f
            if (removeZero && (value - value.toInt()) * 10 == 0f) {
                String.format("%d MB", value.toInt())
            } else {
                String.format("%.1f MB", value)
            }
        } else {
            val value = size / 1024.0f / 1024.0f / 1024.0f
            if (removeZero && (value - value.toInt()) * 10 == 0f) {
                String.format("%d GB", value.toInt())
            } else {
                String.format("%.1f GB", value)
            }
        }
    }*/

    private fun clearFrames() {
        videoFrameView.clearFrames()
    }
}
