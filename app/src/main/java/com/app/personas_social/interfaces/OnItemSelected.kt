package com.app.personas_social.interfaces

interface OnItemSelected {
    fun onItemslectClick(position: Int,itemPos :Int)
    fun onItemDragClick(position: Int,itemPos :Int, startTime : Int , endTime : Int)
}