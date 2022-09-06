package com.audiowaveforms

import android.os.Build
import androidx.annotation.RequiresApi
import java.io.File

object WaveformOptions {

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    @JvmStatic
    fun getSampleFrom(file: File, ignoreExtension: Boolean = false): IntArray? {
        val soundFile = SoundFile.create(file.absolutePath, ignoreExtension)
        return soundFile?.frameGains
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    @JvmStatic
    fun getSampleFrom(path: String, ignoreExtension: Boolean = false): IntArray? {
        val soundFile = SoundFile.create(path, ignoreExtension)
        return if (soundFile != null) soundFile.frameGains else IntArray(0)
    }

    fun addCustomExtension(extension: String) = SoundFile.addCustomExtension(extension)

    fun removeCustomExtension(extension: String) = SoundFile.removeCustomExtension(extension)

    fun addCustomExtensions(extensions: List<String>) = SoundFile.addCustomExtensions(extensions)

    fun removeCustomExtensions(extensions: List<String>) =
        SoundFile.removeCustomExtensions(extensions)
}