package com.app.personas_social.stickerview.callbacks

interface TextEditorOperation {

    fun onDoneClicked(
            inputText: String,
            colorPosition: Int,
            fontPosition: Int,
            textSize: Int,
            lineSpacing: Float,
            letterSpacing: Float,
            gravity: Int,
            yPos: Float = 0f,
            startTime: Long = 0L,
            endTime: Long = 300L,
            strTag: String
    )

    fun onCloseClicked(

    )
}