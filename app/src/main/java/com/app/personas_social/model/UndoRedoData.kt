package com.app.personas_social.model

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Typeface
import com.app.personas_social.stickerview.TextSticker
import com.google.gson.annotations.SerializedName
import jp.co.cyberagent.android.gpuimage.filter.FilterType

 class UndoRedoData(
     var filter: com.videofilter.FilterType = com.videofilter.FilterType.DEFAULT,

     var startDuration: Long = 0L,
     var endDuration: Long = 0L,
     var listOfEffects: ArrayList<MusicData> = arrayListOf(),

     var angle: Float = 0f,

     var scale: Float = 1f,

     var xFrom: Float = 0f,

     var yFrom: Float = 0f,

     var textStyle: Typeface? = null,

     var fontName: Typeface? = null,

     var textAlignment: String? = null,

     var textSize: Float? = null,

     var text: String? = null,

     var textSticker: TextSticker? = null,

     var borderColor: Int?= null,

     var borderType:String= "",

     var colorDialog: Boolean = false,
     var isFromColor:Boolean = false,
     var color: Int? = null,

     var color2: Int? = null,

     var pattern: Int? = null


 )
