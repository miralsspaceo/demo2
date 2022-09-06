package com.app.personas_social.model

class FolderData(
    var position: Int = 0,
    var id: String = "",
    var name: String = "",
    var thumbImagePath: String = "",
    var count: Int = 0,
    var arrVideoItem: ArrayList<VideoItem> = ArrayList(),
    var arrAudioItem: ArrayList<MusicData> = ArrayList(),
    var isSelect: Boolean = false
)