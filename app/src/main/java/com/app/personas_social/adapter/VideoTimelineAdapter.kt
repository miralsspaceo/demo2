package com.app.personas_social.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.personas_social.R
import com.app.personas_social.databinding.RowFramesssBinding
import com.app.personas_social.model.CopyModel
import com.app.personas_social.model.Frames
import com.app.personas_social.model.MusicData
import com.app.personas_social.utlis.*


class VideoTimelineAdapter (
    private val context: Context,
    private val frameList: ArrayList<Frames>,
    private val frameWidth :Int,
    private val totalduration : Int,
    private val textStickerList: ArrayList<CopyModel>,
    private val musicLineList: ArrayList<MusicData>,
    private val callbackItem: (view: View, position: Int) -> Unit,
    private val callbackTouch: ( position: Int,isTouchFrame :Boolean) -> Unit
) :
    RecyclerView.Adapter<BaseAdapter.ItemViewHolder>() {
    private var layoutManagerText: LinearLayoutManager? = null
    private var layoutManagerMusic: LinearLayoutManager? = null
    private var   layoutManager: LinearLayoutManager? = null
    private var isTouchFrame = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseAdapter.ItemViewHolder {
        val binding: RowFramesssBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.row_framesss,
            parent,
            false
        )

        return   BaseAdapter.ItemViewHolder(binding)

    }

    override fun getItemCount(): Int = 1


    override fun onBindViewHolder(holder: BaseAdapter.ItemViewHolder, position: Int) {
        if (holder.binding is RowFramesssBinding) {

              layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
              layoutManager = SliderLayoutManager(context, holder.binding.rvFrame, callbackDelete = {
                    Log.e("onBindViewHolder", "isTouchFrame: "+isTouchFrame )
                    if (isTouchFrame)
                        callbackTouch.invoke(it,isTouchFrame)
                })
                holder.binding.rvFrame.layoutManager = layoutManager
                holder.binding.rvFrame.adapter = VideoFrameAdapter(context, frameList, frameWidth, callbackDelete = { view, position ->
                    callbackItem.invoke(view,position)
                })


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
                            callbackTouch.invoke(position,isTouchFrame)
                        }
                    })
            )

            if (textStickerList.size != 0 && !textStickerList.isNullOrEmpty()) {
                holder.binding.rvTextFinalLine.visible()
                layoutManagerText = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
                holder.binding.rvTextFinalLine.layoutManager = layoutManagerText
                holder.binding.rvTextFinalLine.adapter = TextLineAdapter(context, textStickerList)
            }else{
                holder.binding.rvTextFinalLine.hide()
            }

            if (musicLineList.size != 0 && !musicLineList.isNullOrEmpty()) {
                holder.binding.rvMusicFinalLine.visible()
                layoutManagerMusic = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
                holder.binding.rvMusicFinalLine.layoutManager = layoutManagerMusic
                holder.binding.rvMusicFinalLine.adapter = MusicLineAdapter(context, musicLineList,totalduration)
            }else{
                holder.binding.rvMusicFinalLine.hide()
            }

        }
    }


}