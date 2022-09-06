package com.app.personas_social.adapter

import android.app.Dialog
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.util.Log
import android.util.Size
import android.view.View
import android.view.Window
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleCoroutineScope
import com.app.personas_social.R
import com.app.personas_social.databinding.RawMusicDeviceBinding
import com.app.personas_social.model.MusicData
import com.app.personas_social.utlis.FileUtils
import com.app.personas_social.utlis.IS_EFFECT
import com.app.personas_social.viewmodel.VideoEditViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import video.amaze.app.network.RetrofitImage
import java.io.File

class MusicAdapter(
    private var arrMusicData: ArrayList<MusicData>,
    private val viewModel: VideoEditViewModel,
    val updateList: (Int) -> Unit
) : BaseAdapter(arrMusicData) {
    lateinit var progressDialog: Dialog
    var pos = 0
    override fun getItemViewType(position: Int) = R.layout.raw_music_device

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.setIsRecyclable(false)

        if (holder.binding is RawMusicDeviceBinding && arrMusicData.size > 0) {
            arrMusicData[holder.bindingAdapterPosition].position = holder.bindingAdapterPosition
            holder.binding.musicData = arrMusicData[holder.bindingAdapterPosition]
            holder.binding.viewModel = viewModel
            holder.binding.executePendingBindings()

            if (arrMusicData[holder.bindingAdapterPosition].isfrom != IS_EFFECT){
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
            }else{
                Glide.with(holder.binding.ivMusicThumb.context)
                    .load(arrMusicData[holder.bindingAdapterPosition].thumb).placeholder(R.drawable.music)
                    .into(holder.binding.ivMusicThumb)
            }
            holder.binding.tvMusicName.text =
                arrMusicData[holder.bindingAdapterPosition].title.trim()

            if (!arrMusicData[holder.bindingAdapterPosition].isDownload && arrMusicData[holder.bindingAdapterPosition].isfrom != IS_EFFECT) {
                if (arrMusicData[holder.bindingAdapterPosition].itemisSelected) {
                    holder.binding.ivPlay.visibility = View.VISIBLE
                    holder.binding.imgMusicDone.visibility = View.VISIBLE
                } else {
                    holder.binding.imgMusicDone.visibility = View.GONE
                    holder.binding.ivPlay.visibility = View.GONE
                }
            } else {
                if (arrMusicData[holder.bindingAdapterPosition].isDownload) {
                    holder.binding.imgMusicDownload.visibility = View.GONE
                    if (arrMusicData[holder.bindingAdapterPosition].itemisSelected) {
                        holder.binding.ivPlay.visibility = View.VISIBLE
                        holder.binding.imgMusicDone.visibility = View.VISIBLE
                    } else {
                        holder.binding.imgMusicDone.visibility = View.GONE
                        holder.binding.ivPlay.visibility = View.GONE
                    }
                } else {
                    holder.binding.imgMusicDownload.visibility = View.VISIBLE

                }
            }

            holder.binding.ccCard.setOnClickListener {
                Log.e("onBindViewHolder", "onBindViewHolder: "+arrMusicData[position].audioUrl )
                Log.e("onBindViewHolder", "onBindViewHolder: "+arrMusicData[position].duration )
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
//                    showProgress(holder.binding.imgMusicDownload)
                      holder.binding.circularProgressbar.visibility = View.VISIBLE
                      holder.binding.imgMusicDownload.visibility = View.GONE
                    val res: Resources = holder.binding.imgMusicDownload.context.resources
                    val drawable: Drawable = ContextCompat.getDrawable(holder.binding.imgMusicDownload.context, R.drawable.circular)!!
                    holder.binding.circularProgressbar.progress = 0 // Main Progress
                    holder.binding.circularProgressbar.secondaryProgress = 100 // Secondary Progress
                    holder.binding.circularProgressbar.max = 100 // Maximum Progress
                    holder.binding.circularProgressbar.progressDrawable = drawable

                    GlobalScope.launch(Dispatchers.Main) {
                        RetrofitImage.getVideoFrom(itUrl.toString(), object :
                            RetrofitImage.OnAttachmentDownloadListener {
                            override fun onAttachmentDownloadedSuccess() {
                            }

                            override fun onAttachmentDownloadedError() {
                            }

                            override fun onAttachmentDownloadedFinished() {
                            }

                            override fun onAttachmentDownloadUpdate(percent: Int) {
                                holder.binding.circularProgressbar.progress = percent
                                if (percent == 100){
                                    holder.binding.circularProgressbar.visibility = View.GONE
                                }
                            }

                        }) { mInputStream ->
                            print("$mInputStream")
                            mInputStream?.let { it2 ->
                                this.let { it1 ->
                                    val fileName = FileUtils.saveFileAudio(
                                        it2,
                                        holder.binding.imgMusicDownload.context,
                                        arrMusicData[position].title,
                                    )
                                    Log.e("fileName", "onBindViewHolder: " + fileName)
                                    val cw = ContextWrapper(holder.binding.ivMusicThumb.context)

                                    val directory: File = cw.getDir("music", Context.MODE_PRIVATE)

                                    val mypath = File(
                                        directory.absolutePath,
                                        fileName
                                    )
                                    // load data file
                                    val metaRetriever = MediaMetadataRetriever()
                                    metaRetriever.setDataSource( holder.binding.imgMusicDownload.context, Uri.parse(mypath.absolutePath))
                                    Log.d("time=================>", "time=================>")
                                    // get mp3 info
                                    // convert duration to minute:seconds
                                    val duration =
                                        metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                                    Log.e("time=================>", duration!!)

                                    arrMusicData[position].isDownload = true
                                    arrMusicData[position].audioUrl = mypath.absolutePath
                                    arrMusicData[position].duration = duration.toLong()
                                    arrMusicData[position].endTime = duration.toLong()
                                    notifyItemChanged(position)
                                    notifyDataSetChanged()
                                }
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