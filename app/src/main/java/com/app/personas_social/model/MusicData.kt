package com.app.personas_social.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class MusicData(
    var position: Int = 0,
//    var uri: Uri? = null,
//    var albumId: Long = 0,
    var audioUrl: String = "",
    var effectUrl: String = "",
    var thumb: String = "",
    var waves: IntArray? = null,
    var title: String = "",
//    var album: String = "",
    var artist: String = "",
    var duration: Long = 0L,
    var startTime: Long = 0L,
    var endTime: Long = 0L,
    var strstartTime: Long = 0L,
    var strendTime: Long = 0L,
    var type: Int = 0,
    var play: Int = 0,
    var isTrimmed: Boolean = false,
    var audioVolume: Int = 100,
    var isDownload: Boolean = false,
    var isSelected: Boolean = false,
    var itemisSelected: Boolean = false,
    var isfrom : String = ""
) : Parcelable
