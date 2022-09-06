package com.app.personas_social.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.app.personas_social.R
import com.app.personas_social.databinding.RowAnnimationBinding
import com.app.personas_social.databinding.RowColoursBinding
import com.app.personas_social.model.AnimationItem
import com.app.personas_social.model.ColorItem
import com.app.personas_social.utlis.VIDEO_1080
import com.howto.interfaces.OnItemClick
import java.util.ArrayList


class ColorsAdapter(
    private val  context : Context,
    private val colorList: ArrayList<ColorItem>,
    private val callBack: (position: Int) -> Unit
) :
    BaseAdapter(colorList) {

    override fun getItemViewType(position: Int) = R.layout.row_colours
    private var mSelectedItem = -1

    override fun onBindViewHolder(holder: ItemViewHolder, @SuppressLint("RecyclerView") position: Int) {
        if (holder.binding is RowColoursBinding) {
            if (position == mSelectedItem) {
                holder.binding.imgbackground.visibility = View.VISIBLE
            }else{
                holder.binding.imgbackground.visibility = View.GONE

            }

            if (colorList[position].colorId == R.drawable.mutlicolor){
                holder.binding.imgSelecterImage.colorFilter= null
                holder.binding.imgSelecterImage.setImageResource(R.drawable.mutlicolor)
            }
            else{
                holder.binding.imgSelecterImage.colorFilter = PorterDuffColorFilter(
                    ContextCompat.getColor(context, colorList[position].colorId),
                    PorterDuff.Mode.SRC_IN
                )
        }



            holder.binding.imgSelecterImage.setOnClickListener {
                mSelectedItem = position;
                notifyDataSetChanged()
                callBack(position)
//                notifyItemChanged(position)

            }

        }
    }
}