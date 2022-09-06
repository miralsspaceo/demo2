package com.app.personas_social.videoEditing

interface OnEditorListener {
    fun onSuccess()

    fun onCancel()
    fun onFailure(error:String)

    fun onProgress(progress: Float)
}