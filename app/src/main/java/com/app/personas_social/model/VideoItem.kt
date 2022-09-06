package com.app.personas_social.model

import android.graphics.Rect
import android.net.Uri
import android.os.Parcelable
import com.videofilter.FilterType
import kotlinx.android.parcel.Parcelize

@Parcelize
data class VideoItem(
    var videoUrl: String = "",
    var videoThumb: String = "",
    var isSelected: Boolean = false,
    var selectedItemCount: Int = 0,
    var videoDuration: Long = 0L,
    var isVideoPortrait: Boolean = false,
    var isAudioAvail: Boolean = false,
    var isVideoProper: Boolean = false,
    var width: Int = 0,
    var height: Int = 0,
    var videoUri: Uri? = null,
    var startDuration: Long = 0L,
    var endDuration: Long = 0L,
    var isTrimmed: Boolean = false,
    var speed: Float = 50f,
    var speedPlayer: Float = 1f,
    var speedStartDuration: Long = 0,
    var speedEndDuration: Long = 0,
    var isStartSpeedChange: Boolean = false,
    var isEndSpeedChange: Boolean = false,
    var isSplit: Boolean = false,
    var videoSpeedDuration: Long = 0,
    var videoRotation: Float =
        0f,
    var orientation: Int = 0,
    var filter: FilterType = FilterType.DEFAULT,
    var filteredVideoUrl: String = "",
    var videoVolume: Int = 100,
    var audioVolume: Int = 100,
    var position: Int = 0,
    var cropRect: Rect? = null,
    var hFlip: Boolean = false,
    var vFlip: Boolean = false,
) :  Parcelable

data class PlaybackInfo(val position: Long, val window: Int)
