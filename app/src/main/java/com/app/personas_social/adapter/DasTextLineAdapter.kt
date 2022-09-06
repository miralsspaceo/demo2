package com.app.personas_social.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.app.personas_social.R
import com.app.personas_social.databinding.ItemLineTextLayoutBinding
import com.app.personas_social.databinding.ItemLineeTextLayoutBinding
import com.app.personas_social.model.CopyModel
import com.app.personas_social.stickerview.StickerTextView
import java.util.ArrayList

class DasTextLineAdapter (
    private val context: Context,
    private val copylist: ArrayList<CopyModel>
) :
    BaseAdapter(copylist) {
    private var listener: StickerTextView.TimeLineChangeListener? = null

    override fun getItemViewType(position: Int) = R.layout.item_linee_text_layout

    override fun onBindViewHolder(
        holder: BaseAdapter.ItemViewHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {
        if (holder.binding is ItemLineeTextLayoutBinding) {

            holder.binding.splitRange.layoutParams.width = copylist[position].totalDuration

            for (i in 0 until copylist[position].duplicatetext!!.size) {
                holder.binding.splitRange.addSpan(
                    copylist[position].duplicatetext!![i].offset,
                    copylist[position].duplicatetext!![i].length,
                    "",
                    copylist[position].duplicatetext!![i].mobject
                )


            }




        }
    }


}