package com.app.personas_social.adapter

import com.app.personas_social.R
import com.app.personas_social.databinding.ItemVideoEditBtnBinding
import com.app.personas_social.model.VideoEditBtn
import com.howto.interfaces.OnItemClick
import java.util.ArrayList

class MusicEditAdapter(
    private val arrVideo: ArrayList<VideoEditBtn>,
    private val onItemClick: OnItemClick
) :
    BaseAdapter(arrVideo) {

    override fun getItemViewType(position: Int) = R.layout.item_video_edit_btn

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        if (holder.binding is ItemVideoEditBtnBinding) {
            holder.binding.tvName.text = arrVideo[holder.absoluteAdapterPosition].btnText

            holder.binding.ivEditIcon.setImageResource(arrVideo[holder.absoluteAdapterPosition].btnIcon)

            holder.binding.llitem.setOnClickListener {
                onItemClick.onItemClick(holder.absoluteAdapterPosition)
            }

        }
    }
}