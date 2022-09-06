package com.app.personas_social.viewmodel

import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.app.personas_social.adapter.SelectVideoAdapter
import com.app.personas_social.model.VideoItem
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.app.personas_social.R
import com.app.personas_social.adapter.VideoGalleryAdapter
import com.app.personas_social.app.ResponseObserver
import com.app.personas_social.utlis.RESET_DATA
import com.app.personas_social.utlis.getDurationFromUrl
import com.app.personas_social.utlis.outputPathOfInternalFolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File


class VideoListViewModel : BaseViewModel() {
    private lateinit var job: Job
     var arrVideo = ArrayList<VideoItem>()
    var arrAllVideo = ArrayList<VideoItem>()
    var arrSelectedVideo = ArrayList<VideoItem>()
    private var counter = 1
    var videoLibraryAdapter = VideoGalleryAdapter(arrVideo, this)
    var videoSelectAdapter = SelectVideoAdapter(arrSelectedVideo, this)
    private var mutableArrVideo = MutableLiveData<ArrayList<VideoItem>>()
    var liveArrVideo: LiveData<ArrayList<VideoItem>> = mutableArrVideo
    private val mIsSelectOne = ObservableBoolean(false)
    val mIsDisplayFolder = ObservableBoolean(false)


    init {
        //Set All required observer
        liveArrVideo.observeForever {
            videoLibraryAdapter.setData(it)
        }

    }

    fun getVideoList(activity: Activity) {
        counter = 1
        mIsSelectOne.set(counter > 1)
        arrSelectedVideo.clear()
        currentStatus.postValue(ResponseObserver.Loading(true))
        job = GlobalScope.launch(Dispatchers.IO) {
            val uri =
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
//                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY) else
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            arrAllVideo.clear()
            getVideoFromInternalFolder(activity)
            val projection =
                arrayOf(
                    MediaStore.Video.Media._ID,
                    MediaStore.Video.VideoColumns.DATA,
                    MediaStore.Video.Thumbnails.DATA,
                    MediaStore.Video.Media.DISPLAY_NAME,
                    MediaStore.Video.VideoColumns.DURATION
                )
            val orderBy = MediaStore.Video.Media.DATE_ADDED
            val cursor =
                activity.contentResolver.query(uri, projection, null, null, "$orderBy DESC")!!
            while (cursor.moveToNext()) {
                val videoUri = ContentUris.withAppendedId(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID))
                )
                val videoUrl =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA))
                arrAllVideo.add(
                    VideoItem(
                        videoUrl = videoUrl,
                        videoThumb = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Thumbnails.DATA)),
                        isSelected = false,
                        selectedItemCount = 0,
                        videoDuration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DURATION)),
                        isVideoPortrait = false, isAudioAvail = false, isVideoProper = false,
                        videoUri = videoUri
                    )
                )
            }
            cursor.close()
            currentStatus.postValue(ResponseObserver.Loading(false))
            arrVideo.clear()
            arrVideo.addAll(arrAllVideo)
            if (arrVideo.size != 0){
              mIsDisplayFolder.set(false)
            }else{
                mIsDisplayFolder.set(true) }
            mutableArrVideo.postValue(arrVideo)
            currentStatus.postValue(ResponseObserver.PerformAction(RESET_DATA))
        }
    }

    private fun getVideoFromInternalFolder(activity: Activity) {
        val folder = File(activity.outputPathOfInternalFolder)
        val fileList = folder.listFiles()!!
        fileList.reverse()
        try {
            for (file in fileList) {
                if (file.absolutePath.contains(".mp4")) {
                    arrAllVideo.add(
                        VideoItem(
                            videoUrl = file.absolutePath,
                            videoThumb = file.absolutePath,
                            isSelected = false,
                            selectedItemCount = 0,
                            videoDuration = activity.getDurationFromUrl(file.absolutePath),
                            isVideoPortrait = false, isAudioAvail = false, isVideoProper = false,
                            width =0,
                            height =0,
                            videoUri = Uri.parse(file.absolutePath)
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setCounter(videoItem: VideoItem) {
        if (!videoItem.isVideoProper)
            currentStatus.postValue(ResponseObserver.DisplayAlert(R.string.msg_video_not_proper))
        else {
            Log.e("setCounter", "setCounter: "+videoItem )
            videoItem.isSelected = !videoItem.isSelected
            if (videoItem.isSelected) {
                videoItem.selectedItemCount = counter++

                videoLibraryAdapter.notifyItemChanged(videoItem.position)
                arrSelectedVideo.add(videoItem)


            } else if (counter > 1) {
                Log.e("setCounter", "counter2222: "+counter)
                for ((i, mVideoItem) in arrVideo.withIndex()) {
                    if (mVideoItem.isSelected && videoItem.selectedItemCount < mVideoItem.selectedItemCount) {
                        mVideoItem.selectedItemCount--
                    }
                }
                counter--
                mutableArrVideo.postValue(arrVideo)
                arrSelectedVideo.remove(videoItem)
            }
            mIsSelectOne.set(counter > 1)

        }

    }

    fun cancelAllSelectedItem() {
        for ((i, videoItem) in arrVideo.withIndex()) {
            if (videoItem.isSelected) {
                videoItem.isSelected = false
                videoItem.selectedItemCount = 0
                counter = 1
                mIsSelectOne.set(counter > 1)
            }
        }
        arrSelectedVideo.clear()
        mutableArrVideo.postValue(arrVideo)
    }


    fun getSelectedItem() = arrSelectedVideo

    fun getSelectedItemSize() = arrSelectedVideo.size.toString()

    fun onDestroy() {
        if (::job.isInitialized)
            job.cancel()
    }

}