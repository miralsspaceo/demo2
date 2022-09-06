package com.howto.interfaces

interface OnItemTouchAdapter {
    fun onItemMove(fromPosition: Int, toPosition: Int): Boolean
}