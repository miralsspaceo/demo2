package com.app.personas_social.videoEditing

interface Callback {

    fun onProgress(type: Int, progress: Int)

    fun onSuccess(type: Int, outPutPath: String)

    fun onSuccess(type: Int, outPutPath: Array<String>)

    fun onFailure(type: Int, error: String)

}