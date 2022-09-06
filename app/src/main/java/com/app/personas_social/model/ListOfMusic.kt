package com.app.personas_social.model

import com.app.personas_social.model.Music
import com.google.gson.annotations.SerializedName


data class ListOfMusic (

  @SerializedName("music" ) var music : ArrayList<Music> = arrayListOf()

)