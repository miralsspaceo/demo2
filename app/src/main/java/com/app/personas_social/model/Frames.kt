package com.app.personas_social.model

import android.graphics.Bitmap
import android.net.Uri


data class Frames(
    var frame: Bitmap? = null,
    var videoSequence: Int? = null,
    var videoDuration: Int? = null,
    var frameposition: Int? = null,
    var textStickerPosition:Int? = null

)