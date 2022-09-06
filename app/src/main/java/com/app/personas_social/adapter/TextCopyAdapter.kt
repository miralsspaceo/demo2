package com.app.personas_social.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.databinding.adapters.AdapterViewBindingAdapter
import com.app.personas_social.R
import com.app.personas_social.databinding.RawItemLayoutBinding
import com.app.personas_social.interfaces.OnItemSelected
import com.app.personas_social.model.CopyModel
import com.app.personas_social.stickerview.StickerTextView
import java.util.ArrayList
import kotlin.math.roundToInt

class TextCopyAdapter(
    private val context: Context,
    private val copylist: ArrayList<CopyModel>,
    private val onItemClick: OnItemSelected

) :
    BaseAdapter(copylist) {
    private var listener: StickerTextView.TimeLineChangeListener? = null

    override fun getItemViewType(position: Int) = R.layout.raw_item_layout

    override fun onBindViewHolder(
        holder: BaseAdapter.ItemViewHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {

        if (holder.binding is RawItemLayoutBinding) {

            holder.binding.splitRange.layoutParams.width = copylist[position].totalDuration - 35
            holder.binding.splitRange.setTextSize(40f)

            for (i in 0 until copylist[position].duplicatetext!!.size) {
                Log.e("onBindViewHolder", "onBindViewHolder: "+copylist[position].duplicatetext!! )
                holder.binding.splitRange.addSpan(
                    copylist[position].duplicatetext!![i].offset,
                    copylist[position].duplicatetext!![i].length,
                    copylist[position].duplicatetext!![i].info,
                    copylist[position].duplicatetext!![i].mobject
                )
                holder.binding.splitRange.updateSpan(copylist[position].duplicatetext!![i].mobject,copylist[position].duplicatetext!![i].isSelected)

            }

            listener = object : StickerTextView.TimeLineChangeListener {
                override fun onRangeChanged(
                    tag: Any?,
                    startFraction: Float,
                    endFraction: Float
                ) {

               onItemClick.onItemDragClick(position,tag as Int, startFraction.roundToInt() , endFraction.roundToInt() )



                }
                override fun onSelectionChange(tag: Any?, selected: Boolean) {
                     onItemClick.onItemslectClick(position, tag as Int)

                }

                override fun onThumbClicked(tag: Any?, thumbId: Int) {
                }

            }
            holder.binding.splitRange.addIndicatorChangeListener(listener)



        }
    }


}