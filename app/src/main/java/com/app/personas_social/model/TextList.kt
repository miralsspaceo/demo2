package com.app.personas_social.model

import android.graphics.Bitmap
import com.app.personas_social.stickerview.Sticker
import java.text.FieldPosition


data class SpanData(
    var text: String = "",
    var startTime: Long = 0L,
    var endTime: Long = 0L,
    var finalstartTime: Long = 0L,
    var finalendTime: Long = 0L,
    var x: Float = 0f,
    var y: Float = 0f,
    var sticker: Sticker? = null,
    var info : String? = "",
    var offset : Int = 0,
    var length: Int = 0,
    var mobject :Int = 0,
    var strTag: String = "",
    var bitmap:Bitmap? = null,
    var isSelected: Boolean = false,

)


data class  CopyModel(
   var duplicatetext : ArrayList<SpanData>? = null,
   var totalDuration : Int = 0

)
