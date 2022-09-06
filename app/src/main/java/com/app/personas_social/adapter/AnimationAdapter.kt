package com.app.personas_social.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.util.Log
import androidx.core.content.ContextCompat
import com.app.personas_social.R
import com.app.personas_social.databinding.RowAnnimationBinding
import com.app.personas_social.model.AnimationItem
import com.app.personas_social.model.VideoEditBtn
import com.howto.interfaces.OnItemClick
import java.util.ArrayList

class AnimationAdapter (
    private val  context : Context,
    private val animationlist: ArrayList<AnimationItem>,
    private val animationpinklist: ArrayList<AnimationItem>,
    private val callBack: (position: Int) -> Unit
) :
    BaseAdapter(animationlist) {

    override fun getItemViewType(position: Int) = R.layout.row_annimation
    private var mSelectedItem = -1

    override fun onBindViewHolder(holder: ItemViewHolder, @SuppressLint("RecyclerView") position: Int) {
        if (holder.binding is RowAnnimationBinding) {
            if (position == mSelectedItem) {
                holder.binding.imgSelecterImage.setImageResource(animationpinklist[holder.absoluteAdapterPosition].drawbleid)
            }else{
                holder.binding.imgSelecterImage.setImageResource(animationlist[holder.absoluteAdapterPosition].drawbleid)

            }

            holder.binding.imgSelecterImage.setOnClickListener {
                mSelectedItem = position;
                callBack(holder.absoluteAdapterPosition)
                notifyItemChanged(position)
                notifyDataSetChanged()
            }

        }
    }
}