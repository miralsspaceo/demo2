package com.app.personas_social.model

import android.graphics.Bitmap


class VideoFrameData(
    var frame: Bitmap? = null,
    var frameTime: Long = 0L,
    var isSelected: Boolean = false
)