package com.app.personas_social.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import androidx.core.content.ContextCompat
import com.app.personas_social.R
import com.app.personas_social.databinding.RowColoursBinding
import com.app.personas_social.model.GradientItem
import com.app.personas_social.model.PatternItem
import java.util.ArrayList

class PatternAdapter (
    private val  context : Context,
    private val gradiantlist: ArrayList<PatternItem>,
    private val callBack: (position: Int) -> Unit
) :
    BaseAdapter(gradiantlist) {
    private var mSelectedItem = -1

    override fun getItemViewType(position: Int) = R.layout.row_colours

    override fun onBindViewHolder(holder: BaseAdapter.ItemViewHolder, @SuppressLint("RecyclerView") position: Int) {
        if (holder.binding is RowColoursBinding) {
            if (position == mSelectedItem) {
                holder.binding.imgbackground.visibility = View.VISIBLE
            }else{
                holder.binding.imgbackground.visibility = View.GONE

            }
            holder.binding.imgSelecterImage.setImageResource(gradiantlist[holder.absoluteAdapterPosition].drawbleid)


            holder.binding.imgSelecterImage.setOnClickListener {
                mSelectedItem = position;
                callBack(holder.absoluteAdapterPosition)
                notifyItemChanged(position)
                notifyDataSetChanged()
            }

        }
    }
}