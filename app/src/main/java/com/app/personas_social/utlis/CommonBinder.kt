package com.app.personas_social.utlis

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.text.InputType
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import androidx.appcompat.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.*
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.tabs.TabLayout
import com.app.personas_social.R
import com.app.personas_social.adapter.BaseAdapter
import com.app.personas_social.model.VideoItem
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


object CommonBinder {

    @BindingAdapter("duration", "isShowHours")
    @JvmStatic
    fun setVideoDuration(textView: AppCompatTextView, videoDuration: Long, isShowHours: Boolean) {
        textView.text = textView.context.convertMillieToHMS(videoDuration, isShowHours)
    }


//    @BindingAdapter("noOfVideoSize")
//    @JvmStatic
//    fun setNoOfVideoSize(textView: AppCompatTextView, size: Int) {
//        textView.text =
//            textView.context.resources.getQuantityString(R.plurals.video_format, size, size)
//    }

    @BindingAdapter("viewVisibility")
    @JvmStatic
    fun setViewVisibility(view: View, isVisible: Boolean) {
        view.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    @BindingAdapter("navigationIconClick")
    @JvmStatic
    fun setNavigationIconClick(toolbar: Toolbar, isBack: Boolean) {
        toolbar.setNavigationOnClickListener {
            if (isBack)
                (toolbar.context as Activity).onBackPressed()
        }
    }

    @BindingAdapter("isSelected")
    @JvmStatic
    fun setTextSelection(textView: AppCompatTextView, isSelected: Boolean) {
        textView.isSelected = isSelected
    }

    @BindingAdapter("isSelected")
    @JvmStatic
    fun setButtonSelection(button: AppCompatButton, isSelected: Boolean) {
        button.isSelected = isSelected
    }

    @BindingAdapter("isSelected")
    @JvmStatic
    fun setButtonSelection(imageView: AppCompatImageView, isSelected: Boolean) {
        imageView.isSelected = isSelected
    }

    @BindingAdapter("adapter")
    @JvmStatic
    fun setAdapter(recyclerView: RecyclerView, baseAdapter: BaseAdapter) {
        recyclerView.adapter = baseAdapter
    }

    @BindingAdapter("loadImage")
    fun setImageSrc(imageView: AppCompatImageView, srcId: Int) {
        if (imageView.id == R.id.ivPlay) {
            when (srcId) {
                0 -> imageView.setImageResource(R.drawable.ic_play)
                1 -> imageView.setImageResource(R.drawable.ic_musicplay)
                2 -> imageView.setImageResource(R.drawable.ic_play)
                3 -> imageView.setImageResource(R.drawable.ic_musicplay)
            }
        } else
            imageView.setImageResource(srcId)
    }
    @BindingAdapter("itemSpanCount", "itemSpacingValue", "includeEdges")
    @JvmStatic
    fun setRecyclerViewItemDecorator(
        recyclerView: RecyclerView,
        spanCount: Int, spacing: Float, isIncludeEdge: Boolean
    ) {
        recyclerView.addItemDecoration(
            RecyclerViewItemDecoration(
                spanCount,
                spacing.toInt(),
                isIncludeEdge
            )
        )
    }

//    @BindingAdapter("loadImage")
//    @JvmStatic
//    fun setImageUrl(imageView: AppCompatImageView, imageUrl: String?) {
//        if (!TextUtils.isEmpty(imageUrl))
//            Glide.with(imageView.context)
//                .load(imageUrl).into(imageView)
//        else Glide.with(imageView.context).load(R.drawable.ic_launcher_background).into(imageView)
//    }
//
//    @BindingAdapter("loadImage")
//    @JvmStatic
//    fun setImageSrc(imageView: AppCompatImageView, srcId: Int) {
//        if (imageView.id == R.id.ivAudioPlayPause) {
//            when (srcId) {
//                0 -> imageView.setImageResource(R.drawable.ic_play_white)
//                1 -> imageView.setImageResource(R.drawable.ic_pause_white)
//                2 -> imageView.setImageResource(R.drawable.ic_play_white)
//                3 -> imageView.setImageResource(R.drawable.ic_pause_white)
//            }
//        } else
//            imageView.setImageResource(srcId)
//    }

//    @BindingAdapter("musicData", "viewModel")
//    @JvmStatic
//    fun setLongClick(
//        imageView: AppCompatImageButton,
//        musicData: MusicData,
//        viewModel: VideoEditViewModel
//    ) {
//        imageView.setOnLongClickListener {
//            viewModel.onMoreClickMusicItem(musicData, 2)
//            true
//        }
//    }

    @BindingAdapter(
        "isAudioEnable"
    )
    @JvmStatic
    fun setAudioEnable(
        seekBar: SeekBar, isAudioEnable: Boolean
    ) {
        seekBar.isEnabled = isAudioEnable
    }

    @BindingAdapter(
        "loadBitmap"
    )
    @JvmStatic
    fun loadVideoFrameImage(
        imageView: AppCompatImageView, imageBitmap: Bitmap
    ) {
        if (imageBitmap != null)
            Glide.with(imageView.context).load(imageBitmap).into(imageView)
    }

    @BindingAdapter(
        "onSeekBarChange"
    )
//    @JvmStatic
//    fun setListener(
//        seekBar: SeekBar, onSeekBarChange: OnSeekBarChange
//    ) {
//
//        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
//            override fun onProgressChanged(
//                seekBar: SeekBar,
//                progress: Int,
//                fromUser: Boolean
//            ) {
//                onSeekBarChange.onProgressChanged(seekBar, progress, fromUser)
//            }
//
//            override fun onStartTrackingTouch(seekBar: SeekBar) {
//                onSeekBarChange.onStartTrackingTouch(seekBar)
//            }
//
//            override fun onStopTrackingTouch(seekBar: SeekBar) {
//                onSeekBarChange.onStopTrackingTouch(seekBar)
//            }
//        })
//    }
//
//    @BindingAdapter("fullScreen")
//    @JvmStatic
//    fun setFullScreenIcon(imageView: AppCompatImageView, isFullScreen: Boolean) {
//        imageView.setImageResource(if (isFullScreen) R.drawable.ic_restore_full_screen else R.drawable.ic_full_screen)
//    }
//
//    @BindingAdapter("recordingImage")
//    @JvmStatic
//    fun setRecordingImage(imageView: AppCompatImageView, srcId: Int) {
//        when (srcId) {
//            0 -> imageView.setImageResource(R.drawable.bg_circle_red)
//            1 -> imageView.setImageResource(R.drawable.ic_pause_btn)
//            2 -> imageView.setImageResource(R.drawable.ic_play_button)
//            else -> imageView.setImageResource(R.drawable.bg_rect_red)
//        }
//    }
//
//    @BindingAdapter("recordingText")
//    @JvmStatic
//    fun setRecordingTextView(textView: AppCompatTextView, status: Int) {
//        textView.text =
//            when (status) {
//                0 -> textView.context.getString(R.string.tap_to_record)
////            1 -> textView.context.getString(R.string.tap_to_pause)
////            2 -> textView.context.getString(R.string.tap_to_resume)
//                3 -> ""
//                else -> textView.context.getString(R.string.tap_to_stop)
//            }
//    }
//

    @JvmStatic
    fun setViewInVisibility(view: View, isVisible: Boolean) {
        view.visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
    }

    @BindingAdapter("fixedSize")
    @JvmStatic
    fun setAdapter(recyclerView: RecyclerView, isFixedSize: Boolean) {
        recyclerView.setHasFixedSize(isFixedSize)
    }

    @BindingAdapter("loadImage")
    @JvmStatic
    fun setImage(imageView: AppCompatImageView, videoItem: VideoItem) {
        Log.e("setImage", "setImage: "+videoItem.videoThumb)
        if (!TextUtils.isEmpty(videoItem.videoThumb))
            Glide.with(imageView.context)
                .load(videoItem.videoThumb)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        videoItem.isVideoProper = false
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        videoItem.isVideoProper = true
                        return false
                    }
                }).into(imageView)
    }

    @BindingAdapter(
        "rvItemSpanCount",
        "rvItemSpacing",
        "rvIsIncludeEdges",
        "isLandScape",
        "topPadding",
        "rvAdapter"
    )
    @JvmStatic
    fun setRecyclerView(
        recyclerView: RecyclerView,
        spanCount: Int,
        spacing: Float,
        isIncludeEdge: Boolean,
        isLandScape: Boolean,
        topPadding: Float,
        adapter: BaseAdapter
    ) {
        if (isLandScape) {
            recyclerView.layoutManager = GridLayoutManager(recyclerView.context, 4)
            recyclerView.addItemDecoration(
                RecyclerViewItemDecoration(
                    spanCount,
                    spacing.toInt(),
                    isIncludeEdge
                )
            )
            recyclerView.setPadding(
                topPadding.toInt(),
                0,
                0,
                0
            )

        } else {
            recyclerView.layoutManager =
                LinearLayoutManager(recyclerView.context, LinearLayoutManager.HORIZONTAL, false)
        }
        recyclerView.adapter = adapter
    }

    @BindingAdapter("loadSquareImage")
    @JvmStatic
    fun loadSquareImage(imageView: ImageView, imageUrl: String?) {
        if (!TextUtils.isEmpty(imageUrl))
            Glide.with(imageView.context).load(imageUrl).placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background).into(imageView)
        else Glide.with(imageView.context).load(R.drawable.ic_launcher_background).into(imageView)
    }

    @BindingAdapter("loadImageFromUrl")
    @JvmStatic
    fun setImage(imageView: AppCompatImageView, imageUrl: String?) {
        if (!TextUtils.isEmpty(imageUrl))
            Glide.with(imageView.context).load(imageUrl).placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background).into(imageView)
    }

    @BindingAdapter("loadFeedThumb")
    @JvmStatic
    fun setFeedImage(imageView: AppCompatImageView, imageUrl: String?) {
        if (!TextUtils.isEmpty(imageUrl))
            Glide.with(imageView.context).load(imageUrl).placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background).into(imageView)
    }

//    @BindingAdapter("firstName", "lastName")
//    @JvmStatic
//    fun setName(textView: AppCompatTextView, firstName: String?, lastName: String?) {
//        textView.text = String.format(
//            textView.context.getString(R.string.username),
//            firstName ?: "", lastName ?: ""
//        )
//    }

//    @BindingAdapter("viewCount")
//    @JvmStatic
//    fun setViewCount(textView: AppCompatTextView, num: Int) {
//        textView.text = String.format(textView.context.getString(R.string.views), num)
//    }

//    @BindingAdapter("videoTime", "videoTimeUnit")
//    @JvmStatic
//    fun setTime(textView: AppCompatTextView, time: String?, videoTimeUnit: String?) {
//        textView.text =
//            String.format(textView.context.getString(R.string.time_unit), time, videoTimeUnit)
//
//        /*if (!TextUtils.isEmpty(time)) {
//            var sec = ""
//            var min = ""
//            var hrs = ""
//            var timeStr = ""
//            if (time!!.split(":").size == 3) {
//                hrs = time.split(":")[0]
//                min = time.split(":")[1]
//                sec = time.split(":")[2]
//                timeStr =
//                    if (hrs != "00") textView.context.getString(R.string.hours) else if (min != "00") textView.context.getString(
//                        R.string.minutes
//                    ) else textView.context.getString(R.string.secs)
//            }
//            textView.text =
//                String.format(
//                    textView.context.getString(R.string.time_in_minutes),
//                    hrs,
//                    min,
//                    sec,
//                    timeStr
//                )
//        }*/
//
//        /*textView.text = String.format(
//        val seconds = if (isFromOverview) time else time * 60 * 1000
//        textView.context.getString(R.string.video_time),
//        TimeUnit.MILLISECONDS.toMinutes(seconds.toLong()) - TimeUnit.HOURS.toMinutes(
//            TimeUnit.MILLISECONDS.toHours(seconds.toLong())
//        ),
//        TimeUnit.MILLISECONDS.toSeconds(seconds.toLong()) - TimeUnit.MINUTES.toSeconds(
//            TimeUnit.MILLISECONDS.toMinutes(seconds.toLong())
//        )
//        )*/
//    }

    @BindingAdapter("viewPager")
    @JvmStatic
    fun setViewPager(tabLayout: TabLayout, viewPager: ViewPager) {
        tabLayout.setupWithViewPager(viewPager)
    }

    @BindingAdapter("viewPager", "tabIcon")
    @JvmStatic
    fun setViewPager(tabLayout: TabLayout, viewPager: ViewPager, arrIcons: ArrayList<Int>? = null) {
        tabLayout.setupWithViewPager(viewPager)
        if (tabLayout.tabCount > 0 && arrIcons != null) {
            for ((i, icon) in arrIcons.withIndex()) {
                tabLayout.getTabAt(i)!!.icon = ContextCompat.getDrawable(tabLayout.context, icon)
            }
        }
    }

    @BindingAdapter(value = ["fromChangeProfile", "selected"])
    @JvmStatic
    fun setItemSelection(
        imageView: AppCompatImageView,
        isFromChangeProfile: Boolean,
        isSelected: Boolean
    ) {
        setViewVisibility(imageView, if (isFromChangeProfile) false else isSelected)
    }

    @BindingAdapter(value = ["fromFavouriteCategory", "count", "selected"])
    @JvmStatic
    fun setCountVisibility(
        textView: AppCompatTextView,
        isFromChangeProfile: Boolean,
        count: Int,
        isSelected: Boolean
    ) {
        setViewVisibility(textView, isFromChangeProfile && isSelected && count > 0)
    }

    @BindingAdapter(value = ["setSelector"])
    @JvmStatic
    fun setItemSelection(imageView: AppCompatImageView, count: Int) {
        imageView.isSelected = count > 0
    }

//    @BindingAdapter(value = ["playPause"])
//    @JvmStatic
//    fun setPlayPauseIcon(imageView: AppCompatImageView, isPlaying: Boolean) {
//        imageView.setImageResource(if (isPlaying) R.drawable.ic_pause_btn else R.drawable.ic_play_button)
//    }

    @BindingAdapter(value = ["description"])
    @JvmStatic
    fun setDescription(textView: AppCompatTextView, text: String?) {
        if (!TextUtils.isEmpty(text))
            textView.text = text!!.replace("\n", " ")
    }

    @BindingAdapter(value = ["fieldType"])
    @JvmStatic
    fun setFieldInputType(editText: AppCompatEditText, text: String?) {
        editText.inputType =
            if (text == "Email") InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS else InputType.TYPE_CLASS_TEXT
    }

//    @BindingAdapter(value = ["downArrow"])
//    @JvmStatic
//    fun setDownArrow(imageView: AppCompatImageView, isDownArrow: Boolean) {
//        if (isDownArrow) {
//            imageView.setImageDrawable(
//                ContextCompat.getDrawable(
//                    imageView.context,
//                    R.drawable.ic_arrow_top
//                )
//            )
//            imageView.setColorFilter(
//                ContextCompat.getColor(
//                    imageView.context,
//                    R.color.colorBlue9
//                ), android.graphics.PorterDuff.Mode.SRC_IN
//            )
//        } else {
//            imageView.setColorFilter(
//                ContextCompat.getColor(
//                    imageView.context,
//                    R.color.colorBlue9
//                ), android.graphics.PorterDuff.Mode.SRC_IN
//            )
//            imageView.setImageDrawable(
//                ContextCompat.getDrawable(
//                    imageView.context,
//                    R.drawable.ic_arrow_bottom
//                )
//            )
//        }
//    }
//
    @BindingAdapter(value = ["time"])
    @JvmStatic
    fun setTime(textView: AppCompatTextView, createdTime: String?) {
        if (!TextUtils.isEmpty(createdTime)) {
            textView.text = covertTimeToText(createdTime!!)
        }
    }

    private fun covertTimeToText(date: String): String {
        var time = ""
        val suffix = "Ago"
        try {
            val pastTime = Date(date.toLong() * 1000)
            val nowTime = Date()
            val dateDiff = nowTime.time - pastTime.time
            var second: Long = TimeUnit.MILLISECONDS.toSeconds(dateDiff)
            val minute: Long = TimeUnit.MILLISECONDS.toMinutes(dateDiff)
            val hour: Long = TimeUnit.MILLISECONDS.toHours(dateDiff)
            val day: Long = TimeUnit.MILLISECONDS.toDays(dateDiff)
            when {
                second < 60 -> {
                    if (second < 0) second = 0
                    time = "$second Seconds $suffix"
                }
                minute < 60 -> {
                    time = "$minute Minutes $suffix"
                }
                hour < 24 -> {
                    time = "$hour Hours $suffix"
                }
                day >= 7 -> {
                    time = when {
                        day > 360 -> {
                            (day / 360).toString() + " Years " + suffix
                        }
                        day > 30 -> {
                            (day / 30).toString() + " Months " + suffix
                        }
                        else -> {
                            (day / 7).toString() + " Week " + suffix
                        }
                    }
                }
                day < 7 -> {
                    time = "$day Days $suffix"
                }
            }
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return time
    }

//    @BindingAdapter(value = ["subscribe"])
//    @JvmStatic
//    fun setLikeCommentItemBg(button: AppCompatButton, subscribe: Int) {
//        button.text =
//            button.context.getString(if (subscribe == 1) R.string.subscribed else R.string.subscribe)
//        button.isSelected = subscribe == 1
//    }

//    @BindingAdapter(value = ["subscribe", "otherUserId", "loggedInUserId"])
//    @JvmStatic
//    fun setEditSubscribeButton(
//        button: AppCompatButton,
//        subscribe: Int,
//        otherUserId: Int,
//        loggedInUserId: Int
//    ) {
//        if (otherUserId == loggedInUserId) {
//            button.text = button.context.getString(R.string.edit)
//            button.isSelected = true
//        } else {
//            button.text =
//                button.context.getString(if (subscribe == 1) R.string.subscribed else R.string.subscribe)
//            button.isSelected = subscribe == 1
//        }
//    }

//    @BindingAdapter(value = ["whiteBackground"])
//    @JvmStatic
//    fun setLikeCommentItemBg(imageView: AppCompatImageView, position: Int) {
//        imageView.setImageResource(if (position == -1) R.drawable.bg_white_rect3 else if (position == -2) R.drawable.bg_white_rect1 else if (position == -3) R.drawable.bg_white_rect2 else R.drawable.bg_white_rect)
//    }

    @BindingAdapter("itemDecoration")
    @JvmStatic
    fun setItemDecoration(recyclerView: RecyclerView, isVertical: Boolean) {
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                recyclerView.context,
                if (isVertical) LinearLayoutManager.VERTICAL else LinearLayoutManager.HORIZONTAL
            )
        )
    }

    @BindingAdapter("formatDate")
    @JvmStatic
    fun displayDateInFormat(editText: AppCompatEditText, date: String?) {
        if (!TextUtils.isEmpty(date)) {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            editText.setText(formatToDisplayDate().format(sdf.parse(date!!)!!))
        }
    }

//    fun displayDifficultyLevel(imageView: AppCompatImageView, isSelected: Boolean, level: Int = 1) {
//        if (isSelected) {
//            imageView.background =
//                ContextCompat.getDrawable(imageView.context, R.drawable.circle_blue)
//            imageView.backgroundTintList = null
//            imageView.alpha = 1f
//        } else {
//            imageView.background =
//                ContextCompat.getDrawable(imageView.context, R.drawable.white_circle)
//            imageView.backgroundTintList =
//                ContextCompat.getColorStateList(imageView.context, R.color.colorPitch)
//            imageView.alpha = when (level) {
//                1 -> 0.20f
//                2 -> 0.40f
//                3 -> 0.60f
//                4 -> 0.80f
//                else -> 1f
//            }
//        }
//    }

    @BindingAdapter("verticalBias")
    @JvmStatic
    fun setVerticalBiasOfConstraint(constraintLayout: ConstraintLayout, isInVertical: Boolean) {
        val params = constraintLayout.layoutParams as ConstraintLayout.LayoutParams
        if (isInVertical) {
            params.verticalBias = 0f
            constraintLayout.layoutParams = params
        }
    }

//    @BindingAdapter("tabImage")
//    @JvmStatic
//    fun setTabImage(imageView: AppCompatImageView, tabType: Int) {
//        imageView.setImageResource(
//            when (tabType) {
//                TAB_TEXT -> R.drawable.ic_text_tab
//                TAB_PRODUCT -> R.drawable.ic_product_tab
//                TAB_FORM -> R.drawable.ic_form_tab
//                else -> R.drawable.ic_text_tab
//            }
//        )
//    }

//    @BindingAdapter("addRemoveText", "fromCreationSpace")
//    @JvmStatic
//    fun setAddRemoveText(
//        textView: AppCompatTextView,
//        isSelected: Boolean,
//        isFromCreationSpace: Boolean
//    ) {
//        textView.text =
//            if (isFromCreationSpace) textView.context.getString(R.string.statistical)
//            else {
//                if (isSelected) textView.context.getString(R.string.remove) else textView.context.getString(
//                    R.string.add
//                )
//            }
//    }
//
//    @BindingAdapter("fromCreationSpace")
//    @JvmStatic
//    fun setPreviewEditText(
//        textView: AppCompatTextView,
//        isFromCreationSpace: Boolean
//    ) {
//        textView.text =
//            if (isFromCreationSpace) textView.context.getString(R.string.edit) else textView.context.getString(
//                R.string.preview
//            )
//    }

//    @BindingAdapter("forBuyProduct")
//    @JvmStatic
//    fun setBuyOkText(
//        button: AppCompatButton,
//        isForBuyProduct: Boolean
//    ) {
//        button.text =
//            if (isForBuyProduct) button.context.getString(R.string.title_buy)
//            else button.context.getString(android.R.string.ok)
//    }
//
//    @BindingAdapter("imagePadding")
//    @JvmStatic
//    fun setEditableStepTitle(imageView: AppCompatImageView, isImageSet: Boolean) {
//        if (isImageSet)
//            imageView.setPadding(0, 0, 0, 0)
//        else
//            imageView.setPadding(
//                imageView.context.resources.getDimension(R.dimen.dimen_10dp).toInt(),
//                imageView.context.resources.getDimension(R.dimen.dimen_10dp).toInt(),
//                imageView.context.resources.getDimension(R.dimen.dimen_10dp).toInt(),
//                imageView.context.resources.getDimension(R.dimen.dimen_10dp).toInt()
//            )
//    }

    @BindingAdapter("snapHelper")
    @JvmStatic
    fun setSnapHelper(recyclerView: RecyclerView, isAttach: Boolean) {
        if (isAttach) {
            val snapHelper = PagerSnapHelper()
            snapHelper.attachToRecyclerView(recyclerView)
        }
    }
}