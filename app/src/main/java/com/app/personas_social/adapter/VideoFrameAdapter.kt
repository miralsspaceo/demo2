package com.app.personas_social.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.personas_social.R
import com.app.personas_social.databinding.RowFramesBinding
import com.app.personas_social.model.Frames
import com.bumptech.glide.Glide
import java.util.ArrayList
import kotlin.math.ceil

class VideoFrameAdapter(
    private val context: Context,
    private val frameList: ArrayList<Frames>,
    private val frameWidth :Int,
    private val callbackDelete: (view: View, position: Int) -> Unit
) :
    RecyclerView.Adapter<BaseAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseAdapter.ItemViewHolder {
        val binding: RowFramesBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.row_frames,
                parent,
                false
            )

        return   BaseAdapter.ItemViewHolder(binding)

    }

    override fun getItemCount(): Int = frameList.size


    override fun onBindViewHolder(holder: BaseAdapter.ItemViewHolder, position: Int) {
        if (holder.binding is RowFramesBinding) {
            holder.binding.ivImage.layoutParams.width = frameWidth


            Glide.with(context).asBitmap().load(frameList[position].frame)
                .into(holder.binding.ivImage)

            holder.itemView.setOnClickListener {
                callbackDelete(it, holder.absoluteAdapterPosition)
            }
        }

    }

}