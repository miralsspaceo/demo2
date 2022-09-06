package com.app.personas_social.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.app.personas_social.R
import com.app.personas_social.databinding.ItemLineMusicLayoutBinding
import com.app.personas_social.databinding.ItemLineTextLayoutBinding
import com.app.personas_social.model.CopyModel
import com.app.personas_social.model.MusicData
import com.app.personas_social.stickerview.StickerTextView
import java.util.ArrayList
import java.util.concurrent.TimeUnit

class MusicLineAdapter (
    private val context: Context,
    private val musiclist: ArrayList<MusicData>,
    private val totalDuration:Int
) :
    BaseAdapter(musiclist) {

    override fun getItemViewType(position: Int) = R.layout.item_line_music_layout

    override fun onBindViewHolder(
        holder: BaseAdapter.ItemViewHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {
        if (holder.binding is ItemLineMusicLayoutBinding) {

            holder.binding.splitRange.layoutParams.width = totalDuration


                for (m in 0 until musiclist.size) {
                    var startTime = 0
                    if (m > 0) {
                        var endTimming =
                            ((TimeUnit.MILLISECONDS.toSeconds(musiclist[m - 1].endTime.toLong())) * 100).toInt()
                        if (endTimming > totalDuration) {
                            endTimming = totalDuration - 30
                        }
                        startTime =
                            ((TimeUnit.MILLISECONDS.toSeconds(musiclist[m].startTime.toLong())) * 100).toInt() + endTimming + 10
                    } else {
                        startTime =
                            ((TimeUnit.MILLISECONDS.toSeconds(musiclist[m].startTime.toLong())) * 100).toInt()
                    }
                    var endTime =
                        ((TimeUnit.MILLISECONDS.toSeconds(musiclist[m].endTime.toLong())) * 100).toInt()
                    if (endTime > totalDuration) {
                        endTime = totalDuration - 30
                    }
                    var startTime1 =
                        ((TimeUnit.MILLISECONDS.toSeconds(musiclist[m].startTime.toLong())) * 100).toInt()
                    holder.binding.splitRange.addSpan(
                        startTime ,
                        endTime - startTime1,
                        "",
                        m
                    )


                }








        }
    }



}