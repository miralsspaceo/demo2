package com.app.personas_social.activity


import android.Manifest
import android.app.Dialog
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.app.personas_social.R
import com.app.personas_social.databinding.DialogDownloadVideoBinding
import com.app.personas_social.utlis.CustomAlertDialog
import com.app.personas_social.utlis.PERMISSION_READ_STORAGE
import com.app.personas_social.utlis.PreferenceHelper
import com.app.personas_social.utlis.PreferenceHelper.customPrefs
import com.app.personas_social.viewmodel.BaseViewModel
import java.util.*


abstract class BaseActivity : AppCompatActivity() {
    val prefs: SharedPreferences by lazy {
        customPrefs()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            window.statusBarColor = ContextCompat.getColor(this, android.R.color.black)
        }

    }

//    val prefs: SharedPreferences by lazy {
//        customPrefs()
//    }
    lateinit var progressDialog: Dialog
    private lateinit var noInternetDialog: Dialog
     lateinit var dialogExportVideo: Dialog

    private var mIsMultipleChoice = false



    fun showProgress() {
        if (!::progressDialog.isInitialized) {
            progressDialog = Dialog(this)
            progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            progressDialog.setContentView(R.layout.dialog_progress)
            progressDialog.window!!.setLayout(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            progressDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            progressDialog.setCancelable(false)
            if (!progressDialog.isShowing)
                progressDialog.show()
        } else if (!progressDialog.isShowing)
            progressDialog.show()
    }

    fun dismissProgress() {
        if (::progressDialog.isInitialized && progressDialog.isShowing)
            progressDialog.dismiss()
    }

    fun Any.showToast(context: Context, message: String) =
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

    protected fun showExportDialog(mViewModel: BaseViewModel, progressText: String) {
        dialogExportVideo = Dialog(this, R.style.DialogCustomTheme)
        dialogExportVideo.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val binding: DialogDownloadVideoBinding = DataBindingUtil.inflate(
            LayoutInflater.from(this),
            R.layout.dialog_download_video,
            null,
            false
        )
        binding.run {
            viewModel = mViewModel
        }
        dialogExportVideo.setContentView(binding.root)
        dialogExportVideo.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialogExportVideo.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        dialogExportVideo.setCancelable(true)
        dialogExportVideo.setCanceledOnTouchOutside(true)
//        binding.cardViewExportVideo.layoutParams.width =
//            getDeviceSize().second - ((getDeviceSize().second * 15) / 100)
        mViewModel.exportProgress.set(0)
        mViewModel.progressText.set("$progressText %")
        dialogExportVideo.show()
    }

    protected fun dismissExportDialog() {
        dialogExportVideo.dismiss()
        dialogExportVideo.cancel()
    }


    }