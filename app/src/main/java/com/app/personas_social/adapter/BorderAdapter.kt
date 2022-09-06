package com.app.personas_social.adapter

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import androidx.core.content.ContextCompat
import com.app.personas_social.R
import com.app.personas_social.databinding.RowColoursBinding
import com.app.personas_social.model.BorderItem
import com.app.personas_social.model.ColorItem
import com.app.personas_social.model.GradientItem
import com.app.personas_social.model.PatternItem
import java.util.ArrayList

class BorderAdapter (
    private val  context : Context,
    private val borderlist: ArrayList<BorderItem>,
    private val callBack: (position: Int) -> Unit
) :
    BaseAdapter(borderlist) {

    override fun getItemViewType(position: Int) = R.layout.row_colours

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        if (holder.binding is RowColoursBinding) {


            holder.binding.imgSelecterImage.setImageResource(borderlist[holder.absoluteAdapterPosition].drawbleid)


            holder.binding.imgSelecterImage.setOnClickListener {
                callBack(holder.absoluteAdapterPosition)
            }

        }
    }
}