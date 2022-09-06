package com.app.personas_social.utlis

import Jni.FFmpegCmd
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Matrix
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.SeekBar
import androidx.annotation.IntRange
import androidx.appcompat.app.AppCompatActivity
import com.app.personas_social.R
import com.app.personas_social.videoEditing.CmdList
import com.app.personas_social.videoEditing.OnEditorListener

import java.io.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import kotlin.math.ceil


fun Context.convertMillieToHMS(millis: Long, isShowHours: Boolean = false): String {
    val hours = (millis / 1000 / 60 / 60).toInt()
    val minutes = (millis / 1000 / 60).toInt()
    val seconds = (millis.toDouble() / 1000).toInt() - (minutes * 60)

    return if (hours > 0 || isShowHours) {
        String.format(getString(R.string.time_format_hms), hours, minutes, seconds)
    } else {
        String.format(getString(R.string.time_format_ms), minutes, seconds)
    }
}

fun execCmd(cmd: VideoHandle.CmdList, duration: Long, onEditorListener: VideoHandle.OnEditorListener) {
    val arrCmd = cmd.toTypedArray()
    var cmdLog = ""
    for (ss in arrCmd) {
        cmdLog += arrCmd
    }
    FFmpegCmd.exec(arrCmd, duration, object : VideoHandle.OnEditorListener {
        override fun onSuccess() {
            onEditorListener.onSuccess()
        }

        override fun onFailure() {
            onEditorListener.onFailure()
        }

        override fun onProgress(progress: Float) {
            onEditorListener.onProgress(progress)
        }
    })
}
fun Context.convertTimeStamp(time :String): Long {
//    val sdf = SimpleDateFormat("hh:mm", Locale.ENGLISH)
//
//    try {
//        val mDate = sdf.parse(givenDateString.toString())
//        time= mDate!!.time / 1000
//
//        //println("Date in milli :: $timeInMilliseconds")
//    } catch (e: ParseException) {
//        e.printStackTrace()
//
//    }
    val inputSimpleDateFormat = SimpleDateFormat("mm:ss", Locale.ENGLISH)
    val time:Long = inputSimpleDateFormat.parse(time).time

    return time
}


fun getfps(fileString: String):Int {
    var FRAME_RATE=0
    val extractor = MediaExtractor()
    val file = File(fileString)
    var fis: FileInputStream? = null
    try {
        fis = FileInputStream(file)
        val fd = fis.fd
        extractor.setDataSource(fd)
        val numTracks = extractor.trackCount
        for (i in 0 until numTracks) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME)
            if (mime?.startsWith("video/") == true) {
                if (format.containsKey(MediaFormat.KEY_FRAME_RATE)) {
                    FRAME_RATE= format.getInteger(MediaFormat.KEY_FRAME_RATE)
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        return FRAME_RATE
    } finally {
        extractor.release()
        try {
            fis?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    return FRAME_RATE
}


fun setColorAlpha(argb: Int, alpha: Int) = Color.argb(
    alpha,
    Color.red(argb),
    Color.green(argb),
    Color.blue(argb)
)

fun Context.convertMillieToHMSMs(millis: Long): String {
    val hours = (millis / 1000 / 60 / 60).toInt()
    val minutes = (millis / 1000 / 60).toInt()
    val seconds = (millis.toDouble() / 1000).toInt() - (minutes * 60)
    val milliSeconds = millis.toDouble().toInt() - (seconds * 1000)

    return if (hours > 0) {
        String.format(getString(R.string.time_format_hmsMs), hours, minutes, seconds, milliSeconds)
    } else {
        String.format(getString(R.string.time_format_hms), minutes, seconds, milliSeconds)
    }
}

private fun Context.copyFileFromAssets(
    filename: String,
    fileModel: File
) {
    val assetManager = assets
    val inputStream: InputStream
    val outputStream: OutputStream

    try {
        inputStream = assetManager.open(filename)
        val cacheFile = File(fileModel, filename)
        outputStream = FileOutputStream(cacheFile)
        val buffer = ByteArray(1024)
        var read = 0
        while (read != -1) {
            outputStream.write(buffer, 0, read)
            read = inputStream.read(buffer)
        }
        inputStream.close()
        outputStream.flush()
        outputStream.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}


fun Context.saveFontFile(fontName: String): String {
    val file =
        if (checkExternalDirectory()) getExternalFilesDir(null) else Environment.getDataDirectory()
    val fileModel = File(file, File.separator + APP_FOLDER)
    if (!fileModel.exists()) {
        fileModel.mkdir()
    }
    copyFileFromAssets(
        fontName,
        fileModel
    )
    return fileModel.absolutePath.plus("/$fontName")
}

private fun checkExternalDirectory(): Boolean {
    return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
}

fun getFilePath(videoUrl: String): String = File(videoUrl).absolutePath

fun Context.getDurationFromUrl(url: String): Long {
    var timeInMilliSec = 0L
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(this, Uri.parse(url))
    val timeStr =
        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
    if (!TextUtils.isEmpty(timeStr))
        timeInMilliSec = timeStr!!.toLong()
    retriever.release()
    return timeInMilliSec //use this duration
}

fun getVideoOrientation(retriever: MediaMetadataRetriever): Int {
    return retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)!!
        .toInt() //To check video orientation
}

fun Context.isVideoIsPortraitOrLandscape(videoUrl: String): Boolean {
    var videoWidth = 0
    var videoHeight = 0
    if (!TextUtils.isEmpty(videoUrl)) {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(this, Uri.parse(videoUrl))
        //Get one "frame"/bitmap - * NOTE - no time was set, so the first available frame will be used
        val bmp = retriever.frameAtTime
        if (bmp != null) {
            //Get the bitmap width and height
            videoWidth = bmp.width
            videoHeight = bmp.height
        }
        retriever.release()
    }

    return videoHeight > videoWidth
}

fun Context.getVideoWidthHeight(videoUrl: String): Pair<Int, Int> {
    var videoWidth = 0
    var videoHeight = 0
    if (!TextUtils.isEmpty(videoUrl)) {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(this, Uri.parse(videoUrl))
        //Get one "frame"/bitmap - * NOTE - no time was set, so the first available frame will be used
        val bmp = retriever.frameAtTime
        if (bmp != null) {
            //Get the bitmap width and height
            videoWidth = bmp.width
            videoHeight = bmp.height
        }
        retriever.release()
    }

    return Pair(videoWidth, videoHeight)
}

fun isVideoIsPortraitOrLandscape(retriever: MediaMetadataRetriever): Pair<Int, Int> {
    var videoWidth = 0
    var videoHeight = 0

    //Get one "frame"/bitmap - * NOTE - no time was set, so the first available frame will be used
    val bmp = retriever.frameAtTime
    if (bmp != null) {
        //Get the bitmap width and height
        videoWidth = bmp.width
        videoHeight = bmp.height
    }
    return Pair(videoWidth, videoHeight)
}

fun isVideoHaveAudioTrack(retriever: MediaMetadataRetriever): Boolean {
    val hasAudioStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO)
    return hasAudioStr != null && hasAudioStr == "yes"
}

fun Context.dpToPx(dp: Float): Float {
    val density = resources.displayMetrics.density
    return dp * density
}
fun Context.dp(context: Context, value: Float): Int {
    return if (value == 0f) {
        0
    } else ceil(context.resources.displayMetrics.density * value.toDouble()).toInt()
}


val Context.outputPathFolder: String
    get() {
        val path =
            Environment.getExternalStorageDirectory()
                .toString() + File.separator + APP_FOLDER + File.separator

        val folder = File(path)
        if (!folder.exists())
            folder.mkdirs()

        return path
    }

val Context.outputPathFolderPromoImage: String
    get() {
        val downloadFolder =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        return downloadFolder!!.absolutePath
    }

fun Context.getExternalFilePathOfImage(): String {
    val f = File(outputPathFolderPromoImage)

    if (!f.exists())
        f.mkdirs()

    return checkUrl(File(f.path + File.separator + "$PROMO_FOLDER${System.currentTimeMillis()}.jpg").absolutePath)
}

val Context.outputPathOfCacheFolder: String
    get() {
        val file = cacheDir
        val fileModel = File(file, File.separator + APP_FOLDER)
        if (!fileModel.exists()) {
            fileModel.mkdir()
        }
        return fileModel.absolutePath
    }

val Context.outputPathOfInternalFolder: String
    get() {
        val file =
            filesDir
//            if (checkExternalDirectory()) getExternalFilesDir(null) else Environment.getDataDirectory()
        val fileModel = File(file, File.separator + APP_FOLDER)
        if (!fileModel.exists()) {
            fileModel.mkdir()
        }
        return fileModel.absolutePath
    }

fun Context.getAudioFilePath(isExternal: Boolean = false): String {
    val f = File(if (isExternal) outputPathFolder else outputPathOfCacheFolder)

    if (!f.exists())
        f.mkdirs()

    return checkUrl(File(f.path + File.separator + "$RECORDED_FILE${System.currentTimeMillis()}.mp3").absolutePath)
}

fun Context.getImageInternalFilePath(): String {
    val f = File(outputPathOfCacheFolder)

    if (!f.exists())
        f.mkdirs()

    return checkUrl(File(f.path + File.separator + "$APP_FOLDER${System.currentTimeMillis()}.png").absolutePath)
}
fun Context.getFilePath(): String {
    val path =
        getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString() + File.separator + APP_FOLDER + File.separator

    val folder = File(path)
    if (!folder.exists())
        folder.mkdirs()

    val f = File(path)

    if (!f.exists())
        f.mkdirs()

    return checkUrl(File(f.path + File.separator + "$APP_FOLDER${System.currentTimeMillis()}.mp4").absolutePath)
}
fun Context.getVideoFilePath(isExternal: Boolean = false): String {
    val f = File(if (isExternal) outputPathFolder else outputPathOfCacheFolder)

    if (!f.exists())
        f.mkdirs()
    var file = File(checkUrl(File(f.path + File.separator + "$APP_FOLDER${System.currentTimeMillis()}.mp4").absolutePath))
    if (file.exists()){
        Log.e("getVideoFinalFilePath", "getVideoFinalFilePath: "+true )
    }
    Log.e("getVideoFinalFilePath", "getVideoFinalFilePath: "+false )
    return checkUrl(File(f.path + File.separator + "$APP_FOLDER${System.currentTimeMillis()}.mp4").absolutePath)
}

fun Context.getVideoFinalFilePath(isExternal: Boolean = false): String {
    val f = File(if (isExternal) outputPathFolder else outputPathOfInternalFolder)

    if (!f.exists())
        f.mkdirs()

    return checkUrl(File(f.path + File.separator + "$APP_FOLDER${System.currentTimeMillis()}.mp4").absolutePath)
}

fun Context.checkUrl(inputPath: String, isAudio: Boolean = false): String {
    val pattern = Pattern.compile(
        "([A-Za-z0-9_.~:/?\\#\\[\\]@!$&'()*+,;" +
                "=-]|%[0-9a-fA-F]{2})+"
    )
    val matcher = pattern.matcher(inputPath)
    var validUri: String? = null
    if (matcher.find()) {
        validUri = matcher.group()
    }
    if (TextUtils.isEmpty(validUri) || inputPath.length == validUri!!.length) {
        return inputPath
    }
    // The uriString is not encoded. Then recreate the uri and encode it
    val uri = Uri.parse(inputPath)
    val uriBuilder =
        Uri.Builder().scheme(uri.scheme).authority(uri.authority)
    for (pathSegment in uri.pathSegments) {
        uriBuilder.appendPath(pathSegment)
    }
    for (key in uri.queryParameterNames) {
        uriBuilder.appendQueryParameter(key, uri.getQueryParameter(key))
    }

    val properPath = uriBuilder.build().toString()
    return if (properPath.contains("%20")) {
        copyFile(
            inputPath,
            if (isAudio) getAudioFilePath() else getVideoFilePath()
        )
    } else
        properPath
}

private fun copyFile(
    inputPath: String,
    outputPath: String
): String {
    try {
        //create output directory if it doesn't exist
        val inputStream = FileInputStream(inputPath)
        val outputStream = FileOutputStream(outputPath)
        val buffer = ByteArray(1024)
        var read: Int
        while (inputStream.read(buffer).also { read = it } != -1) {
            outputStream.write(buffer, 0, read)
        }
        inputStream.close()
        // write the output file (You have now copied the file)
        outputStream.flush()
        outputStream.close()
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
        return ""
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
        return ""
    }
    return outputPath
}

fun isVideoHaveAudioTrack(path: String): Boolean {
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(path)
    val hasAudioStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO)
    return hasAudioStr != null && hasAudioStr == "yes"
}


fun Context.getSilentAudioFilePath(resourceId: Int, resourceName: String): String {
    val file =
        if (checkExternalDirectory()) getExternalFilesDir(null) else Environment.getDataDirectory()
    val fileModel = File(file, File.separator + APP_FOLDER)
    if (!fileModel.exists()) {
        fileModel.mkdir()
    }
    try {
        val inputStream = resources.openRawResource(resourceId)
        val cacheFile = File(fileModel, resourceName)
        val outputStream = FileOutputStream(cacheFile)
        val buffer = ByteArray(1024)
        var read = 0
        while (read != -1) {
            outputStream.write(buffer, 0, read)
            read = inputStream.read(buffer)
        }
        inputStream.close()
        outputStream.flush()
        outputStream.close()
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
    } catch (e: IOException) {
        e.printStackTrace()
    }

    return fileModel.absolutePath.plus("/$resourceName")
}

fun Context.notifyItemAfterSaved(fileNameArray: Array<String>) {
    MediaScannerConnection.scanFile(this, fileNameArray, null) { path, uri ->
//        Log.e("onScanCompleted", path)
    }
}

fun dp(context: Context, value: Float): Int {
    return if (value == 0f) {
        0
    } else ceil(context.resources.displayMetrics.density * value.toDouble()).toInt()
}

fun Activity.getDeviceSize(): Pair<Int, Int> {
    val displayMetrics = DisplayMetrics()
    windowManager.defaultDisplay.getMetrics(displayMetrics)
    val height = displayMetrics.heightPixels
    val width = displayMetrics.widthPixels
    return Pair(height, width)
}

fun Context.closeKeyboard(view: View) {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

fun Activity.getValueFromPercentageOfHeight(percentage: Float): Float {
    val deviceHeight = getDeviceSize().first
    return (deviceHeight * percentage / 100)
}

fun deleteUnWantedFile(outputPath: String) {
    val file = File(outputPath)
    file.delete()
}

fun Context.deleteAllFileFromCacheFolder() {
    val folder = File(outputPathOfCacheFolder)
    for (file in folder.listFiles()!!) {
        file.delete()
    }
}
fun View?.visible() {
    if (this != null)
        this.visibility = View.VISIBLE
}

fun View?.hide() {
    if (this != null)
        this.visibility = View.GONE
}
fun View.visibleIf(isShown: Boolean) {
    if (isShown) {
        visible()
    } else {
        hide()
    }
}

fun moveFile(inputPath: String, outputPath: String): Boolean {
    val inputStream: InputStream
    val outputStream: OutputStream
    try {
        inputStream = FileInputStream(inputPath)
        outputStream = FileOutputStream(outputPath)
        val buffer = ByteArray(1024)
        var read: Int
        while (inputStream.read(buffer).also { read = it } != -1) {
            outputStream.write(buffer, 0, read)
        }
        inputStream.close()
        outputStream.flush()
        outputStream.close()
        val isDeleted = File(inputPath).delete()
        Log.e("CommonUtils", "moveFile isDelete $isDeleted")
        return isDeleted
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
        return false
    } catch (e: Exception) {
        e.printStackTrace()
        return false
    }
}

fun hideKeyboard(activity: Activity) {
    val imm: InputMethodManager =
        activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    //Find the currently focused view, so we can grab the correct window token from it.
    var view: View? = activity.currentFocus
    //If no view currently has focus, create a new one, just so we can grab a window token from it
    if (view == null) {
        view = View(activity)
    }
    imm.hideSoftInputFromWindow(view.windowToken, 0)

}

fun Activity.openKeyboard(view: View) {
    /*   val inputMethodManager: InputMethodManager? =
           getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
       inputMethodManager!!.showSoftInput(
           view,
           InputMethodManager.SHOW_FORCED
       )*/
    val inputMethodManager: InputMethodManager? =
        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
    inputMethodManager!!.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

}


fun formatToDisplayDate() = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())

fun getMatrixValue(matrix: Matrix, @IntRange(from = 0, to = 9) valueIndex: Int): Float {
    val matrixValues = FloatArray(9)
    matrix.getValues(matrixValues)
    return matrixValues.get(valueIndex)
}

fun Int.hue(): Float {
    val hsv = FloatArray(3)
    Color.colorToHSV(this, hsv)
    return hsv[0]
}

fun SeekBar.setOnSeekBarChangeListener(onProgressChanged: (progress: Float) -> Unit) {
    setOnSeekBarChangeListener(object :
        SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            onProgressChanged(progress / 100f)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {

        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {

        }
    })

    fun Context.openKeyboard(view: View) {
        val inputMethodManager: InputMethodManager? =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        inputMethodManager!!.toggleSoftInputFromWindow(
            view.applicationWindowToken,
            InputMethodManager.SHOW_FORCED,
            0
        )
    }

    fun Context.closeKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun convertSpToPx(scaledPixels: Float): Float {
        return scaledPixels * context.resources.displayMetrics.scaledDensity
    }
}