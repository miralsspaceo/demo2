package com.app.personas_social.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import com.app.personas_social.R
import com.app.personas_social.databinding.RawFilterThumbnailBinding
import com.app.personas_social.model.VideoItem
import com.bumptech.glide.Glide

class VideoFilterAdapter  (
    private val  context : Context,
    private val arrVideo: ArrayList<VideoItem>,
    private val callBack: (position: Int) -> Unit
) :
    BaseAdapter(arrVideo) {
    private var mSelectedItem = 0

    override fun getItemViewType(position: Int) = R.layout.raw_filter_thumbnail



    override fun onBindViewHolder(holder: BaseAdapter.ItemViewHolder, @SuppressLint("RecyclerView") position: Int) {
        if (holder.binding is RawFilterThumbnailBinding) {


            if (position == mSelectedItem) {
                if (mSelectedItem == 0 ){
                    holder.binding.llitem.background = ContextCompat.getDrawable(context,R.drawable.left_corner_reactangle_pink)
                }else if (mSelectedItem == arrVideo.size-1){
                    holder.binding.llitem.background = ContextCompat.getDrawable(context,R.drawable.right_corner_reactangle_pink)
                }else{
                    holder.binding.llitem.background = ContextCompat.getDrawable(context,R.drawable.pink_corner_drawble)
                }

            }else{
                holder.binding.llitem.background = null
            }


            Glide.with(context)
                .load(arrVideo[position].videoThumb)
                .centerCrop()
                .into(holder.binding.imgfilter);


            holder.binding.llitem.setOnClickListener {
                mSelectedItem = position;
                notifyDataSetChanged()
                callBack(position)
            }

        }
    }
}