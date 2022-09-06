package com.app.personas_social.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.app.personas_social.R
import com.app.personas_social.app.ResponseObserver
import com.app.personas_social.databinding.ActivityVideoListBinding
import com.app.personas_social.interfaces.OnDeleteClick
import com.app.personas_social.model.VideoItem
import com.app.personas_social.utlis.*
import com.app.personas_social.utlis.PreferenceHelper.set
import com.app.personas_social.viewmodel.VideoListViewModel
import com.howto.interfaces.OnItemClick
import java.util.*
import kotlin.collections.ArrayList

class VideoListActivity : BaseActivity(),OnItemClick,OnDeleteClick {

    private lateinit var binding : ActivityVideoListBinding
    private var isFromToAddVideo = false

    private lateinit var selectvideolist : ArrayList<VideoItem>
    private  var mSelectvideolist : ArrayList<VideoItem> = ArrayList()
    private var mpos = 0
    private val videoLibraryViewModel: VideoListViewModel by lazy {
        VideoListViewModel()
    }
    companion object {
        fun startActivity(activity: Activity, isFromToAddVideo: Boolean = false,arrvideo : ArrayList<VideoItem>) {
            val intent = Intent(activity, VideoListActivity::class.java).putExtra(
                IS_ADD_VIDEO,
                isFromToAddVideo
            ).putParcelableArrayListExtra("ISADDED",arrvideo)
            activity.startActivityForResult(intent, REQUEST_ADD_VIDEO)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bindUI()
        initUI()

    }


 
    private fun bindUI() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_video_list)
        binding.run {
            viewModel = videoLibraryViewModel
            lifecycleOwner = this@VideoListActivity
        }

        binding.includeToolbar.btnClose.setOnClickListener {
            onBackPressed()
        }

    }

    fun slectedItem(view :View){
        when(view.id){
            R.id.tv_next ->{
                Log.e("videoLibraryViewModel", "slectedItem: "+  videoLibraryViewModel.getSelectedItem() )
                VideoEditorActivity.startActivity(
                        this@VideoListActivity,
                        videoLibraryViewModel.getSelectedItem(),
                    isadded
                    )


            }
        }
    }

    override fun onResume() {
        super.onResume()
//        initUI()

    }


    private fun initUI() {
        if (intent.extras != null) {
            isFromToAddVideo = intent.getBooleanExtra(IS_ADD_VIDEO, false)
            mSelectvideolist = intent.getParcelableArrayListExtra("ISADDED")!!
        }
        if (checkStoragePermission()) {
            videoLibraryViewModel.getVideoList(this)
        }
        videoLibraryViewModel.currentStatus.observe(this, observer)
        videoLibraryViewModel.videoLibraryAdapter.setClickListener(this)
    }

    private val observer = Observer<Any> {
        if (it is ResponseObserver.Loading)
//            if (it.isLoading) showProgress() else dismissProgress()
        else if (it is ResponseObserver.DisplayAlert) {
            dismissProgress()
//            showAlert(if (it.msgId > 0) getString(it.msgId) else it.msg)
        } else if (it is ResponseObserver.PerformAction) {
            dismissProgress()
            when (it.data) {
                is Int -> {
                    if (it.data == RESET_DATA) {
                        binding.rvVideoGallery.smoothScrollToPosition(0)

                            } }

            }
        }
    }


    private fun checkStoragePermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) && ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                prefs[PREF_STORAGE] = true
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ),
                    PERMISSION_STORAGE
                )
                return false
            } else {
                 if (!prefs.getBoolean(PREF_STORAGE, false)) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        ),
                        PERMISSION_STORAGE
                    )
                } else {
                    object : CustomAlertDialog(
                        this,
                        "",
                        String.format(
                            this.resources.getString(R.string.msg_enable_permission),
                            this.resources.getString(R.string.storage),
                            this.resources.getString(R.string.library)
                                .toLowerCase(Locale.getDefault())
                        ),
                        getString(android.R.string.ok),
                        ""
                    ) {
                        override fun onClick(id: Int) {
                            super.onClick(id)
                            openSettings()
                        }
                    }
                }
                return false
            }
        } else
            return true
    }

    fun openSettings() {
        val intentSettings = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intentSettings.data = Uri.fromParts("package", packageName, null)
        startActivityForResult(intentSettings,100)


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_STORAGE -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    videoLibraryViewModel.getVideoList(this)

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    checkStoragePermission()
                     }
                return
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100) {
            if (checkStoragePermission()) {
                videoLibraryViewModel.getVideoList(this)
            }

        }else if(resultCode==201 && data!=null){
            var slectlist :  ArrayList<VideoItem> = ArrayList()
            slectlist.addAll(data.getParcelableArrayListExtra(ARR_VIDEO)!!)
            Log.e("onActivityResult", "onActivityResult: "+slectlist )
            for (i in 0 until slectlist.size){
                for (m in 0 until videoLibraryViewModel.arrVideo.size){
                    if (slectlist[i].videoUrl.toString() == videoLibraryViewModel.arrVideo[m].toString()){
                        videoLibraryViewModel.arrVideo[m] =slectlist[i]
                        videoLibraryViewModel.arrAllVideo[m] =slectlist[i]


                    }
                }        }
//            videoLibraryViewModel.arrVideo.addAll(data.getParcelableArrayListExtra(ARR_VIDEO)!!)
        }
    }

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    override fun onItemClick(position: Int) {
        Log.e("setCounter", "videoLibraryViewModel.arrVideo[position]: "+videoLibraryViewModel.arrVideo[position] )
        videoLibraryViewModel.setCounter(videoLibraryViewModel.arrVideo[position])
        selectvideolist = videoLibraryViewModel.getSelectedItem()
        videoSelected()
    }

    override fun onItemDeleteClick(position: Int) {
       var videoItem :VideoItem? = null
        for (i in 0 until videoLibraryViewModel.arrVideo.size){
            if (videoLibraryViewModel.arrVideo[i].videoThumb ==  videoLibraryViewModel.arrSelectedVideo[position].videoThumb){
                videoItem =videoLibraryViewModel.arrVideo[i]
            }
        }
        videoLibraryViewModel.setCounter(videoItem!!)

        videoSelected()
    }

    private fun videoSelected(){
        selectvideolist = videoLibraryViewModel.getSelectedItem()
        Log.e("videoSelected", "videoSelected: "+selectvideolist )
        if (selectvideolist.size > 0){
            binding.ccBottmSheet.visibility = View.VISIBLE
            binding.tvSelectSize.text = "(" + selectvideolist.size +")"
            binding.rvSelectVideo.adapter = videoLibraryViewModel.videoSelectAdapter
            videoLibraryViewModel.videoSelectAdapter.setData(selectvideolist)
            videoLibraryViewModel.videoSelectAdapter.setDeleteClickListener(this)
            videoLibraryViewModel.videoSelectAdapter.notifyDataSetChanged()

        }else{
            binding.ccBottmSheet.visibility = View.GONE
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }


}