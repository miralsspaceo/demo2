package com.app.personas_social.model

import com.google.gson.annotations.SerializedName


data class ListOfEffects(

    @SerializedName("imagePath") var imagePath: String? = null,
    @SerializedName("isDownload") var isDownload: String? = null,
    @SerializedName("musicId") var musicId: Int? = null,
    @SerializedName("musicName") var musicName: String? = null,
    @SerializedName("musicPath") var musicPath: String? = null,
    @SerializedName("time") var time: String? = null

)