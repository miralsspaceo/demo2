package com.audiorecorder

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import com.audiorecorder.utils.getAudioInternalFilePath
import java.io.File
import java.util.*

class RecorderHelper(
    private var context: Context,
    private val onUpdateTime: (sec: Int) -> Unit,
    private val onStartPauseResume: (statusOfRecording: Int) -> Unit,
    private val onSaved: (filePath: String) -> Unit
) {
    private var filePath = ""
    private var duration = 0
    private lateinit var durationTimer: Timer
    private lateinit var recorder: MediaRecorder
    private var statusOfRecording = 0

    fun startPauseStopRecording() {
        Log.e("RecorderHelper", "statusOfRecording2 $statusOfRecording")

        when (statusOfRecording) {
            0, 2 -> startRecording()
//            1-> pauseRecording()
            1 -> stopRecording()
            /**
             * pauseRecording()
             */
        }
    }

    private fun startRecording() {
        Log.e("startRecording", "startRecording: "+statusOfRecording )
        if (statusOfRecording == 0) {
            duration = 0
            filePath = context.getAudioInternalFilePath()
            recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.CAMCORDER)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                try {
                    setOutputFile(filePath)
                    prepare()
                    start()
                    startPauseStopTimer()
                    statusOfRecording = 1
                    onUpdateTime.invoke(duration)
                    onStartPauseResume.invoke(statusOfRecording)
                    Log.e("RecorderHelper", "start Recording"+statusOfRecording)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else if (statusOfRecording == 2) {
            stopRecording()

//             resumeRecording()

        }
    }


   private fun pauseRecording() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
    recorder.pause()
    startPauseStopTimer()
    statusOfRecording = 2
    onStartPauseResume.invoke(statusOfRecording)
    } else {
    stopRecording()
    }
    }

    private fun resumeRecording() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
    recorder.resume()
    startPauseStopTimer()
    statusOfRecording = 1
    onStartPauseResume.invoke(statusOfRecording)
    Log.e("RecorderHelper", "Resume Recording")
    } else {
    stopRecording()
    }
    }



     fun stopRecording() {
        recorder.stop()
        recorder.release()
        durationTimer.cancel()
        statusOfRecording = 3
        onStartPauseResume.invoke(statusOfRecording)
        Log.e("RecorderHelper", "Pause Recording")
    }

    private fun startPauseStopTimer() {
        if (statusOfRecording == 0 || statusOfRecording == 2) {
            durationTimer = Timer()
            durationTimer.scheduleAtFixedRate(getDurationUpdateTask(), 1000, 1000)
        } else if (statusOfRecording == 1 || statusOfRecording == 2) {
            durationTimer.cancel()
            durationTimer.purge()
        }
    }

    fun saveRecording() {
        if (::recorder.isInitialized) {
            resetData()
            try {
                recorder.release()

                onSaved.invoke(filePath)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun discardRecording() {
        if (::recorder.isInitialized) {
            resetData()
            File(filePath).delete()
        }
    }

    private fun resetData() {
        duration = 0
        durationTimer.cancel()
        statusOfRecording = 0
        onStartPauseResume.invoke(statusOfRecording)
        onUpdateTime.invoke(duration)
    }

    private fun getDurationUpdateTask() = object : TimerTask() {
        override fun run() {
            duration++
            onUpdateTime.invoke(duration)
        }
    }
}
