package com.app.personas_social.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

abstract class BaseAdapter(private var items: ArrayList<out Any>) :
    RecyclerView.Adapter<BaseAdapter.ItemViewHolder>() {

    class ItemViewHolder(val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ItemViewHolder(
        DataBindingUtil.inflate(LayoutInflater.from(parent.context), viewType, parent, false)
    )

    override fun getItemCount() = items.size

    fun setData(mItems: ArrayList<out Any>) {
        items = mItems
        notifyDataSetChanged()
    }
}