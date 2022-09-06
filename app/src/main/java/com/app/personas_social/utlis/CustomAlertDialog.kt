package com.app.personas_social.utlis

import android.app.Activity
import android.app.Dialog
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import com.app.personas_social.R
import com.app.personas_social.databinding.DialogAlertBinding


abstract class CustomAlertDialog(
    activity: Activity,
    mTitle: String,
    msg: String,
    positiveBtnStr: String,
    negativeBtnStr: String,
    var visible: Boolean = false
) {

    init {
        val dialog = Dialog(activity, R.style.DialogCustomTheme)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val binding: DialogAlertBinding = DataBindingUtil.inflate(
            LayoutInflater.from(activity),
            R.layout.dialog_alert,
            null,
            false
        )
        dialog.setCancelable(false)
        binding.run {
            title = mTitle
            message = msg
            positiveBtnName = positiveBtnStr
            negativeBtnName = negativeBtnStr
        }
        dialog.setContentView(binding.root)
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        binding.cardViewAlert.layoutParams.width =
            activity.getDeviceSize().second - ((activity.getDeviceSize().second * 15) / 100)
        binding.btnPositive.text = positiveBtnStr
        binding.btnNegative.text = negativeBtnStr
        binding.tvDialogTitle.text = mTitle
        binding.tvDialogMsg.text = msg

        if (visible) {
//            binding.cardViewAlert.radius = 0f
            val layoutParam = binding.cardViewAlert.layoutParams as LinearLayout.LayoutParams
            layoutParam.setMargins(
                activity.resources.getDimension(R.dimen.dimen_30dp).toInt(),
                0,
                activity.resources.getDimension(R.dimen.dimen_30dp).toInt(),
                0
            )
            binding.cardViewAlert.layoutParams = layoutParam
            val layoutParam1 = binding.tvDialogTitle.layoutParams as ConstraintLayout.LayoutParams
            layoutParam1.topMargin = activity.resources.getDimension(R.dimen.dimen_40dp).toInt()
            binding.tvDialogTitle.layoutParams = layoutParam1
        }
        if (TextUtils.isEmpty(negativeBtnStr)) {
            binding.btnNegative.visibility = View.GONE
            binding.view.visibility = View.GONE
        }

        binding.btnPositive.setOnClickListener {
            onClick(R.id.btnPositive)
            dialog.dismiss()
        }
        binding.btnNegative.setOnClickListener {
            dialog.dismiss()
            onClick(R.id.btnNegative)
        }
        dialog.setOnDismissListener {
            onDismiss()
        }
        dialog.show()
    }

    open fun onClick(id: Int) {

        /**
         * Override this method and can identify click on positive or negative button
         */
    }

    open fun onDismiss() {
        /**
         * Override this method and get dialog dismiss callback
         */
    }
}