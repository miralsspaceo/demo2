package com.howto.interfaces

import android.widget.SeekBar

interface OnSeekBarChange {
    fun onStartTrackingTouch(seekBar: SeekBar)
    fun onProgressChanged(
        seekBar: SeekBar,
        progress: Int,
        fromUser: Boolean
    )

    fun onStopTrackingTouch(seekBar: SeekBar)
}