package com.app.personas_social.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.personas_social.R
import com.app.personas_social.databinding.RowFramesssBinding
import com.app.personas_social.databinding.RowVideoFramesBinding
import com.app.personas_social.interfaces.OnItemSelected
import com.app.personas_social.model.CopyModel
import com.app.personas_social.model.Frames
import com.app.personas_social.utlis.RecycleViewTouchListener
import com.app.personas_social.utlis.SliderLayoutManager

class VideoFramesAdapter(
    private val context: Context,
    private val frameList: ArrayList<Frames>,
    private val frameWidth :Int,
    private val textList: ArrayList<CopyModel>,
    private val textStickerList: ArrayList<CopyModel>,
    private val callbackDelete: (view: View, position: Int) -> Unit,
    private val callbackItem: ( position: Int,isTouchFrame :Boolean) -> Unit,
    private val callbackText: (position: Int, mPos: Int) -> Unit,
    private val callbackdrag: (position: Int ,mPos: Int,starttime:Int,endTime :Int) -> Unit
) :
    RecyclerView.Adapter<BaseAdapter.ItemViewHolder>() {
    private var layoutManagerText: LinearLayoutManager? = null
    private var layoutManager: LinearLayoutManager? = null
    private var isTouchFrame = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseAdapter.ItemViewHolder {
        val binding: RowVideoFramesBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.row_video_frames,
            parent,
            false
        )

        return BaseAdapter.ItemViewHolder(binding)

    }

    override fun getItemCount(): Int = 1


    override fun onBindViewHolder(holder: BaseAdapter.ItemViewHolder, position: Int) {
        if (holder.binding is RowVideoFramesBinding) {

            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            layoutManager = SliderLayoutManager(context, holder.binding.rvFrame, callbackDelete = {
                Log.e("onBindViewHolder", "isTouchFrame: " + isTouchFrame)
                if (isTouchFrame)
                    callbackItem.invoke(it, isTouchFrame)
            })
            holder.binding.rvFrame.layoutManager = layoutManager
            holder.binding.rvFrame.adapter = VideoFrameAdapter(
                context,
                frameList,
                frameWidth,
                callbackDelete = { view, position ->
                    callbackDelete.invoke(view, position)
                })
            holder.binding.rvFrame.adapter!!.notifyDataSetChanged()



            holder.binding.rvFrame.addOnItemTouchListener(
                RecycleViewTouchListener(context,
                    holder.binding.rvFrame,
                    object : RecycleViewTouchListener.OnTouchActionListener {

                        override fun onLeftSwipe(view: View?, position: Int) {
                            isTouchFrame = true

                        }

                        override fun onRightSwipe(view: View?, position: Int) {
                            isTouchFrame = true

                        }

                        override fun onClick(view: View?, position: Int) {
                            isTouchFrame = true

                        }
                    })
            )

            if (textList.size != 0 && !textList.isNullOrEmpty()) {
                holder.binding.rvTextFrame.visibility = View.VISIBLE
                layoutManagerText = LinearLayoutManager(context, RecyclerView.VERTICAL, true)
                holder.binding.rvTextFrame.layoutManager= layoutManagerText
                holder.binding.rvTextFrame.adapter = TextCopyAdapter(context, textList,object :OnItemSelected{

                    override fun onItemslectClick(position: Int, itemPos: Int) {
                        callbackText.invoke(position, itemPos)
                    }

                    override fun onItemDragClick(
                        position: Int,
                        itemPos: Int,
                        startTime: Int,
                        endTime: Int
                    ) {
                        callbackdrag.invoke(position, itemPos,startTime,endTime)
                    }
                })
            }
            else{
                holder.binding.rvTextFrame.visibility = View.GONE
            }

            if (textStickerList.size != 0 && !textStickerList.isNullOrEmpty()) {
                layoutManagerText = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
                holder.binding.rvTextFinalLine.layoutManager = layoutManagerText
                holder.binding.rvTextFinalLine.adapter = TextLineAdapter(context, textStickerList)
            }

        }
    }



}

