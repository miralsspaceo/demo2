package com.app.personas_social.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.View
import androidx.core.content.ContextCompat
import com.app.personas_social.R
import com.app.personas_social.databinding.LayoutFontListBinding
import com.app.personas_social.databinding.RowColoursBinding
import com.app.personas_social.model.ColorItem
import com.app.personas_social.model.FontBean
import java.util.ArrayList

class FontAdapter (
    private val  context : Context,
    private val fontlist: ArrayList<FontBean>,
    private val callBack: (position: Int) -> Unit
) :
    BaseAdapter(fontlist) {
    private var mSelectedItem = -1

    override fun getItemViewType(position: Int) = R.layout.layout_font_list

    override fun onBindViewHolder(holder: ItemViewHolder, @SuppressLint("RecyclerView") position: Int) {
        if (holder.binding is LayoutFontListBinding) {
            if (position == mSelectedItem) {
                holder.binding.IvSelect.visibility = View.VISIBLE
             }else{
                holder.binding.IvSelect.visibility = View.GONE
            }
             holder.binding.tvFont.text = fontlist[position].name
            holder.binding.tvFont.typeface = fontlist[position].font



            holder.itemView.setOnClickListener {
                mSelectedItem = position;
                callBack(holder.absoluteAdapterPosition)
                notifyItemChanged(position)
                notifyDataSetChanged()
            }

        }
    }
}