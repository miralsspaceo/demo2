package com.app.personas_social.model

import android.graphics.Matrix
import android.graphics.Typeface
import com.app.personas_social.stickerview.Sticker
import com.app.personas_social.stickerview.TextSticker
import com.videofilter.FilterType

data class ChangeData(var type: Int,var data: UndoRedoData)

data class ChangeData2(var type: Int,var redotype : Int,var data: UndoRedoData2)

data class UndoRedoData2(
    var oldSticker : TextSticker? = null,
    var textSticker: TextSticker? = null,
    var textData : TextStickerData? = null,
    var filter: FilterType = FilterType.DEFAULT,
    var filterpos : Int = 0,
    var startDuration: Long = 0L,
    var endDuration: Long = 0L,
    var listOfEffects: MusicData? = null,
    var videoPos :Int = 0
)

data class TextStickerData(
    var angle: Float = 0f,

    var scale: Float = 1f,

    var xFrom: Double ,

    var yFrom: Double ,

    var startTime: Long = 0L,
    var endTime: Long = 0L,
    var tag: String? = null,

    var textStyle: String? = null,

    var fontName: Typeface? = null,

    var textAlignment: String? = null,

    var textSize: Float? = null,


    var text: String? = null,

    var borderColor: Int?= null,

    var borderType:String= "",

    var colorDialog: Boolean = false,
    var isFromColor:Boolean = false,
    var color: Int? = null,

    var color2: Int? = null,

    var pattern: Int? = null,

    var gradient: IntArray? = null,

    var matrix: Matrix?= null,
    var offset : Int = 0,
    var length: Int = 0,
    var mobject: Int = 0

) {

}
