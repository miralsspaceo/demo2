package com.app.personas_social.adapter

import com.app.personas_social.R
import com.app.personas_social.databinding.ItemMusicEffectBinding
import com.app.personas_social.databinding.ItemVideoEditBtnBinding
import com.app.personas_social.model.VideoEditBtn
import com.howto.interfaces.OnItemClick
import java.util.ArrayList

class MusicEffectsAdapter (
    private val arrVideo: ArrayList<VideoEditBtn>,
    private val callBack: (position: Int) -> Unit
) :
    BaseAdapter(arrVideo) {

    override fun getItemViewType(position: Int) = R.layout.item_music_effect

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        if (holder.binding is ItemMusicEffectBinding) {
            holder.binding.tvName.text = arrVideo[holder.absoluteAdapterPosition].btnText

            holder.binding.ivEditIcon.setImageResource(arrVideo[holder.absoluteAdapterPosition].btnIcon)

            holder.binding.llitem.setOnClickListener {
//                onItemClick.onItemClick(holder.absoluteAdapterPosition)
                callBack(holder.absoluteAdapterPosition)
            }

        }
    }
}