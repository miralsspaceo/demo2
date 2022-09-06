package com.app.personas_social.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.app.personas_social.R
import com.app.personas_social.databinding.LayoutVideoItemBinding
import com.app.personas_social.model.VideoItem
import com.app.personas_social.utlis.convertMillieToHMS
import com.app.personas_social.utlis.hide
import com.app.personas_social.utlis.visible
import com.app.personas_social.viewmodel.VideoListViewModel
import com.bumptech.glide.Glide
import com.howto.interfaces.OnItemClick
import java.io.File

class VideoGalleryAdapter (
    private var arrVideo: ArrayList<VideoItem>,
    private val viewModel: VideoListViewModel
) : BaseAdapter(arrVideo) {
    private var clickListener: OnItemClick? = null
    private var itemWidth = 0
    private var itemWidth2 = 0


    fun setClickListener(itemClickListener: OnItemClick) {
        this.clickListener = itemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding: LayoutVideoItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.layout_video_item,
            parent,
            false
        )

         itemWidth = parent.measuredWidth / 2
         itemWidth2 = parent.measuredWidth / 1
//        binding.ivVideoThumb.layoutParams.width = itemWidth
//        binding.ivVideoThumb.layoutParams.height = itemWidth
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        if (holder.binding is LayoutVideoItemBinding) {


            arrVideo[holder.absoluteAdapterPosition].position = holder.absoluteAdapterPosition
            holder.binding.videoItem = arrVideo[holder.absoluteAdapterPosition]
            holder.binding.viewModel = viewModel

            if ( arrVideo[holder.absoluteAdapterPosition].height == 0){
                if ( position %2 == 0){
                    holder.binding .ivVideoThumb.layoutParams.width = itemWidth
                    holder.binding .ivVideoThumb.layoutParams.height = itemWidth

                }else{
                    holder.binding .ivVideoThumb.layoutParams.width = itemWidth
                    holder.binding .ivVideoThumb.layoutParams.height = itemWidth2

                }
            }

            Log.e("onBindViewHolder", "holder.absoluteAdapterPosition: "+holder.absoluteAdapterPosition )
            Log.e("onBindViewHolder", "isSelected: "+ arrVideo[holder.absoluteAdapterPosition].isSelected )



            holder.binding.ivVideoThumb.setOnClickListener {
                var duration =  convertMillieToHMS(arrVideo[holder.absoluteAdapterPosition].videoDuration, false)
               if (duration != "00:00" && duration != "00:00:00" && duration != "00" )
               {
                   clickListener!!.onItemClick(position)
               }

            }
        }
        holder.binding.executePendingBindings()
    }

    fun convertMillieToHMS(millis: Long, isShowHours: Boolean = false): String {
        val hours = (millis / 1000 / 60 / 60).toInt()
        val minutes = (millis / 1000 / 60).toInt()
        val seconds = (millis.toDouble() / 1000).toInt() - (minutes * 60)

        return if (hours > 0 || isShowHours) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

}