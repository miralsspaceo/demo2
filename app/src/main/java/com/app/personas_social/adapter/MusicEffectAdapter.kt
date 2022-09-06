package com.app.personas_social.adapter

import android.content.Context
import android.content.ContextWrapper
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.util.Log
import android.util.Size
import android.view.View
import android.widget.Toast
import com.app.personas_social.R
import com.app.personas_social.databinding.RawMusicDeviceBinding
import com.app.personas_social.databinding.RawMusicEffectDeviceBinding
import com.app.personas_social.model.MusicData
import com.app.personas_social.utlis.FileUtils
import com.app.personas_social.utlis.IS_EFFECT
import com.app.personas_social.viewmodel.VideoEditViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import video.amaze.app.network.RetrofitImage
import java.io.File

class MusicEffectAdapter (
    private var arrMusicData: ArrayList<MusicData>,
    private val viewModel: VideoEditViewModel,
    private val context: Context,
    val updateList: (Int) -> Unit
) : BaseAdapter(arrMusicData) {

    var pos = 0
    override fun getItemViewType(position: Int) = R.layout.raw_music_effect_device

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.setIsRecyclable(false)
        if (holder.binding is RawMusicEffectDeviceBinding && arrMusicData.size > 0) {
            arrMusicData[holder.bindingAdapterPosition].position = holder.bindingAdapterPosition
            holder.binding.musicData = arrMusicData[holder.bindingAdapterPosition]
            holder.binding.viewModel = viewModel
            holder.binding.executePendingBindings()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    val albumArt = ThumbnailUtils.createAudioThumbnail(
                        File(arrMusicData[holder.bindingAdapterPosition].audioUrl),
                        Size(400, 400),
                        null
                    )
                    Glide.with(holder.binding.ivMusicThumb.context)
                        .applyDefaultRequestOptions(RequestOptions().error(R.drawable.music))
                        .load(albumArt)
                        .into(holder.binding.ivMusicThumb)
                } catch (e: Exception) {
                    Glide.with(holder.binding.ivMusicThumb.context)
                        .load(R.drawable.music)
                        .into(holder.binding.ivMusicThumb)
                }
            } else {
                val retriever = MediaMetadataRetriever()
                try {
                    val bfo = BitmapFactory.Options()
                    retriever.setDataSource(arrMusicData[holder.bindingAdapterPosition].audioUrl)
                    val rawArt: ByteArray? = retriever.embeddedPicture
                    if (rawArt != null) {
                        val art = BitmapFactory.decodeByteArray(rawArt, 0, rawArt.size, bfo)
                        if (art != null)
                            Glide.with(holder.binding.ivMusicThumb.context)
                                .applyDefaultRequestOptions(RequestOptions().error(R.drawable.music))
                                .load(art).into(holder.binding.ivMusicThumb)
                    } else {
                        Log.e("MusicAdapter", "else >= Build.VERSION_CODES.Q try else")
                        Glide.with(holder.binding.ivMusicThumb.context)
                            .load(com.videofilter.R.drawable.filter_thumb)
                            .into(holder.binding.ivMusicThumb)
                    }
                } catch (e: Exception) {
                    Log.e("MusicAdapter", "else >= Build.VERSION_CODES.Q catch")
                    Glide.with(holder.binding.ivMusicThumb.context)
                        .load(R.drawable.music)
                        .into(holder.binding.ivMusicThumb)
                } finally {
                    retriever.release()
                }
            }
            holder.binding.tvMusicName.text =
                arrMusicData[holder.bindingAdapterPosition].title.trim()

            if (arrMusicData[holder.bindingAdapterPosition].isDownload){
                holder.binding.imgMusicDownload.visibility = View.GONE
                if (arrMusicData[holder.bindingAdapterPosition].itemisSelected) {
                    holder.binding.ivPlay.visibility = View.VISIBLE
                    holder.binding.imgMusicDone.visibility = View.VISIBLE
                } else {
                    holder.binding.imgMusicDone.visibility = View.GONE
                    holder.binding.ivPlay.visibility = View.GONE
                }
            }else{
                holder.binding.imgMusicDownload.visibility = View.VISIBLE
            }



            holder.binding.ccCard.setOnClickListener {
                Log.e("MusicEffectAdapter", "onBindViewHolder: "+arrMusicData[position].audioUrl )
                Log.e("MusicEffectAdapter", "onBindViewHolder: "+arrMusicData[position].duration )

                viewModel.onClickMusicItem(arrMusicData[position], 1)
                updateList(position)

            }

            holder.binding.imgMusicDone.setOnClickListener {
                for (i in 0 until arrMusicData.size) {
                    arrMusicData[i].isSelected = false
                }
                arrMusicData[position].isSelected = true
                viewModel.onClickMusicItem(arrMusicData[position], 2)


            }

            holder.binding.imgMusicDownload.setOnClickListener {
                val storage = Firebase.storage
                storage.getReferenceFromUrl(
                    arrMusicData[position].effectUrl
                ).downloadUrl.addOnSuccessListener { itUrl ->
                    // Got the download URL for 'users/me/profile.png'
                    Log.e("tiny", "Music itUrl: " + itUrl)

                    RetrofitImage.getVideoFrom(itUrl.toString(), object :
                        RetrofitImage.OnAttachmentDownloadListener {
                        override fun onAttachmentDownloadedSuccess() {


                        }

                        override fun onAttachmentDownloadedError() {

                        }

                        override fun onAttachmentDownloadedFinished() {


                        }

                        override fun onAttachmentDownloadUpdate(percent: Int) {

                        }

                    }) { mInputStream ->

                        print("$mInputStream")
                        mInputStream?.let { it2 ->
                            this.let { it1 ->
                                var fileName = FileUtils.saveFileAudio(
                                    it2,
                                    context,
                                    arrMusicData[position].title,
                                )
                                notifyItemChanged(position)
                                Log.e("fileName", "onBindViewHolder: "+fileName )


//                               var mypath = File(directory, fileName)
//
//                                loadFromFile(mypath)

//                                Toast.makeText(this, fileName, Toast.LENGTH_LONG).show()
                                Log.e("tiny", "Music fileName: " + fileName)
                            }
                        }
                    }
                }.addOnFailureListener { itE ->
                    Log.e("tiny", "Music itE: " + itE)
                }
            }

        }
    }
}