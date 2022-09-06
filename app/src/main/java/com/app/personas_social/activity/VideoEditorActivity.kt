package com.app.personas_social.activity


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.ContextWrapper
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.media.AudioAttributes
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.text.Layout
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.SeekBar
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.personas_social.R
import com.app.personas_social.adapter.*
import com.app.personas_social.app.ResponseObserver
import com.app.personas_social.databinding.*
import com.app.personas_social.interfaces.OnItemSelected
import com.app.personas_social.model.*
import com.app.personas_social.stickerview.*
import com.app.personas_social.stickerview.callbacks.TextEditorOperation
import com.app.personas_social.stickerview.fragment.TextEditingFragment
import com.app.personas_social.utlis.*
import com.app.personas_social.utlis.PreferenceHelper.set
import com.app.personas_social.videoEditing.VideoEditor
import com.app.personas_social.videoEditing.VideoHelper.getBasicAnimationList
import com.app.personas_social.videoEditing.VideoHelper.getBasicPinkAnimationList
import com.app.personas_social.videoEditing.VideoHelper.getBorderList
import com.app.personas_social.videoEditing.VideoHelper.getColorList
import com.app.personas_social.videoEditing.VideoHelper.getFontList
import com.app.personas_social.videoEditing.VideoHelper.getGradientList
import com.app.personas_social.videoEditing.VideoHelper.getInvertList
import com.app.personas_social.videoEditing.VideoHelper.getPatternList
import com.app.personas_social.videoEditing.VideoHelper.getloopAnimationList
import com.app.personas_social.videoEditing.VideoHelper.getpinkloopAnimationList
import com.app.personas_social.viewmodel.VideoEditViewModel
import com.app.personas_social.viewmodel.VideoListViewModel
import com.audiorecorder.RecorderHelper
import com.audiowaveforms.AudioWaveSeekView
import com.github.antonpopoff.colorwheel.gradientseekbar.setBlackToColor
import com.github.antonpopoff.colorwheel.gradientseekbar.setTransparentToColor
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ClippingMediaSource
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.*
import com.google.gson.Gson
import com.howto.interfaces.OnItemClick
import com.videofilter.*
import com.videofilter.FilterType.DEFAULT
import com.videofilter.FilterType.VIGNETTE
import com.videofilter.composer.Mp4Composer
import com.videoframeview.VideoFrameCutterHelper
import com.videoframeview.VideoFrameHelper
import com.videoseekbar.OnSeekChangeListener
import com.videoseekbar.VideoSeekBar
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.*
import java.io.File
import java.lang.Runnable
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.*


class VideoEditorActivity : BaseActivity(), OnItemClick, TextEditorOperation, OnItemSelected {
    private lateinit var binding: ActivityVideoEditorBinding
    private val videoLibraryViewModel: VideoListViewModel by lazy {
        VideoListViewModel()
    }
    lateinit var database: DatabaseReference

    var filterPosition = 0

    private val glPlayer by lazy { GlPlayerView(this) }
    private var SEGMENT_VIDEO = 100
    private var arrVideo = ArrayList<VideoItem>()
    private var arrVideoEditBtn = ArrayList<VideoEditBtn>()
    private var arrVideTextBtn = ArrayList<VideoEditBtn>()
    private var arrMusicEffect = ArrayList<VideoEditBtn>()
    private var arrMusicEdit = ArrayList<VideoEditBtn>()
    private var musicEffect = ArrayList<MusicData>()
    private var effectsList = ArrayList<ListOfMusic>()
    private var arrFilterList = ArrayList<FilterData>()
    private lateinit var filterAdapter: FilterListAdapter
    private lateinit var videoEditBtnAdapter: VideoEditBtnAdapter
    private lateinit var videoMusicBtnAdapter: VideoMusicBtnAdapter
    private lateinit var videotextBtnAdapter: VideoTextEditAdapter
    private var selectedFilterType = DEFAULT
    private var isUndoRedo = false
    private lateinit var countDownTimer: CountDownTimer
    private var isMediaPlayerPrepared = false
    private var isAudioSeeking = false
    private var isSeekToPos = false
    private var isSingleToAll = false
    private var removeSticker = false
    private var videoLength: Long = 0
    private var isFromMusic = false
    private var isAdded = false
    private var isEdit = false
    private lateinit var strTextSticker: TextSticker
    private var isOutSizeofStickerTouch = false
    private var isTextEdit = false
    private var widthText = 0
    private var heightText = 0
    private var colorID = 0
    private var colorOffset = 0f
    private var borderID = 0
    private var musicPos = -1
    private var recordPos = -1
    private var isMusicFrom = false
    private var frVideoHeight = 0
    private var frVideoWidth = 0
    private var borderType = ""
    private var borderColor = 0
    private var borderOffset = 0f
    private lateinit var musicSelectionDialog: BottomSheetDialog
    private lateinit var effectDialog: BottomSheetDialog
    private lateinit var bindingMusic: BottomSheetAddMusicBinding
    private lateinit var bindingEffects: BottomSheetEffectsBinding
    private lateinit var bindingVolume: BottomVolumeDialogBinding
    private lateinit var volumeDialog: BottomSheetDialog
    private lateinit var discardDialog : Dialog
    private lateinit var saveDialog : Dialog
    private val durationList = mutableListOf<Duration>()
    private val undolist : ArrayList<ChangeData> = ArrayList()
    private val undoredoList : ArrayList<ChangeData2> = ArrayList()
    private val redolist : ArrayList<ChangeData> = ArrayList()
    private val undoRedoPointer = ObservableField(-1)
    val indicatorColor = ObservableInt()
    var undoClick = 0
    var redoClick = 0

    private var disposable: Disposable? = null
    private val TAG = javaClass.simpleName


    private var isFirstTimeLoadMusic = false

    private var isFrameSizeSet = false

    private var isChangingFromMusic = false
    private var isTimerStarted = false
    private var isFromAudio = ""
    private var selectedFunction = -1
    private var isChangeInTrim = false
    private var tempVideoVolume = 100
    private var tempAudioVolume = 100
    private var tempSplitDuration = 0L
    private var isNewVideoAdded = false

    private var currentAudioPosition = 0
    private var verticalVideoCount = 0
    private var horizontalVideoCount = 0

    private var isAudioSeekPositionSet = false
    private var currentPosition = 0
    private var tempCurrentPosition = 0
    private var isChangingFromText = false
    private var isChangingFromTrim = false
    private var isBackWardForward = false
    private var isSeeking = false
    private var tempSpeed = 50f
    private var isSplitInitialize = false
    private var isLeftRightTrim = false
    private var totalVideoDuration = 0L
    private var updatedDuration = 0L
    private var tempStartTime = 0L
    private var tempVideoHeight = 0
    private var tempVideoWidth = 0
    private var tempEndTime = 0L
    private var musicStartTime = 0L
    private var musicEndTime = 0L
    private var videoUri: Uri? = null
    private var invertCount = -1
    private var frameWidth = 0
    private var frameHeight = 0
    private var framesToLoad = 0
    private var frameTimeOffset: Int = 0
    private var currentTask: AsyncTask<Int?, Int?, Bitmap?>? = null
    private var numFrame = 0
    private val frames = ArrayList<Frames>()
    private lateinit var recorderHelper: RecorderHelper
    private var layoutManager: LinearLayoutManager? = null
    private var layoutManager2: LinearLayoutManager? = null
    private var layoutManagerline: LinearLayoutManager? = null
    private var layoutManagerText: LinearLayoutManager? = null
    private var currentFrame = 0
    private var isTouchFrame = false
    private lateinit var job: Job
    private var invertPos = 0

    private var handlerSeekBar = Handler(Looper.getMainLooper())
    private val concatenatingMediaSource = ConcatenatingMediaSource()
    private lateinit var videoFrameHelpercuttor: VideoFrameCutterHelper
    private lateinit var patternAdapter : PatternAdapter
    private lateinit var colorAdapter : ColorsAdapter
    private lateinit var gradientAdapter: GradientAdapter
    private lateinit var videoFrame: VideoFrameHelper
    private lateinit var triple: Triple<Long, Long, Int>
    private var colorList = ArrayList<ColorItem>()
    private var gradientList = ArrayList<GradientItem>()
    private var patternList = ArrayList<PatternItem>()
    private var borderlist = ArrayList<BorderItem>()
    private var invertList = ArrayList<ColorItem>()
    private var basiclist = ArrayList<AnimationItem>()
    private var basicpinklist = ArrayList<AnimationItem>()
    private var looppinklist = ArrayList<AnimationItem>()
    private var looplist = ArrayList<AnimationItem>()
    private var fontlist = ArrayList<FontBean>()
    private lateinit var mp4Composer: Mp4Composer
    private var outputPath = ""
    private var isPlay = true
    private var isDataSet =""
    private var mediaMetadataRetriever: MediaMetadataRetriever? = null
    private var mPosition = -1
    private var itemPosition = -1
    var colorType = false

    var textStickerList: ArrayList<CopyModel> = ArrayList()
    var textlineList: ArrayList<SpanData> = ArrayList()
    var textfinallineList: ArrayList<CopyModel> = ArrayList()
    var musicLineList: ArrayList<MusicData> = ArrayList()
    var list: ArrayList<SpanData> = ArrayList()
    var itemWidth = 0
    var filterpos = 0

    private val videoEditViewModel: VideoEditViewModel by lazy {
        VideoEditViewModel()
    }

    private val player: ExoPlayer by lazy {
        ExoPlayer.Builder(this).build().also {
            it.repeatMode = ExoPlayer.REPEAT_MODE_OFF
//            it.repeatMode = ExoPlayer.REPEAT_MODE_ALL
            it.setHandleAudioBecomingNoisy(true)
            binding.playerView.player = it

        }
    }

    private val dataSourceFactory: DataSource.Factory by lazy {
        DefaultDataSourceFactory(this, "VideoEditing")

    }

    private val mediaPlayer: MediaPlayer by lazy {
        MediaPlayer().apply {
            AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        }
    }

    companion object {
        fun startActivity(
            activity: Activity,
            arrVideo: ArrayList<VideoItem>,
            isVdeoAddeed: Boolean,requestCode: Int?=null
        ) {
            val intent = Intent(activity, VideoEditorActivity::class.java)
            intent.putParcelableArrayListExtra(ARR_VIDEO, arrVideo)
            intent.putExtra(IS_ADD_VIDEO, isVdeoAddeed)
            activity.startActivityForResult(intent, REQUEST_PROJECT)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = FirebaseDatabase.getInstance("https://personas-music.firebaseio.com/").reference
        bindUI()
        initUI()
        binding.llAddTextView.typetext = ObservableInt(1)
        binding.llAddTextView.llAnimation.typetext = ObservableInt(1)
        binding.llAddTextView.llcolor.type = ObservableInt(1)
        strTextSticker = TextSticker(this)

    }

    private fun bindUI() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_video_editor)
        videoEditViewModel.currentStatus.observe(this, observer)

    }

    private fun initUI() {
        arrVideo = intent.getParcelableArrayListExtra(ARR_VIDEO)!!
        isAdded = intent.getBooleanExtra(IS_ADD_VIDEO, false)
        undoRedoPointer.set(-1)
        setData()
        setPlayer()
        setStickerViewListener()
        letterSizeSeekBar()
        letterSpacingSeekBar()
        lineSpacingSeekBar()
        getFirebaseEffectsData()

        colorList = getColorList()
        gradientList = getGradientList()
        patternList = getPatternList()
        invertList = getInvertList()
        basiclist = getBasicAnimationList()
        basicpinklist = getBasicPinkAnimationList()
        looppinklist = getpinkloopAnimationList()
        looplist = getloopAnimationList()
        fontlist = getFontList(this)
        borderlist = getBorderList()
        initRecycler()

        binding.frVideoController.post {
            frVideoWidth = binding.frVideo.width
            frVideoHeight = binding.frVideo.height
            setFilterView()
            binding.ivContains.layoutParams.width = binding.playerView.width
            binding.ivContains.layoutParams.height = binding.playerView.height


        }

        val arr: IntArray = intArrayOf(1, 2, 3, 23, 12, 34, 56, 3, 13, 56, 34, 3, 5)
        binding.audioWave.sample = arr
        binding.audioWave.waveGap = 2.1f
        binding.audioWave.waveWidth = 3.1499999f
        binding.audioWave.addSpan(10, 600, "Thunder", 0)



        undoRedoPointer.addOnPropertyChangedCallback(object :
            androidx.databinding.Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: androidx.databinding.Observable?, propertyId: Int) {
                if (  undoRedoPointer.get()!! >= 0 && undoRedoPointer.get()!! < undoredoList.size) {
                    binding.imgUndo.setImageDrawable( getDrawable(R.drawable.ic_undoblack))
                } else {
                    binding.imgUndo.setImageDrawable( getDrawable(R.drawable.ic_undo))

                }
                if (undoRedoPointer.get()!! < undoredoList.size - 1 && undoRedoPointer.get()!! >= -1  ) {
                    binding.imgRedo.setImageDrawable( getDrawable(R.drawable.ic_redoblack))

                } else {
                    binding.imgRedo.setImageDrawable( getDrawable(R.drawable.ic_redo))
                }
            }

        })
    }

    private fun addToChangeList(type:Int,redotype:Int,data: UndoRedoData2?) {
        Log.e(TAG, "addToChangeList: "+undoRedoPointer.get() )
        if (undoredoList.size == 2){
            undoredoList.removeAt(0)
            undoRedoPointer.set(undoRedoPointer.get()!! - 1)
        }
          if (null != data) {
                undoredoList.add(
                    ChangeData2(
                        type = type,
                        redotype = redotype,
                        data = data
                    )
                )
                undoRedoPointer.set(undoRedoPointer.get()!! + 1)

        }
        Log.e(TAG, "addToChangeList: "+undoredoList )


    }

    private fun initRecycler() {
        colorAdapter()
        patternAdapter()
        gradientAdapter()

        // list for animation for basic
        binding.llAddTextView.llAnimation.rvBasic.layoutManager =
            GridLayoutManager(this, 5)
        binding.llAddTextView.llAnimation.rvBasic.adapter =
            AnimationAdapter(this, basiclist, basicpinklist) {

//                binding.stickerView.showBorder = false
//                binding.stickerView.showIcons = false

                if (it == 0) {
//                    val animFadein: Animation = AnimationUtils.loadAnimation(
//                        applicationContext, R.anim.push_left_in
//
//                    )
//                    binding.stickerView.startAnimation(animFadein)

                    val sticker = binding.stickerView.currentSticker as TextSticker
                    val oldCenter = PointF(
                        (sticker.currentWidth / 2 + sticker.matrix.getTranslateX()),
                        (sticker.currentHeight / 2 + sticker.matrix.getTranslateY())
                    )
                    notifyStickerView()
                    val newCenter = PointF(
                        (sticker.currentWidth / 2 + sticker.matrix.getTranslateX()),
                        (sticker.currentHeight / 2 + sticker.matrix.getTranslateY())
                    )

                    Log.e(TAG, "initRecycler: oldcenter $oldCenter newCenter $newCenter")
                    val diffX = newCenter.x - oldCenter.x
                    val diffY = newCenter.y - oldCenter.y
                    Log.e(TAG, "initRecycler: diffX $diffX diffY $diffY")

                    val fontMatrix = Matrix()
                    fontMatrix.postScale(sticker.currentScale, sticker.currentScale)
                    fontMatrix.postRotate(sticker.currentAngle)
                    fontMatrix.postTranslate(
                        sticker.matrix.getTranslateX() - diffX,
                        sticker.matrix.getTranslateY() - diffY
                    )

                    val px: Float =
                        (sticker.currentWidth / 2 + sticker.matrix.getTranslateX())
                    val py: Float =
                        (sticker.currentHeight / 2 + sticker.matrix.getTranslateY())

                    val newAnimateMatrix = Matrix()
                    newAnimateMatrix.setTranslate(
                        sticker.matrix.getTranslateX() - diffX,
                        sticker.matrix.getTranslateY() - diffY
                    )
                    val scale = sticker.currentScale
                    Log.e(TAG, "initRecycler: " + scale)
                    newAnimateMatrix.postScale(
                        sticker.currentScale * 1.2f,
                        sticker.currentScale * 1.2f
                    )
                    newAnimateMatrix.postRotate(sticker.currentAngle)
                    sticker.matrix.animateToMatrixText(
                        newAnimateMatrix,
                        px,
                        py,
                        sticker.width.toFloat(),
                        sticker.height.toFloat(),
                        diffX,
                        diffY,
                        Pair(newAnimateMatrix.getScaleX(), newAnimateMatrix.getScaleX() * 0.8f),
                        loopDuration = 2000f,
                        onUpdate = {
                            notifyStickerView()
                        },
                        onEnd = {

                            sticker.matrix.animateToMatrixText(
                                newAnimateMatrix,
                                px,
                                py,
                                sticker.width.toFloat(),
                                sticker.height.toFloat(),
                                diffX,
                                diffY,
                                Pair(sticker.currentScale, scale),
                                loopDuration = 2000f, onUpdate = {
                                    notifyStickerView()
                                },
                                onEnd = {
                                    notifyStickerView()

                                }
                            )
                        }
                    )


                } else if (it == 1) {
                    val animFadein: Animation = AnimationUtils.loadAnimation(
                        applicationContext, R.anim.push_up_in

                    )
                    binding.stickerView.startAnimation(animFadein)
                    notifyStickerView()


                } else if (it == 2) {
                    val animFadein: Animation = AnimationUtils.loadAnimation(
                        applicationContext, R.anim.push_right_in

                    )
                    binding.stickerView.startAnimation(animFadein)
                } else if (it == 3) {
                    val animFadein: Animation = AnimationUtils.loadAnimation(
                        applicationContext, R.anim.push_down_in

                    )
                    binding.stickerView.startAnimation(animFadein)
                } else {
                    val animFadein: Animation = AnimationUtils.loadAnimation(
                        applicationContext, R.anim.push_right_in

                    )
                    binding.stickerView.startAnimation(animFadein)
                }


            }

        // list for animation for loop
        binding.llAddTextView.llAnimation.rvLoop.layoutManager =
            GridLayoutManager(this, 5)
        binding.llAddTextView.llAnimation.rvLoop.adapter =
            AnimationAdapter(this, looplist, looppinklist) {

            }

        // list for animation for invert
        binding.llAddTextView.llcolor.rvInvert.layoutManager = GridLayoutManager(this, 7)
        binding.llAddTextView.llcolor.rvInvert.adapter = ColorsAdapter(this, invertList) {

        }

        // list for font
        binding.llAddTextView.llFont.rvFont.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.llAddTextView.llFont.rvFont.adapter = FontAdapter(this, fontlist) {
            if (binding.stickerView.currentSticker != null) {
                (binding.stickerView.currentSticker as TextSticker).setTypeface(fontlist[it].font)
                (binding.stickerView.currentSticker as TextSticker).resizeText()
                notifyStickerView()
                if (binding.stickerView.currentSticker != null) {
                    var textData = TextStickerData((binding.stickerView.currentSticker as TextSticker).currentAngle,
                        (binding.stickerView.currentSticker as TextSticker).currentScale,
                        (binding.stickerView.currentSticker as TextSticker).x,
                        (binding.stickerView.currentSticker as TextSticker).y,
                        (binding.stickerView.currentSticker as TextSticker).startTime,
                        (binding.stickerView.currentSticker as TextSticker).endTime,
                        (binding.stickerView.currentSticker as TextSticker).getstrtag(),
                        fontlist[it].name,
                        fontlist[it].font,
                        (binding.stickerView.currentSticker as TextSticker).alignment().toString(),
                        (binding.stickerView.currentSticker as TextSticker).size,
                        (binding.stickerView.currentSticker as TextSticker).text.toString(),
                        (binding.stickerView.currentSticker as TextSticker).borderColor.toInt(),
                        (binding.stickerView.currentSticker as TextSticker).borderType.toString(),
                        false,
                        false,
                        (binding.stickerView.currentSticker as TextSticker).color,
                        (binding.stickerView.currentSticker as TextSticker).color,
                        (binding.stickerView.currentSticker as TextSticker).pattern,
                        (binding.stickerView.currentSticker as TextSticker).gradient,
                        (binding.stickerView.currentSticker as TextSticker).matrix
                    )

                    val data = UndoRedoData2(
                    binding.stickerView.currentSticker as TextSticker,
                    copySticker(binding.stickerView.currentSticker as TextSticker, ""),
                    textData
                )
                addToChangeList(TEXT_ADD, ACTION_FONT,data)
                }
            }
        }

        // list for border
        binding.llAddTextView.llcolor.rvborder.layoutManager = GridLayoutManager(this, 6)
        binding.llAddTextView.llcolor.rvborder.adapter = BorderAdapter(this, borderlist) {
            if (it == 3) {
                colorBorderPickerDialog()
            } else {
                if (binding.stickerView.currentSticker != null) {
                    borderType = borderlist[it].type
                    borderColor = borderlist[it].colorId
                    (binding.stickerView.currentSticker as TextSticker).setPaintToOutline(
                        ContextCompat.getColor(
                            this,
                            borderlist[it].colorId
                        ), borderlist[it].type
                    )
                    notifyStickerView()
                }
            }
            if (binding.stickerView.currentSticker != null) {
                var textData = TextStickerData((binding.stickerView.currentSticker as TextSticker).currentAngle,
                    (binding.stickerView.currentSticker as TextSticker).currentScale,
                    (binding.stickerView.currentSticker as TextSticker).x,
                    (binding.stickerView.currentSticker as TextSticker).y,
                    (binding.stickerView.currentSticker as TextSticker).startTime,
                    (binding.stickerView.currentSticker as TextSticker).endTime,
                    (binding.stickerView.currentSticker as TextSticker).getstrtag(),
                    (binding.stickerView.currentSticker as TextSticker).fontStyle,
                    (binding.stickerView.currentSticker as TextSticker).typeface,
                    (binding.stickerView.currentSticker as TextSticker).alignment().toString(),
                    (binding.stickerView.currentSticker as TextSticker).size,
                    (binding.stickerView.currentSticker as TextSticker).text.toString(),
                    borderColor,
                    borderType,
                    false,
                    false,
                    (binding.stickerView.currentSticker as TextSticker).color,
                    (binding.stickerView.currentSticker as TextSticker).color,
                    (binding.stickerView.currentSticker as TextSticker).pattern,
                    (binding.stickerView.currentSticker as TextSticker).gradient,
                    (binding.stickerView.currentSticker as TextSticker).matrix
                )

                val data = UndoRedoData2(
                binding.stickerView.currentSticker as TextSticker,
                copySticker(binding.stickerView.currentSticker as TextSticker, ""),
                textData
            )
            addToChangeList(TEXT_ADD, ACTION_BORDER,data)}

        }


    }

    private fun patternAdapter(){
        patternList.clear()
        patternList = getPatternList()
        // list for animation for pattern
        binding.llAddTextView.llcolor.rvPattern.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        patternAdapter = PatternAdapter(this, patternList) {
            colorAdapter()
            gradientAdapter()
            if (it == 0) {
                if (binding.stickerView.currentSticker != null) {
                    (binding.stickerView.currentSticker as TextSticker).setTextColor(
                        ContextCompat.getColor(
                            this,
                            gradientList[it].color1
                        )
                    )
                    notifyStickerView()
                }
            } else {
                if (binding.stickerView.currentSticker != null) {
                    (binding.stickerView.currentSticker as TextSticker).pattern = patternList[it].drawable
                    notifyStickerView()
                }
            }
            if (binding.stickerView.currentSticker != null) {
                var textData = TextStickerData((binding.stickerView.currentSticker as TextSticker).currentAngle,
                    (binding.stickerView.currentSticker as TextSticker).currentScale,
                    (binding.stickerView.currentSticker as TextSticker).x,
                    (binding.stickerView.currentSticker as TextSticker).y,
                    (binding.stickerView.currentSticker as TextSticker).startTime,
                    (binding.stickerView.currentSticker as TextSticker).endTime,
                    (binding.stickerView.currentSticker as TextSticker).getstrtag(),
                    (binding.stickerView.currentSticker as TextSticker).fontStyle,
                    (binding.stickerView.currentSticker as TextSticker).typeface,
                    (binding.stickerView.currentSticker as TextSticker).alignment().toString(),
                    (binding.stickerView.currentSticker as TextSticker).size,
                    (binding.stickerView.currentSticker as TextSticker).text.toString(),
                    borderColor,
                    borderType,
                    false,
                    false,
                    gradientList[it].color1,
                    gradientList[it].color1,
                    patternList[it].drawable,
                    (binding.stickerView.currentSticker as TextSticker).gradient,
                    (binding.stickerView.currentSticker as TextSticker).matrix
                )

                val data = UndoRedoData2(
                binding.stickerView.currentSticker as TextSticker,
                copySticker(binding.stickerView.currentSticker as TextSticker, ""),
                textData
            )
            addToChangeList(TEXT_ADD, ACTION_PATTERN,data)}


        }
        binding.llAddTextView.llcolor.rvPattern.adapter = patternAdapter
    }

    private fun colorAdapter(){
         colorList.clear()
        colorList = getColorList()
        // list for animation for color
        binding.llAddTextView.llcolor.rvColor.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        colorAdapter = ColorsAdapter(this, colorList) {
            patternAdapter()
            gradientAdapter()
            if (it == 0) {
                colorPickerDialog()
            } else {
                if (binding.stickerView.currentSticker != null) {
                    colorType = false
                    colorID =  colorList[it].colorId
                    (binding.stickerView.currentSticker as TextSticker).setTextColor(
                        ContextCompat.getColor(
                            this,
                            colorList[it].colorId
                        )
                    )
                    notifyStickerView()
                }
            }
            if (borderColor != 0){
                if (borderType == MULTICOLOR_BORDER){
                    (binding.stickerView.currentSticker as TextSticker).setPaintToOutline(
                        borderColor, MULTICOLOR_BORDER
                    )
                }else{
                    (binding.stickerView.currentSticker as TextSticker).setPaintToOutline(
                        ContextCompat.getColor(
                            this,
                            borderColor
                        ), borderType)
                }
                notifyStickerView()
            }

            if (binding.stickerView.currentSticker != null) {
                var textData = TextStickerData((binding.stickerView.currentSticker as TextSticker).currentAngle,
                    (binding.stickerView.currentSticker as TextSticker).currentScale,
                    (binding.stickerView.currentSticker as TextSticker).x,
                    (binding.stickerView.currentSticker as TextSticker).y,
                    (binding.stickerView.currentSticker as TextSticker).startTime,
                    (binding.stickerView.currentSticker as TextSticker).endTime,
                    (binding.stickerView.currentSticker as TextSticker).getstrtag(),
                    (binding.stickerView.currentSticker as TextSticker).fontStyle,
                    (binding.stickerView.currentSticker as TextSticker).typeface,
                    (binding.stickerView.currentSticker as TextSticker).alignment().toString(),
                    (binding.stickerView.currentSticker as TextSticker).size,
                    (binding.stickerView.currentSticker as TextSticker).text.toString(),
                    (binding.stickerView.currentSticker as TextSticker).borderColor,
                    (binding.stickerView.currentSticker as TextSticker).borderType,
                    false,
                    false,
                    colorID,
                    colorID,
                    (binding.stickerView.currentSticker as TextSticker).pattern,
                    (binding.stickerView.currentSticker as TextSticker).gradient,
                    (binding.stickerView.currentSticker as TextSticker).matrix
                )
                var data = UndoRedoData2(
                binding.stickerView.currentSticker as TextSticker,
                copySticker(binding.stickerView.currentSticker as TextSticker, ""),
                    textData
            )
            addToChangeList(TEXT_ADD, ACTION_COLOR,data)}

        }
        binding.llAddTextView.llcolor.rvColor.adapter = colorAdapter
    }

    private fun gradientAdapter(){
        gradientList.clear()
        gradientList = getGradientList()
        // list for animation for gradiant
        // list for animation for gradiant
        binding.llAddTextView.llcolor.rvGradient.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        gradientAdapter = GradientAdapter(this, gradientList) {
            colorAdapter()
            patternAdapter()
            invertPos = it
            invertCount = 0
            gradientList[it].isSelected = true
            if (it == 0) {
                if (binding.stickerView.currentSticker != null) {
                    (binding.stickerView.currentSticker as TextSticker).setTextColor(
                        ContextCompat.getColor(
                            this,
                            gradientList[it].color1
                        )
                    )
                    notifyStickerView()
                }
            } else {
                if (binding.stickerView.currentSticker != null) {
                    (binding.stickerView.currentSticker as TextSticker).setGradient(
                        ContextCompat.getColor(
                            this,
                            gradientList[it].color1
                        ),
                        ContextCompat.getColor(
                            this,
                            gradientList[it].color2
                        )
                    )
                    notifyStickerView()
                }
            }
            var gradientArray : IntArray = IntArray(2)
            gradientArray[0] = gradientList[it].color1
            gradientArray[1] = gradientList[it].color2

            if (binding.stickerView.currentSticker != null) {
                var textData = TextStickerData((binding.stickerView.currentSticker as TextSticker).currentAngle,
                    (binding.stickerView.currentSticker as TextSticker).currentScale,
                    (binding.stickerView.currentSticker as TextSticker).x,
                    (binding.stickerView.currentSticker as TextSticker).y,
                    (binding.stickerView.currentSticker as TextSticker).startTime,
                    (binding.stickerView.currentSticker as TextSticker).endTime,
                    (binding.stickerView.currentSticker as TextSticker).getstrtag(),
                    (binding.stickerView.currentSticker as TextSticker).fontStyle,
                    (binding.stickerView.currentSticker as TextSticker).typeface,
                    (binding.stickerView.currentSticker as TextSticker).alignment().toString(),
                    (binding.stickerView.currentSticker as TextSticker).size,
                    (binding.stickerView.currentSticker as TextSticker).text.toString(),
                    (binding.stickerView.currentSticker as TextSticker).borderColor,
                    (binding.stickerView.currentSticker as TextSticker).borderType,
                    false,
                    false,
                    gradientList[it].color1,
                    gradientList[it].color1,
                    (binding.stickerView.currentSticker as TextSticker).pattern,
                    gradientArray,
                    (binding.stickerView.currentSticker as TextSticker).matrix
                )
                val data = UndoRedoData2(
                binding.stickerView.currentSticker as TextSticker,
                copySticker(binding.stickerView.currentSticker as TextSticker, ""),
                    textData
            )
            addToChangeList(TEXT_ADD, ACTION_GRADIENT,data)}


        }

        binding.llAddTextView.llcolor.rvGradient.adapter = gradientAdapter

    }


    fun onColorChanged(valueGradient: Int, alphaGradientColor: Int) {
        indicatorColor.set(setColorAlpha(valueGradient, alphaGradientColor))
        Log.e(TAG, "onColorChanged: $valueGradient  $alphaGradientColor")

    }

    private fun handleTextAlignment(alignment: Layout.Alignment) {
        if (binding.stickerView.currentSticker != null) {
            (binding.stickerView.currentSticker as TextSticker).setTextAlign(alignment)
            (binding.stickerView.currentSticker as TextSticker).resizeText()
            notifyStickerView()
        }
    }

    private fun setSeekBarForOneVideo(startDuration: Long, endDuration: Long, videoDuration: Long) {
        removeHandleCallback()
        updatedDuration = if (selectedFunction == TRIM) tempStartTime else 0
        player.seekTo(0)
        binding.videoSeekBar.setProgress(if (selectedFunction == TRIM) tempStartTime.toFloat() else 0f)
        binding.tvVideoDuration.text =
            convertMillieToHMS(if (selectedFunction == TRIM) tempStartTime else 0)
        val durationArray = LongArray(2)
        durationArray[0] = 0
        durationArray[1] = if (videoDuration > 0) videoDuration else (endDuration - startDuration)
        binding.videoSeekBar.refreshView(durationArray)

    }

    private fun setSelectedVideoAsSingleSource(
        startDuration: Long, endDuration: Long
    ) {
        player.removeListener(eventListener)

        if (startDuration < endDuration) {
            val mediaItem: MediaItem = MediaItem.fromUri(arrVideo[tempCurrentPosition].videoUri!!)
            val source = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(mediaItem).let {
                    ClippingMediaSource(
                        it,
                        startDuration * 1000L,
                        endDuration * 1000L
                    )
                }
            player.addListener(eventListener)
            player.setMediaSource(source, false)
            applyVideoVolumeOnPlayer()
            applyAudioVolumeOnPlayer()
        }

    }

    @SuppressLint("SetTextI18n")
    fun onClickViewEditScreen(view: View) {
        when (view.id) {
            R.id.btnDiscard -> {
                discardVideoDialog()

            }
            R.id.btnSave -> {
                saveVideoDialog()
            }
            R.id.img_play -> {
//                addStickerToList()
                removeSticker = false
                Handler(Looper.getMainLooper()).postDelayed({
                    binding.stickerView.removeAllStickers()
                    playPauseVideo(isPlay)
                }, 500)

            }
            R.id.img_done -> {
                saveChangesOnVideo()
            }
            R.id.img_allapply -> {
                videoEditViewModel.isFilterApplyForAll.set(true)
                if (videoEditViewModel.isFilterApplyForAll.get()) {
                    for (videoItem in arrVideo) {
                        videoItem.filter = videoEditViewModel.getSelectedFilter()
                   }
               }
                val data = UndoRedoData2(
                    null,
                    null,
                    null,
                    videoEditViewModel.getSelectedFilter(),
                    filterPosition

                )
                addToChangeList(ACTION_FILTER, ACTION_FILTER_ALL,data)
                saveFilterChangeOnVideo()
            }
            R.id.ivAddGallery -> {
//                VideoEditorActivity.startActivity(this,arrVideo,true)
                onBackPressed()

            }
            R.id.imgFullscreen -> {
                visibilityFullScreen(false)
            }
            R.id.imgFullExitscreen -> {
                visibilityFullScreen(true)
            }
            R.id.img_undo -> {
                if (undoRedoPointer.get()!! >= 0 && undoRedoPointer.get()!! < undoredoList.size) {
                    isUndoRedo = true
                    performUndo()
                    isUndoRedo = false
                }
            }
            R.id.img_redo -> {
                if (undoRedoPointer.get()!! < undoredoList.size - 1 && undoRedoPointer.get()!! >= -1) {
                    isUndoRedo = true
                    performRedo()
                    isUndoRedo = false
                }
            }
            R.id.ll_previous -> {
                currentPosition -= 1
                if (currentPosition >= 0) {
                    saveNextChangeVideo()
                    handleTrim(true)

                    binding.tvitemCount.text =
                        (currentPosition + 1).toString() + " / " + arrVideo.size

                } else {
                    currentPosition += 1
                }


            }
            R.id.btnInvert -> {
                if (invertCount % 2 == 0) {
                    if (binding.stickerView.currentSticker != null) {
                        if (gradientList[invertPos].isSelected) {
                            (binding.stickerView.currentSticker as TextSticker).setGradient(
                                ContextCompat.getColor(
                                    this,
                                    gradientList[invertPos].color2
                                ),
                                ContextCompat.getColor(
                                    this,
                                    gradientList[invertPos].color1
                                )
                            )
                            var gradientArray : IntArray = IntArray(2)
                            gradientArray[0] =  gradientList[invertPos].color2
                            gradientArray[1] =   gradientList[invertPos].color1

                            if (binding.stickerView.currentSticker != null) {
                                var textData = TextStickerData((binding.stickerView.currentSticker as TextSticker).currentAngle,
                                    (binding.stickerView.currentSticker as TextSticker).currentScale,
                                    (binding.stickerView.currentSticker as TextSticker).x,
                                    (binding.stickerView.currentSticker as TextSticker).y,
                                    (binding.stickerView.currentSticker as TextSticker).startTime,
                                    (binding.stickerView.currentSticker as TextSticker).endTime,
                                    (binding.stickerView.currentSticker as TextSticker).getstrtag(),
                                    (binding.stickerView.currentSticker as TextSticker).fontStyle,
                                    (binding.stickerView.currentSticker as TextSticker).typeface,
                                    (binding.stickerView.currentSticker as TextSticker).alignment().toString(),
                                    (binding.stickerView.currentSticker as TextSticker).size,
                                    (binding.stickerView.currentSticker as TextSticker).text.toString(),
                                    (binding.stickerView.currentSticker as TextSticker).borderColor,
                                    (binding.stickerView.currentSticker as TextSticker).borderType,
                                    false,
                                    false,
                                    gradientList[invertPos].color1,
                                    gradientList[invertPos].color1,
                                    (binding.stickerView.currentSticker as TextSticker).pattern,
                                    gradientArray,
                                    (binding.stickerView.currentSticker as TextSticker).matrix
                                )
                                val data = UndoRedoData2(
                                    binding.stickerView.currentSticker as TextSticker,
                                    copySticker(
                                        binding.stickerView.currentSticker as TextSticker,
                                        ""
                                    ),
                                    textData
                                )
                                addToChangeList(TEXT_ADD, ACTION_GRADIENT,data)}
                            notifyStickerView()
                        }
                    }
                } else {
                    if (binding.stickerView.currentSticker != null) {
                        if (gradientList[invertPos].isSelected) {
                            (binding.stickerView.currentSticker as TextSticker).setGradient(
                                ContextCompat.getColor(
                                    this,
                                    gradientList[invertPos].color1
                                ),
                                ContextCompat.getColor(
                                    this,
                                    gradientList[invertPos].color2
                                )
                            )
                            var gradientArray : IntArray = IntArray(2)
                            gradientArray[0] =  gradientList[invertPos].color1
                            gradientArray[1] =   gradientList[invertPos].color2

                            if (binding.stickerView.currentSticker != null) {
                                var textData = TextStickerData((binding.stickerView.currentSticker as TextSticker).currentAngle,
                                    (binding.stickerView.currentSticker as TextSticker).currentScale,
                                    (binding.stickerView.currentSticker as TextSticker).x,
                                    (binding.stickerView.currentSticker as TextSticker).y,
                                    (binding.stickerView.currentSticker as TextSticker).startTime,
                                    (binding.stickerView.currentSticker as TextSticker).endTime,
                                    (binding.stickerView.currentSticker as TextSticker).getstrtag(),
                                    (binding.stickerView.currentSticker as TextSticker).fontStyle,
                                    (binding.stickerView.currentSticker as TextSticker).typeface,
                                    (binding.stickerView.currentSticker as TextSticker).alignment().toString(),
                                    (binding.stickerView.currentSticker as TextSticker).size,
                                    (binding.stickerView.currentSticker as TextSticker).text.toString(),
                                    (binding.stickerView.currentSticker as TextSticker).borderColor,
                                    (binding.stickerView.currentSticker as TextSticker).borderType,
                                    false,
                                    false,
                                    gradientList[invertPos].color1,
                                    gradientList[invertPos].color1,
                                    (binding.stickerView.currentSticker as TextSticker).pattern,
                                    gradientArray,
                                    (binding.stickerView.currentSticker as TextSticker).matrix
                                )
                                val data = UndoRedoData2(
                                    binding.stickerView.currentSticker as TextSticker,
                                    copySticker(
                                        binding.stickerView.currentSticker as TextSticker,
                                        ""
                                    ),
                                    textData
                                )
                                addToChangeList(TEXT_ADD, ACTION_GRADIENT,data)}
                            notifyStickerView()
                        }
                    }
                }

                invertCount++
            }

            R.id.ll_next -> {
                currentPosition += 1
                if (currentPosition < arrVideo.size) {
                    saveNextChangeVideo()
                    handleTrim(true)

                    binding.tvitemCount.text =
                        (currentPosition + 1).toString() + " / " + arrVideo.size

                } else {
                    currentPosition -= 1
                }
            }

            R.id.ibAdjustment -> {
                binding.llAddTextView.typetext = ObservableInt(3)
            }

            R.id.ibColor -> {
                binding.llAddTextView.typetext = ObservableInt(1)

            }

            R.id.ibAlign -> {
                binding.llAddTextView.typetext = ObservableInt(2)
            }

            R.id.ibFilter -> {
                binding.llAddTextView.typetext = ObservableInt(4)

            }

            R.id.btnColor -> {
                binding.llAddTextView.llcolor.type = ObservableInt(1)

            }
            R.id.ibAddText -> {
                isEdit = false
                addTextSticker()
//                openTextEditingMenu(getString(R.string.enter_the_text), "0_copy", 300L, 0L)
            }

            R.id.btnGradiant -> {
                binding.llAddTextView.llcolor.type = ObservableInt(2)

            }
            R.id.btnPattern -> {
                binding.llAddTextView.llcolor.type = ObservableInt(3)

            }

            R.id.btnIn -> {
                binding.llAddTextView.llAnimation.typetext = ObservableInt(1)

            }
            R.id.btnOut -> {
                binding.llAddTextView.llAnimation.typetext = ObservableInt(2)

            }
            R.id.ivAlignLeft -> {
                binding.llAddTextView.llAlignment1.typealign = ObservableInt(1)
                handleTextAlignment(Layout.Alignment.ALIGN_NORMAL)

            }
            R.id.ivAlignCenter -> {
                binding.llAddTextView.llAlignment1.typealign = ObservableInt(2)
                handleTextAlignment(Layout.Alignment.ALIGN_CENTER)

            }
            R.id.ivAlignRight -> {
                binding.llAddTextView.llAlignment1.typealign = ObservableInt(3)
                handleTextAlignment(Layout.Alignment.ALIGN_OPPOSITE)

            }

            R.id.btnTick -> {
                editChangesDone()
            }

            R.id.ivRecord -> {
                playPauseVideo(true)
                recordPlayPause()
            }
            R.id.ivRecordDone -> {
                binding.llRecord.hide()
                binding.ccVideoPlay.visible()
                binding.horizontalScroll.visible()
                binding.imgUndo.visible()
                binding.imgRedo.visible()
                binding.rvVideoMusic.visible()
                binding.includeToolbar.btnSave.hide()
                binding.includeToolbar.btnTick.visible()
                binding.includeToolbar.btnDiscard.visible()
                binding.recordSeekWave.visible()
                binding.effectsSeekWave.visible()
                binding.audioSeekWave.visible()

                createVideoMusicBtnList()
                seekBarMusicTimeline()
            }
            R.id.ivRecordCancel -> {
                discardVideoDialog()
            }
            R.id.ivRecordReplay -> {
                videoEditViewModel.getSelectedMusic()
                    .removeAt(videoEditViewModel.getSelectedMusic().size - 1)
                binding.llRecord.hide()
                binding.horizontalScroll.visible()
                binding.rvFrame2.visible()
                binding.audioSeekWave.hide()
                binding.recordSeekWave.hide()
                binding.tvRecordTime.visible()
                binding.ivRecord.visible()
                binding.ivRecord.setImageResource(R.drawable.ic_recordpause)
                playPauseVideo(true)
                handleRecord()
            }
        }
    }
    fun addData(textSticker: TextSticker, data: UndoRedoData2) :TextSticker{
        val sticker = TextSticker(this)
        sticker.text = textSticker.text
        sticker.setTextColor(textSticker.color)

        if (textSticker.borderColor != 0 && textSticker.borderType != "") {
            sticker.setPaintToOutline(textSticker.borderColor, textSticker.borderType)
        }
        sticker.resizeText()
        sticker.setMaxTextSize(dpToPx(textSticker.size))
        sticker.setTypeface(textSticker.typeface)
        sticker.fontStyle = textSticker.fontStyle
        sticker.resizeText()
        sticker.setStrTag(textSticker.getstrtag())
        sticker.startTime = data.textData!!.startTime
        if (textSticker.gradient != null){
        sticker.setGradient(textSticker.gradient[0],textSticker.gradient[1])}
        if (textSticker.pattern != 0){
            sticker.pattern = textSticker.pattern
        }
//        sticker.letterSpacing = textSticker.letterSpacing
//        sticker.setLetterSpacing(textSticker.letterLining)
        sticker.endTime = data.textData!!.endTime
        binding.stickerView.onStickerOperationListener!!.onStickerAdded(sticker)
        binding.stickerView.addSticker(sticker, 2f)
        val matrix = textSticker.matrix.clone()
        binding.stickerView.changeMatrix(sticker,matrix)
        notifyStickerView()

        val textData = SpanData()
        textData.startTime = data.textData!!.startTime
        textData.endTime = data.textData!!.endTime
        textData.finalstartTime = (data.textData!!.startTime.toInt() * 10).toLong()
        textData.finalendTime =
            (data.textData!!.endTime.toInt()).toLong() * 10
        textData.endTime = data.textData!!.endTime
        textData.x = sticker.x.toFloat()
        if (data.textData!!.offset != 0){
            textData.offset = data.textData!!.startTime.toInt()

        }else{
            textData.offset = sticker.startTime.toInt()

        }
        if (data.textData!!.length != 0){
            textData.length = data.textData!!.length

        }else{
            textData.length = sticker.endTime.toInt() - sticker.startTime.toInt()

        }
        if (data.textData!!.mobject != 0){
            textData.mobject = data.textData!!.mobject

        }else{
            textData.mobject = 0
        }

        textData.info = sticker.text
        textData.y = sticker.y.toFloat()
//                    textData.bitmap = textAsBitmap(sticker)
        textData.strTag = sticker.getstrtag()
        textData.text = sticker.text.toString()
        textData.sticker = sticker
        textData.isSelected = false
        val itemlist: ArrayList<SpanData> = ArrayList()
        itemlist.add(textData)
        textStickerList.add(
            CopyModel(
                itemlist,
                ((TimeUnit.MILLISECONDS.toSeconds(totalVideoDuration)) * SEGMENT_VIDEO).toInt()
            )
        )
        binding.rvTextFrame.adapter = null
        binding.rvTextFrame.adapter = TextCopyAdapter(this, textStickerList, this)
        binding.rvTextFrame.adapter!!.notifyDataSetChanged()
        binding.rvTextLine.adapter = null
        binding.rvTextLine.adapter = TextLineAdapter(this, textStickerList)
        binding.rvTextLine.adapter!!.notifyDataSetChanged()
        Log.e(TAG, "addData: "+undoredoList )
        return sticker
    }

    private fun   performUndo(){
        val item = undoredoList[undoRedoPointer.get()!!]
        when(item.type){
            DELETE_TEXT ->{
                addTextList(item.data.textData!!)
            }
            EDIT_TEXT ->{
                if ( binding.stickerView.stickerList != null) {
                    binding.stickerView.stickerList.forEach { it ->
                        val sticker = it as TextSticker
                        if (it.getstrtag() == item.data.oldSticker!!.getstrtag()) {
                            binding.stickerView.onStickerOperationListener!!.onStickerDeleted(it)
                            binding.stickerView.removeStickerlist(it)
                            var sticker2=  addData(item.data.oldSticker as TextSticker,item.data)
                            notifyStickerView()
                            undoredoList.forEach {
                                if (it.data.oldSticker == sticker){
//                                    it.data.oldSticker = sticker2
                                    it.data.textSticker = sticker2
                                }
                            }

                        }
                    }
                }
            }
            TEXT_ADD ->{
                if ( binding.stickerView.stickerList != null) {
                    binding.stickerView.stickerList.forEach { it ->
                        val sticker = it
                        if (it == item.data.oldSticker) {
                            binding.stickerView.onStickerOperationListener!!.onStickerDeleted(it)
                            binding.stickerView.removeStickerlist(it)
                            var sticker2=  addData(sticker as TextSticker,item.data)
                            notifyStickerView()
                            undoredoList.forEach {
                                if (it.data.oldSticker == sticker){
                                    it.data.oldSticker = sticker2
                                    it.data.textSticker = sticker2
                                }
                            }
                        }
                    }
                }
            }
            DRAG_TEXT ->{
                Log.e(TAG, "performUndo: "+item.data.oldSticker!!.endTime.toInt()  )
                for (i in 0 until binding.stickerView.stickerList.size){
                    if (binding.stickerView.stickerList[i] == item.data.oldSticker){
                        for (s in 0 until textStickerList.size){
                            for (m in 0 until textStickerList[s].duplicatetext!!.size){
                                if (textStickerList[s].duplicatetext?.get(m)?.strTag == item.data.textData!!.tag){
                                    textStickerList[s].duplicatetext?.get(m)?.length = item.data.oldSticker!!.endTime.toInt() - item.data.oldSticker!!.startTime.toInt()
                                    textStickerList[s].duplicatetext?.get(m)?.offset = item.data.oldSticker!!.startTime.toInt()
                                    textStickerList[s].duplicatetext?.get(m)?.startTime= item.data.oldSticker!!.startTime.toLong()
                                    textStickerList[s].duplicatetext?.get(m)?.finalstartTime = item.data.oldSticker!!.startTime.toLong() * 10
                                    textStickerList[s].duplicatetext?.get(m)?.endTime = item.data.oldSticker!!.endTime.toLong()
                                    textStickerList[s].duplicatetext?.get(m)?.finalendTime =  item.data.oldSticker!!.endTime.toLong() * 10

                                }
                            }
                        }
                    }
                }
                Log.e(TAG, "performUndo:textStickerList "+textStickerList )
                binding.rvTextFrame.adapter = null
                binding.rvTextFrame.adapter = TextCopyAdapter(this, textStickerList, this)
                binding.rvTextLine.adapter =
                    TextLineAdapter(this, textStickerList)
                binding.rvTextFrame.adapter!!.notifyDataSetChanged()
                binding.rvTextLine.adapter!!.notifyDataSetChanged()
            }
            ADD->{
                Log.e(TAG, "oldSticker>>>>>: "+binding.stickerView.stickerList   + " oldSticker  "+item.data.oldSticker + " textSticker  "+item.data.textSticker)
                    for (i in 0 until binding.stickerView.stickerList.size){
                        Log.e(TAG, "oldSticker>>>>>: "+binding.stickerView.stickerList[i]   + " oldSticker  "+item.data.oldSticker + " textSticker  "+item.data.textSticker)
                        if (binding.stickerView.stickerList[i] == item.data.oldSticker){
                            isPlay = false
                            binding.stickerView.onStickerOperationListener!!.onStickerDeleted(item.data.oldSticker as TextSticker)
                            binding.stickerView.removeStickerlist(item.data.oldSticker)
                            notifyStickerView()
                            break
                        }
                    }

                    Log.e(TAG, "performUndobinding.strTAG.stickerList: "+binding.stickerView.stickerList )



            }
            ACTION_FILTER ->{
                if (item.redotype == ACTION_FILTER_ALL){
                    videoEditViewModel.getVideoFilterList(
                        arrVideo[item.data.videoPos].filter,
                        this,
                        frames[0].frame!!
                    )
                    videoEditViewModel.applySelectionFromFilter(arrVideo[item.data.videoPos].filter)
                    arrFilterList = videoEditViewModel.arrFilterData

                    for (i in 0 until arrFilterList.size) {
                        arrFilterList[i].isSelected = false
                    }
                    arrFilterList[item.data.filterpos].isSelected = true
                    videoEditViewModel.applyFilterOnVideo(0)
                    arrVideo[tempCurrentPosition].filter = videoEditViewModel.getSelectedFilter()
                    filterAdapter!!.notifyDataSetChanged()

                    videoEditViewModel.isFilterApplyForAll.set(true)
                    if (videoEditViewModel.isFilterApplyForAll.get()) {
                        for (videoItem in arrVideo) {
                            videoItem.filter =  FilterType.DEFAULT
                        }
                    }

            }else{
                    videoEditViewModel.getVideoFilterList(
                        arrVideo[item.data.videoPos].filter,
                        this,
                        frames[0].frame!!
                    )
                    videoEditViewModel.applySelectionFromFilter(arrVideo[item.data.videoPos].filter)
                    arrFilterList = videoEditViewModel.arrFilterData

                    for (i in 0 until arrFilterList.size) {
                        arrFilterList[i].isSelected = false
                    }
                    arrFilterList[item.data.filterpos].isSelected = true
                    videoEditViewModel.applyFilterOnVideo(0)
                    arrVideo[item.data.videoPos].filter = videoEditViewModel.getSelectedFilter()
                    filterAdapter!!.notifyDataSetChanged()
            }
            }
            ACTION_TRIM ->{
                currentPosition = item.data.videoPos
                binding.tvitemCount.text =
                    (currentPosition + 1 ).toString() + " / " + arrVideo.size
                arrVideo[item.data.videoPos].startDuration = 0
                arrVideo[item.data.videoPos].isTrimmed = false
                arrVideo[item.data.videoPos].endDuration =  arrVideo[item.data.videoPos].videoDuration
                videoFrameHelpercuttor.initialize(
                    arrVideo[item.data.videoPos].videoUri,
                    arrVideo[item.data.videoPos].videoDuration,
                    arrVideo[item.data.videoPos].startDuration,
                    arrVideo[item.data.videoPos].endDuration,
                    arrVideo[item.data.videoPos].isTrimmed,
                    arrVideo[item.data.videoPos].isSplit, false
                )
                setSeekBarForOneVideo(
                    0,
                    arrVideo[item.data.videoPos].videoDuration,
                    0
                )
                setSelectedVideoAsSingleSource(
                    arrVideo[item.data.videoPos].startDuration,
                    if (arrVideo[item.data.videoPos].isSplit) arrVideo[item.data.videoPos].startDuration + arrVideo[item.data.videoPos].endDuration else arrVideo[tempCurrentPosition].endDuration
                )
                binding.videoSeekBar.setProgress(item.data.startDuration.toFloat())
                binding.tvVideoDuration.text = convertMillieToHMS(item.data.startDuration)
                isChangeInTrim = false
                Log.e(TAG, "updateCountDownTimer1: " )
                updateCountDownTimer()
            }
            ACTION_MUSIC ->{
                when (item.redotype){
                    MUSIC_ADD ->{
                        var musicArray = videoEditViewModel.getSelectedMusic()
                        musicArray.forEachIndexed { index, musicData ->
                            Log.e(TAG, "performUndo: "+musicData.title   +"  "+item.data.listOfEffects!!.title)
                            if (musicData.title == item.data.listOfEffects!!.title){
                                 setAudioSource()
                                if (musicData.isfrom == IS_MUSIC) {
                                    binding.audioSeekWave.removeSpan(index)
                                } else if (musicData.isfrom == IS_EFFECT) {
                                    binding.effectsSeekWave.removeSpan(index)

                                } else if (musicData.isfrom == IS_RECORD) {
                                    binding.recordSeekWave.removeSpan(index)
                                }
                                videoEditViewModel.onDeleteSelectedItem(musicData)
                            }
                        }
                    }
                    MUSIC_DRAG ->{
                        var musicArray = videoEditViewModel.getSelectedMusic()
                        musicArray.forEachIndexed { index, musicData ->
                            Log.e(TAG, "performUndo: "+musicData.title   +"  "+item.data.listOfEffects!!.title)
                            if (musicData.title == item.data.listOfEffects!!.title){
                                setAudioSource()
                                if (musicData.isfrom == IS_MUSIC) {
                                    binding.audioSeekWave.removeSpan(index)
                                } else if (musicData.isfrom == IS_EFFECT) {
                                    binding.effectsSeekWave.removeSpan(index)

                                } else if (musicData.isfrom == IS_RECORD) {
                                    binding.recordSeekWave.removeSpan(index)
                                }
                                videoEditViewModel.onDeleteSelectedItem(musicData)
                            }
                        }
                        playPauseAudio(item.data.listOfEffects!!)
                        addAudioFileInList(item.data.listOfEffects!!)
                    }
                }
            }
        }
        undoRedoPointer.set(undoRedoPointer.get()!! - 1)

    }

    private fun performRedo() {

        undoRedoPointer.set(undoRedoPointer.get()!! + 1)
        val item = undoredoList[undoRedoPointer.get()!!]
        if (item.type == ADD || item.type == TEXT_ADD) {
            when (item.redotype) {
                TEXT_ADD-> {
                    addTextList(item.data.textData!!)
                }
                ACTION_COLOR -> {
                    if (binding.stickerView.stickerList != null) {
                        binding.stickerView.stickerList.forEach {
                            Log.e(TAG, "addRedoData: " + it + "  >> " + item.data.oldSticker)
                            Log.e(TAG, "addRedoData: " + item.data.textData!!.color)
                            if (it == item.data.oldSticker) {
                                if (item.data.textData!!.color == null) {
                                    (it as TextSticker).setTextColor(
                                        ContextCompat.getColor(
                                            this@VideoEditorActivity,
                                            R.color.black
                                        )
                                    )
                                } else {
                                    (it as TextSticker).setTextColor(
                                        ContextCompat.getColor(
                                            this,
                                            item.data.textData!!.color!!
                                        )
                                    )
                                }
                            }
                        }

                        notifyStickerView()
                    }
                    else{
                        addTextList(item.data.textData!!)
                    }
                }
                ACTION_GRADIENT -> {
                    if (binding.stickerView.stickerList != null) {
                        if (item.data.textData!!.gradient != null) {
                            binding.stickerView.stickerList.forEach {
                                if (it == item.data.oldSticker) {
                                    (it as TextSticker).setGradient(
                                        ContextCompat.getColor(
                                            this,
                                            item.data.textData!!.gradient!![0]
                                        ),
                                        ContextCompat.getColor(
                                            this,
                                            item.data.textData!!.gradient!![1]
                                        )
                                    )
                                }
                            }
                            notifyStickerView()
                        }
                    }
                    else{
                        addTextList(item.data.textData!!)
                    }
                }
                ACTION_PATTERN -> {
                    if (binding.stickerView.stickerList != null) {
                        if (item.data.textData!!.pattern != null) {
                            binding.stickerView.stickerList.forEach {
                                if (it == item.data.oldSticker) {
                                    (it as TextSticker).pattern = item.data.textData!!.pattern!!
                                }
                            }
                            notifyStickerView()
                        }
                    }
                    else{
                        addTextList(item.data.textData!!)
                    }
                }
                ACTION_BORDER -> {
                    if (binding.stickerView.stickerList != null) {
                        if (item.data.textData!!.borderColor != null) {
                            binding.stickerView.stickerList.forEach {
                                if (it == item.data.oldSticker) {
                                    (it as TextSticker).setPaintToOutline(
                                        ContextCompat.getColor(
                                            this,
                                            item.data.textData!!.borderColor!!
                                        ), item.data.textData!!.borderType
                                    )
                                    (it as TextSticker).resizeText()
                                }
                            }
                            notifyStickerView()
                        }
                    }
                    else{
                        addTextList(item.data.textData!!)
                    }

                }
                ACTION_RESIZE -> {
                    if (binding.stickerView.stickerList != null) {
                        if (item.data.textData!!.textSize != null) {
                            binding.stickerView.stickerList.forEach {
                                if (it == item.data.oldSticker) {
                                    (it as TextSticker).setMaxTextSize(item.data.textData!!.textSize!!)
                                }
                            }
                            notifyStickerView()
                        }
                    }
                    else{
                        addTextList(item.data.textData!!)
                    }
                }
                ACTION_FONT -> {
                    if (binding.stickerView.stickerList != null) {
                        binding.stickerView.stickerList.forEach {
                                if (it == item.data.oldSticker) {
                                    (it as TextSticker).setTypeface(item.data.textData!!.fontName!!)
                                    (it as TextSticker).resizeText()
                                }
                            }
                        notifyStickerView()
                    }
                    else{
                        addTextList(item.data.textData!!)
                    }
                }
            }
        }
        else if (item.type == DRAG_TEXT){
            for (i in 0 until binding.stickerView.stickerList.size){
                if (binding.stickerView.stickerList[i] == item.data.oldSticker){
                    for (s in 0 until textStickerList.size){
                        for (m in 0 until textStickerList[s].duplicatetext!!.size){
                            if (textStickerList[s].duplicatetext?.get(m)?.sticker == item.data.oldSticker){
                                textStickerList[s].duplicatetext?.get(m)?.length = item.data.textData!!.length.toInt()
                                textStickerList[s].duplicatetext?.get(m)?.offset =  item.data.textData!!.startTime.toInt()
                                textStickerList[s].duplicatetext?.get(m)?.startTime=  item.data.textData!!.startTime.toLong()
                                textStickerList[s].duplicatetext?.get(m)?.finalstartTime =  item.data.textData!!.startTime.toLong() * 10
                                textStickerList[s].duplicatetext?.get(m)?.endTime =  item.data.textData!!.endTime.toLong()
                                textStickerList[s].duplicatetext?.get(m)?.finalendTime =  item.data.textData!!.endTime.toLong() * 10

                            }
                        }
                    }
                }
            }
            binding.rvTextFrame.adapter = null
            binding.rvTextFrame.adapter = TextCopyAdapter(this, textStickerList, this)
            binding.rvTextLine.adapter =
                TextLineAdapter(this, textStickerList)
            binding.rvTextFrame.adapter!!.notifyDataSetChanged()
            binding.rvTextLine.adapter!!.notifyDataSetChanged()
        }
        else if (item.type == ACTION_FILTER){
            videoEditViewModel.getVideoFilterList(
                arrVideo[item.data.videoPos].filter,
                this,
                frames[item.data.filterpos].frame!!
            )
            videoEditViewModel.applySelectionFromFilter(arrVideo[item.data.videoPos].filter)
            arrFilterList = videoEditViewModel.arrFilterData

            for (i in 0 until arrFilterList.size) {
                arrFilterList[i].isSelected = false
            }
            arrFilterList[item.data.filterpos].isSelected = true
            videoEditViewModel.applyFilterOnVideo(item.data.filterpos)
            arrVideo[tempCurrentPosition].filter = videoEditViewModel.getSelectedFilter()
            filterAdapter!!.notifyDataSetChanged()
            if (item.redotype == ACTION_FILTER_ALL){
                videoEditViewModel.isFilterApplyForAll.set(true)
                if (videoEditViewModel.isFilterApplyForAll.get()) {
                    for (videoItem in arrVideo) {
                        videoItem.filter = videoEditViewModel.getSelectedFilter()
                    }
                }
            }
        }
        else if (item.type == ACTION_TRIM ){
            currentPosition = item.data.videoPos
            arrVideo[item.data.videoPos].startDuration = item.data.startDuration
            arrVideo[item.data.videoPos].endDuration = item.data.endDuration
            arrVideo[item.data.videoPos].isTrimmed = true

            binding.tvitemCount.text =
                (currentPosition +1 ).toString() + " / " + arrVideo.size
            videoFrameHelpercuttor.initialize(
                arrVideo[item.data.videoPos].videoUri,
                arrVideo[item.data.videoPos].videoDuration,
                arrVideo[item.data.videoPos].startDuration,
                arrVideo[item.data.videoPos].endDuration,
                arrVideo[item.data.videoPos].isTrimmed ,
                arrVideo[item.data.videoPos].isSplit, false
            )
            videoFrameHelpercuttor.onUpdate()
            setSeekBarForOneVideo(
                0,
                arrVideo[item.data.videoPos].videoDuration,
                0
            )
            setSelectedVideoAsSingleSource(
                arrVideo[item.data.videoPos].startDuration,
                if (arrVideo[item.data.videoPos].isSplit) arrVideo[item.data.videoPos].startDuration + arrVideo[item.data.videoPos].endDuration else arrVideo[tempCurrentPosition].endDuration
            )
            Log.e(TAG, "updateCountDownTimer1: " )
            binding.videoSeekBar.setProgress(item.data.startDuration.toFloat())
            binding.tvVideoDuration.text = convertMillieToHMS(item.data.startDuration)
            isChangeInTrim = false
            Log.e(TAG, "updateCountDownTimer1: " )
            updateCountDownTimer()
        }
        else if (item.type == DELETE_TEXT){

            for (i in 0 until binding.stickerView.stickerList.size){
                Log.e(TAG, "oldSticker>>>>>: "+binding.stickerView.stickerList[i]   + " oldSticker  "+item.data.oldSticker + " textSticker  "+item.data.textSticker)
                if (binding.stickerView.stickerList[i] == item.data.oldSticker){
                    isPlay = false
                    binding.stickerView.onStickerOperationListener!!.onStickerDeleted(item.data.oldSticker as TextSticker)
                    binding.stickerView.removeStickerlist(item.data.oldSticker)
                    notifyStickerView()
                    break
                }
            }


        }
        else if (item.type == EDIT_TEXT){
            if ( binding.stickerView.stickerList != null) {
                binding.stickerView.stickerList.forEach { it ->
                    val sticker = it as TextSticker
                    if (it.getstrtag() == item.data.oldSticker!!.getstrtag()) {
                        binding.stickerView.onStickerOperationListener!!.onStickerDeleted(it)
                        binding.stickerView.removeStickerlist(it)
                        var sticker2=  addData(item.data.oldSticker as TextSticker,item.data)
                        notifyStickerView()
                        undoredoList.forEach {
                            if (it.data.oldSticker == sticker){
//                                    it.data.oldSticker = sticker2
                                it.data.textSticker = sticker2
                            }
                        }

                    }
                }
            }
        }
        else if (item.type == ACTION_MUSIC){
           when(item.redotype){
               MUSIC_ADD ->{
                   playPauseAudio(item.data.listOfEffects!!)
                   addAudioFileInList(item.data.listOfEffects!!)
               }
           }
        }
    }

    private fun addTextList(textData2 : TextStickerData){
        val sticker = TextSticker(this)
        sticker.text = textData2.text
        sticker.setTextColor(
            ContextCompat.getColor(
                this@VideoEditorActivity,
                R.color.black
            )
        )

        if (textData2.borderColor != 0 && textData2.borderType != "") {
            sticker.setPaintToOutline(textData2.borderColor!!, textData2.borderType)
        }
//        sticker.setTextAlign()
        sticker.setMaxTextSize(dpToPx(textData2.textSize!!))
        sticker.setTypeface(textData2.fontName)
        sticker.fontStyle = textData2.textStyle
        Log.e(TAG, "addTextList: "+sticker.text)
        sticker.resizeText()
        sticker.setStrTag(textData2.tag)
        sticker.startTime = textData2.startTime

        sticker.endTime = textData2.endTime
        binding.stickerView.onStickerOperationListener!!.onStickerAdded(sticker)
        binding.stickerView.addSticker(sticker, 2f)
        val matrix = textData2.matrix!!.clone()
        binding.stickerView.changeMatrix(sticker,matrix)
        notifyStickerView()

        val textData = SpanData()
        textData.startTime = textData2.startTime
        textData.endTime = textData2.endTime
        textData.finalstartTime = (textData2.startTime.toInt() * 10).toLong()
        textData.finalendTime =
            (textData2.endTime.toInt()).toLong() * 10
        textData.x = sticker.x.toFloat()
        if (textData.offset != 0){
            textData.offset = textData2.offset

        }else{
            textData.offset = sticker.startTime.toInt()

        }
        if (textData.length != 0){
            textData.length = textData2.length

        }else{
            textData.length = sticker.endTime.toInt() - sticker.startTime.toInt()

        }
        if (textData.mobject != 0){
            textData.mobject = textData2.mobject

        }else{
            textData.mobject = 0
        }
        textData.info = sticker.text
        textData.y = sticker.y.toFloat()
//                    textData.bitmap = textAsBitmap(sticker)
        textData.strTag = sticker.getstrtag()
        textData.text = sticker.text.toString()
        textData.sticker = sticker
        textData.isSelected = false
        val itemlist: ArrayList<SpanData> = ArrayList()
        itemlist.add(textData)
        if (textData2.tag.toString().contains("duplicate")){
            textStickerList[mPosition].duplicatetext!!.addAll(itemlist)
            binding.rvTextFrame.adapter!!.notifyItemChanged(mPosition)
        }   else{
            textStickerList.add(CopyModel(itemlist,((TimeUnit.MILLISECONDS.toSeconds(totalVideoDuration)) * SEGMENT_VIDEO).toInt()))

        }

        binding.rvTextFrame.adapter = null
        binding.rvTextFrame.adapter = TextCopyAdapter(this, textStickerList, this)
        binding.rvTextFrame.adapter!!.notifyDataSetChanged()
        binding.rvTextLine.adapter = null
        binding.rvTextLine.adapter = TextLineAdapter(this, textStickerList)
        binding.rvTextLine.adapter!!.notifyDataSetChanged()
        Log.e(TAG, "addTextList: "+textStickerList )

        undoredoList[undoRedoPointer.get()!!].data.textSticker = sticker
        undoredoList[undoRedoPointer.get()!!].data.oldSticker = sticker
//        undoredoList.forEach {
//               it.data.textSticker = sticker
//               it.data.oldSticker = sticker
//
//        }



    }

    private fun recordPlayPause() {
        if (isRecordAudioPermission()) {
            Log.e(TAG, "RecorderHelper: " + videoEditViewModel.audioRecordingStatus.get())
            if (videoEditViewModel.audioRecordingStatus.get() == 0 || videoEditViewModel.audioRecordingStatus.get() == 2) {
                binding.ivRecord.setImageResource(R.drawable.ic_recordplay)
            } else {
                binding.ivRecord.setImageResource(R.drawable.ic_recordpause)
                binding.llRecord.visible()
                binding.horizontalScroll.visible()
                binding.rvFrame2.visible()
                binding.recordSeekWave.visible()
                binding.effectsSeekWave.hide()
                binding.audioSeekWave.visible()
                binding.ivRecord.hide()
                binding.tvRecordTime.hide()
            }
            if (videoEditViewModel.audioRecordingStatus.get() == 0)
                playPauseVideo(true)
            recorderHelper.startPauseStopRecording()
        }

    }

    fun isRecordAudioPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.RECORD_AUDIO
                )
            ) {
                prefs[PreferenceHelper.PREF_AUDIO_RECORD] = true
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.RECORD_AUDIO
                    ),
                    PERMISSION_RECORD_AUDIO
                )
                return false
            } else {
                if (!prefs.getBoolean(PreferenceHelper.PREF_AUDIO_RECORD, false)) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(
                            Manifest.permission.RECORD_AUDIO
                        ),
                        PERMISSION_RECORD_AUDIO
                    )
                } else {
                    object : CustomAlertDialog(
                        this,
                        "",
                        String.format(
                            getString(R.string.msg_enable_permission),
                            getString(R.string.record_audio),
                            getString(R.string.record_audio).lowercase(
                                Locale.getDefault()
                            )
                        ),
                        getString(R.string.txt_ok),
                        ""
                    ) {

                    }
                }
                return false
            }
        } else
            return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_RECORD_AUDIO -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    binding.ivRecord.setImageResource(R.drawable.ic_recordplay)
                    recorderHelper.startPauseStopRecording()
                }
                return
            }
        }
    }

    private fun saveAudioRecordings() {
        Log.e(TAG, "addAudioFileInList11: " + videoEditViewModel.audioRecordingStatus.get())
        if (videoEditViewModel.audioRecordingStatus.get() != 0) {
//            if (videoEditViewModel.getSelectedMusic().size > 0)
            videoEditViewModel.isShowMultipleMusic.set(true)
            recorderHelper.saveRecording()
            selectedFunction = ADD_MUSIC
        } else
            object : CustomAlertDialog(
                this,
                "",
                getString(R.string.msg_start_recording),
                getString(R.string.txt_ok),
                ""
            ) {
                /**
                 * ovveride method to handle click
                 */
            }
    }

    private fun editChangesDone() {
        if (binding.ccText.visibility == View.VISIBLE) {
            binding.llItemView.visible()
            binding.rvVideoTextEdit.visible()
            binding.rvTextFrame.visible()
            binding.rvVideoEditBtn.hide()
            binding.ivAddGallery.hide()
            binding.audioSeekWave.hide()
            binding.recordSeekWave.hide()
            binding.effectsSeekWave.hide()
            binding.includeToolbar.btnSave.hide()
            binding.includeToolbar.btnDiscard.hide()
            binding.ccText.hide()
            binding.flFrames.hide()
            createVideoTextBtnList()
            binding.horizontalScroll.visible()

            binding.rvFrame2.post {
                binding.rvFrame2.setPadding(
                    0,
                    0,
                    0,
                    0
                )
            }

            for (i in 0 until textStickerList.size){
                textStickerList[i].duplicatetext!!.forEach {
                    it.bitmap = textAsBitmap(it.sticker!! as TextSticker)
                }
            }


        } else if (binding.rvVideoTextEdit.visibility == View.VISIBLE) {
            binding.ccTop.layoutParams.height =
                resources.displayMetrics.heightPixels / 2 + 50
            createVideoEditBtnList()

            binding.llItemView.visible()
            binding.ivAddGallery.visible()
            binding.rvVideoEditBtn.visible()
            binding.includeToolbar.btnSave.visible()
            binding.includeToolbar.btnDiscard.visible()
            binding.rvVideoTextEdit.hide()
            binding.includeToolbar.btnTick.hide()
            binding.rvTextFrame.hide()
            binding.ccText.hide()
            binding.flFrames.visible()
            binding.horizontalScroll.hide()


            var pos = 0
            var max = Int.MIN_VALUE
            textfinallineList.clear()
            textlineList.clear()
            for (i in 0 until textStickerList.size) {
                var max = Int.MIN_VALUE
                for (m in 0 until textStickerList[i].duplicatetext!!.size) {
                    if (textStickerList[i].duplicatetext!![m].length > max) {
                        max = textStickerList[i].duplicatetext!![m].length
                        textlineList.addAll(textStickerList[i].duplicatetext!!)
                    }
                }
            }
            textfinallineList.add(CopyModel(textlineList,((TimeUnit.MILLISECONDS.toSeconds(totalVideoDuration)) * SEGMENT_VIDEO).toInt()))

            binding.rvFrame.adapter?.notifyDataSetChanged()

        } else if (binding.rvVideoEditMusic.visibility == View.VISIBLE) {
            Log.e(TAG, "editChangesDone:1 "+selectedFunction )
            binding.includeToolbar.btnSave.visible()
            binding.includeToolbar.btnDiscard.visible()
            binding.videoSeekBar.visible()
            binding.tvVideoDuration.visible()
            binding.includeToolbar.btnTick.hide()
            binding.ivAddGallery.visible()
            binding.rvFrame.visible()
            binding.flFrames.visible()
            createVideoEditBtnList()
            if (selectedFunction == MUSIC_MULTIPLE)
                saveMusicChangesOnVideo()
            binding.rvFrame.post {
                binding.rvFrame.setPadding(
                    binding.llframe.width / 2,
                    0,
                    binding.llframe.width / 2,
                    0
                )

                musicLineList.clear()
                var max = Int.MIN_VALUE
                for (i in 0 until videoEditViewModel.getSelectedMusic().size) {
                        if (videoEditViewModel.getSelectedMusic()[i].endTime > max) {
                            max = videoEditViewModel.getSelectedMusic()[i].endTime.toInt()
                            musicLineList.add(videoEditViewModel.getSelectedMusic()[i])
                        }
                }
                Log.e(TAG, "editChangesDone:musicLineList>> "+musicLineList )
                binding.rvFrame.adapter?.notifyDataSetChanged()

            }
        } else if (binding.rvVideoMusic.visibility == View.VISIBLE) {
            Log.e(TAG, "editChangesDone:2 "+selectedFunction )
            binding.includeToolbar.btnSave.visible()
            binding.includeToolbar.btnDiscard.visible()
            binding.videoSeekBar.visible()
            binding.tvVideoDuration.visible()
            binding.includeToolbar.btnTick.hide()
            binding.ivAddGallery.visible()
            binding.rvFrame.visible()
            binding.flFrames.visible()
            createVideoEditBtnList()
            if (selectedFunction == ADD_MUSIC)
                saveMusicChangesOnVideo()
            binding.rvFrame.post {
                binding.rvFrame.setPadding(
                    binding.llframe.width / 2,
                    0,
                    binding.llframe.width / 2,
                    0
                ) }



        } else {
            Log.e(TAG, "editChangesDone:3 "+selectedFunction )
            createVideoEditBtnList()
            binding.llItemView.visible()
            binding.ccVideoPlay.visible()
            binding.videoSeekBar.visible()
            binding.tvVideoDuration.visible()
            binding.rvVideoEditBtn.visible()
            binding.includeToolbar.btnSave.visible()
            binding.ivAddGallery.visible()
            binding.includeToolbar.btnDiscard.visible()
            binding.includeToolbar.btnTick.hide()
            binding.rvVideoTextEdit.hide()
            binding.rvTextFrame.hide()
            binding.ccText.hide()
            binding.horizontalScroll.hide()
        }

    }

    private fun copySticker(textSticker: TextSticker, strTag: String) :TextSticker{
            val sticker = TextSticker(this)
        sticker.text = textSticker.text
        sticker.textWidth = textSticker.textWidth
        sticker.textHeight = textSticker.textHeight

        sticker.setMatrix(textSticker.matrix)
        sticker.setTextColor(textSticker.color)
        sticker.setTextAlign(textSticker.alignment())
        sticker.setMaxTextSize(dpToPx(textSticker.size))
        sticker.id = textSticker.id
        if (textSticker.borderColor != 0 && textSticker.borderType != "") {
            sticker.setPaintToOutline(textSticker.borderColor, textSticker.borderType)
        }
        if (textSticker.pattern != 0) {
            sticker.pattern = textSticker.pattern
        }
        if (  textSticker.gradient != null && textSticker.gradient.isNotEmpty()) {
            sticker.setGradient(textSticker.gradient[0],textSticker.gradient[1])
        }

        sticker.setTypeface(textSticker.typeface)
        sticker.fontStyle = textSticker.fontStyle
        sticker.resizeText()
        if (strTag == ""){
            sticker.setStrTag(textSticker.getstrtag())
        }else{
            sticker.setStrTag(strTag)

        }
        sticker.startTime = textSticker.startTime
        sticker.endTime = textSticker.endTime
        binding.llAddTextView.llAlignment1.skLineSpacing.progress = 0
        binding.llAddTextView.llAlignment1.skLetterSpacing.progress = 0
         return sticker
    }

    private fun handleTrim(isFirstTime: Boolean) {
        selectedFunction = TRIM
        if (isFirstTime) {
            tempCurrentPosition = currentPosition
            tempStartTime = arrVideo[tempCurrentPosition].startDuration

            videoFrameHelpercuttor = VideoFrameCutterHelper(
                binding.videoFramecuttorView,
                0,
                { //onProgressChanged
                        startTime, endTime, action ->
                    if (!isChangeInTrim) isChangeInTrim = true
                    if (action == VideoFrameHelper.PROGRESS_LEFT) {
                        isLeftRightTrim = true
                        binding.videoSeekBar.setProgress(startTime.toFloat())
                        binding.tvVideoDuration.text = convertMillieToHMS(startTime)
                    } else if (action == VideoFrameHelper.PROGRESS_RIGHT) {
                        isLeftRightTrim = true
                        binding.videoSeekBar.setProgress(endTime.toFloat())
                        binding.tvVideoDuration.text = convertMillieToHMS(endTime)
                    } else if (action == VideoFrameHelper.PROGRESS_PLAY) {
                        isLeftRightTrim = false
                        binding.videoSeekBar.setProgress(startTime.toFloat())
                        binding.tvVideoDuration.text = convertMillieToHMS(startTime)


                    }
                }, {
                    //onDidStartDragging
                    playPauseVideo(true)
                    isSeeking = true
                    isChangingFromTrim = true
                }
            ) {
                //onDidStopDragging
                    startTime, endTime ->
                playPauseVideo(true)
                tempStartTime = startTime
                tempEndTime = endTime
                if (isLeftRightTrim){
                val data = UndoRedoData2(
                    null,
                    null,
                    null,
                    FilterType.DEFAULT,
                    0,
                    tempStartTime,
                    tempEndTime,
                    null,
                    tempCurrentPosition

                )
                addToChangeList(ACTION_TRIM, 0,data)}


                setSelectedVideoAsSingleSource(
                    tempStartTime,
                    tempEndTime
                )

                setSeekBarForOneVideo(
                    0,
                    arrVideo[tempCurrentPosition].videoDuration,
                    0
                )

                isSeeking = false
            }
            videoFrameHelpercuttor.initialize(
                arrVideo[tempCurrentPosition].videoUri,
                arrVideo[tempCurrentPosition].videoDuration,
                arrVideo[tempCurrentPosition].startDuration,
                arrVideo[tempCurrentPosition].endDuration,
                arrVideo[tempCurrentPosition].isTrimmed,
                arrVideo[tempCurrentPosition].isSplit, false
            )

            setSeekBarForOneVideo(
                0,
                arrVideo[tempCurrentPosition].videoDuration,
                0
            )
            setSelectedVideoAsSingleSource(
                arrVideo[tempCurrentPosition].startDuration,
                if (arrVideo[tempCurrentPosition].isSplit) arrVideo[tempCurrentPosition].startDuration + arrVideo[tempCurrentPosition].endDuration else arrVideo[tempCurrentPosition].endDuration
            )


//            setVisibilityOfMediaController(false)
        } else {
            videoFrameHelpercuttor.initialize(
                arrVideo[tempCurrentPosition].videoUri,
                arrVideo[tempCurrentPosition].videoDuration,
                arrVideo[tempCurrentPosition].startDuration,
                arrVideo[tempCurrentPosition].endDuration,
                arrVideo[tempCurrentPosition].isTrimmed,
                arrVideo[tempCurrentPosition].isSplit, false
            )
            setSeekBarForOneVideo(
                0,
                arrVideo[tempCurrentPosition].videoDuration,
                0
            )
            setSelectedVideoAsSingleSource(
                arrVideo[tempCurrentPosition].startDuration,
                if (arrVideo[tempCurrentPosition].isSplit) arrVideo[tempCurrentPosition].startDuration + arrVideo[tempCurrentPosition].endDuration else arrVideo[tempCurrentPosition].endDuration
            )
            Log.e(TAG, "updateCountDownTimer1: " )
            updateCountDownTimer()
        }

        binding.videoSeekBar.setProgress(tempStartTime.toFloat())
        binding.tvVideoDuration.text = convertMillieToHMS(tempStartTime)
        isChangeInTrim = false
    }

    private fun saveTrimChangeOnVideo() {
        saveNextChangeVideo()
        totalVideoDuration = 0
        Log.e(TAG, "saveTrimChangeOnVideo: " + arrVideo)
        for (i in 0 until arrVideo.size) {
            totalVideoDuration += arrVideo[i].endDuration - arrVideo[i].startDuration
        }

        isChangingFromTrim = false
        updateDurationNPlayVideo()
        frames.clear()
        binding.rvFrame.adapter?.notifyDataSetChanged()
        mediaMetadataRetriever = null
        addVideoFrames(
            ((TimeUnit.MILLISECONDS.toSeconds(arrVideo[0].startDuration))).toInt(),
            0,
            arrVideo[0].endDuration,
            arrVideo[0].videoUri!!
        )

        resetSeekBar()
        setVisibilityOfViewOnCancel()
//        updateDurationNPlayVideo()
        trimVisibility()
        currentPosition = 0


    }

    private fun saveNextChangeVideo() {
        if (isChangeInTrim) {
            arrVideo[tempCurrentPosition].startDuration = tempStartTime
            arrVideo[tempCurrentPosition].endDuration = tempEndTime
            arrVideo[tempCurrentPosition].isTrimmed = true
//            isChangingFromTrim = false
        }
    }

    private fun discardVideoDialog() {
         discardDialog = Dialog(this, R.style.DialogCustomTheme)
        discardDialog.setCancelable(false)

        val binding: DialogBottomDiscardBinding = DataBindingUtil.inflate(
            LayoutInflater.from(this),
            R.layout.dialog_bottom_discard,
            null,
            false
        )
        val window: Window? = discardDialog.window
        val layoutParams: WindowManager.LayoutParams = window!!.attributes
        layoutParams.gravity = Gravity.BOTTOM
        discardDialog.setContentView(binding.root)
        discardDialog.show()

        binding.tvNo.setOnClickListener {
            discardDialog.dismiss()
        }

        binding.tvDiscard.setOnClickListener {
            videoLibraryViewModel.arrSelectedVideo.clear()
            val intent = Intent(this, VideoListActivity::class.java)
            startActivityForResult(intent, 100)
            finish()
            discardDialog.dismiss()

        }
    }

    private fun saveVideoDialog() {
         saveDialog = Dialog(this, R.style.DialogCustomTheme)
        //set false for testing
        saveDialog.setCancelable(true)

        val binding: DialogExportVideoBinding = DataBindingUtil.inflate(
            LayoutInflater.from(this),
            R.layout.dialog_export_video,
            null,
            false
        )
        val window: Window? = saveDialog.window
        val layoutParams: WindowManager.LayoutParams = window!!.attributes
        layoutParams.gravity = Gravity.BOTTOM
        saveDialog.setContentView(binding.root)
        saveDialog.show()

        binding.imgCancle.setOnClickListener {
            saveDialog.dismiss()
        }
        binding.tvSavetoGallery.setOnClickListener {
//            if (selectedFunction != -1) {
//                object : CustomAlertDialog(
//                    this,
//                    "",
//                    getString(R.string.msg_save_changes),
//                    getString(android.R.string.ok),
//                    ""
//                ) {
//                    /**
//                     * You can override method
//                     */
//                }
//            } else {
                object : CustomAlertDialog(
                    this,
                    "",
                    getString(R.string.msg_finish_editing),
                    getString(android.R.string.ok),
                    getString(android.R.string.cancel)
                ) {
                    override fun onClick(id: Int) {
                        super.onClick(id)
                        if (id == R.id.btnPositive) {
                            if (totalVideoDuration < 10 * 60 * 1000)
                                applyChangesOnVideo()
                            showExportDialog(
                                videoEditViewModel,
                                getString(R.string.exporting_video)
                            )
                        } else {
                            object : CustomAlertDialog(
                                this@VideoEditorActivity,
                                "",
                                getString(R.string.msg_video_time_limit_exceed),
                                getString(android.R.string.ok),
                                ""
                            ) {
                                /**
                                 * You can override method
                                 */
                            }
                        }
//                    }
                }
            }
        }
    }

    private fun colorPickerDialog() {
        val colorDialog = Dialog(this, R.style.DialogCustomTheme)
        colorDialog.setCancelable(false)

        val bindingcolor: ColorPickerDialogBinding = DataBindingUtil.inflate(
            LayoutInflater.from(this),
            R.layout.color_picker_dialog,
            null,
            false
        )
        val window: Window? = colorDialog.window
        val layoutParams: WindowManager.LayoutParams = window!!.attributes
        layoutParams.gravity = Gravity.BOTTOM
        colorDialog.setCancelable(true)
        colorDialog.setContentView(bindingcolor.root)

        colorDialog.show()
        if (colorID != 0) {
            bindingcolor.colorWheel.rgb = colorID
            bindingcolor.gradientSeekBar.setTransparentToColor(bindingcolor.colorWheel.rgb)
            bindingcolor.valueSeekBar.setBlackToColor(bindingcolor.colorWheel.rgb)
            bindingcolor.valueSeekBar.thumbStrokeColor = colorID
            bindingcolor.valueSeekBar.offset = colorOffset
        }

        bindingcolor.colorWheel.colorChangeListener = { rgb: Int ->
            bindingcolor.gradientSeekBar.setTransparentToColor(rgb)
            bindingcolor.valueSeekBar.setBlackToColor(rgb)
        }

        bindingcolor.btnCancel.setOnClickListener {
            colorDialog.dismiss()
        }

        bindingcolor.btnDone.setOnClickListener {
            if (binding.stickerView.currentSticker != null) {
                colorOffset = bindingcolor.valueSeekBar.offset.toFloat()
                colorID = bindingcolor.valueSeekBar.argb
                colorType = true
                (binding.stickerView.currentSticker as TextSticker).setTextColor(
                    bindingcolor.valueSeekBar.argb
                )
                notifyStickerView()
            }
            if (borderColor != 0){
                if (borderType == MULTICOLOR_BORDER){
                    (binding.stickerView.currentSticker as TextSticker).setPaintToOutline(
                        borderColor, MULTICOLOR_BORDER
                    )
                }else{
                    (binding.stickerView.currentSticker as TextSticker).setPaintToOutline(
                        ContextCompat.getColor(
                            this,
                            borderColor
                        ), borderType)
                }
                notifyStickerView()
            }
            colorDialog.dismiss()
        }

    }

    private fun colorBorderPickerDialog() {
        val colordialog = Dialog(this, R.style.DialogCustomTheme)
        colordialog.setCancelable(false)

        val bindingcolor: ColorPickerDialogBinding = DataBindingUtil.inflate(
            LayoutInflater.from(this),
            R.layout.color_picker_dialog,
            null,
            false
        )
        val window: Window? = colordialog.window
        val layoutParams: WindowManager.LayoutParams = window!!.attributes
        layoutParams.gravity = Gravity.BOTTOM
        colordialog.setCancelable(true)
        colordialog.setContentView(bindingcolor.root)

        colordialog.show()
        if (borderID != 0) {
            bindingcolor.colorWheel.rgb = borderID
            bindingcolor.gradientSeekBar.setTransparentToColor(bindingcolor.colorWheel.rgb)
            bindingcolor.valueSeekBar.setBlackToColor(bindingcolor.colorWheel.rgb)
            bindingcolor.valueSeekBar.thumbStrokeColor = borderID
            bindingcolor.valueSeekBar.offset = borderOffset
        }

        bindingcolor.colorWheel.colorChangeListener = { rgb: Int ->
            bindingcolor.gradientSeekBar.setTransparentToColor(rgb)
            bindingcolor.valueSeekBar.setBlackToColor(rgb)
        }

        bindingcolor.btnCancel.setOnClickListener {
            colordialog.dismiss()
        }

        bindingcolor.btnDone.setOnClickListener {
            if (binding.stickerView.currentSticker != null) {
                borderOffset = bindingcolor.valueSeekBar.offset.toFloat()
                borderID = bindingcolor.valueSeekBar.argb
                borderColor = bindingcolor.valueSeekBar.argb
                borderType = MULTICOLOR_BORDER
                (binding.stickerView.currentSticker as TextSticker).setPaintToOutline(
                    bindingcolor.valueSeekBar.argb, MULTICOLOR_BORDER
                )
                notifyStickerView()
            }
            colordialog.dismiss()
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setStickerViewListener() {

        binding.stickerView.onStickerOperationListener =
            object : StickerView.OnStickerOperationListener {
                override fun onStickerAdded(sticker: Sticker) {
                    isOutSizeofStickerTouch = false
                }

                override fun onStickerClicked(sticker: Sticker) {
                    Log.e(TAG, "onStickerClicked: ")
                    isOutSizeofStickerTouch = false
                    playPauseVideo(true)
                    val bounds = Rect()


                    if (sticker is TextSticker) {
                        sticker.textPaint.getTextBounds(
                            sticker.text,
                            0,
                            sticker.text?.length!!,
                            bounds
                        )

                        val fmPaint: Paint.FontMetricsInt = sticker.textPaint.fontMetricsInt
                        val height = bounds.height()
                        val textHeight: Int = fmPaint.descent - fmPaint.ascent
                        val lineHeight: Int = fmPaint.bottom - fmPaint.top + fmPaint.leading
                        var y = 0f

                        updateXyPositionOfTextSticker()

                        if (abs(fmPaint.ascent) < Math.abs(bounds.top))
                            y = sticker.mappedBound.top
                        else
                            y =
                                sticker.mappedBound.top + (Math.abs(fmPaint.top) - Math.abs(bounds.top)) * sticker.currentScale
                    } else {
                        sticker as DrawableSticker

                    }

                }

                override fun onStickerDeleted(sticker: Sticker) {
                    if (!isPlay) {
                        removeStickerFromList(sticker)

                    }
                }

                override fun onStickerDragFinished(sticker: Sticker) {
                    if (sticker is TextSticker)
                        sticker.resizeText()
                    var sticker2 = sticker as TextSticker
                    Log.e(TAG, "onStickerDragFinished: x >>> ${sticker2.x} y>> ${sticker2.y}")
                    updateXyPositionOfTextSticker()
                    playPauseVideo(true)
                }

                override fun onStickerTouchedDown(sticker: Sticker) {
                    Log.e(TAG, "onStickerTouchedDown: ")
                    Log.e(TAG, "onStickerDragFinished: width >>> ${sticker.width} height>> ${sticker.height}")
                    playPauseVideo(true)
                }

                override fun onStickerZoomFinished(sticker: Sticker) {
                    Log.e(TAG, "onStickerZoomFinished: ")
                    isOutSizeofStickerTouch = false
                    if (sticker is TextSticker)
                        sticker.resizeText()
                    playPauseVideo(true)

                }

                override fun onStickerFlipped(sticker: Sticker) {
                    isOutSizeofStickerTouch = false
                    playPauseVideo(true)

                }

                override fun onStickerDoubleTapped(sticker: Sticker) {
                    Log.e(TAG, "onStickerDoubleTapped: ")
                    strTextSticker = sticker as TextSticker
                    isOutSizeofStickerTouch = false

                    binding.stickerView.removeStickerlist(sticker)

                    if (sticker is TextSticker) {
                        strTextSticker = sticker
                        binding.includeToolbar.btnTick.hide()
                        isEdit = true
                        openTextEditingMenu(
                            sticker.text.toString(),
                            sticker.getstrtag(),
                            sticker.endTime,
                            sticker.startTime
                        )

                    }
                }
            }
        binding.stickerView.setOnTouchListener { view, motionEvent ->
            binding.stickerView.showBorder = true
            binding.stickerView.showIcons = true

            if (!binding.stickerView.isLocked)
                binding.stickerView.isLocked = isOutSizeofStickerTouch
            false
        }

    }

    private fun removeStickerFromList(sticker: Sticker) {
        val stickerList: ArrayList<CopyModel> = ArrayList()
        stickerList.addAll(textStickerList)

        if (textStickerList.size != 0) {
            for (m in 0 until textStickerList.size) {
                textStickerList[m].duplicatetext!!.filter { it.sticker == sticker }.forEach { textStickerList[m].duplicatetext!!.remove(it) }

                if (textStickerList[m].duplicatetext!!.size == 0) {
                    if (!isUndoRedo){
                        itemWidth += 40
                        binding.ccTop.layoutParams.height =
                            binding.frVideoController.height + itemWidth
                    }

                    textStickerList.removeAt(m)
                    break
                }
            }
        }
//        textStickerList.clear()
//        textStickerList.addAll(stickerList)
        binding.rvTextFrame.adapter = null
        binding.rvTextFrame.adapter = TextCopyAdapter(this, textStickerList, this)
        binding.rvTextFrame.adapter!!.notifyDataSetChanged()
        binding.rvTextLine.adapter = null
        binding.rvTextLine.adapter = TextLineAdapter(this, textStickerList)
        binding.rvTextLine.adapter!!.notifyDataSetChanged()



    }

    private fun replaceStickerFromList(sticker: Sticker, newsticker: Sticker) {
        for (i in 0 until textStickerList.size) {
            for (m in 0 until textStickerList[i].duplicatetext!!.size) {
                if (textStickerList[i].duplicatetext!![m].sticker == sticker) {
                    textStickerList[i].duplicatetext!![m].sticker = newsticker
                }
            }

        }
    }

    private fun updateXyPositionOfTextSticker() {
        textStickerList.forEach { it ->
            it.duplicatetext!!.forEach {
                if (it.sticker is TextSticker) {
                    it.x = getStickerXPosition(it.sticker as TextSticker).toFloat()
                    it.y = getStickerYPosition(it.sticker as TextSticker).toFloat()
                } else {
                    it.x = getStickerXPosition(it.sticker as DrawableSticker).toFloat()
                    it.y = getStickerYPosition(it.sticker as DrawableSticker).toFloat()
                }
                Log.d("TAG", "x${it.x}----->y${it.y}")
            }
        }
        Log.e(TAG, "updateXyPositionOfTextSticker: " + textStickerList)
    }

    /*open textinput fragment to enter text */
    private fun openTextEditingMenu(text: String, strTag: String, endTime: Long, startTime: Long) {
        val textEditorDialogFragment = TextEditingFragment().newInstance()
        textEditorDialogFragment.setStyle(
            DialogFragment.STYLE_NO_TITLE, R.style.ThemeOverlay_AppCompat_Dialog
        )
        val bundle = Bundle()
        bundle.putString("text", text)
        bundle.putInt("text_size", 100)
        bundle.putFloat("line_spacing", 0f)
        bundle.putFloat("letter_spacing", 0f)
        bundle.putInt("font", 1)
        bundle.putLong("start_time", startTime)
        bundle.putLong("end_time", endTime)
        bundle.putString("str_tag", strTag)
        bundle.putInt("color", Color.BLACK)
        bundle.putInt("gravity", Gravity.CENTER)
        textEditorDialogFragment.arguments = bundle
        textEditorDialogFragment.show(supportFragmentManager, "text_editor_dialog_fragment")

        textEditorDialogFragment.OnDone(this)

    }

    private fun getStickerXPosition(sticker: Sticker): Double {
        if (sticker is TextSticker) {
            val width = widthText * sticker.currentScale
            val rAngle: Float = sticker.currentAngle
            val height =
                (sticker.textPaint.fontMetrics.descent - sticker.textPaint.fontMetrics.ascent) * sticker.currentScale
            val x1: Double =
                (sticker.transX + (sticker.currentWidth - width) / 2).toDouble()
            val x2: Double = x1 + width * Math.cos(sticker.getRadian(rAngle))
            val x3: Double = x2 + height * Math.cos(sticker.getRadian(rAngle + 90))
            val x4: Double =
                x3 + width * Math.cos(sticker.getRadian(rAngle + 180))
            return Math.min(x1, Math.min(x2, Math.min(x3, x4)))


        } else {
            sticker as DrawableSticker
            val rAngle: Float = sticker.currentScale
            val x1: Double =
                (sticker.mappedBound.left).toDouble()
            val x2: Double = x1 + sticker.currentWidth * Math.cos(sticker.getRadian(rAngle))
            val x3: Double = x2 + sticker.currentHeight * Math.cos(sticker.getRadian(rAngle + 90))
            val x4: Double =
                x3 + sticker.currentWidth * Math.cos(sticker.getRadian(rAngle + 180))
            return Math.min(x1, Math.min(x2, Math.min(x3, x4)))
        }

    }

    private fun getStickerYPosition(sticker: Sticker): Double {
        if (sticker is TextSticker) {

            val rAngle: Float = sticker.currentAngle
            val height =
                sticker.textPaint.fontMetrics.descent - sticker.textPaint.fontMetrics.ascent
            val bounds = Rect()
            sticker.textPaint.getTextBounds(sticker.text, 0, sticker.text?.length!!, bounds)
            val y =
                sticker.transY.toDouble() + (abs(sticker.textPaint.fontMetrics.top) - abs(
                    bounds.top
                )) * sticker.currentScale

            val y1: Double = y.toDouble()
            val y2: Double =
                y1 + widthText * sticker.currentScale * sin(sticker.getRadian(rAngle))
            val y3: Double = y2 + height * sin(sticker.getRadian(rAngle + 90))
            val y4: Double =
                y3 + widthText * sticker.currentScale * sin(sticker.getRadian(rAngle + 180))
            return min(y1, min(y2, min(y3, y4)))

        } else {
            sticker as DrawableSticker

            val rAngle: Float = sticker.currentScale
            val y =
                sticker.mappedBound.top
            val y1: Double = y.toDouble()
            val y2: Double =
                y1 + sticker.currentWidth * sin(sticker.getRadian(rAngle))
            val y3: Double = y2 + sticker.currentHeight * sin(sticker.getRadian(rAngle + 90))
            val y4: Double =
                y3 + sticker.currentWidth * sin(sticker.getRadian(rAngle + 180))
            return y1.coerceAtMost(y2.coerceAtMost(y3.coerceAtMost(y4)))
        }

    }

    private fun createVideoEditBtnList() {
        arrVideoEditBtn.clear()
        arrVideoEditBtn.add(VideoEditBtn(getString(R.string.text), R.drawable.ic_text))
        arrVideoEditBtn.add(VideoEditBtn(getString(R.string.music), R.drawable.ic_music))
        arrVideoEditBtn.add(VideoEditBtn(getString(R.string.trim), R.drawable.ic_cutter))
        arrVideoEditBtn.add(VideoEditBtn(getString(R.string.filter), R.drawable.ic_filter))


        videoEditBtnAdapter = VideoEditBtnAdapter(arrVideoEditBtn, this)
        binding.rvVideoEditBtn.layoutManager =
            GridLayoutManager(this, 4)

        binding.rvVideoEditBtn.adapter = videoEditBtnAdapter

    }

    private fun createVideoTextBtnList() {
        playPauseVideo(true)
        binding.rvVideoEditBtn.hide()
        binding.rvVideoTextEdit.visible()
        binding.rvTextFrame.visible()
        binding.recordSeekWave.hide()
        binding.audioSeekWave.hide()
        binding.includeToolbar.btnSave.hide()
        binding.includeToolbar.btnDiscard.hide()
        binding.ivAddGallery.hide()
        binding.includeToolbar.btnTick.visible()
//        binding.ccTop.con(view.getId(), ConstraintSet.MATCH_CONSTRAINT_SPREAD);

        arrVideTextBtn.clear()
        arrVideTextBtn = ArrayList()
        arrVideTextBtn.add(VideoEditBtn(getString(R.string.text), R.drawable.ic_textedit))
        arrVideTextBtn.add(VideoEditBtn(getString(R.string.edit), R.drawable.ic_edit))
        arrVideTextBtn.add(VideoEditBtn(getString(R.string.delete), R.drawable.ic_delete_black))
        arrVideTextBtn.add(VideoEditBtn(getString(R.string.copy), R.drawable.ic_copy))
        arrVideTextBtn.add(VideoEditBtn(getString(R.string.duplicate), R.drawable.ic_duplicate))


        //layout of sticker text timeline
        layoutManagerText = LinearLayoutManager(this, RecyclerView.VERTICAL, true)
        binding.rvTextFrame.layoutManager = layoutManagerText
        binding.rvTextFrame.adapter = TextCopyAdapter(this, textStickerList, this)

        //layout of sticker text timeline for line
        layoutManagerline = LinearLayoutManager(this, RecyclerView.VERTICAL, true)
        binding.rvTextLine.layoutManager = layoutManagerline
        binding.rvTextLine.adapter = TextLineAdapter(this, textStickerList)

        if (textStickerList.size == 5) {
//            binding.frVideoController.layoutParams.height  =  resources.displayMetrics.heightPixels / 2 + 100
            binding.ccTop.layoutParams.height = binding.frVideoController.height - 400


        } else if (textStickerList.size == 4) {
//            binding.frVideoController.layoutParams.height  =  resources.displayMetrics.heightPixels / 3 + 200
            binding.ccTop.layoutParams.height = binding.frVideoController.height - 300

        } else if (textStickerList.size == 3) {
//            binding.frVideoController.layoutParams.height  = resources.displayMetrics.heightPixels / 2 - 50
            binding.ccTop.layoutParams.height = binding.frVideoController.height - 150

        } else if (textStickerList.size == 2) {
//            binding.frVideoController.layoutParams.height  = resources.displayMetrics.heightPixels / 2 - 50
            binding.ccTop.layoutParams.height = binding.frVideoController.height - 60

        }


        binding.rvVideoTextEdit.layoutManager =
            GridLayoutManager(this, 5)
        videotextBtnAdapter = VideoTextEditAdapter(arrVideTextBtn) { it ->
            when (it) {
                0 -> {
                    //add text sticker
                    addTextSticker()
                }
                1 -> {
                    //edit text sticker
                    editTextSticker()
                }
                2 -> {
                    //delete text sticker
                    deleteTextSticker()

                }
                3 -> {
                    //copy text sticker
                    copyTextSticker()
                }
                4 -> {
                    //duplicate text sticker
                    duplicateTextSticker()
                }

            }
        }
        binding.rvVideoTextEdit.adapter = videotextBtnAdapter
    }

    private fun createVideoMusicBtnList() {
        selectedFunction = ADD_MUSIC
        arrVideoEditBtn.clear()
        arrVideoEditBtn.add(VideoEditBtn(getString(R.string.track), R.drawable.ic_track))
        arrVideoEditBtn.add(VideoEditBtn(getString(R.string.effects), R.drawable.ic_effect_))
        arrVideoEditBtn.add(VideoEditBtn(getString(R.string.record), R.drawable.ic_record_))
//        arrVideoEditBtn.add(VideoEditBtn(getString(R.string.track), R.drawable.icon_track))
//        arrVideoEditBtn.add(VideoEditBtn(getString(R.string.effects), R.drawable.ic_effect))
//        arrVideoEditBtn.add(VideoEditBtn(getString(R.string.record), R.drawable.ic_record))


        videoMusicBtnAdapter = VideoMusicBtnAdapter(arrVideoEditBtn, object : OnItemClick {
            override fun onItemClick(position: Int) {
                when (position) {
                    MUSIC -> {
                        isMusicFrom = false
                        bottomSheetMusicDialog()
                    }
                    EFFECTS -> {
                        isMusicFrom = true
                        bottomSheetMusicDialog()
                    }
                    RECORD -> {
                        binding.ccVideoPlay.hide()
                        binding.audioSeekWave.hide()
                        binding.effectsSeekWave.hide()
                        binding.recordSeekWave.hide()
                        binding.includeToolbar.btnTick.hide()
                        binding.includeToolbar.btnSave.hide()
                        binding.ccBottom.visible()
                        binding.videoSeekBar.hide()
                        binding.tvVideoDuration.hide()
                        binding.horizontalScroll.visible()
                        binding.rvFrame2.visible()
                        playPauseVideo(true)
                        handleRecord()
                    }
                }
            }

        })
        binding.rvVideoMusic.layoutManager = GridLayoutManager(this, 3)
        binding.rvVideoMusic.adapter = videoMusicBtnAdapter

    }

    private fun handleRecord() {
        videoEditViewModel.isShowMultipleMusic.set(false)
        selectedFunction = AUDIO_RECORDER
        recorderHelper = RecorderHelper(this, { //OnUpdateTime
                seconds ->
            var totaltime = (TimeUnit.MILLISECONDS.toSeconds(totalVideoDuration) ).toInt()
            videoEditViewModel.audioRecordingTime.set((seconds * 1000).toLong())
            binding.tvRecordTime.text =
                convertMillieToHMS(videoEditViewModel.audioRecordingTime.get(), true)
            Log.e(TAG, "handleRecord: "+totaltime +" second>>>> "+seconds)
//            if (totaltime < seconds){
//                videoEditViewModel.audioRecordingStatus.set(3)
//                recorderHelper.stopRecording()
//                binding.ivRecord.setImageResource(R.drawable.ic_recordpause)
//            }
        },
            //OnStart Pause Resume player
            {
                videoEditViewModel.audioRecordingStatus.set(it)
                if (videoEditViewModel.audioRecordingStatus.get() == 3) {
                    saveAudioRecordings()
                }
                Log.e(TAG, "handleRecord: " + it)
                Log.e(TAG, "handleRecord: " + videoEditViewModel.audioRecordingStatus.get())
            }, { //OnSaved file
                    filePath ->
                Log.e(TAG, "handleRecord:filepath " + filePath)
//                val finalOutputPath = getAudioFilePath(true)
//                Log.e(TAG, "handleRecord: "+finalOutputPath )
//                if (moveFile(filePath, finalOutputPath))
                object : CustomAlertDialog(
                    this,
                    "",
                    getString(R.string.msg_audio_saved),
                    getString(R.string.txt_ok),
                    ""
                ) {
                    override fun onClick(id: Int) {
                        super.onClick(id)
                        notifyItemAfterSaved(arrayOf(filePath))
                        try {
                            val audioDuration = getDurationFromUrl(filePath)
                            isFromAudio = IS_RECORD
                            addAudioFileInList(
                                MusicData(
                                    audioUrl = filePath,
                                    duration = audioDuration,
                                    startTime = 0,
                                    endTime = audioDuration,
                                    strstartTime = 0,
                                    strendTime = audioDuration,
                                    isfrom = IS_RECORD
                                )
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            })
        videoEditViewModel.audioRecordingStatus.set(0)
        binding.horizontalScroll.visible()
        binding.ivRecord.visible()
        binding.rvFrame2.visible()
        binding.tvRecordTime.visible()
        binding.audioSeekWave.hide()
        binding.effectsSeekWave.hide()
//        binding.recordSeekWave.hide()
        binding.rvVideoEditMusic.hide()
        binding.rvVideoMusic.hide()
        binding.rvTextFrame.hide()
        binding.rvTextLine.hide()
        binding.rvFrame.hide()
        binding.flFrames.hide()
    }

    private fun bottomSheetMusicDialog() {
        bindingMusic = DataBindingUtil.inflate(
            LayoutInflater.from(this),
            R.layout.bottom_sheet_add_music,
            null,
            false
        )

        bindingMusic.run {
            viewModel = videoEditViewModel
            lifecycleOwner = this@VideoEditorActivity
        }
//        if (!::musicSelectionDialog.isInitialized) {
        isFirstTimeLoadMusic = true
        musicSelectionDialog = BottomSheetDialog(this, R.style.DialogCustomTheme)
        musicSelectionDialog.setContentView(bindingMusic.root)
        musicSelectionDialog.show()

        val window: Window? = musicSelectionDialog.window
        val layoutParams: WindowManager.LayoutParams = window!!.attributes
        layoutParams.gravity = Gravity.BOTTOM
        musicSelectionDialog.setCancelable(true)
//        musicSelectionDialog.getBehavior().setDraggable(false)
        musicSelectionDialog.setOnDismissListener {
            if (selectedFunction != AUDIO_RECORDER) {
                if (videoEditViewModel.getSelectedMusic().isEmpty())
                    selectedFunction = ADD_MUSIC
            }

            videoEditViewModel.isShowMusicFolder.set(false)
            videoEditViewModel.isShowMusicList.set(true)
            resetMediaPlayer()
        }
        videoEditViewModel.getAudioFileFromStorage(this)

        musicSelectionDialog.show()
        isFirstTimeLoadMusic = false

//        } else {
//        isFirstTimeLoadMusic = false
//        videoEditViewModel.getAudioFileFromStorage(this)
//        musicSelectionDialog.show()
//    }
        Log.e(TAG, "bottomSheetMusicDialog: " + isMusicFrom)
        if (!isMusicFrom) {
            bindingMusic.toggle.check(R.id.rbDevice)
            bindingMusic.rvMusicDevice.visible()
            bindingMusic.rvMusicEffects.hide()
            bindingMusic.rvMusicDevice.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            bindingMusic.rvMusicDevice.adapter = videoEditViewModel.musicAdapter
        } else {
            bindingMusic.toggle.check(R.id.rbEffect)
            bindingMusic.rbEffect.setTextColor(ContextCompat.getColor(this, R.color.white))
            bindingMusic.rbDevice.setTextColor(ContextCompat.getColor(this, R.color.black))
            videoEditViewModel.isShowMusicFolder.set(true)
            videoEditViewModel.isShowMusicList.set(false)
            bindingMusic.rvMusicDevice.hide()
            bindingMusic.rvMusicEffects.visible()
            createMusicEffectList()

        }

        bindingMusic.toggle.setOnCheckedChangeListener { radioGroup, optionId ->
            run {
                when (optionId) {
                    R.id.rbDevice -> {
                        bindingMusic.toggle.check(R.id.rbDevice)
                        Log.e(
                            TAG,
                            "bottomSheetMusicDialog:rbDevice" + bindingMusic.toggle.checkedRadioButtonId
                        )
                        bindingMusic.rbEffect.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.black
                            )
                        )
                        bindingMusic.rbDevice.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.white
                            )
                        )
                        bindingMusic.rvMusicDevice.visible()
                        bindingMusic.rvMusicEffects.hide()
                        videoEditViewModel.onClickMyMusicBtn()
//                        if (isFirstTimeLoadMusic){
                        bindingMusic.rvMusicDevice.layoutManager =
                            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                        bindingMusic.rvMusicDevice.adapter = videoEditViewModel.musicAdapter
//                        }
                        bindingMusic.rvMusicDevice.adapter!!.notifyDataSetChanged()
                    }//        }
                    R.id.rbEffect -> {
                        bindingMusic.toggle.check(R.id.rbEffect)
                        Log.e(
                            TAG,
                            "bottomSheetMusicDialog:rbEffect " + bindingMusic.toggle.checkedRadioButtonId
                        )
                        bindingMusic.rbEffect.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.white
                            )
                        )
                        bindingMusic.rbDevice.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.black
                            )
                        )
                        bindingMusic.rvMusicDevice.hide()
                        bindingMusic.rvMusicEffects.visible()
                        createMusicEffectList()

                    }
                    // add more cases here to handle other buttons in the your RadioGroup
                }
            }
        }

    }

    private fun bottomSheetEffectDialog(effectname: String) {
        bindingEffects = DataBindingUtil.inflate(
            LayoutInflater.from(this),
            R.layout.bottom_sheet_effects,
            null,
            false
        )
//        bindingMusic.run {
//            viewModel = videoEditViewModel
//            lifecycleOwner = this@VideoEditorActivity
//        }
//        if (!::musicSelectionDialog.isInitialized) {

        effectDialog = BottomSheetDialog(this, R.style.DialogCustomTheme)
        effectDialog.setContentView(bindingEffects.root)
        effectDialog.behavior.isDraggable = false
        effectDialog.show()

        val window: Window? = effectDialog.window
        val layoutParams: WindowManager.LayoutParams = window!!.attributes
        layoutParams.gravity = Gravity.BOTTOM
        effectDialog.setCancelable(true)

        effectDialog.show()

        bindingEffects.tvEffectName.text = effectname

        bindingEffects.ivcancel.setOnClickListener {
            resetMediaPlayer()
            effectDialog.dismiss()
        }

        effectDialog.setOnDismissListener(object : DialogInterface.OnDismissListener {
            override fun onDismiss(dialogInterface: DialogInterface?) {
                Log.e(TAG, "onDismiss: ")
            }
        })
        var musicAdapter = MusicAdapter(musicEffect, videoEditViewModel) {
            musicEffect.forEachIndexed { position, data ->
                run {
                    if (position == it) {
                        data.itemisSelected = !data.itemisSelected
                        musicEffect[position] = data
                    } else {
                        data.itemisSelected = false
                        musicEffect[position] = data
                    }
                }
            }
            bindingEffects.rvEffects.adapter!!.notifyDataSetChanged()

        }

        bindingEffects.rvEffects.adapter = musicAdapter


    }

    private fun bottomSheetVolumeDialog(musicPos: Int) {
        playPauseVideo(true)
        bindingVolume = DataBindingUtil.inflate(
            LayoutInflater.from(this),
            R.layout.bottom_volume_dialog,
            null,
            false
        )

        volumeDialog = BottomSheetDialog(this, R.style.DialogCustomTheme)
        volumeDialog.setContentView(bindingVolume.root)
        volumeDialog.show()

        val window: Window? = volumeDialog.window
        val layoutParams: WindowManager.LayoutParams = window!!.attributes
        layoutParams.gravity = Gravity.BOTTOM
        volumeDialog.setCancelable(true)

        if (videoEditViewModel.getSelectedMusic()[musicPos].waves != null) {
            bindingVolume.videoFramecuttor.sample =
                videoEditViewModel.getSelectedMusic()[musicPos].waves
        }
        bindingVolume.videoFramecuttor.waveGap = 2.1f
        bindingVolume.videoFramecuttor.waveWidth = 3.1499999f

        Log.e(TAG, "bottomSheetVolumeDialog: "+videoEditViewModel.getSelectedMusic()[musicPos].duration )
        bindingVolume.tvMusicName.text = videoEditViewModel.getSelectedMusic()[musicPos].title
        bindingVolume.tvStartTime.text =
            convertMillieToHMS(videoEditViewModel.getSelectedMusic()[musicPos].startTime)
        bindingVolume.tvEndTime.text =
            convertMillieToHMS(videoEditViewModel.getSelectedMusic()[musicPos].duration)

        videoFrame = VideoFrameHelper(
            bindingVolume.videoFramecuttor,
            0,
            { //onProgressChanged
                    startTime, endTime, action ->
                musicStartTime = startTime
                musicEndTime = endTime
                Log.e(TAG, "bottomSheetVolumeDialog:musicStartTime>> " + musicStartTime)
                Log.e(TAG, "bottomSheetVolumeDialog:musicEndTime>> " + musicEndTime)
                if (action == VideoFrameHelper.PROGRESS_LEFT) {
                    bindingVolume.tvStartTime.text = convertMillieToHMS(startTime)
                    videoEditViewModel.getSelectedMusic()[musicPos].startTime = startTime

                } else if (action == VideoFrameHelper.PROGRESS_RIGHT) {

                    bindingVolume.tvStartTime.text = convertMillieToHMS(startTime)
                } else if (action == VideoFrameHelper.PROGRESS_PLAY) {
                    bindingVolume.tvStartTime.text = convertMillieToHMS(startTime)
                }
            }, {
                //onDidStartDragging
                playPauseVideo(true)
                playPauseAudio(videoEditViewModel.getSelectedMusic()[musicPos])


            }
        ) {
            //onDidStopDragging
                startTime, endTime ->
            playPauseVideo(true)
            playPauseAudio(videoEditViewModel.getSelectedMusic()[musicPos])
        }
        videoFrame.initialize(
            Uri.parse(videoEditViewModel.getSelectedMusic()[musicPos].audioUrl),
            videoEditViewModel.getSelectedMusic()[musicPos].duration,
            videoEditViewModel.getSelectedMusic()[musicPos].startTime,
            videoEditViewModel.getSelectedMusic()[musicPos].endTime,
            videoEditViewModel.getSelectedMusic()[musicPos].isTrimmed,
            false
        )

        bindingVolume.volumeseekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                videoEditViewModel.setAudioVolumeProgress(progress)
                tempAudioVolume = seekBar!!.progress
                bindingVolume.tvVolume.text = "( ${seekBar.progress}  %)"
                videoEditViewModel.getSelectedMusic()[musicPos].audioVolume = progress
                applyAudioVolumeOnPlayer()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                /**
                 * Called when start touch
                 */
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                tempAudioVolume = seekBar!!.progress
                applyAudioVolumeOnPlayer()
            }
        })

        bindingVolume.ivVolumeDone.setOnClickListener {
            saveVolumeChangesOnVideo(musicPos, musicStartTime, musicEndTime)
            seekBarMusicTimeline()

        }
        videoEditViewModel.setAudioVolumeProgress(arrVideo[tempCurrentPosition].audioVolume)


    }

    private fun saveVolumeChangesOnVideo(musicPos: Int, starttime: Long, endTime: Long) {
        videoEditViewModel.getSelectedMusic()[musicPos].startTime = starttime
        videoEditViewModel.getSelectedMusic()[musicPos].endTime = endTime
        arrVideo[tempCurrentPosition].audioVolume = videoEditViewModel.getAudioVolume()
        Log.e(TAG, "saveVolumeChangesOnVideo: " + starttime)
        Log.e(TAG, "saveVolumeChangesOnVideo: " + endTime)
        volumeDialog.dismiss()

    }

    fun createMusicEffectList() {
        arrMusicEffect.clear()
        arrMusicEffect.add(VideoEditBtn(getString(R.string.popular), R.drawable.icon_popular))
        arrMusicEffect.add(VideoEditBtn(getString(R.string.games), R.drawable.icon_games))
        arrMusicEffect.add(VideoEditBtn(getString(R.string.bells), R.drawable.icon_bells))
        arrMusicEffect.add(VideoEditBtn(getString(R.string.guitar), R.drawable.icon_guitar))
        arrMusicEffect.add(VideoEditBtn(getString(R.string.drum), R.drawable.icon_drum))
        arrMusicEffect.add(VideoEditBtn(getString(R.string.applause), R.drawable.icon_applause))
        arrMusicEffect.add(VideoEditBtn(getString(R.string.birds), R.drawable.icon_bird))
        arrMusicEffect.add(VideoEditBtn(getString(R.string.insects), R.drawable.icon_insects))
        arrMusicEffect.add(VideoEditBtn(getString(R.string.animal), R.drawable.icon_animal))
        arrMusicEffect.add(VideoEditBtn(getString(R.string.laugh), R.drawable.icon_laugh))
        arrMusicEffect.add(VideoEditBtn(getString(R.string.heartbeats), R.drawable.icon_heart))
        arrMusicEffect.add(VideoEditBtn(getString(R.string.footsteps), R.drawable.icon_foot))

        bindingMusic.rvMusicEffects.layoutManager =
            GridLayoutManager(this, 3)
        var videoMusicEffectAdapter = MusicEffectsAdapter(arrMusicEffect) {


            musicEffect = ArrayList()
            if (effectsList.isNotEmpty() && !effectsList.isNullOrEmpty()){
            for (i in 0 until effectsList[0].music.size) {
                if (arrMusicEffect[it].btnText == effectsList[0].music[i].catName) {
                    for (m in 0 until effectsList[0].music[i].listOfEffects.size) {
                        val cw = ContextWrapper(this)
                        var duration = 0L
                        var audioUrl = ""
                        val directory: File = cw.getDir("music", Context.MODE_PRIVATE)
                        val mypath = File(
                            directory.absolutePath,
                            effectsList[0].music[i].listOfEffects[m].musicName.toString() + ".mp3"
                        )

                        if (mypath.exists()) {
                            val metaRetriever = MediaMetadataRetriever()
                            metaRetriever.setDataSource(this, Uri.parse(mypath.absolutePath))
                            val time =
                                metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                                    .toString()
                            duration = time.toLong()
                            audioUrl = mypath.absolutePath
                        }

                        musicEffect.add(
                            MusicData(
                                audioUrl = audioUrl,
                                effectUrl = effectsList[0].music[i].listOfEffects[m].musicPath.toString(),
                                title = effectsList[0].music[i].listOfEffects[m].musicName.toString(),
                                artist = "",
                                duration = duration.toLong(),
                                startTime = 0,
                                thumb = effectsList[0].music[i].listOfEffects[m].imagePath.toString(),
                                endTime = duration.toLong(),
                                strstartTime = 0,
                                strendTime = duration.toLong(),
                                isfrom = IS_EFFECT,
                                isDownload = mypath.exists()
                            )
                        )


                    }
                }
            }
            bottomSheetEffectDialog(arrMusicEffect[it].btnText)
            }

       0 }
        bindingMusic.rvMusicEffects.adapter = videoMusicEffectAdapter

    }

    private fun addTextSticker() {
        if (textStickerList.size < 5) {
            binding.includeToolbar.btnTick.hide()
            binding.includeToolbar.btnSave.hide()
            binding.includeToolbar.btnDiscard.hide()
            playPauseVideo(true)
            isEdit = false
            openTextEditingMenu(getString(R.string.enter_the_text), "0_copy", 300L, 0L)
            addIcon(binding.stickerView)
        } else {
            showToast(this, getString(R.string.you_can_not_add_more_than_5_text))
        }

    }

    private fun editTextSticker() {
        if (itemPosition != -1 && mPosition != -1) {
            strTextSticker =
                textStickerList[mPosition].duplicatetext!![itemPosition].sticker as TextSticker
            isEdit = true
            binding.stickerView.removeStickerlist(textStickerList[mPosition].duplicatetext!![itemPosition].sticker)
            openTextEditingMenu(
                textStickerList[mPosition].duplicatetext!![itemPosition].text,
                textStickerList[mPosition].duplicatetext!![itemPosition].strTag,
                textStickerList[mPosition].duplicatetext!![itemPosition].endTime,
                textStickerList[mPosition].duplicatetext!![itemPosition].startTime
            )
        } else {
            showToast(this, getString(R.string.please_select_text_first))
        }
    }

    private fun deleteTextSticker() {
        itemWidth = 0
        if (itemPosition != -1 && mPosition != -1) {

            val itemIterator = textStickerList[mPosition].duplicatetext!!.iterator()
            while (itemIterator.hasNext()) {
                val item = itemIterator.next()
                if (item.mobject == itemPosition) {
                    var stickerdata = item.sticker as TextSticker

                    if (binding.stickerView.currentSticker != null) {
                        var textData = TextStickerData(stickerdata.currentAngle,
                            stickerdata.currentScale,
                            stickerdata.x,
                            stickerdata.y,
                            textStickerList[mPosition].duplicatetext?.get(itemPosition)!!.startTime,
                            textStickerList[mPosition].duplicatetext?.get(itemPosition)!!.endTime,
                            stickerdata.getstrtag(),
                            stickerdata.fontStyle,
                            stickerdata.typeface,
                            stickerdata.alignment().toString(),
                            stickerdata.size,
                            stickerdata.text.toString(),
                            stickerdata.borderColor,
                            stickerdata.borderType,
                            false,
                            false,
                            stickerdata.color,
                            stickerdata.color,
                            stickerdata.pattern,
                            stickerdata.gradient,
                            stickerdata.matrix,
                            textStickerList[mPosition].duplicatetext?.get(itemPosition)!!.offset,
                            textStickerList[mPosition].duplicatetext?.get(itemPosition)!!.length,
                            0
                        )
                        var data = UndoRedoData2(
                            stickerdata,
                            copySticker(stickerdata as TextSticker, ""),
                            textData
                        )
                        addToChangeList(DELETE_TEXT, DELETE_TEXT,data)}

                    binding.stickerView.removeStickerlist(item.sticker)
                    itemIterator.remove()
                    notifyStickerView()
                }
            }

            if (textStickerList[mPosition].duplicatetext!!.size == 0) {
                if (textStickerList.size == 5) {
                    binding.ccTop.layoutParams.height =
                        resources.displayMetrics.heightPixels / 3
                } else if (textStickerList.size == 4) {
                    binding.ccTop.layoutParams.height =
                        resources.displayMetrics.heightPixels / 3
                } else if (textStickerList.size == 3) {
                    binding.ccTop.layoutParams.height =
                        resources.displayMetrics.heightPixels / 2 - 250
                } else if (textStickerList.size == 2) {
                    binding.ccTop.layoutParams.height =
                        resources.displayMetrics.heightPixels / 2
                }
                textStickerList.removeAt(mPosition)
            }


            binding.rvTextFrame.adapter!!.notifyItemRemoved(mPosition)
            binding.rvTextLine.adapter!!.notifyItemRemoved(mPosition)
            binding.rvTextFrame.adapter = TextCopyAdapter(this, textStickerList, this)
            binding.rvTextLine.adapter = TextLineAdapter(this, textStickerList)
            notifyStickerView()
            itemPosition = -1
            mPosition = -1


        } else {

            showToast(this, getString(R.string.please_select_text_first))
        }
    }

    private fun copyTextSticker() {
        if (itemPosition != -1 && mPosition != -1) {
            if (textStickerList.size <= 4) {
                itemWidth += 40

                binding.ccTop.layoutParams.height =
                    binding.frVideoController.height - itemWidth

                val itemList: ArrayList<SpanData> = ArrayList()
                val stickerdata =
                    textStickerList[mPosition].duplicatetext?.get(itemPosition)!!.sticker as TextSticker
                val sticker = TextSticker(this)
                sticker.text = stickerdata.text
                sticker.setTextColor(stickerdata.color)

                if (borderType != "0" && borderType != "") {
                    sticker.setPaintToOutline(stickerdata.borderColor, borderType)
                }
                sticker.setTextAlign(stickerdata.alignment())
                sticker.setMaxTextSize(dpToPx(stickerdata.size))
                sticker.setTypeface(stickerdata.typeface)
                sticker.fontStyle = stickerdata.fontStyle
                if (stickerdata.borderColor != 0 && stickerdata.borderType != "") {
                    sticker.setPaintToOutline(stickerdata.borderColor, stickerdata.borderType)
                }
                if (stickerdata.pattern != 0) {
                    sticker.pattern = stickerdata.pattern
                }
                if (  stickerdata.gradient != null && stickerdata.gradient.isNotEmpty()) {
                    sticker.setGradient(stickerdata.gradient[0],stickerdata.gradient[1])
                }
                sticker.resizeText()
                sticker.setStrTag((mPosition + 1).toString() + "_copy")
                sticker.startTime =
                    textStickerList[mPosition].duplicatetext?.get(itemPosition)!!.offset.toLong()
                sticker.endTime =
                    textStickerList[mPosition].duplicatetext?.get(itemPosition)!!.offset.toLong() + textStickerList[mPosition].duplicatetext?.get(
                        itemPosition
                    )!!.length.toLong()
                binding.stickerView.addSticker(sticker, 2f)


                itemList.add(
                    SpanData(
                        textStickerList[mPosition].duplicatetext?.get(itemPosition)!!.text,
                        textStickerList[mPosition].duplicatetext?.get(itemPosition)!!.startTime,
                        textStickerList[mPosition].duplicatetext?.get(itemPosition)!!.endTime,
                        textStickerList[mPosition].duplicatetext?.get(itemPosition)!!.finalstartTime,
                        textStickerList[mPosition].duplicatetext?.get(itemPosition)!!.finalendTime,
                        textStickerList[mPosition].duplicatetext?.get(itemPosition)!!.x,
                        textStickerList[mPosition].duplicatetext?.get(itemPosition)!!.y,
                        sticker,
                        textStickerList[mPosition].duplicatetext?.get(itemPosition)!!.info,
                        textStickerList[mPosition].duplicatetext?.get(itemPosition)!!.offset,
                        textStickerList[mPosition].duplicatetext?.get(itemPosition)!!.length,
                        0,
                        (mPosition + 1).toString() + "_copy",
                        textStickerList[mPosition].duplicatetext?.get(itemPosition)!!.bitmap,
                        false
                    )
                )
                textStickerList.add(
                    CopyModel(
                        itemList,
                        ((TimeUnit.MILLISECONDS.toSeconds(totalVideoDuration)) * SEGMENT_VIDEO).toInt()
                    )
                )
                var tag = (mPosition + 1).toString() + "_copy"
                if (binding.stickerView.currentSticker != null) {
                    var textData = TextStickerData(stickerdata.currentAngle,
                        stickerdata.currentScale,
                        stickerdata.x,
                        stickerdata.y,
                        textStickerList[mPosition].duplicatetext?.get(itemPosition)!!.startTime,
                        textStickerList[mPosition].duplicatetext?.get(itemPosition)!!.endTime,
                        tag,
                        stickerdata.fontStyle,
                        stickerdata.typeface,
                        stickerdata.alignment().toString(),
                        stickerdata.size,
                        stickerdata.text.toString(),
                        stickerdata.borderColor,
                        stickerdata.borderType,
                        false,
                        false,
                        stickerdata.color,
                        stickerdata.color,
                        stickerdata.pattern,
                        stickerdata.gradient,
                        stickerdata.matrix,
                        textStickerList[mPosition].duplicatetext?.get(itemPosition)!!.offset,
                        textStickerList[mPosition].duplicatetext?.get(itemPosition)!!.length,
                        0
                    )
                    var data = UndoRedoData2(
                        sticker,
                        copySticker(sticker as TextSticker, tag),
                        textData
                    )
                    addToChangeList(ADD, TEXT_ADD,data)}
                Log.e(TAG, "copyTextSticker: " + textStickerList)
                binding.rvTextFrame.adapter = null
                binding.rvTextFrame.adapter =
                    TextCopyAdapter(this, textStickerList, this)
                binding.rvTextFrame.adapter!!.notifyDataSetChanged()
                binding.rvTextLine.adapter = null
                binding.rvTextLine.adapter =
                    TextLineAdapter(this, textStickerList)
                binding.rvTextLine.adapter!!.notifyDataSetChanged()
                notifyStickerView()

            } else {
                showToast(this, getString(R.string.you_can_only_copy_5_item))
            }

        } else {
            showToast(this, getString(R.string.please_select_text_first))
        }
        Log.e(TAG, "copyTextSticker::textStickerList>>: $textStickerList")
    }

    private fun  duplicateTextSticker() {
        if (itemPosition != -1 && mPosition != -1) {
            val itemList: ArrayList<SpanData> = ArrayList()
            var itemPos = 0
            for (m in 0 until textStickerList[mPosition].duplicatetext!!.size) {
                if (textStickerList.size > 1) {
                    if (textStickerList[mPosition].duplicatetext!!.size == 1) {
                        itemPos =
                            textStickerList[mPosition].duplicatetext!![m].length.toInt() + textStickerList[mPosition].duplicatetext!![m].offset.toInt()
                    } else {
                        itemPos =
                            textStickerList[mPosition].duplicatetext!![m].length.toInt() + textStickerList[mPosition].duplicatetext!![m].offset.toInt()
                    }
                } else {
                    itemPos += textStickerList[mPosition].duplicatetext!![m].length.toInt()

                }
            }

            if (itemPos >= textStickerList[mPosition].totalDuration - 100) {
                showToast(this, getString(R.string.video_length_finish))
            } else {
                if (itemPosition != textStickerList[mPosition].duplicatetext!!.size - 1) {
                    val pos = textStickerList[mPosition].duplicatetext!!.size - 1
                    val stickerdata =
                        textStickerList[mPosition].duplicatetext?.get(pos)!!.sticker as TextSticker

                    val sticker = TextSticker(this)
                    sticker.text = stickerdata.text
                    sticker.setTextColor(stickerdata.color)
                    if (borderType != "0" && borderType != "") {
                        sticker.setPaintToOutline(stickerdata.borderColor, borderType)
                    }
                    sticker.setTextAlign(stickerdata.alignment())
                    sticker.setMaxTextSize(dpToPx(stickerdata.size))
                    sticker.setTypeface(stickerdata.typeface)
                    sticker.fontStyle = stickerdata.fontStyle
                    sticker.resizeText()
                    sticker.setStrTag(mPosition.toString() + (pos + 1).toString() + "_duplicate")
                    sticker.startTime =
                        itemPos.toLong()
                    sticker.endTime =
                        itemPos.toLong() + textStickerList[mPosition].duplicatetext?.get(
                            pos
                        )!!.length.toLong()
                    binding.stickerView.addSticker(sticker, 2f)

                    Log.e(
                        TAG, "duplicateTextSticker: ${
                            itemPos.toLong() + textStickerList[mPosition].duplicatetext?.get(
                                pos
                            )!!.length.toLong() * 10
                        }"
                    )
                    Log.e(TAG, "duplicateTextSticker>>>>@: ${itemPos.toLong()}")

                    itemList.add(
                        SpanData(
                            textStickerList[mPosition].duplicatetext?.get(pos)!!.text,
                            itemPos.toLong(),
                            itemPos.toLong() + textStickerList[mPosition].duplicatetext?.get(
                                pos
                            )!!.length.toLong(),
                            itemPos.toLong() * 10,
                            (itemPos.toLong() + textStickerList[mPosition].duplicatetext?.get(
                                pos
                            )!!.length.toLong()) * 10,
                            textStickerList[mPosition].duplicatetext?.get(pos)!!.x,
                            textStickerList[mPosition].duplicatetext?.get(pos)!!.y,
                            sticker,
                            textStickerList[mPosition].duplicatetext?.get(pos)!!.info,
                            itemPos + 0,
                            textStickerList[mPosition].duplicatetext?.get(pos)!!.length,
                            textStickerList[mPosition].duplicatetext?.get(pos)!!.mobject + 1,
                            mPosition.toString() + (pos + 1).toString() + "_duplicate",
                            textStickerList[mPosition].duplicatetext?.get(itemPosition)!!.bitmap,
                            false
                        )
                    )

                    var strTag = mPosition.toString() + (pos + 1).toString() + "_duplicate"
                    if (binding.stickerView.currentSticker != null) {
                        var textData = TextStickerData(stickerdata.currentAngle,
                            stickerdata.currentScale,
                            stickerdata.x,
                            stickerdata.y,
                            itemPos.toLong(),
                            itemPos.toLong() + textStickerList[mPosition].duplicatetext?.get(
                                pos
                            )!!.length.toLong(),
                            strTag,
                            stickerdata.fontStyle,
                            stickerdata.typeface,
                            stickerdata.alignment().toString(),
                            stickerdata.size,
                            stickerdata.text.toString(),
                            stickerdata.borderColor,
                            stickerdata.borderType,
                            false,
                            false,
                            stickerdata.color,
                            stickerdata.color,
                            stickerdata.pattern,
                            stickerdata.gradient,
                            stickerdata.matrix,
                            itemPos + 0,
                            textStickerList[mPosition].duplicatetext?.get(pos)!!.length,
                            textStickerList[mPosition].duplicatetext?.get(pos)!!.mobject + 1

                        )
                        var data = UndoRedoData2(
                            sticker,
                            copySticker(sticker as TextSticker, strTag),
                            textData
                        )
                        addToChangeList(ADD, TEXT_ADD,data)}
                }
                else {
                    val stickerdata =
                        textStickerList[mPosition].duplicatetext?.get(itemPosition)!!.sticker as TextSticker
                    val sticker = TextSticker(this)
                    sticker.text = stickerdata.text
                    sticker.setTextColor(stickerdata.color)
                    sticker.setTextAlign(stickerdata.alignment())
                    if (stickerdata.borderColor != 0 && stickerdata.borderType != "") {
                        sticker.setPaintToOutline(stickerdata.borderColor, stickerdata.borderType)
                    }
                    if (stickerdata.pattern != 0) {
                        sticker.pattern = stickerdata.pattern
                    }
                    if (  stickerdata.gradient != null && stickerdata.gradient.isNotEmpty()) {
                        sticker.setGradient(stickerdata.gradient[0],stickerdata.gradient[1])
                    }
                    sticker.setMaxTextSize(dpToPx(stickerdata.size))
                    sticker.setTypeface(stickerdata.typeface)
                    sticker.fontStyle = stickerdata.fontStyle
                    sticker.resizeText()
                    sticker.setStrTag(mPosition.toString() + (itemPosition + 1).toString() + "_duplicate")
                    sticker.startTime =
                        itemPos.toLong()
                    sticker.endTime =
                        itemPos.toLong() + textStickerList[mPosition].duplicatetext?.get(
                            itemPosition
                        )!!.length.toLong()
                    binding.stickerView.addSticker(sticker, 2f)

                    Log.e(
                        TAG, "duplicateTextSticker: ${
                            itemPos.toLong() + textStickerList[mPosition].duplicatetext?.get(
                                itemPosition
                            )!!.length.toLong()
                        }"
                    )

                    Log.e(TAG, "duplicateTextSticker112>>: ${itemPos.toLong()}")
                    itemList.add(
                        SpanData(
                            textStickerList[mPosition].duplicatetext?.get(itemPosition)!!.text,
                            itemPos.toLong(),
                            itemPos.toLong() + textStickerList[mPosition].duplicatetext?.get(
                                itemPosition
                            )!!.length.toLong(),
                            itemPos.toLong() * 10,
                            (itemPos.toLong() + textStickerList[mPosition].duplicatetext?.get(
                                itemPosition
                            )!!.length.toLong()) * 10,
                            textStickerList[mPosition].duplicatetext?.get(itemPosition)!!.x,
                            textStickerList[mPosition].duplicatetext?.get(itemPosition)!!.y,
                            sticker,
                            textStickerList[mPosition].duplicatetext?.get(itemPosition)!!.info,
                            itemPos + 0,
                            textStickerList[mPosition].duplicatetext?.get(itemPosition)!!.length,
                            textStickerList[mPosition].duplicatetext?.get(itemPosition)!!.mobject + 1,
                            mPosition.toString() + (itemPosition + 1).toString() + "_duplicate",
                            textStickerList[mPosition].duplicatetext?.get(itemPosition)!!.bitmap,
                            false
                        )
                    )
                      var strTag = mPosition.toString() + (itemPosition + 1).toString() + "_duplicate"
                    if (binding.stickerView.currentSticker != null) {
                        var textData = TextStickerData(stickerdata.currentAngle,
                            stickerdata.currentScale,
                            stickerdata.x,
                            stickerdata.y,
                            itemPos.toLong(),
                            itemPos.toLong() + textStickerList[mPosition].duplicatetext?.get(
                                itemPosition
                            )!!.length.toLong(),
                            strTag,
                            stickerdata.fontStyle,
                            stickerdata.typeface,
                            stickerdata.alignment().toString(),
                            stickerdata.size,
                            stickerdata.text.toString(),
                            stickerdata.borderColor,
                            stickerdata.borderType,
                            false,
                            false,
                            stickerdata.color,
                            stickerdata.color,
                            stickerdata.pattern,
                            stickerdata.gradient,
                            stickerdata.matrix,
                            itemPos + 0,
                            textStickerList[mPosition].duplicatetext?.get(itemPosition)!!.length,
                            textStickerList[mPosition].duplicatetext?.get(itemPosition)!!.mobject + 1
                        )
                        var data = UndoRedoData2(
                            sticker,
                            copySticker(sticker as TextSticker,strTag),
                            textData
                        )
                        addToChangeList(ADD, TEXT_ADD,data)}
                }

                textStickerList[mPosition].duplicatetext!!.addAll(itemList)
                binding.rvTextFrame.adapter!!.notifyItemChanged(mPosition)
                binding.rvTextLine.adapter!!.notifyItemChanged(mPosition)
                binding.rvTextFrame.adapter =
                    TextCopyAdapter(this, textStickerList, this)
                binding.rvTextFrame.adapter!!.notifyDataSetChanged()
                binding.rvTextLine.adapter = null
                binding.rvTextLine.adapter =
                    TextLineAdapter(this, textStickerList)
                binding.rvTextLine.adapter!!.notifyDataSetChanged()
                notifyStickerView()


            }
        } else {
            showToast(this, getString(R.string.please_select_text_first))
        }
    }

    override fun onItemslectClick(position: Int, itemPos: Int) {
        itemPosition = itemPos
        mPosition = position

        for (i in 0 until textStickerList.size) {
            for (m in 0 until textStickerList[i].duplicatetext!!.size) {
                textStickerList[i].duplicatetext!![m].isSelected = false
            }
        }

        for (i in 0 until textStickerList.size) {
            if (i == position) {
                for (m in 0 until textStickerList[position].duplicatetext!!.size) {
                    if (textStickerList[position].duplicatetext!![m].mobject == itemPos) {
                        textStickerList[position].duplicatetext!![m].isSelected = true
                    }
                }
            }
        }

        binding.rvTextFrame.adapter =
            TextCopyAdapter(this, textStickerList, this)
        binding.rvTextFrame.adapter!!.notifyDataSetChanged()
//        binding.rvTextFrame.adapter!!.notifyDataSetChanged()
    }

    override fun onItemDragClick(position: Int, itemPos: Int, startTime: Int, endTime: Int) {
        val length = endTime - startTime
        textStickerList[position].duplicatetext!![itemPos].length = length
        textStickerList[position].duplicatetext!![itemPos].offset = startTime
        textStickerList[position].duplicatetext!![itemPos].startTime = startTime.toLong()
        textStickerList[position].duplicatetext!![itemPos].finalstartTime = startTime.toLong() * 10
        textStickerList[position].duplicatetext!![itemPos].endTime = endTime.toLong()
        textStickerList[position].duplicatetext!![itemPos].finalendTime = endTime.toLong() * 10

        Log.e(TAG, "onItemDragClick: " + textStickerList)

//        binding.rvTextFrame.adapter!!.notifyDataSetChanged()
        binding.rvTextLine.adapter =
            TextLineAdapter(this, textStickerList)
        binding.rvTextLine.adapter!!.notifyDataSetChanged()

        if (binding.stickerView.currentSticker != null) {
            var sticker = textStickerList[position].duplicatetext!![itemPos].sticker
            var textData = TextStickerData((sticker as TextSticker).currentAngle,
                (sticker as TextSticker).currentScale,
                (sticker as TextSticker).x,
                (sticker as TextSticker).y,
                startTime.toLong(),
                endTime.toLong(),
                (sticker as TextSticker).getstrtag(),
                (sticker as TextSticker).fontStyle,
                (sticker as TextSticker).typeface,
                (sticker as TextSticker).alignment().toString(),
                (sticker as TextSticker).size,
                (sticker as TextSticker).text.toString(),
                (sticker as TextSticker).borderColor,
                (sticker as TextSticker).borderType,
                false,
                false,
                (sticker as TextSticker).color,
                (sticker as TextSticker).color,
                (sticker as TextSticker).pattern,
                (sticker as TextSticker).gradient,
                (sticker as TextSticker).matrix,
                textStickerList[position].duplicatetext!![itemPos].offset,
                textStickerList[position].duplicatetext!![itemPos].length

            )
            val data = UndoRedoData2(
                binding.stickerView.currentSticker as TextSticker,
                copySticker(binding.stickerView.currentSticker as TextSticker, ""),
                textData
            )
            addToChangeList(DRAG_TEXT, DRAG_TEXT,data)}

    }

    override fun onItemClick(position: Int) {
        when (position) {
            TRIM -> {
                binding.imgFullscreen.hide()
                binding.imgDone.visible()
                binding.llItemView.hide()
                binding.trimCuttr.visible()
                binding.tvitemCount.text = (currentPosition + 1).toString() + " / " + arrVideo.size
                videoEditViewModel.videoTrim()
                handleTrim(true)
            }
            ADD_TEXT_VIEW -> {
//                createVideoTextBtnList()
                if (textStickerList.size == 0){
                    binding.includeToolbar.btnSave.hide()
                    binding.includeToolbar.btnDiscard.hide()
                    playPauseVideo(true)
                    isEdit = false
                    openTextEditingMenu(getString(R.string.enter_the_text), "0_copy", 300L, 0L)
                    addIcon(binding.stickerView)
                }else{
                    binding.includeToolbar.btnTick.visible()
                    binding.includeToolbar.btnDiscard.visible()
                    binding.includeToolbar.btnSave.hide()
                    binding.llItemView.hide()
                    binding.trimCuttr.hide()
                    binding.ccText.visible()
                }
//                if (textStickerList.size < 5) {
//                    binding.includeToolbar.btnSave.hide()
//                    binding.includeToolbar.btnDiscard.hide()
//                    playPauseVideo(true)
//                    isEdit = false
//                    openTextEditingMenu(getString(R.string.enter_the_text), "0_copy", 300L, 0L)
//                    addIcon(binding.stickerView)
//                } else {
//                    showToast(this, getString(R.string.you_can_not_add_more_than_5_text))
//                }
            }
            FILTER -> {
                selectedFunction = FILTER
                currentPosition = 0
                tempCurrentPosition = currentPosition

                playPauseAudio(true)
                binding.llItemView.hide()
                binding.trimCuttr.hide()
                binding.ccText.hide()
                binding.imgFullscreen.hide()
                binding.imgAllapply.visible()
                binding.imgDone.visible()
                binding.ccFilter.visible()
                glPlayer.visible()
                arrFilterList = ArrayList()



                videoEditViewModel.getVideoFilterList(
                    arrVideo[tempCurrentPosition].filter,
                    this,
                    frames[0].frame!!
                )
                videoEditViewModel.applySelectionFromFilter(arrVideo[tempCurrentPosition].filter)
                arrFilterList = videoEditViewModel.arrFilterData



                binding.llFilterView.rvFilter.layoutManager =
                    LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
                filterAdapter = FilterListAdapter(this, arrFilterList) {
                    for (i in 0 until arrFilterList.size) {
                        arrFilterList[i].isSelected = false
                    }
                    arrFilterList[it].isSelected = true
                    filterPosition = it
                    videoEditViewModel.applyFilterOnVideo(it)
                    arrVideo[tempCurrentPosition].filter = videoEditViewModel.getSelectedFilter()

                    val data = UndoRedoData2(
                        null,
                        null,
                        null,
                        videoEditViewModel.getSelectedFilter(),
                        it,0L,0L ,null,tempCurrentPosition

                    )
                    addToChangeList(ACTION_FILTER, 0,data)
                    Log.e(TAG, "onItemClick: " + arrFilterList[it].filterName)
                }
                binding.llFilterView.rvFilter.adapter = filterAdapter

                binding.llFilterView.rvFilterThumbnail.layoutManager =
                    LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)

                binding.llFilterView.rvFilterThumbnail.adapter =
                    VideoFilterAdapter(this, arrVideo) {
                        filterpos = it
                        arrFilterList.clear()
                        tempCurrentPosition = it

                        videoEditViewModel.applySelectionFromFilter(arrVideo[tempCurrentPosition].filter)
                        arrFilterList = videoEditViewModel.arrFilterData

                        setSelectedVideoAsSingleSource(
                            arrVideo[tempCurrentPosition].startDuration,
                            arrVideo[tempCurrentPosition].endDuration
                        )
                        setSeekBarForOneVideo(
                            arrVideo[tempCurrentPosition].startDuration,
                            arrVideo[tempCurrentPosition].endDuration,
                            if (arrVideo[tempCurrentPosition].videoSpeedDuration > 0) arrVideo[tempCurrentPosition].videoSpeedDuration else 0
                        )
                        if (tempCurrentPosition == 0) {
                            videoEditViewModel.getVideoFilterList(
                                arrVideo[tempCurrentPosition].filter,
                                this,
                                frames[0].frame!!
                            )
                        } else {
                            var time =
                                (TimeUnit.MILLISECONDS.toSeconds(arrVideo[tempCurrentPosition - 1].endDuration)).toInt()
                            videoEditViewModel.getVideoFilterList(
                                arrVideo[tempCurrentPosition].filter,
                                this,
                                frames[time].frame!!
                            )
                        }

                        binding.llFilterView.rvFilter.adapter!!.notifyDataSetChanged()
//                        videoEditViewModel.applyFilterOnVideo(filterPosition)
                    }

                setSelectedVideoAsSingleSource(
                    arrVideo[tempCurrentPosition].startDuration,
                    arrVideo[tempCurrentPosition].endDuration
                )
                setSeekBarForOneVideo(
                    arrVideo[tempCurrentPosition].startDuration,
                    arrVideo[tempCurrentPosition].endDuration,
                    if (arrVideo[tempCurrentPosition].videoSpeedDuration > 0) arrVideo[tempCurrentPosition].videoSpeedDuration else 0
                )


            }
            ADD_MUSIC -> {
                binding.rvVideoEditBtn.hide()
                binding.imgFullscreen.hide()
                binding.rvVideoMusic.visible()
                binding.llItemView.visible()
                binding.trimCuttr.hide()
                binding.flFrames.visible()
                binding.rvFrame.visible()
                binding.rvTextFrame.hide()
                binding.ivAddGallery.hide()

                binding.rvFrame.post {
                    binding.rvFrame.setPadding(
                        0,
                        0,
                        0,
                        0
                    )
                }

                createVideoMusicBtnList()
            }

        }
    }

    private fun saveFilterChangeOnVideo() {
//        if (videoEditViewModel.isFilterApplyForAll.get()) {
//            for (videoItem in arrVideo) {
//                videoItem.filter = videoEditViewModel.getSelectedFilter()
//            }
//        } else {
//            arrVideo[tempCurrentPosition].filter = videoEditViewModel.getSelectedFilter()
//        }

        setVisibilityOfViewOnCancel()
        filterVisibility()
    }

    private fun setFilterView() {
        val contentFrame =
            findViewById<FrameLayout>(com.google.android.exoplayer2.R.id.exo_content_frame)
        contentFrame.addView(glPlayer)
        glPlayer.init(object : IVideoSurface {
            override fun onCreated(surfaceTexture: SurfaceTexture?) {
                player.setVideoSurface(Surface(surfaceTexture))

            }
        })
        glPlayer.visible()
    }

    // notify sticker changes
    private fun notifyStickerView() {
        binding.stickerView.invalidate()
    }

    fun convertSpToPx(scaledPixels: Float): Float {
        return scaledPixels * resources.displayMetrics.scaledDensity
    }

    private val observer = Observer<Any> {
        if (it is ResponseObserver.Loading)
            if (it.isLoading) showProgress() else dismissProgress()
        else if (it is ResponseObserver.DisplayAlert) {
            dismissProgress()
        } else if (it is ResponseObserver.PerformAction) {
            when (it.data) {
                is FilterType -> applyFilter(it.data)
                is MusicData -> {
                    if (it.nextData != null && it.nextData is Int && it.nextData != -1) {
//                        if (it.nextData == MUSIC_DELETE) deleteMusicFromList(musicData = it.data)
                    } else {
                        if (it.data.type == 1) playPauseAudio(it.data)
                        else if (it.data.type == 2) {
                            playPauseAudio(it.data)
                            addAudioFileInList(it.data)
                            val data = UndoRedoData2(
                                null,
                                null,
                                null,
                                FilterType.DEFAULT,
                                0,
                                0L,0L,it.data,0

                            )
                            addToChangeList(ACTION_MUSIC, MUSIC_ADD,data)
                        }
                    }
                }
                is Int -> {
                    when (it.data) {
                        GENERATE_THUMB -> {
                            dismissProgress()
                            setResult(RESULT_OK)
                            finish()
                        }
                        PROGRESS_CANCEL_CLICK -> {
                            object : CustomAlertDialog(
                                this,
                                "",
                                getString(R.string.msg_export_alert),
                                getString(R.string.yes),
                                getString(R.string.no)
                            ) {
                                override fun onClick(id: Int) {
                                    if (id == R.id.btnPositive)
                                        cancelExportingVideo()
                                }
                            }
                        }
                        TRIM -> {
                            handleTrim(true)
                        }
                        DIALOG_DISMISS -> {
                            Log.e(TAG, "DIOALOG ")
                            dismissProgress()
                            if (::musicSelectionDialog.isInitialized && musicSelectionDialog.isShowing)
                                musicSelectionDialog.dismiss()
                        }

                    }
                }

                is VideoFrameData -> {
                    playPauseVideo(true)
                    CommonBinder.loadVideoFrameImage(
                        binding.ivContains,
                        it.data.frame!!
                    )
                }
            }
        }
    }

    private fun addAudioFileInList(musicData: MusicData) {
//        if (videoEditViewModel.getSelectedMusic().size > 0) {
//            selectedFunction = MUSIC_MULTIPLE
//            videoEditViewModel.isShowMultipleMusic.set(true)
//        }

        Log.e(TAG, "handleRecord:musicData " + musicData.audioUrl)
        if (mediaPlayer.isPlaying) {
            videoEditViewModel.updateMusicPlayValue()
        }
        playPauseAudio(musicData)
        if (::musicSelectionDialog.isInitialized && musicSelectionDialog.isShowing)
            musicSelectionDialog.dismiss()

        if (::effectDialog.isInitialized && effectDialog.isShowing)
            effectDialog.dismiss()
        selectedFunction = MUSIC_MULTIPLE
        videoEditViewModel.addSelectedMusic(musicData)
        isTimerStarted = false
        isMediaPlayerPrepared = false
        updateCountDownTimer()
        setAudioSource()
        setVisibilityOfMediaController()

        Log.e(TAG, "musicSelectionDialog.isShowing: " + musicSelectionDialog.isShowing)
        Log.e(TAG, "isFromAudio: " + isFromAudio)
        Log.e(TAG, "addAudioFileInList: " + videoEditViewModel.getSelectedMusic().size)

    }

    private fun setVisibilityOfMediaController() {


        binding.includeToolbar.btnSave.hide()
        binding.includeToolbar.btnDiscard.hide()
        binding.includeToolbar.btnTick.visible()
        if (isFromAudio != IS_RECORD) {
            createMusicEditBtnList()
        } else {
            isFromAudio = ""
            binding.imgUndo.hide()
            binding.imgRedo.hide()
            binding.effectsSeekWave.hide()
            binding.audioSeekWave.hide()
            binding.imgFullscreen.hide()
            binding.includeToolbar.btnTick.hide()
            binding.rvFrame.hide()
            binding.flFrames.hide()
            binding.llRecord.visible()
            binding.rvFrame2.visible()
            binding.videoSeekBar.visible()
            binding.ccVideoPlay.visible()
            binding.tvVideoDuration.visible()
            binding.horizontalScroll.visible()
            binding.recordSeekWave.visible()

            val musicArray = videoEditViewModel.getSelectedMusic()

            val totalWidth =
                ((TimeUnit.MILLISECONDS.toSeconds(totalVideoDuration)) * SEGMENT_VIDEO).toInt()
            binding.recordSeekWave.layoutParams.width = totalWidth

            val recordlist: ArrayList<MusicData> = ArrayList()

            if (musicArray.size != 0) {
                for (i in 0 until musicArray.size) {
                    Log.e(TAG, "createMusicEditBtnList: ${musicArray[i].isfrom}")
                    if (musicArray[i].isfrom == IS_RECORD) {
                        recordlist.add(musicArray[i])
                    }
                }
                binding.recordSeekWave.visible()
                if (recordlist.size != 0) {
                    for (m in 0 until recordlist.size) {
                        var startTime = 0
                        if (m > 0) {
                            var endTimming =
                                ((TimeUnit.MILLISECONDS.toSeconds(recordlist[m - 1].endTime.toLong())) * SEGMENT_VIDEO).toInt()
                            if (endTimming > totalWidth) {
                                endTimming = totalWidth - 30
                            }
                            startTime =
                                ((TimeUnit.MILLISECONDS.toSeconds(recordlist[m].startTime.toLong())) * SEGMENT_VIDEO).toInt() + endTimming + 10
                        } else {
                            startTime =
                                ((TimeUnit.MILLISECONDS.toSeconds(recordlist[m].startTime.toLong())) * SEGMENT_VIDEO).toInt()
                        }
                        var endTime =
                            ((TimeUnit.MILLISECONDS.toSeconds(recordlist[m].endTime.toLong())) * SEGMENT_VIDEO).toInt()
                        if (endTime > totalWidth) {
                            endTime = totalWidth - 30
                        }
                        var startTime1 =
                            ((TimeUnit.MILLISECONDS.toSeconds(recordlist[m].startTime.toLong())) * SEGMENT_VIDEO).toInt()
                        binding.recordSeekWave.addSpan(
                            startTime + 15,
                            endTime - startTime1,
                            recordlist[m].title,
                            m
                        )
                        binding.recordSeekWave.updateSpan(m, recordlist[m].isSelected)
                        Log.e(
                            TAG,
                            "createMusicEditBtnList: ${startTime}     endtime ${endTime - startTime1}"
                        )

                    }
                }

                binding.recordSeekWave.visibleIf(recordlist.size != 0)

                val recordlistener = object : AudioWaveSeekView.TimeLineChangeListener {
                    override fun onRangeChanged(
                        tag: Any?,
                        startFraction: Float,
                        endFraction: Float
                    ) {
                        Log.e(
                            TAG,
                            "onRangeChangedFIRST>>: " + musicArray[tag as Int].startTime + musicArray[tag].endTime
                        )
                        var endTime =
                            ((TimeUnit.MILLISECONDS.toSeconds(musicArray[tag].strendTime.toLong())) * SEGMENT_VIDEO).toInt()
                        var starttime =
                            ((TimeUnit.MILLISECONDS.toSeconds(musicArray[tag].strstartTime.toLong())) * SEGMENT_VIDEO).toInt()
                        if (endTime > endFraction.roundToInt().toLong()) {
                            endTime = endFraction.roundToInt()
                            musicArray[tag].endTime = endFraction.roundToInt().toLong()
                        }
                        starttime = startFraction.roundToInt()
                        musicArray[tag].startTime = startFraction.roundToInt().toLong()
                        binding.recordSeekWave.updateSpanRange(tag, starttime, endTime - starttime)
                        val data = UndoRedoData2(
                            null,
                            null,
                            null,
                            FilterType.DEFAULT,
                            0,
                            0L,0L,musicArray[tag as Int],0

                        )
                        addToChangeList(ACTION_MUSIC, MUSIC_DRAG,data)
                    }

                    override fun onSelectionChange(tag: Any?, selected: Boolean) {
                        musicPos = tag as Int
                    }

                    override fun onThumbClicked(tag: Any?, thumbId: Int) {

                    }

                }
                binding.recordSeekWave.addIndicatorChangeListener(recordlistener)
            }
//            seekBarMusicTimeline()
        }
    }

    /** set music ,effect ,record timeline  **/
    private fun seekBarMusicTimeline() {
        val musicArray = videoEditViewModel.getSelectedMusic()
        val totalWidth =
            ((TimeUnit.MILLISECONDS.toSeconds(totalVideoDuration)) * SEGMENT_VIDEO).toInt()
        binding.recordSeekWave.layoutParams.width = totalWidth

        binding.audioSeekWave.layoutParams.width = totalWidth
        binding.effectsSeekWave.layoutParams.width = totalWidth
        binding.recordSeekWave.layoutParams.width = totalWidth

        val musiclist: ArrayList<MusicData> = ArrayList()
        val effectlist: ArrayList<MusicData> = ArrayList()
        val recordlist: ArrayList<MusicData> = ArrayList()

        if (musicArray.size != 0) {
            for (i in 0 until musicArray.size) {
                Log.e(TAG, "createMusicEditBtnList: ${musicArray[i].isfrom}")
                if (musicArray[i].isfrom == IS_MUSIC) {
                    musiclist.add(musicArray[i])
                } else if (musicArray[i].isfrom == IS_EFFECT) {
                    effectlist.add(musicArray[i])
                } else if (musicArray[i].isfrom == IS_RECORD) {
                    recordlist.add(musicArray[i])
                }
            }

            if (musiclist.size != 0) {
                Log.e(TAG, "createMusicEditBtnList: ${musiclist.size}")
                for (m in 0 until musiclist.size) {
                    Log.e(
                        TAG, "createMusicEditBtnList:startTime ${musiclist[m].startTime} >> endtime ${musiclist[m].endTime}  >> title ${musiclist[m].title}")
                    var startTime = 0
                    if (m > 0) {
                        var endTimming = musiclist[m - 1].endTime
                        Log.e(TAG, "createMusicEditBtnList:endTimming: ${endTimming}")

                        if (endTimming > totalWidth) {
                            endTimming = totalWidth - 30L
                        }
                        Log.e(TAG, "createMusicEditBtnList:2endTimming: ${endTimming}")

                        startTime = musiclist[m].startTime.toLong().toInt() +endTimming.toInt() + 10

                        Log.e(TAG, "createMusicEditBtnList:IFstartTime: ${startTime}")
                    } else {
                        startTime = musiclist[m].startTime.toLong().toInt()
                        Log.e(TAG, "createMusicEditBtnList:ELSEstartTime: ${startTime}")
                    }

                    Log.e(TAG, "createMusicEditBtnList11:endTime: ${musiclist[m].endTime.toLong()}")
                    var endTime = musiclist[m].endTime
                    Log.e(TAG, "createMusicEditBtnList:endTime: ${endTime}")
                    Log.e(TAG, "createMusicEditBtnList:totalWidth: ${totalWidth}")

                    if (m>0){
                        if (endTime > totalWidth) {
                            endTime = totalWidth -musiclist[m - 1].endTime - 30
                        }
                        Log.e(TAG, "createMusicEditBtnListmm:IFendTime: ${endTime}")

                    }else{
                        if (endTime > totalWidth) {
                            endTime = totalWidth - 30L
                        }}
                    Log.e(TAG, "createMusicEditBtnListmm:ELSEendTime: ${endTime}")

                    var startTime1 = musiclist[m].startTime.toLong()
                    var endtime = endTime - startTime1
                    Log.e(TAG, "createMusicEditBtnList:startTime1: ${startTime1}")
                    Log.e(TAG, "createMusicEditBtnListfinal:endtime: ${endtime}")

                    binding.audioSeekWave.addSpan(
                        startTime +15,
                        (endTime - startTime1).toInt(),
                        musiclist[m].title,
                        m
                    )
                    binding.audioSeekWave.updateSpan(m, musiclist[m].isSelected)

                }
            }
            Log.e(TAG, "effectlist: ${effectlist.size}")

            if (effectlist.size != 0) {
                for (m in 0 until effectlist.size) {
                    Log.e(TAG, "createMusicEditBtnList: ${effectlist[m].startTime}     endtime ${effectlist[m].endTime}")
                    var startTime = 0
                    if (m > 0) {
                        var endTimming =
                            (effectlist[m - 1].endTime.toLong()).toInt()
                        if (endTimming > totalWidth) {
                            endTimming = totalWidth - 30
                        }
                        startTime =
                            (effectlist[m].startTime.toLong()).toInt() + endTimming + 10
                    } else {
                        startTime =
                            (effectlist[m].startTime.toLong()).toInt()
                    }
                    var endTime =
                        effectlist[m].endTime.toLong().toInt()
                    if (endTime > totalWidth) {
                        endTime = totalWidth - 30
                    }
                    val startTime1 = effectlist[m].startTime.toLong()
                    Log.e(TAG, "seekBarMusicTimeline: "+  effectlist[m].title)
                    Log.e(TAG, "seekBarMusicTimelinestartTime: "+ startTime)
                    Log.e(TAG, "seekBarMusicTimelinestartTime1: "+ endTime  +" IS_SELECTED>>> "+effectlist[m].isSelected)
                   binding.effectsSeekWave.visible()

                    binding.effectsSeekWave.addSpan(startTime+15, (endTime - startTime1).toInt(), effectlist[m].title, m)
                    Log.e(TAG, "seekBarMusicTimeline: "+binding.effectsSeekWave.visibility )
                    binding.effectsSeekWave.updateSpan(m, effectlist[m].isSelected)
                    Log.e(TAG, "seekBarMusicTimeline: ${  binding.effectsSeekWave.layoutParams.height}" )
                    Log.e(TAG, "seekBarMusicTimeline: ${  binding.effectsSeekWave.layoutParams.width}" )

                }
            }
            if (recordlist.size != 0) {
                for (m in 0 until recordlist.size) {
                    var startTime = 0
                    if (m > 0) {
                        var endTimming =
                            ((TimeUnit.MILLISECONDS.toSeconds(recordlist[m - 1].endTime.toLong())) * SEGMENT_VIDEO).toInt()
                        if (endTimming > totalWidth) {
                            endTimming = totalWidth - 30
                        }
                        startTime =
                            ((TimeUnit.MILLISECONDS.toSeconds(recordlist[m].startTime.toLong())) * SEGMENT_VIDEO).toInt() + endTimming + 10
                    } else {
                        startTime =
                            ((TimeUnit.MILLISECONDS.toSeconds(recordlist[m].startTime.toLong())) * SEGMENT_VIDEO).toInt()
                    }
                    var endTime =
                        ((TimeUnit.MILLISECONDS.toSeconds(recordlist[m].endTime.toLong())) * SEGMENT_VIDEO).toInt()
                    if (endTime > totalWidth) {
                        endTime = totalWidth - 30
                    }
                    var startTime1 =
                        ((TimeUnit.MILLISECONDS.toSeconds(recordlist[m].startTime.toLong())) * SEGMENT_VIDEO).toInt()
                    binding.recordSeekWave.addSpan(
                        startTime+15,
                        endTime - startTime1,
                        recordlist[m].title,
                        m
                    )
                    binding.recordSeekWave.updateSpan( m, recordlist[m].isSelected)

                    Log.e(
                        TAG,
                        "createMusicEditBtnList: ${startTime}     endtime ${endTime - startTime1}"
                    )

                }
            }

            binding.audioSeekWave.visibleIf(musiclist.size != 0)
            binding.effectsSeekWave.visibleIf(effectlist.size != 0)
            binding.recordSeekWave.visibleIf(recordlist.size != 0)

            val listener = object : AudioWaveSeekView.TimeLineChangeListener {
                override fun onRangeChanged(
                    tag: Any?,
                    startFraction: Float,
                    endFraction: Float
                ) {
                    Log.e(
                        TAG,
                        "onRangeChangedFIRST>>: " + musicArray[tag as Int].startTime + musicArray[tag].endTime
                    )
                    var endTime = (musicArray[tag].strendTime.toLong())
                    var starttime = musicArray[tag].strstartTime.toLong()
                    if (endTime < endFraction.roundToInt().toLong()) {
                        endTime = endFraction.roundToInt().toLong()
                        musicArray[tag].endTime = endFraction.roundToInt().toLong()
                    }
                    starttime = startFraction.roundToInt().toLong()
                    musicArray[tag].startTime = startFraction.roundToInt().toLong()
                    Log.e(
                        TAG,
                        "onRangeChanged: " + musicArray[tag].startTime + musicArray[tag].endTime
                    )
                    binding.audioSeekWave.updateSpanRange(tag, starttime.toInt(),
                        (endTime - starttime).toInt()
                    )
                    val data = UndoRedoData2(
                        null,
                        null,
                        null,
                        FilterType.DEFAULT,
                        0,
                        0L,0L,musicArray[tag as Int],0

                    )
                    addToChangeList(ACTION_MUSIC, MUSIC_DRAG,data)
                }

                override fun onSelectionChange(tag: Any?, selected: Boolean) {
                    musicPos = tag as Int
                }

                override fun onThumbClicked(tag: Any?, thumbId: Int) {

                }

            }
            binding.audioSeekWave.addIndicatorChangeListener(listener)

            val effectlistener = object : AudioWaveSeekView.TimeLineChangeListener {
                override fun onRangeChanged(
                    tag: Any?,
                    startFraction: Float,
                    endFraction: Float
                ) {
                    var endTime = (musicArray[tag as Int].strendTime.toLong())
                    var starttime = musicArray[tag].strstartTime.toLong()
                    Log.e(TAG, "onRangeChanged: "+endFraction  +"  >>> endtime >>>  "+endTime )
                    if (endTime > endFraction.roundToInt().toLong()) {
                        endTime = endFraction.roundToInt().toLong()
                        Log.e(TAG, "onRangeChanged22: "+endTime )
                        musicArray[tag].endTime = endFraction.roundToInt().toLong()
                    }
                    starttime = startFraction.roundToInt().toLong()
                    musicArray[tag].startTime = startFraction.roundToInt().toLong()
                    binding.effectsSeekWave.updateSpanRange(tag, starttime.toInt(),
                        (endTime - starttime).toInt()
                    )
                    val data = UndoRedoData2(
                        null,
                        null,
                        null,
                        FilterType.DEFAULT,
                        0,
                        0L,0L,musicArray[tag as Int],0

                    )
                    addToChangeList(ACTION_MUSIC, MUSIC_DRAG,data)
                }

                override fun onSelectionChange(tag: Any?, selected: Boolean) {
                    musicPos = tag as Int
                }

                override fun onThumbClicked(tag: Any?, thumbId: Int) {

                }

            }
            binding.effectsSeekWave.addIndicatorChangeListener(effectlistener)

            val recordlistener = object : AudioWaveSeekView.TimeLineChangeListener {
                override fun onRangeChanged(
                    tag: Any?,
                    startFraction: Float,
                    endFraction: Float
                ) {
                    Log.e(
                        TAG,
                        "onRangeChangedFIRST>>: " + musicArray[tag as Int].startTime + musicArray[tag].endTime
                    )
                    var endTime =
                        ((TimeUnit.MILLISECONDS.toSeconds(musicArray[tag].strendTime.toLong())) * SEGMENT_VIDEO).toInt()
                    var starttime =
                        ((TimeUnit.MILLISECONDS.toSeconds(musicArray[tag].strstartTime.toLong())) * SEGMENT_VIDEO).toInt()

                    if (endTime > endFraction.roundToInt().toLong()) {
                        endTime = endFraction.roundToInt()
                        musicArray[tag].endTime = endFraction.roundToInt().toLong()
                    }
                    starttime = startFraction.roundToInt()
                    musicArray[tag].startTime = startFraction.roundToInt().toLong()
                    binding.recordSeekWave.updateSpanRange(tag, starttime, endTime - starttime)
                    val data = UndoRedoData2(
                        null,
                        null,
                        null,
                        FilterType.DEFAULT,
                        0,
                        0L,0L,musicArray[tag as Int],0

                    )
                    addToChangeList(ACTION_MUSIC, MUSIC_DRAG,data)
                }

                override fun onSelectionChange(tag: Any?, selected: Boolean) {
                    musicPos = tag as Int
                }

                override fun onThumbClicked(tag: Any?, thumbId: Int) {

                }

            }
            binding.recordSeekWave.addIndicatorChangeListener(recordlistener)
        }

    }

    /** delete music item from list **/
    private fun deleteMusicFromList(musicData: MusicData) {
        object : CustomAlertDialog(
            this,
            "",
            getString(R.string.msg_remove_music),
            getString(R.string.yes),
            getString(R.string.no)
        ) {
            override fun onClick(id: Int) {
                if (id == R.id.btnPositive) {

                    isTimerStarted = false
                    isMediaPlayerPrepared = false
                    setAudioSource()
                    if (videoEditViewModel.getSelectedMusic()[musicPos].isfrom == IS_MUSIC) {
                        binding.audioSeekWave.removeSpan(musicPos)
                    } else if (videoEditViewModel.getSelectedMusic()[musicPos].isfrom == IS_EFFECT) {
                        binding.effectsSeekWave.removeSpan(musicPos)

                    } else if (videoEditViewModel.getSelectedMusic()[musicPos].isfrom == IS_RECORD) {
                        binding.recordSeekWave.removeSpan(musicPos)
                    }
                    videoEditViewModel.onDeleteSelectedItem(musicData)
                }
            }
        }
    }

    private fun createMusicEditBtnList() {
        isFromMusic = true
      binding.rvVideoEditMusic.visible()
        binding.audioSeekWave.visible()
        binding.effectsSeekWave.visible()
        binding.rvVideoTextEdit.hide()
        binding.rvVideoMusic.hide()
        binding.rvVideoEditBtn.hide()
        binding.llRecord.hide()
        binding.horizontalScroll.visible()
        binding.rvFrame2.visible()
        binding.flFrames.hide()
        arrMusicEdit.clear()
        arrMusicEdit.add(VideoEditBtn(getString(R.string.track), R.drawable.icon_track))
        arrMusicEdit.add(VideoEditBtn(getString(R.string.effects), R.drawable.ic_effect))
        arrMusicEdit.add(VideoEditBtn(getString(R.string.record), R.drawable.ic_record))
        arrMusicEdit.add(VideoEditBtn(getString(R.string.delete), R.drawable.ic_delete_black))
        arrMusicEdit.add(VideoEditBtn(getString(R.string.volume), R.drawable.ic_volume))

        var musicEditAdapter = MusicEditAdapter(arrMusicEdit, object : OnItemClick {
            override fun onItemClick(position: Int) {
                when (position) {
                    0 -> {
                        isMusicFrom = false
                        bottomSheetMusicDialog()
                    }
                    1 -> {
                        isMusicFrom = true
                        bottomSheetMusicDialog()
                    }
                    2 -> {
                        binding.includeToolbar.btnTick.hide()
                        binding.includeToolbar.btnSave.hide()
                        binding.videoSeekBar.hide()
                        binding.tvVideoDuration.hide()
                        binding.ccVideoPlay.hide()
                        playPauseVideo(true)
                        handleRecord()
                    }
                    3 -> {
                        if (musicPos != -1 && videoEditViewModel.getSelectedMusic().size != 0) {
                            Log.e(
                                TAG,
                                "onItemClick:musicPos>>>>> " + videoEditViewModel.getSelectedMusic()
                            )
                            deleteMusicFromList(videoEditViewModel.getSelectedMusic()[musicPos])
                        }
                    }
                    4 -> {
                        if (musicPos != -1 && videoEditViewModel.getSelectedMusic().size != 0) {
                            bottomSheetVolumeDialog(musicPos)
                        }
                    }
                }
            }

        })

        binding.rvVideoEditMusic.layoutManager =
            GridLayoutManager(this, 5)

        binding.rvVideoEditMusic.adapter = musicEditAdapter

        binding.audioSeekWave.visible()
        seekBarMusicTimeline()

    }

    private fun setAudioSource() {
        Log.e(TAG, "setAudioSource: "+videoEditViewModel.getSelectedMusic() )
        if (videoEditViewModel.getSelectedMusic().size > 0 && selectedFunction != SPEED) {
            currentAudioPosition = 0
            try {
                isMediaPlayerPrepared = false
                isTimerStarted = false
                videoEditViewModel.setCurrentMusic(currentAudioPosition)
                try {
                    mediaPlayer.reset()
                    mediaPlayer.setDataSource(videoEditViewModel.getSelectedMusic()[currentAudioPosition].audioUrl)
                    mediaPlayer.prepareAsync()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                setCountDownTimer(videoEditViewModel.getSelectedMusic()[currentAudioPosition])
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            resetMediaPlayer()
        }
    }

    private fun resetMediaPlayer() {
        isMediaPlayerPrepared = false
        isTimerStarted = false
        try {
            mediaPlayer.reset()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (::countDownTimer.isInitialized)
            countDownTimer.cancel()
    }

    private fun playPauseAudio(musicData: MusicData) {
        Log.e(TAG, "playPauseAudio: " + musicData.play)
        Log.e(TAG, "playPauseAudio: " + musicData.audioUrl)
        when (musicData.play) {
            0 -> {
                try {
                    if (mediaPlayer.isPlaying || mediaPlayer.isLooping)
                        mediaPlayer.reset()
                    mediaPlayer.setDataSource(musicData.audioUrl)
                    mediaPlayer.prepareAsync()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            1 -> {
                try {
                    mediaPlayer.reset()
                    mediaPlayer.setDataSource(musicData.audioUrl)
                    mediaPlayer.prepareAsync()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            2 -> {
                try {
                    if (videoEditViewModel.isSelectDifferent.get()) {
                        mediaPlayer.reset()
                        mediaPlayer.setDataSource(musicData.audioUrl)
                    } else if (mediaPlayer.isPlaying)
                        mediaPlayer.pause()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            3 -> {
                try {
                    if (videoEditViewModel.isSelectDifferent.get()) {
                        mediaPlayer.reset()
                        mediaPlayer.setDataSource(musicData.audioUrl)
                        mediaPlayer.prepareAsync()
                    } else
                        mediaPlayer.start()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun applyFilter(filterType: FilterType) {
        val filter = glPlayer.videoGlRender.filter
        when (filterType) {
            VIGNETTE -> filter!!.vignette * 2f

            else -> glPlayer.filter = FilterHelper.getFilter(filterType, this)

        }


    }

    private fun setPlayer() {
        player.addListener(eventListener)
        mediaPlayer.setOnPreparedListener { mPlayer ->
            try {
                isMediaPlayerPrepared = true
                Log.e(TAG, "setPlayer: "+isAudioSeeking )
                Log.e(TAG, "setPlayerselectedFunction: "+selectedFunction )
                if (isAudioSeeking) {
                    mediaPlayer.seekTo(triple.first.toInt() + videoEditViewModel.getSelectedMusic()[currentAudioPosition].startTime.toInt())
                    if (player.playWhenReady && !mediaPlayer.isPlaying) mediaPlayer.start()
                    if (mediaPlayer.isPlaying && ::countDownTimer.isInitialized) {
                        countDownTimer.start()
                    }
                    isAudioSeeking = false
                } else {
                    if (selectedFunction == ADD_MUSIC) {
                        if (!isPlay) mPlayer.start() else if (mPlayer.isPlaying) mPlayer.pause()
                    } else if (videoEditViewModel.getSelectedMusic().size > 0 && selectedFunction != SPEED && selectedFunction != SPLIT) {
                        if (isAudioSeekPositionSet) {
                            isAudioSeekPositionSet = false
                            mPlayer.seekTo(triple.first.toInt() + videoEditViewModel.getSelectedMusic()[currentAudioPosition].startTime.toInt())
                        } else
                            mPlayer.seekTo(videoEditViewModel.getSelectedMusic()[currentAudioPosition].startTime.toInt())
                        if (player.playWhenReady && !mediaPlayer.isPlaying) mPlayer.start() else if (mPlayer.isPlaying) mPlayer.pause()
                        if (mPlayer.isPlaying && ::countDownTimer.isInitialized) {
                            countDownTimer.start()
                            isTimerStarted = true
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        mediaPlayer.setOnCompletionListener {
            videoEditViewModel.playComplete(selectedFunction)
        }
        mediaPlayer.setOnErrorListener { mp, what, extra ->
            if (::countDownTimer.isInitialized)
                countDownTimer.cancel()
            true
        }
    }

    private val runnableSeekBar = Runnable {
        updateProgress()
    }

    private fun updateProgress() {
        // Remove scheduled updates.
        updateSeekBar()
        updateAddedTextVisibility()
        updateVideoSpeed()
        updateVideoFrameProgress()
        removeHandleCallback()
        // Schedule an update if necessary.
        if (player.playWhenReady && player.playbackState != Player.STATE_ENDED) {
            handlerSeekBar.postDelayed(runnableSeekBar, 100)
        }
    }

    private fun updateAddedTextVisibility() {

        if (textStickerList.isNotEmpty()) {
            Log.e(TAG, "updateAddedTextVisibility:updatedDuration ::  $updatedDuration ")
            for (m in 0 until textStickerList.size) {
                for (i in 0 until textStickerList[m].duplicatetext!!.size) {

                    if (updatedDuration >= textStickerList[m].duplicatetext!![i].finalstartTime && updatedDuration <= textStickerList[m].duplicatetext!![i].finalendTime) {
                        if (!binding.stickerView.stickerList.contains(textStickerList[m].duplicatetext!![i].sticker)) {
                            binding.stickerView.showBorder = false
                            binding.stickerView.showIcons = false
//                            binding.stickerView.isLocked = true
                            binding.stickerView.addCopySticker(
                                textStickerList[m].duplicatetext!![i].sticker!!, false
                            )
                        }
                    } else if (updatedDuration >= textStickerList[m].duplicatetext!![i].finalendTime) {
                        removeSticker = true
                        binding.stickerView.remove(textStickerList[m].duplicatetext!![i].sticker!!)
                    }

                    binding.stickerView.invalidate()
                }
            }
        }
    }

    private fun updateVideoFrameProgress() {

        if (::videoFrameHelpercuttor.isInitialized && (selectedFunction == TRIM)) {

            videoFrameHelpercuttor.updatePlayProgress(
                binding.videoSeekBar.progressFloat.toLong(),
                updatedDuration,
                if (selectedFunction == TRIM) arrVideo[tempCurrentPosition].videoDuration else arrVideo[tempCurrentPosition].videoSpeedDuration
            )
        }
    }

    private fun setCurrentPositionZero() {
        arrVideo[currentPosition].isSelected = false
        currentPosition = 0
        arrVideo[currentPosition].isSelected = true
    }

    private fun removeHandleCallback() {
        handlerSeekBar.removeCallbacks(runnableSeekBar)
    }

    private fun updateSeekBar() {
        if (!isSeeking) {
            if (updatedDuration > totalVideoDuration) {
                setCurrentPositionZero()
                updatedDuration = 0
                setSourceNPlay()
                currentAudioPosition = 0
                setAudioSource()

            } else {
                updatedDuration += 100
                binding.videoSeekBar.setProgress(updatedDuration.toFloat())
                binding.tvVideoDuration.text = convertMillieToHMS(updatedDuration)
                if (updatedDuration.toInt() > 0 && player.playWhenReady) {

                    val currentPosition = player.currentPosition
                    val currentWindowIndex = player.currentMediaItemIndex
                    val playback = PlaybackInfo(currentPosition, currentWindowIndex)
                    scrollTimeline(playback)
                }
                updateVideoSpeed()

            }

        }
    }

    private fun scrollTimeline(info: PlaybackInfo) {
        var window = info.window
        val position = info.position

        val windowOffset = durationList.subList(0, window).sumOf { it.seconds.toInt() }
        val positionInSeconds = position * 0.001F
        val scrollPosition = (windowOffset + positionInSeconds) * 100f

        layoutManager!!.isSmoothScrolling.apply {
            true
        }
        //scroll the thumbnail images
        layoutManager!!.scrollToPositionWithOffset(
            0,
            -scrollPosition.toInt()
        )




//        scroll textview the thumbnail images
//        layoutManager2!!.scrollToPositionWithOffset(
//            0,
//            -scrollPosition.toInt()
//        )
//
//        layoutManagerText!!.scrollToPositionWithOffset(
//            0,
//            -scrollPosition.toInt()
//        )
//
//        layoutManagerline!!.scrollToPositionWithOffset(
//            0,
//            -scrollPosition.toInt()
//        )

    }

    private fun updateVideoSpeed() {
        var totalTillCurrent = binding.videoSeekBar.getTotalDurationTillCurrentPos(currentPosition)
        totalTillCurrent = if (updatedDuration > totalTillCurrent)
            (updatedDuration - totalTillCurrent)
        else updatedDuration
        val totalTillCurrentPos =
            if (currentPosition < arrVideo.size - 1) binding.videoSeekBar.getTotalDurationTillCurrentPos(
                currentPosition + 1
            )
            else 0L

        if (currentPosition < arrVideo.size - 1 && updatedDuration > totalTillCurrentPos - 150) {
            changeSpeedOfVideos(arrVideo[currentPosition + 1].speed)
        } else {
            when (selectedFunction) {
                TRIM -> changeSpeedOfVideos(NORMAL_VIDEO_SPEED)
                else -> {
                    val start =
                        if (arrVideo[currentPosition].isStartSpeedChange) arrVideo[currentPosition].speedStartDuration
                        else 0
                    val end =
                        if (arrVideo[currentPosition].isEndSpeedChange) arrVideo[currentPosition].speedStartDuration + ((arrVideo[currentPosition].speedEndDuration - arrVideo[currentPosition].speedStartDuration).toFloat() / arrVideo[currentPosition].speedPlayer).toLong() else arrVideo[currentPosition].videoSpeedDuration
                    if (arrVideo[currentPosition].speed != 50f) {
                        if (totalTillCurrent in (start + 1) until end) {
                            changeSpeedOfVideos(arrVideo[currentPosition].speed)
                        } else {
                            changeSpeedOfVideos(NORMAL_VIDEO_SPEED)
                        }
                    } else
                        changeSpeedOfVideos(NORMAL_VIDEO_SPEED)
                }
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun setData() {
        showProgress()
        GlobalScope.launch(Dispatchers.Unconfined) {
            try {
                val retriever = MediaMetadataRetriever()
                for ((i, videoItem) in arrVideo.withIndex()) {
                    try {
                        retriever.setDataSource(
                            this@VideoEditorActivity,
                            Uri.parse(videoItem.videoUrl)
                        )
                        val pair = isVideoIsPortraitOrLandscape(retriever)
                        val videoWidth = pair.first
                        val videoHeight = pair.second
                        videoItem.isVideoPortrait = videoHeight > videoWidth
                        videoItem.width = videoWidth
                        videoItem.height = videoHeight
                        videoItem.isAudioAvail = isVideoHaveAudioTrack(retriever)
                        videoItem.startDuration = 0
                        videoItem.endDuration = videoItem.videoDuration
                        videoItem.speedEndDuration = videoItem.videoDuration
                        videoItem.isSelected = i == 0
                        videoItem.orientation = getVideoOrientation(retriever)
                        if (videoWidth > videoHeight) horizontalVideoCount++ else verticalVideoCount++


                    } catch (e: Exception) {
                        //writeLog("setData catch MediaMetadataRetriever --> ${e.localizedMessage}")
                        e.printStackTrace()
                    }

                }
                retriever.release()
                //this method is use for download thumbnail using MediaMetadataRetriever
                addVideoFrames(
                    0,
                    0,
                    arrVideo[0].videoDuration,
                    arrVideo[0].videoUri!!
                )
                runOnUiThread {
                    dismissProgress()
                    createVideoEditBtnList()
                    setLayoutManagerForFrames()
                    setVideoSeekBar()
                    setConcatenatingSourceNPlay()
                    playPauseVideo(isPlay)


                }

            } catch (e: Exception) {
                //writeLog("setData GlobalScope.launch--> ${e.localizedMessage}")
                e.printStackTrace()
                dismissProgress()
            }
        }
    }

    private fun addVideoFrames(frameNum: Int, position: Int?, videoLength: Long, videoUri: Uri) {
        if (position == null)
            return
        if (mediaMetadataRetriever == null) {
            mediaMetadataRetriever = MediaMetadataRetriever()

            try {
                mediaMetadataRetriever!!.setDataSource(this, videoUri)
                this.videoLength = videoLength
                this.videoUri = videoUri
            } catch (e: Exception) {
                e.printStackTrace()
            }
            // videoLength = videoData.videoDuration
        }

        if (frameNum == 0) {
            framesToLoad =
                ((TimeUnit.MILLISECONDS.toSeconds(this.videoLength))).toInt()
            frameWidth =
                ((TimeUnit.MILLISECONDS.toSeconds(this.videoLength))).toInt() * SEGMENT_VIDEO / framesToLoad
            frameHeight = frameWidth
            Log.e(TAG, "addVideoFrames: "+frameWidth   +"  >>> "+frameHeight)

        }

        frameTimeOffset = (this.videoLength / framesToLoad).toInt()
        currentTask =
            @SuppressLint("StaticFieldLeak")
            object : AsyncTask<Int?, Int?, Bitmap?>() {

                override fun onPostExecute(bitmap: Bitmap?) {
                    if (!isCancelled) {
                        if (framesToLoad > numFrame) {
                            frames.add(
                                Frames(
                                    bitmap,
                                    position,
                                    frameTimeOffset * (numFrame),
                                    numFrame
                                )
                            )
                        }

                        binding.rvFrame.post {
                            binding.rvFrame.setPadding(
                                binding.llframe.width / 2,
                                0,
                                binding.llframe.width / 2,
                                0
                            )

                        }
                        binding.rvFrame.adapter?.notifyDataSetChanged()


                        if (position == 0 && numFrame == 1)
                            binding.rvFrame.smoothScrollToPosition(0)
                        when {
                            numFrame < framesToLoad -> {
                                addVideoFrames(
                                    numFrame + 1,
                                    position,
                                    this@VideoEditorActivity.videoLength,
                                    this@VideoEditorActivity.videoUri!!,
                                )
                            }
                            position < arrVideo.size - 1 -> {
                                mediaMetadataRetriever = null
                                val newPos = position + 1
                                numFrame = 0
                                addVideoFrames(
                                    (TimeUnit.MILLISECONDS.toSeconds(arrVideo[newPos].startDuration)).toInt(),
                                    newPos,
                                    arrVideo[newPos].endDuration,
                                    arrVideo[newPos].videoUri!!
                                )

                            }
                            else -> {
                                mediaMetadataRetriever = null
                            }
                        }


                    }

                }

                override fun doInBackground(vararg params: Int?): Bitmap? {
                    numFrame = params[0]!!
                    var bitmap: Bitmap? = null
                    if (isCancelled) {
                        return null
                    }
                    try {
                        bitmap = mediaMetadataRetriever!!.getFrameAtTime(
                            (1000 * numFrame * 1000)
                                .toLong(), MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                        )
                        if (isCancelled) {
                            return null
                        }
                        if (bitmap != null) {
                            val result =
                                Bitmap.createBitmap(frameWidth, frameHeight, bitmap.config)
                            val canvas = Canvas(result)
                            val scaleX =
                                frameWidth.toFloat() / bitmap.width.toFloat()
                            val scaleY =
                                frameHeight.toFloat() / bitmap.height.toFloat()
                            val scale = if (scaleX > scaleY) scaleX else scaleY
                            val w = (bitmap.width * scale).toInt()
                            val h = (bitmap.height * scale).toInt()
                            val srcRect =
                                Rect(0, 0, bitmap.width, bitmap.height)
                            val destRect =
                                Rect((frameWidth - w) / 2, (frameHeight - h) / 2, w, h)
                            canvas.drawBitmap(bitmap, srcRect, destRect, null)
                            bitmap.recycle()
                            bitmap = result
                        }
                    } catch (e: Exception) { //FileLog.e(e)
                        e.printStackTrace()
                    }
                    return bitmap
                }
            }
        currentTask!!.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, frameNum, null, null)
    }

    private fun setLayoutManagerForFrames() {
        try {
          /** Home screen thumbnail list*/
            for (i in 0 until arrVideo.size) {
                totalVideoDuration += arrVideo[i].endDuration - arrVideo[i].startDuration
            }
          val totalWidth =  ((TimeUnit.MILLISECONDS.toSeconds(totalVideoDuration)) * SEGMENT_VIDEO).toInt()

            layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
            binding.rvFrame.layoutManager = layoutManager
            binding.rvFrame.adapter =
                VideoTimelineAdapter(
                    this,
                    frames,
                    frameWidth,
                    totalWidth,
                    textfinallineList,
                    musicLineList,
                    callbackItem = { view, position ->
                        currentFrame = position
                        if (frames[currentFrame].videoSequence!! != 0) {
                            player.seekTo(
                                frames[currentFrame].videoSequence!!,
                                frames[currentFrame].videoDuration!!.toLong()
                            )
                            updatedDuration =
                                frames[currentFrame].videoDuration!!.toLong() + arrVideo[frames[currentFrame - 1].videoSequence!!].endDuration

                        } else {
                            player.seekTo(
                                frames[currentFrame].videoSequence!!,
                                frames[currentFrame].videoDuration!!.toLong()
                            )

                            updatedDuration = player.currentPosition

                        }
                        binding.tvVideoDuration.text =
                            convertMillieToHMS(updatedDuration)
                        playPauseVideo(true)
                        isSeeking = true
                        binding.rvFrame.scrollToPosition(position)


                    },
                    callbackTouch = { it, isTouchFrame ->
                        //this method to scroll left and right touch
                        currentFrame = it
                        if (isTouchFrame) {

                            binding.rvTextFrame.scrollToPosition(currentFrame)


                            updateCurrentPosition(frames[currentFrame].videoSequence!!)
                            if (frames[currentFrame].videoSequence!! != 0) {
                                player.seekTo(
                                    frames[currentFrame].videoSequence!!,
                                    frames[currentFrame].videoDuration!!.toLong()
                                )
                                updatedDuration =
                                    frames[currentFrame].videoDuration!!.toLong() + arrVideo[frames[currentFrame - 1].videoSequence!!].endDuration

                            } else {
                                player.seekTo(
                                    frames[currentFrame].videoSequence!!,
                                    frames[currentFrame].videoDuration!!.toLong()
                                )

                                updatedDuration = player.currentPosition
                                playPauseVideo(true)
                            }
                            binding.tvVideoDuration.text =
                                convertMillieToHMS(updatedDuration)
                            playPauseVideo(true)
                            isSeeking = true

                        }
                    })



            //for text sticker thumbnail generate
            layoutManager2 = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
            binding.rvFrame2.layoutManager = layoutManager2
            binding.rvFrame2.adapter =
                VideoFrameAdapter(this, frames, frameWidth, callbackDelete = { view, position ->

                })


        } catch (e: IllegalSeekPositionException) {
            Log.e("message", "setLayoutManagerForFrames: " + e.message)
        }

    }

    private fun observePlayback() {
        disposable = timerObservable
            .subscribe(::scrollTimeline)
    }

    private val timerObservable: Observable<PlaybackInfo>
        get() =
            Observable.interval(50, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .map {
                    var currentPosition = player.currentPosition
                    val currentWindowIndex = player.currentMediaItemIndex

                    PlaybackInfo(currentPosition, currentWindowIndex)
                }


    private fun positionAndOffsetFromCenter(): Pair<Int, Float> {
        val (index, offset) = binding.rvFrame.findChildViewUnder(
            getDeviceSize().first / 2.toFloat(),
            binding.rvFrame.pivotY
        )?.run {
            val position = binding.rvFrame.getChildAdapterPosition(this)
            position to (getDeviceSize().first / 2 - left).toFloat() / width
        } ?: 0 to 0F

        return index to offset
    }

    private fun setVideoSeekBar() {
        val durationArray = LongArray(arrVideo.size + 1)
        durationArray[0] = 0
        for (i in 0 until arrVideo.size) {
            durationArray[i + 1] = arrVideo[i].videoDuration
        }
        videoEditViewModel.setActualVideoDurationArray(durationArray)
        binding.videoSeekBar.setTickCount(durationArray)
        binding.videoSeekBar.setOnSeekChangeListener(videoSeekBarChangeListener)
    }

    private val videoSeekBarChangeListener = object : OnSeekChangeListener {
        override fun onSeeking(seekBar: VideoSeekBar?) {
            try {
                updatedDuration = seekBar!!.progressFloat.toLong()
                Log.e(TAG, "onSeeking: " + seekBar.progress)
                if (isSeeking || isBackWardForward || isChangingFromText || isChangingFromTrim) {
                    val pair = seekBar.getVideoDurationFromPosition()
                    changeSpeedOfVideos(if (selectedFunction == SPEED) tempSpeed else if (selectedFunction == TRIM || selectedFunction == SPLIT || selectedFunction == ROTATE || selectedFunction == FILTER || selectedFunction == VOLUME) arrVideo[tempCurrentPosition].speed else arrVideo[if (pair.first == -1) 0 else pair.first].speed)
                    applyFilterOnPlayer()
                    when {
                        isChangingFromText -> {
                            updateCurrentPosition(if (pair.first == -1) 0 else pair.first)
                            val duration =
                                if (arrVideo[pair.first].speed != 50f) pair.second * (arrVideo[pair.first].endDuration - arrVideo[pair.first].startDuration) / arrVideo[pair.first].videoSpeedDuration else pair.second
                            player.seekTo(
                                if (pair.first == -1) 0 else pair.first,
                                if (pair.first == -1) C.TIME_UNSET else duration
                            )
                        }
                        isChangingFromTrim -> {
                            val duration =
                                if (arrVideo[tempCurrentPosition].speed != 50f) (seekBar.progressFloat * (arrVideo[tempCurrentPosition].endDuration - arrVideo[tempCurrentPosition].startDuration).toFloat() / arrVideo[tempCurrentPosition].videoSpeedDuration.toFloat()).toLong() else seekBar.progressFloat.toLong()
                            player.seekTo(duration)
                            binding.tvVideoDuration.text = convertMillieToHMS(seekBar.progressFloat.toLong())
                        }
                        isSeeking || isBackWardForward -> {
                            updateCurrentPosition(if (pair.first == -1) 0 else pair.first)

                            val duration =
                                if (arrVideo[pair.first].speed != 50f) pair.second * (arrVideo[pair.first].endDuration - arrVideo[pair.first].startDuration) / arrVideo[pair.first].videoSpeedDuration else pair.second

                            player.seekTo(
                                if (pair.first == -1) 0 else pair.first,
                                if (pair.first == -1) C.TIME_UNSET else duration
                            )
                            binding.tvVideoDuration.text = convertMillieToHMS(seekBar.progressFloat.toLong())

                            val durationSecond = ((TimeUnit.MILLISECONDS.toSeconds(duration))).toInt()

                            if (isBackWardForward) {
                                isBackWardForward = false
                                playPauseVideo(true)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                //writeLog("videoSeekBarChangeListener onSeeking --> ${e.localizedMessage}")
                e.printStackTrace()
            }
        }

        override fun onStartSeeking(seekBar: VideoSeekBar?) {
            isSeeking = true
            videoEditViewModel.isShowThumbnail.set(false)
            removeHandleCallback()
            playPauseVideo(true)

        }

        private fun removeHandleCallback() {
            handlerSeekBar.removeCallbacks(runnableSeekBar)
        }


        override fun onStopSeeking(seekBar: VideoSeekBar?) {
            if (!isChangingFromText && !isChangingFromTrim) {
                try {
                    val pair = seekBar!!.getVideoDurationFromPosition()
                    updateCurrentPosition(pair.first)
                    val duration =
                        if (arrVideo[pair.first].videoSpeedDuration > 0) pair.second * (arrVideo[pair.first].endDuration - arrVideo[pair.first].startDuration) / arrVideo[pair.first].videoSpeedDuration else pair.second
                    player.seekTo(
                        if (pair.first == -1) 0 else pair.first,
                        if (pair.first == -1) C.TIME_UNSET else duration
                    )
                    if (selectedFunction == MUSIC_MULTIPLE)
                        playPauseVideo(isChangingFromMusic)
                    else if (selectedFunction != ADD_TEXT_VIEW_MULTIPLE && selectedFunction != ADD_TEXT_VIEW)

                        playPauseVideo(true)
                    applyVideoVolumeOnPlayer()
                    applyAudioVolumeOnPlayer()
                    updateCountDownTimer()
                    Log.e(TAG, "updateCountDownTimer3: " )
                    isSeeking = false
                } catch (e: Exception) {
                    //writeLog("videoSeekBarChangeListener onStopSeeking --> ${e.localizedMessage}")
                    e.printStackTrace()
                }
            }
        }
    }

    private fun applyVideoVolumeOnPlayer() {
       val volume =
            if (selectedFunction == VOLUME)
                tempVideoVolume.toFloat()
            else if (selectedFunction == TRIM  || selectedFunction == FILTER)
                arrVideo[tempCurrentPosition].videoVolume.toFloat()
            else arrVideo[player.currentMediaItemIndex].videoVolume.toFloat()
//                    ) / 100
        player.volume = volume
    }

    private fun applyAudioVolumeOnPlayer() {
        val volume = if (selectedFunction == VOLUME) tempAudioVolume.toFloat()
        else if (selectedFunction == TRIM || selectedFunction == FILTER)
            arrVideo[tempCurrentPosition].audioVolume.toFloat()
        else arrVideo[player.currentMediaItemIndex].audioVolume.toFloat()
        mediaPlayer.setVolume(volume / 100, volume / 100)

    }

    private fun applyFilterOnPlayer() {
        if (selectedFunction == TRIM || selectedFunction == SPLIT || selectedFunction == SPEED || selectedFunction == VOLUME) {
            videoEditViewModel.applyFilter(arrVideo[tempCurrentPosition].filter)
        } else if (selectedFunction == FILTER) {
            videoEditViewModel.applyFilter(videoEditViewModel.getSelectedFilter())
        } else {
            videoEditViewModel.applyFilter(arrVideo[player.currentMediaItemIndex].filter)
        }
    }

    private fun updateCountDownTimer(isChooseVideo: Boolean = false) {
        if (videoEditViewModel.getSelectedMusic()
                .isNotEmpty() && selectedFunction != SPEED
        ) {
            val duration: Long =
                if (selectedFunction == TRIM || selectedFunction == FILTER || selectedFunction == VOLUME) {
                    videoEditViewModel.getTotalVideoDurationTillCurrentPos(tempCurrentPosition) + updatedDuration
                } else {
                    if (isChooseVideo) videoEditViewModel.getTotalVideoDurationTillCurrentPos(
                        currentPosition
                    )
                    else updatedDuration
                }
            triple = videoEditViewModel.getMusicSeekPosition(duration)
            if (triple.second > 0) {
//                if (selectedFunction == VOLUME) {
//                    videoEditViewModel.isAudioAvailable.set(true)
//                    bindingVolume.seekBarAudioVolume.progress =
//                        if (videoEditViewModel.isAudioAvailable.get()) tempAudioVolume else 0
//                }
                if (triple.third == currentAudioPosition) {
                    if (isMediaPlayerPrepared) {
                        mediaPlayer.seekTo(triple.first.toInt() + videoEditViewModel.getSelectedMusic()[currentAudioPosition].startTime.toInt())
                        if (player.playWhenReady && !mediaPlayer.isPlaying) mediaPlayer.start()
                        if (::countDownTimer.isInitialized) {
                            countDownTimer.cancel()
                            setCountDownTimer(null, triple.second)
                            countDownTimer.start()
                        }
                    } else {
                        try {
                            currentAudioPosition = triple.third
                            mediaPlayer.reset()
                            mediaPlayer.setDataSource(videoEditViewModel.getSelectedMusic()[triple.third].audioUrl)
                            mediaPlayer.prepareAsync()
                            isAudioSeekPositionSet = true
                            if (::countDownTimer.isInitialized) {
                                countDownTimer.cancel()
                                setCountDownTimer(null, triple.second)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } else {
                    try {
                        Log.e(TAG, "setPlayertcurrentAudioPosition12>>> :${triple.third} " )
                        isMediaPlayerPrepared = false
                        currentAudioPosition = triple.third
                        mediaPlayer.reset()
                        mediaPlayer.setDataSource(videoEditViewModel.getSelectedMusic()[triple.third].audioUrl)
                        mediaPlayer.prepareAsync()
                        isAudioSeekPositionSet = true
                        if (::countDownTimer.isInitialized) {
                            countDownTimer.cancel()
                            setCountDownTimer(null, triple.second)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } else {
//                if (selectedFunction == VOLUME) {
//                    videoEditViewModel.isAudioAvailable.set(false)
//                    bindingVolume.seekBarAudioVolume.progress =
//                        if (videoEditViewModel.isAudioAvailable.get()) tempAudioVolume else 0
//                }
                mediaPlayer.reset()
                currentAudioPosition = 0
                isMediaPlayerPrepared = false
                isTimerStarted = false
                if (::countDownTimer.isInitialized)
                    countDownTimer.cancel()
            }
        } else {
            mediaPlayer.reset()
            currentAudioPosition = 0
            isMediaPlayerPrepared = false
            isTimerStarted = false
            if (::countDownTimer.isInitialized)
                countDownTimer.cancel()
        }
    }

    private fun setCountDownTimer(musicData: MusicData? = null, time: Long = 0) {
        countDownTimer = object : CountDownTimer(
            if (musicData != null) musicData.endTime - musicData.startTime else time,
            100
        ) {
            override fun onFinish() {
                isMediaPlayerPrepared = false
                isTimerStarted = false
                if (currentAudioPosition < videoEditViewModel.getSelectedMusic().size - 1) {
                    currentAudioPosition++

                    try {
                        countDownTimer.cancel()
                        setCountDownTimer(videoEditViewModel.getSelectedMusic()[currentAudioPosition])
                        mediaPlayer.reset()
                        mediaPlayer.setDataSource(videoEditViewModel.getSelectedMusic()[currentAudioPosition].audioUrl)
                        mediaPlayer.prepareAsync()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                } else {
                    try {
                        mediaPlayer.reset()
                        currentAudioPosition = 0
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onTick(millisUntilFinished: Long) {
                if (updatedDuration >= videoEditViewModel.getTotalMusicDuration()) {
                    try {
                        mediaPlayer.reset()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    currentAudioPosition = 0
                    isMediaPlayerPrepared = false
                    isTimerStarted = false
                    countDownTimer.cancel()
                }
            }
        }
    }

    private fun updateCurrentPosition(currentPos: Int) {
        if (currentPosition < arrVideo.size && currentPosition != currentPos) {
            arrVideo[currentPosition].isSelected = false
            currentPosition = currentPos
            arrVideo[currentPosition].isSelected = true

        }
    }

    private fun setConcatenatingSourceNPlay() {
        player.removeListener(eventListener)

        concatenatingMediaSource.clear()
        totalVideoDuration = 0
        for (i in 0 until arrVideo.size) {
            val mediaItem: MediaItem = MediaItem.fromUri(arrVideo[i].videoUri!!)
            concatenatingMediaSource.addMediaSource(
                ProgressiveMediaSource.Factory(
                    dataSourceFactory
                ).createMediaSource(mediaItem).let {
                    ClippingMediaSource(
                        it,
                        arrVideo[i].startDuration * 1000L,
                        arrVideo[i].endDuration * 1000L
                    )
                }
            )
            totalVideoDuration += if (arrVideo[i].speed != 50f) arrVideo[i].videoSpeedDuration else (arrVideo[i].endDuration - arrVideo[i].startDuration)
            /**
             * //concatenatingMediaSource.addMediaSource(ProgressiveMediaSource.Factory(dataSourceFactory)
             * //.createMediaSource(Uri.parse(arrVideo[currentPosition].videoUrl)))
             */
        }
        Log.e("VideoEditing", "totalVideoDuration $totalVideoDuration")
        player.setMediaSource(concatenatingMediaSource)
        player.prepare()

//        player.prepare(concatenatingMediaSource, false, false)
        /**
         * // val source = ProgressiveMediaSource.Factory(dataSourceFactory)
         * // .createMediaSource(Uri.parse(arrVideo[currentPosition].videoUrl))
         * // player.prepare(source, false, false)
         */
        player.addListener(eventListener)

//        videotextStickerListAdapter.setTotalVideoDuration(totalVideoDuration)
    }

    private fun setSourceNPlay() {
        try {
            //writeLog("setSourceNPlay try --> currentPosition $currentPosition concatenatingMediaSource.size ${concatenatingMediaSource.size}")
            if (currentPosition < concatenatingMediaSource.size) {
                //writeLog("setSourceNPlay player seek")
                playPauseVideo(true)
                player.seekTo(currentPosition, C.TIME_UNSET)
                isSeekToPos = true
            }

        } catch (e: Exception) {
            //writeLog("setSourceNPlay catch --> ${e.localizedMessage}")
            e.printStackTrace()
        }
    }

    private fun changeSpeedOfVideos(speed: Float) {
        val videoSpeed = getSpeedValue(speed)
        if (player.playbackParameters.speed != videoSpeed) {
            player.playbackParameters = PlaybackParameters(videoSpeed)
        }
    }

    private fun getSpeedValue(speed: Float): Float {
        return when (speed) {
            0f -> 0.2f
            12.5f -> 0.4f
            25.0f -> 0.6f
            37.5f -> 0.8f
            50.0f -> 1.0f
            62.5f -> 2.0f
            75.0f -> 3.0f
            87.5f -> 4.0f
            100.0f -> 5.0f
            else -> 1f
        }
    }

    private fun playPauseVideo(isPlay: Boolean) {
        Log.e("playPauseVideo", "playPauseVideo: " + videoEditViewModel.audioRecordingStatus.get() )
        if (videoEditViewModel.audioRecordingStatus.get() != 1) {
            this.isPlay = isPlay
            playPauseAudio(isPlay)
            if (isPlay) {
                //30/03/2022
                isSeeking = true
                player.playWhenReady = false
                binding.imgPlay.setImageResource(R.drawable.ic_play)


            } else {
                //30/03/2022
                isSeeking = false
                player.playWhenReady = true
                isTouchFrame = false
                binding.imgPlay.setImageResource(R.drawable.ic_pause)


                if (videoEditViewModel.isShowThumbnail.get())
                    videoEditViewModel.isShowThumbnail.set(false)
            }
            this.isPlay = !this.isPlay
        }
    }

    private var eventListener = object : Player.Listener {
        @SuppressLint("SwitchIntDef")
        override fun onPositionDiscontinuity(reason: Int) {
            when (reason) {
                Player.DISCONTINUITY_REASON_REMOVE -> {
                    applyFilterOnPlayer()
                    //writeLog("Player.DISCONTINUITY_REASON_PERIOD_TRANSITION --> ${Player.DISCONTINUITY_REASON_PERIOD_TRANSITION}")
                    if (selectedFunction != ON_PROGRESS && selectedFunction != TRIM && selectedFunction != SPLIT && selectedFunction != SPEED && selectedFunction != FILTER && selectedFunction != ROTATE && selectedFunction != VOLUME) {
                        changeSpeedOfVideos(arrVideo[player.currentMediaItemIndex].speed)
                        applyVideoVolumeOnPlayer()
                        applyAudioVolumeOnPlayer()
                        updateCurrentPosition(player.currentMediaItemIndex)
                    }
                }
            }

        }

        @SuppressLint("SwitchIntDef")
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            Log.e(TAG, "onPlayerStateChanged: " + playbackState)
            when (playbackState) {
                Player.STATE_READY -> {

                    Log.e(TAG, "onPlayerStateChanged: "+isFrameSizeSet )
                    if (!isFrameSizeSet) {
                        setMaxVideoFrameSize()
                    }
                    applyFilterOnPlayer()
                    if (isSeekToPos) {
                        if (tempCurrentPosition != 0) {
                        } else {
                            isSeekToPos = false
                            applyFilterOnPlayer()
                            applyVideoVolumeOnPlayer()
                            applyAudioVolumeOnPlayer()
                        }
                    }
                    if (isChangingFromTrim && !isSeeking) {

                        isChangingFromTrim = false
                        setCurrentPositionZero()
                        updatedDuration = if (selectedFunction == TRIM) tempStartTime else 0
                        setSourceNPlay()
                        updateCountDownTimer()
                        Log.e(TAG, "updateCountDownTimer4: " )

                    }
                    if (!isSplitInitialize) {
                        updateProgress()
                        if (isChangingFromMusic) {
                            isChangingFromMusic = false
                            updateCountDownTimer()
                            Log.e(TAG, "updateCountDownTimer5: " )

                        }

                    } else {
                        isChangingFromText = false
                        isSplitInitialize = false
                        isSeeking = false
                    }
                }
                Player.STATE_ENDED -> {
                    try {
                        setCurrentPositionZero()
                        updatedDuration = if (selectedFunction == TRIM) tempStartTime else 0

                        setSourceNPlay()
                        binding.rvFrame.post {
                            binding.rvFrame.smoothScrollToPosition(0)
                        }


                        updateCountDownTimer()
                        Log.e(TAG, "updateCountDownTimer6: " )
                    } catch (e: Exception) {
                        //writeLog("Player.STATE_ENDED --> ${e.localizedMessage}")
                        e.printStackTrace()
                    }
                }


            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
//            if (isPlaying) {
//                observePlayback()
//            } else {
//                disposable?.dispose()
//            }
        }

        override fun onTimelineChanged(timeline: Timeline, reason: Int) {
            durationList.clear()
            val timeline = player.currentTimeline
            var totalTime = 0F
            val tempWindow = Timeline.Window()
            for (i in 0 until timeline.windowCount) {
                val windowDuration = timeline.getWindow(i, tempWindow).durationMs
                totalTime += windowDuration
                durationList.add(Duration(windowDuration))
            }

        }

    }

    private fun setMaxVideoFrameSize() {
        var resizeWidth = 0
        var resizeHeight = 0
        for (videoItem in arrVideo) {
            val pair = getVideoFrameSize(videoItem.width, videoItem.height)
            resizeWidth = max(resizeWidth, pair.first)
            resizeHeight = max(resizeHeight, pair.second)
        }
        val params = binding.stickerView.layoutParams
        params.width = resizeWidth
        params.height = resizeHeight
        binding.stickerView.layoutParams = params
        isFrameSizeSet = true

    }
    private fun getVideoFrameSize(width: Int, height: Int): Pair<Int, Int> {
        val playerWidth = binding.playerView.width
        val playerHeight = binding.playerView.height
        var ivContentWidth = playerWidth
        var ivContentHeight = playerHeight
        if (height > 0 && width > 0)
            if (width > height) {
//                ivContentHeight = playerHeight
//                ivContentWidth = (ivContentHeight * width) / height
//                if (ivContentWidth > playerWidth) {
//                    ivContentWidth = playerWidth
//                    ivContentHeight = (ivContentWidth * height) / width
//                }
            } else {
                ivContentWidth = playerWidth
                ivContentHeight = (ivContentWidth * height) / width
                if (ivContentHeight > playerHeight) {
                    ivContentHeight = playerHeight
                    ivContentWidth = (ivContentHeight * width) / height
                }
            }
        return Pair(ivContentWidth, ivContentHeight)
    }


    private fun playPauseAudio(isPlay: Boolean) {
        if (videoEditViewModel.getSelectedMusic().size > 0) {
            try {
                if (isMediaPlayerPrepared) {
                    if (isPlay) {
                        if (mediaPlayer.isPlaying)
                            mediaPlayer.pause()
                        if (::countDownTimer.isInitialized && isTimerStarted) {
                            countDownTimer.cancel()
                        }
                    } else {
                        mediaPlayer.start()
                        if (::countDownTimer.isInitialized) {
                            countDownTimer.start()
                            isTimerStarted = true
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun setDataForAdditionVideo(mArrVideo: ArrayList<VideoItem>) {
        showProgress()
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val retriever = MediaMetadataRetriever()
                for (videoItem in mArrVideo) {
                    try {
                        retriever.setDataSource(
                            this@VideoEditorActivity,
                            Uri.parse(videoItem.videoUrl)
                        )
                        val pair = isVideoIsPortraitOrLandscape(retriever)
                        val videoWidth = pair.first
                        val videoHeight = pair.second
                        videoItem.isVideoPortrait = videoHeight > videoWidth
                        videoItem.width = videoWidth
                        videoItem.height = videoHeight
                        videoItem.isAudioAvail = isVideoHaveAudioTrack(retriever)
                        videoItem.startDuration = 0
                        videoItem.endDuration = videoItem.videoDuration
                        videoItem.speedEndDuration = videoItem.videoDuration
                        videoItem.isSelected = false
                        videoItem.orientation = getVideoOrientation(retriever)
                        if (videoWidth > videoHeight) horizontalVideoCount++ else verticalVideoCount++
                    } catch (e: Exception) {
                        //writeLog("setData catch MediaMetadataRetriever --> ${e.localizedMessage}")
                        e.printStackTrace()
                    }
                }
                retriever.release()

                runOnUiThread {
                    addVideoFrames(
                        0,
                        0,
                        mArrVideo[0].videoDuration,
                        mArrVideo[0].videoUri!!
                    )


                    dismissProgress()
                    createVideoEditBtnList()
                    setConcatenatingSourceNPlay()
                    resetSeekBar()
                    setCurrentPositionZero()
                    setSourceNPlay()

                }
            } catch (e: Exception) {
                //writeLog("setData GlobalScope.launch--> ${e.localizedMessage}")
                e.printStackTrace()
                dismissProgress()
            }
        }
    }

    private fun resetSeekBar() {
        totalVideoDuration = 0
        val durationArray = LongArray(arrVideo.size + 1)
        durationArray[0] = 0
        for (i in 0 until arrVideo.size) {
            durationArray[i + 1] =
                if (arrVideo[i].speed != 50f) arrVideo[i].videoSpeedDuration else (arrVideo[i].endDuration - arrVideo[i].startDuration)
            totalVideoDuration += durationArray[i + 1]
        }
        videoEditViewModel.setActualVideoDurationArray(durationArray)
        binding.videoSeekBar.refreshView(durationArray)
        binding.videoSeekBar.post {
            updatedDuration = 0
            player.seekTo(0)
            binding.tvVideoDuration.text = convertMillieToHMS(0)
            binding.videoSeekBar.setProgress(0f)
            playPauseVideo(true)
            Log.e("setSeekBarForOneVideo", "setSeekBarForOneVideo: 10" + updatedDuration)
//            updateTextArr()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_ADD_VIDEO -> {
                    isNewVideoAdded = true
                    setDataForAdditionVideo(data!!.getParcelableArrayListExtra(ARR_VIDEO)!!)
                }
            }
        }

    }

    private fun saveChangesOnVideo() {
        Log.e(TAG, "saveChangesOnVideo: '" + selectedFunction)
        when (selectedFunction) {
            TRIM -> saveTrimChangeOnVideo()
            FILTER -> saveFilterChangeOnVideo()

        }
    }

    private fun trimVisibility() {
        binding.imgDone.hide()
        binding.imgFullscreen.visible()
        binding.llItemView.visible()
        binding.trimCuttr.hide()
    }

    private fun musicVisibility() {
        binding.imgDone.hide()
        binding.imgFullscreen.visible()
        binding.llItemView.visible()
        binding.flFrames.visible()
        binding.llRecord.hide()
        binding.rvVideoEditBtn.visible()
        binding.rvVideoEditMusic.hide()
        binding.rvVideoMusic.hide()
        binding.ivRecord.hide()
        binding.tvRecordTime.hide()
        binding.horizontalScroll.hide()
    }

    private fun filterVisibility() {
        binding.imgDone.hide()
        binding.imgAllapply.hide()
        binding.ccFilter.hide()
        binding.imgFullscreen.visible()
        binding.llItemView.visible()

    }

    private fun updateDurationNPlayVideo() {
        player.removeListener(eventListener)
        concatenatingMediaSource.clear()
        for (i in 0 until arrVideo.size) {
            Log.e(
                TAG,
                "updateDurationNPlayVideo: " + arrVideo[i].startDuration + " endDuration>> " + arrVideo[i].endDuration
            )
            val mediaItem: MediaItem = MediaItem.fromUri(arrVideo[i].videoUri!!)
            concatenatingMediaSource.addMediaSource(
                ProgressiveMediaSource.Factory(
                    dataSourceFactory
                ).createMediaSource(mediaItem).let {
                    ClippingMediaSource(
                        it,
                        arrVideo[i].startDuration * 1000L,
                        arrVideo[i].endDuration * 1000L
                    )
                }
            )
            totalVideoDuration += if (arrVideo[i].speed != 50f) arrVideo[i].videoSpeedDuration else (arrVideo[i].endDuration - arrVideo[i].startDuration)
            /**
             * //concatenatingMediaSource.addMediaSource(ProgressiveMediaSource.Factory(dataSourceFactory)
             * //.createMediaSource(Uri.parse(arrVideo[currentPosition].videoUrl)))
             */
        }
        player.setMediaSource(concatenatingMediaSource, false)
        player.addListener(eventListener)
    }

    override fun onDestroy() {
        player.release()
        mediaPlayer.release()
        videoEditViewModel.onDestroy()
        super.onDestroy()
    }

    private fun setVisibilityOfViewOnCancel() {
        when (selectedFunction) {

            TRIM, SPLIT, ROTATE -> {
                isSingleToAll = true
                isChangingFromTrim = false
                tempStartTime = 0L
                tempEndTime = 0L
                tempSplitDuration = 0L
                removeFrameView()
                player.setMediaSource(concatenatingMediaSource, false)
                resetSeekBar()
                removeAllViews()
            }
            FILTER -> {
                isSingleToAll = true
                player.setMediaSource(concatenatingMediaSource, false)
                resetSeekBar()
                removeAllViews()


            }
        }
    }

    private fun removeFrameView() {
        if (::videoFrameHelpercuttor.isInitialized)
            videoFrameHelpercuttor.onDestroy()

    }

    private fun removeAllViews() {
        if (!isPlay)
            binding.imgPlay.visible()
        Log.e(TAG, "removeAllViews: " + arrVideo)
        selectedFunction = -1
//        if (arrVideo.size == 1) {
//
        updatedDuration = 0
        currentPosition = 0
        currentAudioPosition = 0
        player.seekTo(0)
        binding.videoSeekBar.setProgress(0f)
        binding.tvVideoDuration.text = convertMillieToHMS(0)
        setAudioSource()


//        }
    }

    /*this method is set the icon of textbox that appear on image (icon is for rotation,zoomin zoomout,cancel)*/
    private fun addIcon(sticker: StickerView) {
        val zoomIcon = BitmapStickerIcon(
            ContextCompat.getDrawable(
                this,
                R.drawable.ic_expand
            ),
            BitmapStickerIcon.RIGHT_BOTOM
        )
        zoomIcon.iconEvent = ZoomIconEvent()

        val rotateIcon = BitmapStickerIcon(
            ContextCompat.getDrawable(
                this,
                R.drawable.ic_rotate_small
            ),
            BitmapStickerIcon.RIGHT_TOP
        )
        rotateIcon.iconEvent = RotationIconEvent()


        val deleteIcon = BitmapStickerIcon(
            ContextCompat.getDrawable(
                this,
                R.drawable.ic_delete
            ),
            BitmapStickerIcon.LEFT_TOP
        )
        deleteIcon.iconEvent = DeleteIconEvent()

        val list = ArrayList<BitmapStickerIcon>()
        list.add(zoomIcon)
        list.add(rotateIcon)
        list.add(deleteIcon)
        sticker.icons = list

    }

    private fun saveMusicChangesOnVideo() {
        removeAllViews()
//        handleMultipleMusicView(false)
        player.setMediaSource(concatenatingMediaSource ,true)
        updatedDuration = 0
        currentAudioPosition = 0
        isChangingFromMusic = false
        isAudioSeeking = false
        isTimerStarted = false
        isMediaPlayerPrepared = false
        resetSeekBar()
        setCurrentPositionZero()
        setSourceNPlay()
        setAudioSource()
        musicVisibility()
    }

    //this method for removing sticker from list
    private fun addStickers() {
        for (m in textStickerList.indices) {
            for (i in textStickerList[m].duplicatetext!!.indices) {
                if (!binding.stickerView.stickerList.contains(textStickerList[m].duplicatetext!![i].sticker)) {
                    binding.stickerView.addCopySticker(
                        textStickerList[m].duplicatetext!![i].sticker!!,
                        false
                    )
                }
            }
        }

        Log.e(TAG, "updateAddedTextVisibility: " + textStickerList)
        binding.stickerView.invalidate()
    }

    /* text sticker*/
    private fun addStickerToList() {
        if (removeSticker) {
            addStickers()
        }
        if (isEdit && textStickerList.size != 0) {
            val sticker = binding.stickerView.currentSticker as TextSticker
            for (i in 0 until textStickerList.size) {
                textStickerList[i].duplicatetext!!.filter { it.sticker == sticker }.forEach {
                    it.info = sticker.text
                    it.text = sticker.text.toString()
                }
            }
            if (binding.stickerView.currentSticker != null) {
                var textData = TextStickerData((binding.stickerView.currentSticker as TextSticker).currentAngle,
                    (binding.stickerView.currentSticker as TextSticker).currentScale,
                    (binding.stickerView.currentSticker as TextSticker).x,
                    (binding.stickerView.currentSticker as TextSticker).y,
                    (binding.stickerView.currentSticker as TextSticker).startTime,
                    (binding.stickerView.currentSticker as TextSticker).endTime,
                    (binding.stickerView.currentSticker as TextSticker).getstrtag(),
                    (binding.stickerView.currentSticker as TextSticker).fontStyle,
                    (binding.stickerView.currentSticker as TextSticker).typeface,
                    (binding.stickerView.currentSticker as TextSticker).alignment().toString(),
                    (binding.stickerView.currentSticker as TextSticker).size,
                    (binding.stickerView.currentSticker as TextSticker).text.toString(),
                    (binding.stickerView.currentSticker as TextSticker).borderColor,
                    (binding.stickerView.currentSticker as TextSticker).borderType,
                    false,
                    false,
                    (binding.stickerView.currentSticker as TextSticker).color,
                    (binding.stickerView.currentSticker as TextSticker).color,
                    (binding.stickerView.currentSticker as TextSticker).pattern,
                    (binding.stickerView.currentSticker as TextSticker).gradient,
                    (binding.stickerView.currentSticker as TextSticker).matrix
                )
                var data = UndoRedoData2(
                    strTextSticker as TextSticker,
                    copySticker(binding.stickerView.currentSticker as TextSticker, ""),
                    textData
                )
                addToChangeList(EDIT_TEXT, TEXT_ADD,data)}

        } else {
            if (binding.stickerView.stickerList.isNotEmpty()) {
                val itemlist: ArrayList<SpanData> = ArrayList()
                if (binding.stickerView.currentSticker is TextSticker) {
                    var sticker = binding.stickerView.currentSticker as TextSticker
                    val textData = SpanData()
                    textData.startTime = sticker.startTime
                    textData.endTime = sticker.endTime
                    textData.finalstartTime = (sticker.startTime.toInt() * 10).toLong()
                    textData.finalendTime =
                        (sticker.endTime.toInt()).toLong() * 10
                    textData.endTime = sticker.endTime
                    textData.x = sticker.x.toFloat()
                    textData.offset = sticker.startTime.toInt()
                    textData.length = sticker.endTime.toInt() - sticker.startTime.toInt()
                    textData.mobject = 0
                    textData.info = sticker.text
                    textData.y = sticker.y.toFloat()
//                    textData.bitmap = textAsBitmap(sticker)
                    textData.strTag = sticker.getstrtag()
                    textData.text = sticker.text.toString()
                    textData.sticker = sticker
                    textData.isSelected = false
                    itemlist.add(textData)
                    textStickerList.add(
                        CopyModel(
                            itemlist,
                            ((TimeUnit.MILLISECONDS.toSeconds(totalVideoDuration)) * SEGMENT_VIDEO).toInt()
                        )
                    )


                }

                if (binding.stickerView.currentSticker != null) {
                    var textData = TextStickerData((binding.stickerView.currentSticker as TextSticker).currentAngle,
                        (binding.stickerView.currentSticker as TextSticker).currentScale,
                        (binding.stickerView.currentSticker as TextSticker).x,
                        (binding.stickerView.currentSticker as TextSticker).y,
                        (binding.stickerView.currentSticker as TextSticker).startTime,
                        (binding.stickerView.currentSticker as TextSticker).endTime,
                        (binding.stickerView.currentSticker as TextSticker).getstrtag(),
                        (binding.stickerView.currentSticker as TextSticker).fontStyle,
                        (binding.stickerView.currentSticker as TextSticker).typeface,
                        (binding.stickerView.currentSticker as TextSticker).alignment().toString(),
                        (binding.stickerView.currentSticker as TextSticker).size,
                        (binding.stickerView.currentSticker as TextSticker).text.toString(),
                        (binding.stickerView.currentSticker as TextSticker).borderColor,
                        (binding.stickerView.currentSticker as TextSticker).borderType,
                        false,
                        false,
                        (binding.stickerView.currentSticker as TextSticker).color,
                        (binding.stickerView.currentSticker as TextSticker).color,
                        (binding.stickerView.currentSticker as TextSticker).pattern,
                        (binding.stickerView.currentSticker as TextSticker).gradient,
                        (binding.stickerView.currentSticker as TextSticker).matrix
                    )
                    var data = UndoRedoData2(
                        binding.stickerView.currentSticker as TextSticker,
                        copySticker(binding.stickerView.currentSticker as TextSticker, ""),
                        textData
                    )
                    addToChangeList(ADD, TEXT_ADD,data)}

            }
        }
        notifyStickerView()


//        binding.ccTop.layoutParams.height =
//            resources.displayMetrics.heightPixels / 3 + 400



    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onDoneClicked(
        inputText: String,
        colorPosition: Int,
        fontPosition: Int,
        textSize: Int,
        lineSpacing: Float,
        letterSpacing: Float,
        gravity: Int,
        yPos: Float,
        startTime: Long,
        endTime: Long,
        strTag: String
    ) {

        binding.includeToolbar.btnTick.visible()
        binding.includeToolbar.btnDiscard.visible()
        binding.llItemView.hide()
        binding.trimCuttr.hide()
        binding.ccText.visible()
//        binding.ccTop.layoutParams.height =
//            binding.frVideoController.height - itemWidth
        binding.ccTop.layoutParams.height =
            resources.displayMetrics.heightPixels / 2

        selectedFunction = ADD_TEXT_VIEW
        isTextEdit = false

        if (strTextSticker.text != null) {
            if (!TextUtils.isEmpty(inputText)) {
                val sticker = TextSticker(this)
                sticker.text = inputText
                sticker.setTextColor(strTextSticker.color)
                sticker.setTextAlign(strTextSticker.alignment())
                sticker.setMaxTextSize(dpToPx(strTextSticker.size))
                if (borderType != "0" && borderType != "") {
                    sticker.setPaintToOutline(strTextSticker.borderColor, borderType)
                }
                sticker.setTypeface(strTextSticker.typeface)
                sticker.fontStyle = strTextSticker.fontStyle
                sticker.resizeText()
                sticker.setStrTag(strTag)
                binding.stickerView.addSticker(sticker, 2f)
                sticker.startTime = startTime
                sticker.endTime = endTime
                binding.llAddTextView.llAlignment1.skLineSpacing.progress = 0
                binding.llAddTextView.llAlignment1.skLetterSpacing.progress = 0
                replaceStickerFromList(strTextSticker, sticker)
                addStickerToList()
                notifyStickerView()

            }
        } else {
            if (!TextUtils.isEmpty(inputText)) {
                val sticker = TextSticker(this)
                sticker.id=getRandomId()
                sticker.text = inputText
                sticker.setTextColor(colorPosition)
                sticker.setTextAlign(Layout.Alignment.ALIGN_CENTER)
                sticker.setMaxTextSize(dpToPx(70f))
                sticker.setTypeface(fontlist[0].font)
                sticker.fontStyle = fontlist[0].name
                sticker.resizeText()
                sticker.setStrTag(strTag)
                binding.stickerView.addSticker(sticker, 2f)
                sticker.startTime = startTime
                sticker.endTime = endTime
                binding.llAddTextView.llAlignment1.skLineSpacing.progress = 0
                binding.llAddTextView.llAlignment1.skLetterSpacing.progress = 0
                addStickerToList()
                notifyStickerView()

            }

        }


    }

    private fun getRandomId(): Int {
        val number = Random().nextInt(100 - 1 + 1) + 1
        return  number
    }

    override fun onCloseClicked() {
        binding.includeToolbar.btnTick.hide()
        binding.includeToolbar.btnSave.visible()
        binding.includeToolbar.btnDiscard.visible()
    }

    private fun visibilityFullScreen(isShow: Boolean) {
        if (isShow) {
            binding.ccBottom.visible()
            binding.imgUndo.visible()
            binding.imgRedo.visible()
            binding.llItem.visible()
            binding.imgFullExitscreen.hide()
            binding.imgFullscreen.visible()

        } else {
            binding.ccBottom.hide()
            binding.llItem.hide()
            binding.imgUndo.hide()
            binding.imgRedo.hide()
            binding.imgFullExitscreen.visible()
            binding.imgFullscreen.hide()

        }

    }

    private fun letterSizeSeekBar() {
        binding.llAddTextView.llAlignment1.skStickerSize.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.llAddTextView.llAlignment1.textSize.text = progress.toString()
//                binding.stickerView.currentSticker?.let {
//                    val newMatrix = Matrix()
//                    val v = FloatArray(9)
//                    it.matrix.getValues(v)
//// translation is simple
//// translation is simple
//// calculate real scale
//
//// calculate real scale
//                    val tx = v[Matrix.MTRANS_X]
//                    val ty = v[Matrix.MTRANS_Y]
//
//                    val oldRange = (100f - 0f )
//                    val newRange = (1.5f -0.5f )
//
//
//                    val newValue = (((progress.toFloat() - 0) * (newRange)) / oldRange) + 0.5f
//                    newMatrix.setScale( newValue, newValue, it.width/2f , it.height/2f)
//                    newMatrix.postTranslate(tx ,ty)
//                    Log.e(TAG, "onProgressChanged: "+newValue )
//                    Log.e(TAG, "onProgressChanged: "+tx + " ,,  "+ty )
//
//                    binding.stickerView.changeMatrix(it , newMatrix)
//                    notifyStickerView()
//
//                }
                if (binding.stickerView.currentSticker != null) {
                    (binding.stickerView.currentSticker as TextSticker).setMaxTextSize(
                        convertSpToPx(progress.toFloat())
                    )

                    (binding.stickerView.currentSticker as TextSticker).resizeTexttt()

                    if (binding.stickerView.currentSticker != null) {
                        var textData = TextStickerData((binding.stickerView.currentSticker as TextSticker).currentAngle,
                            (binding.stickerView.currentSticker as TextSticker).currentScale,
                            (binding.stickerView.currentSticker as TextSticker).x,
                            (binding.stickerView.currentSticker as TextSticker).y,
                            (binding.stickerView.currentSticker as TextSticker).startTime,
                            (binding.stickerView.currentSticker as TextSticker).endTime,
                            (binding.stickerView.currentSticker as TextSticker).getstrtag(),
                            (binding.stickerView.currentSticker as TextSticker).fontStyle,
                            (binding.stickerView.currentSticker as TextSticker).typeface,
                            (binding.stickerView.currentSticker as TextSticker).alignment().toString(),
                             progress.toFloat(),
                            (binding.stickerView.currentSticker as TextSticker).text.toString(),
                            (binding.stickerView.currentSticker as TextSticker).borderColor.toInt(),
                            (binding.stickerView.currentSticker as TextSticker).borderType.toString(),
                            false,
                            false,
                            (binding.stickerView.currentSticker as TextSticker).color,
                            (binding.stickerView.currentSticker as TextSticker).color,
                            (binding.stickerView.currentSticker as TextSticker).pattern,
                            (binding.stickerView.currentSticker as TextSticker).gradient,
                            (binding.stickerView.currentSticker as TextSticker).matrix
                        )

                        var data = UndoRedoData2(
                        binding.stickerView.currentSticker as TextSticker,
                        copySticker(binding.stickerView.currentSticker as TextSticker, ""),
                            textData
                    )
                    addToChangeList(TEXT_ADD,ACTION_RESIZE,data)}


                    notifyStickerView()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
//                (binding.stickerView.currentSticker as TextSticker).resizeText()
                notifyStickerView()
            }
        })
    }

    private fun letterSpacingSeekBar() {
        binding.llAddTextView.llAlignment1.skLetterSpacing.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.llAddTextView.llAlignment1.textSpacing.text = progress.toString()
                if (binding.stickerView.currentSticker != null) {
                    when (progress) {
                        1 -> (binding.stickerView.currentSticker as TextSticker).letterSpacing =
                            0.05f
                        2 -> (binding.stickerView.currentSticker as TextSticker).letterSpacing =
                            0.06f
                        3 -> (binding.stickerView.currentSticker as TextSticker).letterSpacing =
                            0.07f
                        4 -> (binding.stickerView.currentSticker as TextSticker).letterSpacing =
                            0.08f
                        5 -> (binding.stickerView.currentSticker as TextSticker).letterSpacing =
                            0.09f
                        6 -> (binding.stickerView.currentSticker as TextSticker).letterSpacing =
                            0.1f
                        7 -> (binding.stickerView.currentSticker as TextSticker).letterSpacing =
                            0.2f
                        8 -> (binding.stickerView.currentSticker as TextSticker).letterSpacing =
                            0.3f
                        9 -> (binding.stickerView.currentSticker as TextSticker).letterSpacing =
                            0.4f
                        10 -> (binding.stickerView.currentSticker as TextSticker).letterSpacing =
                            0.5f

                    }
                    (binding.stickerView.currentSticker as TextSticker).resizeText()
                    notifyStickerView()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                (binding.stickerView.currentSticker as TextSticker).resizeText()
                notifyStickerView()
            }
        })
    }

    private fun lineSpacingSeekBar() {
        binding.llAddTextView.llAlignment1.skLineSpacing.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.llAddTextView.llAlignment1.textLineSpacing.text = progress.toString()
                if (binding.stickerView.currentSticker != null) {
                    when (progress) {
                        1 -> (binding.stickerView.currentSticker as TextSticker).setLineSpacing(
                            1f,
                            0f
                        )
                        2 -> (binding.stickerView.currentSticker as TextSticker).setLineSpacing(
                            1.25f,
                            0f
                        )
                        3 -> (binding.stickerView.currentSticker as TextSticker).setLineSpacing(
                            1.5f,
                            0f
                        )
                        4 -> (binding.stickerView.currentSticker as TextSticker).setLineSpacing(
                            1.75f,
                            0f
                        )
                        5 -> (binding.stickerView.currentSticker as TextSticker).setLineSpacing(
                            2.25f,
                            0f
                        )
                        6 -> (binding.stickerView.currentSticker as TextSticker).setLineSpacing(
                            2.5f,
                            0f
                        )
                        7 -> (binding.stickerView.currentSticker as TextSticker).setLineSpacing(
                            2.75f,
                            0f
                        )
                        8 -> (binding.stickerView.currentSticker as TextSticker).setLineSpacing(
                            3f,
                            0f
                        )
                        9 -> (binding.stickerView.currentSticker as TextSticker).setLineSpacing(
                            3.25f,
                            0f
                        )
                        10 -> (binding.stickerView.currentSticker as TextSticker).setLineSpacing(
                            3.5f,
                            0f
                        )
                    }
                    (binding.stickerView.currentSticker as TextSticker).resizeText()
                    notifyStickerView()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                (binding.stickerView.currentSticker as TextSticker).resizeText()
                notifyStickerView()
            }
        })
    }

    @JvmInline
    value class Duration(val millis: Long) {
        val seconds: Float
            get() = millis / 1000F

    }

    /**
     * getting effects from firebase
     */

    private fun getFirebaseEffectsData() {

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                val gson = Gson()
                val data = gson.toJson(dataSnapshot.value)
                val category = gson.fromJson(data, ListOfMusic::class.java)
                effectsList.clear()
                effectsList.add(category)

                Log.w("FB", "data:" + category)
                Log.w("FB", "loadPost:" + dataSnapshot.children)
            }

            override fun onCancelled(databaseError: DatabaseError) {

                // Getting Post failed, log a message
                Log.w("FB", "loadPost:onCancelled" + databaseError.toException())
            }
        }
        database.addValueEventListener(postListener)
    }

    private fun applyChangesOnVideo() {
        selectedFunction = ON_PROGRESS
        playPauseVideo(true)
        videoEditViewModel.isShowCancel.set(false)
//        showExportDialog(videoEditViewModel, getString(R.string.exporting_video))

        tempVideoWidth = 1920
        tempVideoHeight = 1080
        var arrTextlist :ArrayList<SpanData> = ArrayList()
        for (i in 0 until textStickerList.size){
            for (m in 0 until textStickerList[i].duplicatetext!!.size){
                arrTextlist.add(textStickerList[i].duplicatetext!![m])
            }
        }

        var isLandScape = false
        for (item in arrVideo) {
            isLandScape = item.width > item.height
            if (isLandScape)
                break
        }

        job = GlobalScope.launch(Dispatchers.IO) {
            VideoEditor(context = this@VideoEditorActivity,
                arrVideos = arrVideo,
                arrTextData = arrTextlist,
                silentAudioPath = getSilentAudioFilePath(R.raw.silent_audio, "silent_audio.mp3"),
                totalVideoDuration = totalVideoDuration,
                videoWidthRatio = binding.stickerView.width.toFloat(),
                videoHeightRatio = binding.stickerView.height.toFloat(),
                isLandScape = isLandScape,
                arrMusicData = videoEditViewModel.getSelectedMusic(),
                arrVideoTimeSlot = binding.videoSeekBar.getVideoDuration(),
                isFilterApplyAll = videoEditViewModel.isFilterApplyForAll.get(),
                videoWidth = tempVideoWidth,
                videoHeight = tempVideoHeight,
                aspectRatioInString = "",
                onSucceeded = { outPutPath ->
                    Log.e(TAG, "applyChangesOnVideoSUCCESS>>: "+outPutPath )
                    //OnSuccess
                        selectedFunction = -1
                        dismissExportDialog()
                    if (::discardDialog.isInitialized && discardDialog.isShowing)
                        discardDialog.dismiss()

                    if (::saveDialog.isInitialized && saveDialog.isShowing)
                        saveDialog.dismiss()

                    val intent = Intent(this@VideoEditorActivity, VideoListActivity::class.java)
                    startActivity(intent)
                    finish()
                },
                onFailed = {
                    //OnFailed
                    if (!videoEditViewModel.isExportingCancel.get()) {
                        runOnUiThread {
                            dismissExportDialog()
                            if (::discardDialog.isInitialized && discardDialog.isShowing)
                            discardDialog.dismiss()

                        }
                        selectedFunction = -1
                    }
                },
                onProgressUpdate = { progress ->
                    //OnProgress
                    if (!videoEditViewModel.isExportingCancel.get()) {
                        videoEditViewModel.exportProgress.set(progress.toInt())
                        videoEditViewModel.progressText.set(getString(R.string.exporting_video) +" "+ progress.toInt() +" %")
                    }

                    Log.e("video creating progress", "progress $progress")
                },
                onGetFilterObject = {
                    mp4Composer = it
                }, onGetOutputPath = {
                    outputPath = it
                })
                .applyVideoFilter()
        }
    }

    private fun cancelExportingVideo() {
        videoEditViewModel.isExportingCancel.set(true)
        selectedFunction = -1
        dismissExportDialog()
        if (::mp4Composer.isInitialized)
            mp4Composer.cancel()
        if (::job.isInitialized)
            job.cancel()
        deleteUnWantedFile(outputPath)
    }

    override fun onBackPressed() {
        if(!arrVideo.isNullOrEmpty()){
            Log.e(TAG, "slectedItem: "+arrVideo )
            setResult(201, Intent().putParcelableArrayListExtra(
                ARR_VIDEO,
                arrVideo
            ))
            finish()
        }else{
            setResult(Activity.RESULT_CANCELED,null)
            finish()
        }


    }


    private fun textAsBitmap(textSticker: TextSticker): Bitmap? {

        var text = getAllLineTextOfStaticLayout(textSticker)
        Log.e(TAG, "textAsBitmap: " + text)
        textSticker.textHeight = heightText * textSticker.currentScale
        textSticker.textWidth = widthText.toFloat() * textSticker.currentScale

        val bitmap = Bitmap.createBitmap(
            textSticker.width.toInt(),
            textSticker.height.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val matrix = Matrix()
//        matrix.postTranslate(50f,50f)
//        matrix.postScale(textSticker.currentScale,textSticker.currentScale)
        binding.stickerView.changeMatrix(textSticker ,textSticker.matrix.clone())
        notifyStickerView()
        textSticker.draw(canvas)
        return bitmap
    }

    private fun resize(image: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        var resizeBitmap: Bitmap = image

        val width = image.width
        val height = image.height
        val ratioBitmap = width.toFloat() / height.toFloat()
        val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()
        var finalWidth = maxWidth
        var finalHeight = maxHeight
        if (ratioMax > ratioBitmap) {
            finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
        } else {
            finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()
        }
        Log.e("BJC", "resizeBitmap finalWidth $finalWidth finalHeight $finalHeight")

        resizeBitmap = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, false)
        return resizeBitmap
    }
    private fun getAllLineTextOfStaticLayout(textSticker: TextSticker): String {
        textSticker.staticLayout.lineCount

        var text: String? = ""
        for (i in 0 until textSticker.staticLayout.lineCount) {

            val lineText = textSticker.text!!.subSequence(
                textSticker.staticLayout.getLineStart(i),
                textSticker.staticLayout.getLineEnd(i)
            ).toString()
            text = when {
                text.isNullOrEmpty() -> {
                    text + lineText + "\n"
                }
                i == textSticker.staticLayout.lineCount - 1 -> {
                    text + lineText
                }
                else -> {
                    text + lineText + "\n"
                }
            }
            text.replace("\n\n", "\n")
            val rect = Rect()

            textSticker.textPaint.getTextBounds(lineText, 0, lineText.length, rect)
            heightText += rect.height()
            if (widthText < textSticker.textPaint.measureText(lineText).toInt())
                widthText = textSticker.textPaint.measureText(lineText).toInt()
            Log.d("TAG", "h${textSticker.textPaint.measureText(lineText)}")

        }
        return text!!
    }

    private fun convertViewToBitmap(
        view: View
    ): Bitmap {
        view.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)

        val bitmap =
            Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap

    }


}
