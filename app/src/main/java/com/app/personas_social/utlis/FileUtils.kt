package com.app.personas_social.utlis

import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.app.personas_social.BuildConfig
import java.io.*
import java.net.URLConnection
import java.text.SimpleDateFormat
import java.util.*




object FileUtils {

    const val TAG = "File Utils"
    const val FOLDER_MEDIA = "media"
    const val FOLDER_FONT = "font"
    const val FOLDER_IMAGE = "image"



    fun getBitmapFromView(view: View): Bitmap? {
        //Define a bitmap with the same size as the view
        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        //Bind a canvas to it
        val canvas = Canvas(returnedBitmap)
        //Get the view's background
        val bgDrawable: Drawable? = view.background
        if (bgDrawable != null) //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas) else  //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE)
        // draw the view on the canvas
        view.draw(canvas)
        //return the bitmap
        return returnedBitmap
    }

    fun saveVideoFiles(body: InputStream?, context: Context, fileName: String = ""): String {

        var fileName = fileName
        if (body == null)
            return ""
        var input: InputStream? = null
        try {
            input = body
            //val file = File(getCacheDir(), "cacheFileAppeal.srl")
            fileName = if (fileName.isEmpty()) {
                "${System.currentTimeMillis()}.mp4"
            } else {
                fileName
            }
            //Output stream

            val cw = ContextWrapper(context)
            val directory: File = cw.getDir("media", Context.MODE_PRIVATE)
            if (!directory.exists()) {
                directory.mkdir()
            }
            val mypath = File(directory, fileName)

            val fos = FileOutputStream(mypath)
            fos.use { output ->
                val buffer = ByteArray(4 * 1024) // or other buffer size
                var read: Int
                while (input.read(buffer).also { read = it } != -1) {
                    output.write(buffer, 0, read)
                }
                output.flush()
            }
            return fileName
        } catch (e: Exception) {
            Log.e("saveFile", e.toString())
        } finally {
            input?.close()
        }
        return ""
    }


    fun saveFileAudio(body: InputStream?, context: Context, filename: String): String {
        var fileName = ""
        if (body == null)
            return ""
        var input: InputStream? = null
        try {
            input = body
            //val file = File(getCacheDir(), "cacheFileAppeal.srl")
//            val filename = fileName1
            //Output stream
            fileName = "$filename.mp3"
            val cw = ContextWrapper(context)

            val directory: File = cw.getDir("music", Context.MODE_PRIVATE)
            Log.e(TAG, "directory: "+directory )
            if (!directory.exists()) {
                directory.mkdir()
            }
            val mypath = File(directory, fileName)
            Log.e(TAG, "directory: "+mypath.path )
            Log.e(TAG, "directory: "+mypath.exists() )
            if (mypath.exists()) {
                return fileName
            }

            val fos = FileOutputStream(mypath)
            fos.use { output ->
                val buffer = ByteArray(4 * 1024) // or other buffer size
                var read: Int
                while (input.read(buffer).also { read = it } != -1) {
                    output.write(buffer, 0, read)
                }
                output.flush()
            }
            return fileName
        } catch (e: Exception) {
            Log.e("saveFile", e.toString())
        } finally {
            input?.close()
        }
        return ""
    }

    fun deleteScreenShots(context: Context): Boolean {

//        val cw = ContextWrapper(context)
//        val directory: File = cw.getDir("screens", Context.MODE_PRIVATE)


//        val demoVideoFolder = Environment.getExternalStorageDirectory().absolutePath + "/amaze/"
        val direct = File(Environment.getExternalStorageDirectory().toString() + "/amaze")
        deleteRecursive(direct)
        return false
    }

    fun deleteRecursive(fileOrDirectory: File) {
        if (fileOrDirectory.isDirectory) for (child in fileOrDirectory.listFiles()) deleteRecursive(
            child
        )
        fileOrDirectory.delete()
    }

    fun saveMediaToStorageScreenShots(bitmap: Bitmap, context: Context, fileName: String): String {
        //Generating a file name
        val filename = "${fileName}.jpg"
        //Output stream
        var fos: OutputStream? = null
        val cw = ContextWrapper(context)
        val directory: File = cw.getDir("screens", Context.MODE_PRIVATE)
        if (!directory.exists()) {
            directory.mkdir()
        }
        val mypath = File(directory, filename)
        fos = FileOutputStream(mypath)
        try {
            fos.use {
                //Finally writing the bitmap to the output stream that we opened
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                Log.e("", "Image saved")
                return filename
            }
        } catch (e: Exception) {
            Log.e("SAVE_IMAGE", e.message, e)
        }
        return ""
    }

    enum class MediaTypeEnum(val mediaType: String) {
        UnSplash("unSplash"), MediaPixels("mediaPexels"), MediaPixabay("mediaPixabay");

        companion object {
            private val map: MutableMap<String, MediaTypeEnum> =
                HashMap()

            fun valueOfSettingsMenuType(pageType: String): MediaTypeEnum? {
                return map[pageType]
            }

            init {
                for (pageType in MediaTypeEnum.values()) {
                    map[pageType.mediaType] = pageType
                }
            }
        }
    }

//    fun saveMedia(
//        bitmap: Bitmap,
//        context: Context,
//        filename: String? = null,
//        folderName: String
//    ): File? {
//        val outputPath = "${context.outputPathOfFileCacheFolder}/$folderName/"
//        if (!File(outputPath).exists())
//            File(outputPath).mkdir()
//        val name = if (filename.isNullOrEmpty()) "${System.currentTimeMillis()}.png" else filename
//
//        //Output stream
//        val fos: OutputStream?
//        val filePath = File(outputPath, name)
//        fos = FileOutputStream(filePath)
//        try {
//            fos.use {
//                //Finally writing the bitmap to the output stream that we opened
//                bitmap.compress(Bitmap.CompressFormat.PNG, 80, it)
//                Log.e("", "Image saved")
//                return filePath
//            }
//        } catch (e: Exception) {
//            Log.e("SAVE_IMAGE", e.message, e)
//        }
//        return null
//    }

    fun saveMediaGifToStorage(bitmap: Bitmap, context: Context): String {
        //Generating a file name
        val filename = "${System.currentTimeMillis()}.gif"


        var imagePath: String = ""
        //Output stream
        var fos: OutputStream? = null

        //For devices running android >= Q
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //getting the contentResolver
            context.contentResolver?.also { resolver ->

                //Content resolver will process the contentvalues
                val contentValues = ContentValues().apply {

                    //putting file information in content values
                    put(MediaStore.MediaColumns.DATA, filename)
//                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }

                //Inserting the contentValues to contentResolver and getting the Uri
                val imageUri: Uri? =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                //Opening an outputstream with the Uri that we got
                fos = imageUri?.let { resolver.openOutputStream(it) }

                imagePath = imageUri?.path.toString()

            }
        } else {
            //These for devices running on android < Q
            //So I don't think an explanation is needed here
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            if (!image.exists()) {
                if (!image.mkdirs()) {
                    fos = FileOutputStream(image)
                }
            }

            imagePath = image.absolutePath.toString()
//            storeImage(bitmap)

        }

        fos?.use {
            //Finally writing the bitmap to the output stream that we opened
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            Log.e("", "Image saved")
//            context?.toast("Saved to Photos")
            return filename
        }

        return ""

    }


//    fun downloadItems(context: Context, name: String, folderName: String): File {
//        val outputPath = "${context.outputPathOfFileCacheFolder}/$folderName/"
//        if (!File(outputPath).exists())
//            File(outputPath).mkdir()
//
//        val fontFile = File(outputPath, name)
//        return fontFile
//    }

    fun View.visible(isVisible: Boolean) {
        visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    fun View.enable(isEnabled: Boolean) {
        setEnabled(isEnabled)
        alpha = if (isEnabled) 1f else 0.5f
    }

    fun Context.toast(text: String?) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
    }

    fun Context.showPermissionRequestDialog(
        title: String,
        body: String,
        callback: () -> Unit
    ) {
        AlertDialog.Builder(this).also {
            it.setTitle(title)
            it.setMessage(body)
            it.setPositiveButton("Ok") { _, _ ->
                callback()
            }
        }.create().show()
    }

    fun storeImage(image: Bitmap) {
        val pictureFile = getOutputMediaFile()
        if (pictureFile == null) {
            Log.d(
                "TAG",
                "Error creating media file, check storage permissions: "
            ) // e.getMessage());
            return
        }
        try {
            val fos = FileOutputStream(pictureFile)
            image.compress(Bitmap.CompressFormat.PNG, 90, fos)
            fos.close()
        } catch (e: FileNotFoundException) {
            Log.d("TAG", "File not found: " + e.message)
        } catch (e: IOException) {
            Log.d("TAG", "Error accessing file: " + e.message)
        }
    }

    /** Create a File for saving an image or video  */
    private fun getOutputMediaFile(): File? {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        val mediaStorageDir = File(
            Environment.getExternalStorageDirectory()
                .toString() + "/Android/data/"
                    + BuildConfig.APPLICATION_ID
                    + "/Files"
        )

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null
            }
        }
        // Create a media file name
        val timeStamp: String = SimpleDateFormat("ddMMyyyy_HHmm").format(Date())
        val mediaFile: File
        val mImageName = "AMAZE_$timeStamp.jpg"
        mediaFile = File(mediaStorageDir.path + File.separator + mImageName)
        return mediaFile
    }

//    fun checkIsFileExists(context: Context, fileName: String, directory: String): Boolean {
//        val outputPath = "${context.outputPathOfFileCacheFolder}/$directory/$fileName"
//        return File(outputPath).exists()
//        /*val cw = ContextWrapper(context)
//        val directory: File = cw.getDir(directory, Context.MODE_PRIVATE)
//
//        val file = File(directory, "/${fileName}")
//        Log.e(TAG, "Check is already file exists ${file.exists()}")
//
//        return file.exists()*/
//    }

//    fun getFilePath(context: Context, directory: String, fileName: String): String {
//        val outputPath = "${context.outputPathOfFileCacheFolder}/$directory/$fileName"
//        return File(outputPath).absolutePath
//    }

    fun createDirectoryAndSaveFile(imageToSave: Bitmap, fileName1: String) {

//        Environment.getExternalStorageDirectory().absolutePath + "/videokit/"

        val filename = "${fileName1}.jpg"

        val direct = File(Environment.getExternalStorageDirectory().toString() + "/amaze")
        if (!direct.exists()) {
            val wallpaperDirectory = File("/sdcard/amaze/")
            wallpaperDirectory.mkdirs()
        }
        val file = File("/sdcard/amaze/", filename)
        if (file.exists()) {
            file.delete()
        }
        try {
            val out = FileOutputStream(file)
            imageToSave.compress(Bitmap.CompressFormat.JPEG, 100, out)
            out.flush()
            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Log.e("3333", "filename: " + file.absolutePath)
    }

    fun createDirectory(fileName: String, context: Context) {

        var fos: OutputStream? = null
        val cw = ContextWrapper(context)
        val directory: File = cw.getDir(fileName, Context.MODE_PRIVATE)
        if (!directory.exists()) {
            directory.mkdir()
        }
    }



    private fun getBitmapFromUri(uri: Uri, context: Context): Bitmap {
        val parcelFileDescriptor: ParcelFileDescriptor =
            context.contentResolver.openFileDescriptor(uri, "r")!!
        val fileDescriptor: FileDescriptor = parcelFileDescriptor.fileDescriptor
        val image: Bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor.close()
        return image
    }

    fun isImageFile(path: String?): Boolean {
        val mimeType: String = URLConnection.guessContentTypeFromName(path)
        return mimeType.startsWith("image")
    }


}



