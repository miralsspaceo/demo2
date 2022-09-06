package com.app.personas_social.viewmodel

import android.app.Activity
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.RectF
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableInt
import androidx.databinding.ObservableLong
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.personas_social.R
import com.app.personas_social.adapter.MusicAdapter
import com.app.personas_social.adapter.MusicEffectAdapter
import com.app.personas_social.app.ResponseObserver
import com.app.personas_social.model.FolderData
import com.app.personas_social.model.MusicData
import com.app.personas_social.model.VideoItem
import com.app.personas_social.utlis.*
import com.audiowaveforms.WaveformOptions
import com.videofilter.FilterData
import com.videofilter.FilterHelper
import com.videofilter.FilterType
import com.videofilter.filters.GlFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.internal.notify
import java.util.HashMap
import kotlin.math.max
import kotlin.math.min

class VideoEditViewModel : BaseViewModel() {
    val audioRecordingStatus = ObservableInt()
    val audioRecordingTime = ObservableLong()
    private val arrSelectedMusicData = ArrayList<MusicData>()
    val arrMyMusicData = ArrayList<MusicData>()
    val arrTempMyMusicData = ArrayList<MusicData>()
    val arrMyMusicDataAll = ArrayList<MusicData>()
    val arrFilterData = ArrayList<FilterData>()
    var filterChangeCallback: (() -> Unit)? = null

    private val mutableArrFilter = MutableLiveData<ArrayList<FilterData>>()
    private val liveArrFilter: LiveData<ArrayList<FilterData>> = mutableArrFilter
    private var selectedFilter: FilterType = FilterType.DEFAULT
    val mutableArrMyMusic = MutableLiveData<ArrayList<MusicData>>()
    val liveArrMyMusic: LiveData<ArrayList<MusicData>> = mutableArrMyMusic
    val isShowThumbnail = ObservableBoolean(false)
    private var currentMusicData = MusicData()
    lateinit var musicAdapter: MusicAdapter
    lateinit var effectAdapter: MusicEffectAdapter

    private var previousPosition = -1
    val isShowMusicFolder = ObservableBoolean(false)
    private lateinit var actualVideoDurationArray: LongArray
    private lateinit var audioDurationArray: LongArray
    private var mutableArrFolder = MutableLiveData<ArrayList<FolderData>>()
    val isLoadingWaveForms = ObservableBoolean(false)
    val isSelectDifferent = ObservableBoolean(false)
    val isFilterApplyForAll = ObservableBoolean(false)
    val isShowMusicList = ObservableBoolean(true)
    val audioVolumeProgress = ObservableInt(100)
    val isShowMultipleMusic = ObservableBoolean(false)
    private var totalMusicDuration = 0L
    private var musicEditItemPosition = -1
    private lateinit var job: Job
    private var arrFolder = ArrayList<FolderData>()
    var isExportingCancel = ObservableBoolean(false)

    private val mutableArrSelectedMusic = MutableLiveData<ArrayList<MusicData>>()
     val liveArrSelectedMusic: LiveData<ArrayList<MusicData>> = mutableArrSelectedMusic

    init {
        liveArrMyMusic.observeForever {
            musicAdapter.notifyDataSetChanged()
        }
        currentMusicData.position = -1
        musicAdapter = MusicAdapter(arrMyMusicData, this) {
            arrMyMusicData.forEachIndexed { position, data ->
                run {
                    if (position == it) {
                        data.itemisSelected = !data.itemisSelected
                        arrMyMusicData[position] = data
                    } else {
                        data.itemisSelected = false
                        arrMyMusicData[position] = data
                    }
                }
            }
            musicAdapter.notifyDataSetChanged()
        }
    }

    fun setAudioVolumeProgress(mVolumeProgress: Int) {
        audioVolumeProgress.set(mVolumeProgress)
    }
    fun getAudioVolume() = audioVolumeProgress.get()
    fun getSelectedMusic() = arrSelectedMusicData

    fun getVideoFilterList(selectedFilterType: FilterType, activity: Activity, image: Bitmap) {
        if (arrFilterData.isEmpty()) {
            currentStatus.postValue(ResponseObserver.Loading(true))
            arrFilterData.addAll(FilterHelper.getFilterList(activity, selectedFilterType, image))
            currentStatus.postValue(ResponseObserver.Loading(false))
            mutableArrFilter.postValue(arrFilterData)
        }
    }

    fun applyFilterOnVideo(position: Int) {
        for ((i, filterData) in arrFilterData.withIndex()) {
            filterData.isSelected = position == i
        }
        mutableArrFilter.postValue(arrFilterData)
        currentStatus.postValue(ResponseObserver.PerformAction(arrFilterData[position].filterType))
        selectedFilter = arrFilterData[position].filterType

    }

    fun updateMusicPlayValue() {
        currentMusicData.play = 2
        currentStatus.postValue(ResponseObserver.PerformAction(currentMusicData))
    }

    fun addSelectedMusic(musicData: MusicData) {
        job = GlobalScope.launch(Dispatchers.IO) {
            if (musicData.waves != null && musicData.waves!!.isNotEmpty()) {
                arrSelectedMusicData.add(musicData)
                mutableArrSelectedMusic.postValue(arrSelectedMusicData)
                isShowMultipleMusic.set(true)
            } else {
                isLoadingWaveForms.set(true)
                isShowMultipleMusic.set(true)
                arrSelectedMusicData.add(musicData)
                mutableArrSelectedMusic.postValue(arrSelectedMusicData)
                val waves = WaveformOptions.getSampleFrom(musicData.audioUrl)
                if (waves!!.isEmpty()) {
                    currentStatus.postValue(
                        ResponseObserver.PerformAction(
                            MSG_MUSIC_ERROR,
                            R.string.msg_wave_form
                        )
                    )
                    isLoadingWaveForms.set(false)
                    //FirebaseCrashlytics.getInstance().log("Could not load waveform of extention ${musicData.audioUrl}")
                } else {
                    if (arrSelectedMusicData.size < 1){
                        arrSelectedMusicData[arrSelectedMusicData.size ].waves = waves
                    }else{
                        arrSelectedMusicData[arrSelectedMusicData.size - 1].waves = waves
                    }
                    currentStatus.postValue(ResponseObserver.PerformAction(DIALOG_DISMISS))
                }
                    mutableArrSelectedMusic.postValue(arrSelectedMusicData)

                    if (arrSelectedMusicData[arrSelectedMusicData.size - 1].waves != null && arrSelectedMusicData[arrSelectedMusicData.size - 1].waves!!.isNotEmpty())
                        isLoadingWaveForms.set(false)
                }
                setAudioDurationArray()
            }

    }

        fun onMoreClickMusicItem(musicData: MusicData, type: Int) {
        /**
         * type = 1 -> edit item, type = 2 -> delete item
         */
//        currentStatus.postValue(
//            ResponseObserver.PerformAction(
//                musicData,
//                MUSIC_DELETE
//            )
//        )
    }

    fun onDeleteSelectedItem(musicData: MusicData) {
        arrSelectedMusicData.remove(musicData)
        if (previousPosition == musicData.position) {
            previousPosition = if (arrSelectedMusicData.isEmpty())
                -1
            else arrSelectedMusicData.size - 1
        }
        mutableArrSelectedMusic.postValue(arrSelectedMusicData)
    }


    fun setCurrentMusic(position: Int) {
        currentMusicData = arrSelectedMusicData[position]
        Log.e("setCurrentMusic", "setCurrentMusic: " + currentMusicData)
    }


    fun applySelectionFromFilter(filterType: FilterType) {
        for (filterData in arrFilterData) {
            filterData.isSelected = filterData.filterType == filterType
        }
        mutableArrFilter.postValue(arrFilterData)
        currentStatus.postValue(ResponseObserver.PerformAction(filterType))
        selectedFilter = filterType

    }

    fun applyFilter(filterType: FilterType) {
        currentStatus.postValue(ResponseObserver.PerformAction(filterType))
    }

    fun getSelectedFilter() = selectedFilter


    fun playComplete(selectedSection: Int) {
        if (selectedSection == ADD_MUSIC) {
            if (arrMyMusicData.size > 0) {
                arrMyMusicData[previousPosition].play = 0
                mutableArrMyMusic.postValue(arrMyMusicData)
            }
        } else {
            if (arrSelectedMusicData.size > 0) {
                Log.e("playComplete", "playComplete: "+arrSelectedMusicData.size)
                Log.e("playComplete", "playComplete: "+currentMusicData.position)
                if (currentMusicData.position == -1){
                    currentMusicData.position = 0
                }
                arrSelectedMusicData[currentMusicData.position].play = 0
                mutableArrSelectedMusic.postValue(arrSelectedMusicData)
            }
        }
    }

    fun setActualVideoDurationArray(array: LongArray) {
        actualVideoDurationArray = array
    }

    fun onClickMyMusicBtn() {
        if (!isShowMusicList.get()) {
            isShowMusicList.set(true)
            isShowMusicFolder.set(false)
        }
    }

    fun onClickMusicFolderBtn() {
        if (!isShowMusicFolder.get()) {
            isShowMusicFolder.set(true)
            isShowMusicList.set(false)

        }
    }

    fun setAudioDurationArray() {
        audioDurationArray = LongArray(arrSelectedMusicData.size + 1)
        GlobalScope.launch(Dispatchers.IO) {
            for ((i, musicData) in arrSelectedMusicData.withIndex()) {
                if (i == 0)
                    audioDurationArray[i] = musicData.endTime - musicData.startTime
                else
                    audioDurationArray[i] =
                        audioDurationArray[i - 1] + musicData.endTime - musicData.startTime
            }
        }
    }

    fun getAudioDurationArray() = audioDurationArray

    fun getTotalVideoDurationTillCurrentPos(currentPosition: Int): Long {
        var totalDuration = 0L
        if (currentPosition < actualVideoDurationArray.size)
            for (i in 0..currentPosition) {
                totalDuration += actualVideoDurationArray[i]
            }

        return totalDuration
    }

    fun videoTrim() {
        currentStatus.postValue(ResponseObserver.PerformAction(TRIM))
    }

    fun getMusicSeekPosition(updatedDuration: Long): Triple<Long, Long, Int> {
        var seekPosition = 0L
        var remainTime = 0L
        var position = 0
        var duration = 0L
        for ((i, data) in arrSelectedMusicData.withIndex()) {
            if (updatedDuration <= getTotalMusicDuration()) {
                duration += (arrSelectedMusicData[i].endTime - arrSelectedMusicData[i].startTime)
                if (updatedDuration <= duration) {
                    val seekPos = if (i == 0) updatedDuration else duration - updatedDuration
                    seekPosition =
                        if (i == 0) updatedDuration else ((data.endTime - data.startTime) - seekPos)
                    remainTime =
                        if (i == 0) ((data.endTime - data.startTime) - seekPosition) else seekPos
                    position = i
                    break
                }
            }
        }
        return Triple(seekPosition, remainTime, position)
    }

    fun getTotalMusicDuration(): Long {
        totalMusicDuration = 0L
        for (data in arrSelectedMusicData) {
            totalMusicDuration += (data.endTime - data.startTime)
        }
        return totalMusicDuration
    }

    fun getAudioFileFromStorage(activity: Activity) {
        musicEditItemPosition = -1
        arrMyMusicData.clear()
//        if (arrMyMusicData.size == 0 || isNeedToUpdateMusicList) {
        job = GlobalScope.launch(Dispatchers.IO) {
            val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val projection = arrayOf(
                MediaStore.Audio.Albums.ALBUM_ID,
                MediaStore.Audio.AudioColumns.DATA,
                MediaStore.Audio.AudioColumns.TITLE,
                MediaStore.Audio.AudioColumns.ALBUM,
                MediaStore.Audio.Media.DURATION
            )
            val cursor: Cursor = activity.contentResolver.query(
                uri,
                projection,
                null,
                null,
                null
            )!!
            while (cursor.moveToNext()) {
                val audioPath =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DATA))
                val duration =
                    cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                if (duration != 0L){
                arrMyMusicData.add(
                    MusicData(
                        audioUrl = audioPath,
                        title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.TITLE)),
//                            album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ALBUM)),
//                        artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ARTIST)),
                        duration = duration,
                        startTime = 0,
                        endTime = duration,
                        strstartTime = 0,
                        strendTime = duration,
                        isfrom = IS_MUSIC
                    )
                )
            }}
            cursor.close()
            arrTempMyMusicData.clear()
            arrTempMyMusicData.addAll(arrMyMusicData)
            arrMyMusicDataAll.clear()
            arrMyMusicDataAll.addAll(arrMyMusicData)
            mutableArrMyMusic.postValue(arrMyMusicData)
        }
//        } else if (isFilteredMusic.get()) {
//            isFilteredMusic.set(false)
//            arrMyMusicData.clear()
//            arrMyMusicData.addAll(arrTempMyMusicData)
//            mutableArrMyMusic.postValue(arrMyMusicData)
//        }
        getVideoFolders(activity)
    }

    private fun getVideoFolders(activity: Activity) {
        arrFolder.clear()
        job = GlobalScope.launch(Dispatchers.IO) {
            try {
                currentStatus.postValue(ResponseObserver.Loading(true))
                val folderFileCountMap = HashMap<String, Int>()
                val projection = arrayOf(
                    MediaStore.Files.FileColumns.BUCKET_ID,
                    MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME,
                    MediaStore.Files.FileColumns.DATA,
                    MediaStore.Files.FileColumns.BUCKET_ID,
                    MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME,
                    MediaStore.Files.FileColumns.DATA
                )
                val selection = (MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                        + MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO)

                val cursor = activity.contentResolver.query(
                    MediaStore.Files.getContentUri("external"), projection,
                    selection, null, MediaStore.MediaColumns.DATE_ADDED + " DESC"
                )
                cursor.let {
                    val columnIndexFolderId = it!!.getColumnIndexOrThrow(projection[0])
                    val columnIndexFolderName = it.getColumnIndexOrThrow(projection[1])
                    val columnIndexFilePath = it.getColumnIndexOrThrow(projection[2])
                    while (it.moveToNext()) {
                        val folderId = it.getString(columnIndexFolderId)
                        if (folderFileCountMap.containsKey(folderId)) {
                            folderFileCountMap[folderId] = folderFileCountMap[folderId]!! + 1
                        } else {
                            val folder = FolderData(
                                id = folderId,
                                name = if (TextUtils.isEmpty(it.getString(columnIndexFolderName))) "Common" else it.getString(
                                    columnIndexFolderName
                                ),
                                thumbImagePath = it.getString(columnIndexFilePath),
                                count = 0
                            )
                            arrFolder.add(folder)
                            folderFileCountMap[folderId] = 1
                        }
                    }
                    cursor!!.close()
                }
                for (folderData in arrFolder) {
                    folderData.count = folderFileCountMap[folderData.id]!!
                    getVideoFromFolder(activity, folderData)
                }
                currentStatus.postValue(ResponseObserver.Loading(false))
                mutableArrFolder.postValue(arrFolder)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun onClickMusicItem(musicData: MusicData, type: Int) {
        Log.e("onBindViewHolder", "audioUrl: "+musicData.audioUrl )
        Log.e("onBindViewHolder", "isSelected: "+musicData.isSelected )
        Log.e("onBindViewHolder", "type: "+musicData.type )
        Log.e("onBindViewHolder", "isfrom: "+musicData.isfrom )
        Log.e("onBindViewHolder", "play: "+musicData.play )
        Log.e("onBindViewHolder", "duration: "+musicData.duration )
        /**
         * type = 1 -> play-pause music, type = 2 -> select item
         */
        if (musicData.audioUrl.contains(".mp4")) {
//            currentStatus.postValue(ResponseObserver.DisplayAlert(msgId = R.string.msg_invalid_audio))
        } else {
            if (type == 1) {
                if (currentMusicData.position == -1) currentMusicData = musicData
                previousPosition = currentMusicData.position
                musicData.type = type
                if (currentMusicData.position == musicData.position) {
                    isSelectDifferent.set(false)
                    Log.e("setImage", "before1 musicData.play ${musicData.play}")
                    when (musicData.play) {
                        0 -> musicData.play = 1
                        1 -> musicData.play = 2
                        2 -> musicData.play = 3
                        3 -> musicData.play = 2
                    }
                    Log.e("setImage", "after1 musicData.play ${musicData.play}")
                    musicAdapter.notifyItemChanged(musicData.position)
                    musicAdapter.notifyDataSetChanged()
                } else {
                    currentMusicData.play = 2
                    isSelectDifferent.set(true)
                    Log.e("setImage", "before musicData.play ${musicData.play}")
                    when (musicData.play) {
                        0 -> musicData.play = 1
                        1 -> musicData.play = 2
                        2 -> musicData.play = 3
                        3 -> musicData.play = 0
                    }
                    musicAdapter.notifyItemChanged(musicData.position)
                    musicAdapter.notifyItemChanged(currentMusicData.position)
                    Log.e("setImage", "after musicData.play ${musicData.play}")
                }
                currentMusicData = musicData
                currentStatus.postValue(ResponseObserver.PerformAction(musicData))
            } else {
                if (musicData.play == 0 || musicData.play == 1)
                    musicData.play = 2
                musicAdapter.notifyItemChanged(musicData.position)
                currentStatus.postValue(ResponseObserver.PerformAction(musicData))
                val selectedMusicData = MusicData(
                    musicData.position,
                    musicData.audioUrl,
                    musicData.effectUrl,
                    musicData.thumb,
                    musicData.waves,
                    musicData.title,
                    musicData.artist,
                    musicData.duration,
                    musicData.startTime,
                    musicData.endTime,
                    musicData.startTime,
                    musicData.endTime,
                    musicData.type,
                    musicData.play,
                    musicData.isTrimmed,
                    musicData.audioVolume,
                    musicData.isDownload,
                    musicData.isSelected,
                    musicData.itemisSelected,
                    musicData.isfrom
                )
                if (currentMusicData.position == -1) currentMusicData = selectedMusicData
                previousPosition = currentMusicData.position
                selectedMusicData.type = type

                currentMusicData = selectedMusicData
                currentStatus.postValue(ResponseObserver.PerformAction(selectedMusicData))
            }
        }
    }


    private fun getVideoFromFolder(activity: Activity, folderData: FolderData) {
        job = GlobalScope.launch(Dispatchers.IO) {
            val orderBy = MediaStore.Audio.Media.DATE_ADDED
            val cursor = activity.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                arrayOf(
                    MediaStore.Audio.AudioColumns.DATA,
                    MediaStore.Audio.AudioColumns.TITLE,
                    MediaStore.Audio.AudioColumns.ALBUM,
                    MediaStore.Audio.AudioColumns.ARTIST,
                    MediaStore.Audio.Media.DURATION
                ),
                "${MediaStore.MediaColumns.BUCKET_ID}=${folderData.id}",
                null,
                "$orderBy DESC"
            )
            folderData.arrAudioItem.clear()
            while (cursor!!.moveToNext()) {
                val audioPath =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DATA))
                val duration =
                    cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                if (!audioPath.contains(".mp4"))
                    folderData.arrAudioItem.add(
                        MusicData(
                            audioUrl = audioPath,
                            title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.TITLE)),
//                            artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ARTIST)),
                            duration = duration,
                            startTime = 0,
                            endTime = duration,
                            strstartTime = 0,
                            strendTime = duration,
                            isfrom = IS_MUSIC
                        )
                    )
            }
            cursor.close()
            currentStatus.postValue(ResponseObserver.Loading(false))
        }
    }

    fun onDestroy() {
        if (::job.isInitialized)
            job.cancel()
    }

}