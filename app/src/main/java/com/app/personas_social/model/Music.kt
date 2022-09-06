package com.app.personas_social.model

import com.google.gson.annotations.SerializedName


data class Music(

    @SerializedName("catId") var catId: Int? = null,
    @SerializedName("catName") var catName: String? = null,
    @SerializedName("listOfEffects") var listOfEffects: ArrayList<ListOfEffects> = arrayListOf()

)