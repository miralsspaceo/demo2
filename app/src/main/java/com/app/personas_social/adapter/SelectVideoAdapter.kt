package com.app.personas_social.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.app.personas_social.R
import com.app.personas_social.databinding.LayoutSelectVideoBinding
import com.app.personas_social.interfaces.OnDeleteClick
import com.app.personas_social.model.VideoItem
import com.app.personas_social.viewmodel.VideoListViewModel
import com.howto.interfaces.OnItemClick


class SelectVideoAdapter(  private var arrselectVideo: ArrayList<VideoItem>,
private val viewModel: VideoListViewModel
) : BaseAdapter(arrselectVideo) {
    private var clickListener: OnDeleteClick? = null

    fun setDeleteClickListener(itemClickListener: OnDeleteClick) {
        this.clickListener = itemClickListener
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding: LayoutSelectVideoBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.layout_select_video,
            parent,
            false
        )

        val itemWidth = parent.measuredWidth / 4
        binding.ivVideoThumb.layoutParams.width = itemWidth
        binding.ivVideoThumb.layoutParams.height = itemWidth
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder:ItemViewHolder, position: Int) {
        if (holder.binding is LayoutSelectVideoBinding) {
            arrselectVideo[holder.absoluteAdapterPosition].position = holder.absoluteAdapterPosition
            holder.binding.videoItem = arrselectVideo[holder.absoluteAdapterPosition]
            holder.binding.viewModel = viewModel
            holder.binding.executePendingBindings()
            holder.binding.imgClose.setOnClickListener {
                clickListener!!.onItemDeleteClick(position)
            }

        }
    }
}