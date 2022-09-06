package com.audiorecorder.utils

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Looper
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Media
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

const val RECORDER_RUNNING_NOTIF_ID = 10000

private const val PATH = "com.simplemobiletools.voicerecorder.action."
const val GET_RECORDER_INFO = PATH + "GET_RECORDER_INFO"
const val STOP_AMPLITUDE_UPDATE = PATH + "STOP_AMPLITUDE_UPDATE"
const val EXTENSION_M4A = 0
const val EXTENSION_MP3 = 1
const val SAMPLE_RATE_44100 = 44100
const val BITRATE_128000 = "128000"

// shared preferences
const val HIDE_NOTIFICATION = "hide_notification"
const val SAVE_RECORDINGS = "save_recordings"
const val EXTENSION = "extension"

@SuppressLint("InlinedApi")
fun getAudioFileContentUri(id: Long): Uri {
    val baseUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    } else {
        Media.EXTERNAL_CONTENT_URI
    }

    return ContentUris.withAppendedId(baseUri, id)
}

fun Context.getCurrentFormattedDateTime(): String {
    val simpleDateFormat = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault())
    return simpleDateFormat.format(Date(System.currentTimeMillis()))
}

fun isQPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

fun isOreoPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

//fun Context.isPathOnSD(path: String) = sdCardPath.isNotEmpty() && path.startsWith(sdCardPath)

fun isOnMainThread() = Looper.myLooper() == Looper.getMainLooper()

fun ensureBackgroundThread(callback: () -> Unit) {
    if (isOnMainThread()) {
        Thread {
            callback()
        }.start()
    } else {
        callback()
    }
}

fun String.getFilenameFromPath() = substring(lastIndexOf("/") + 1)

const val APP_FOLDER = "VideoEditor"

fun Context.getAudioInternalFilePath(): String {
    val f = File(outputPathOfInternalFolder)

    if (!f.exists())
        f.mkdirs()

    return File(f.path + File.separator + "$APP_FOLDER${System.currentTimeMillis()}.mp3").absolutePath
}

val Context.outputPathOfInternalFolder: String
    get() {
        val file =
            if (checkExternalDirectory()) getExternalFilesDir(null) else Environment.getDataDirectory()
        val fileModel = File(file, File.separator + APP_FOLDER)
        if (!fileModel.exists()) {
            fileModel.mkdir()
        }
        return fileModel.absolutePath
    }

private fun checkExternalDirectory(): Boolean {
    return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
}