package com.videofilter.filters

import android.content.Context
import com.videofilter.OpenGlUtils
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

abstract class ScriptEffect(private val context: Context) :
    GlFilter(OpenGlUtils.DEFAULT_VERTEX_SHADER) {

    init {
        setFragmentShaderSource(getShaderString(getFragmentShaderScriptName()))
    }

    protected abstract fun getFragmentShaderScriptName(): String

    override fun supportRotation(): Boolean {
        return true
    }

    private fun getShaderString(fileName: String): String? {
        try {
            val `is`: InputStream = context.assets.open("shader/$fileName")
            val reader =
                BufferedReader(InputStreamReader(`is`))
            val builder = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                builder.append(line).append("\r\n")
            }
            return builder.toString()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

}