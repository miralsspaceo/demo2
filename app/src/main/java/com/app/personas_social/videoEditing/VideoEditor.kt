package com.app.personas_social.videoEditing

import VideoHandle.CmdList
import VideoHandle.OnEditorListener
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.text.TextUtils
import android.util.Log
import android.view.View
import com.app.personas_social.model.MusicData
import com.app.personas_social.model.SpanData
import com.app.personas_social.model.VideoItem
import com.app.personas_social.stickerview.TextSticker
import com.app.personas_social.utlis.*
import com.videofilter.FilterHelper
import com.videofilter.FilterType
import com.videofilter.composer.Mp4Composer
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Math.PI



class VideoEditor(
    private var context: Context,
    private val arrVideos: ArrayList<VideoItem>,
    private var arrTextData: ArrayList<SpanData>,
    private var silentAudioPath: String,
    private var totalVideoDuration: Long,
    private val videoWidthRatio: Float,
    private val videoHeightRatio: Float,
    private var isLandScape: Boolean,
    private var arrMusicData: ArrayList<MusicData>,
    private val arrVideoTimeSlot: LongArray,
    private val isFilterApplyAll: Boolean,
    private var videoWidth: Int,
    private var videoHeight: Int,
    private var aspectRatioInString: String,
    private var onSucceeded: (outputPath: String) -> Unit,
    private var onFailed: () -> Unit,
    private var onProgressUpdate: (progress: Float) -> Unit,
    private var onGetFilterObject: (mp4Composer: Mp4Composer) -> Unit,
    private var onGetOutputPath: (outputPath: String) -> Unit,

    ) {
    var index = 0
    private var isFilterUsed = false

    fun applyChanges() {
        videoWidth = if (isLandScape) 1920 else 1080
        videoHeight = if (isLandScape) 1080 else 1920
        arrTextData.forEach {
            it.bitmap = trimBitmap(it.bitmap!!)
        }

        val outputPath = context.getVideoFilePath()
        var isOneAudioAvail = false
        val cmdList = CmdList()
        /**
         * If same output file will be there then it will override by -y
         */
        cmdList.append("-y")
        /**
         * All video input path
         */
        setSpeedData()
        for (videoItem in arrVideos) {
            if (videoItem.isAudioAvail)
                isOneAudioAvail = true
            /**
             * -ss -> start duration of file
             * -t -> total duration
             * -ss 10 -t 30 -> When write this before input it means we just take 20 second of input which is start from 10th second on input.
             * So all function apply on just 20 second of input
             */

            cmdList.append("-i")
            cmdList.append(getFilePath(context.checkUrl(videoItem.videoUrl)))
            Log.e("applyChanges", "videoUrl: "+context.checkUrl(videoItem.videoUrl))
        }
        /**
         * silent audio file when some video hase no audio file
         */
        cmdList.append("-i")
        cmdList.append(getFilePath(context.checkUrl(silentAudioPath)))
        Log.e("applyChanges", "silentAudioPath: "+getFilePath(context.checkUrl(silentAudioPath)))
        /**
         * All text input converted into images
         */
        if (arrTextData.isNotEmpty()){
        for (i in 0 until arrTextData.size) {
            cmdList.append("-i")
            cmdList.append(storeImage(arrTextData[i].bitmap!!))
            Log.e("applyChanges", "arrTextData.bitmap!!.width: "+ arrTextData[i].bitmap!!.width  +"  arrTextData.bitmap!!.hieght >>  " + arrTextData[i].bitmap!!.height )
        }}
        /**
         * If external music files are selected
         */
        for (musicData in arrMusicData) {
            Log.e("applyChanges", "arrMusicData: "+arrMusicData.size)
            if (musicData.endTime - musicData.startTime > 0) {
                cmdList.append("-ss")
                cmdList.append("${musicData.startTime / 1000}")
                cmdList.append("-t")
                cmdList.append("${(musicData.endTime - musicData.startTime) / 1000}")
                cmdList.append("-i")
                cmdList.append(getFilePath(context.checkUrl(musicData.audioUrl, true)))
            }
        }
        /**
         * First trim video the apply scaling and then concat all videos
         *
         * ====Filters====
         * Box Blur -> ,boxblur=7
         * Gray Scale -> ,colorchannelmixer=0.3:0.4:0.3:0:0.3:0.4:0.3:0:0.3:0.4:0.3
         * Brightness -> colorlevels=rimax=0.902:gimax=0.902:bimax=0.902
         *
         * ====Rotate video====
         * ,rotate=$angle*$PI/180
         * ,rotate=${arrVideos[i].videoRotation}*sin(2*$PI/${arrVideos[i].endDuration - arrVideos[i].startDuration}*t) -> rotate 0 to angle while playing
         */
        val videoScaleConcatStr = StringBuilder()
        for (i in 0 until arrVideos.size) {
            Log.e("applyChanges", "arrVideos: "+arrVideos.size)
            val rotationStr =
                if (arrVideos[i].videoRotation == 90f || arrVideos[i].videoRotation == 270f)
                    ",transpose=${if (arrVideos[i].videoRotation == 90f) 1 else 2}"
                else if (arrVideos[i].videoRotation == 180f)
                    ",hflip,vflip"
                else ""
            val pairSpeed = setSpeedOfVideoAudio(arrVideos[i].speed)
            val aspectRatioStr = aspectRatioInString
                videoScaleConcatStr.append(
                    if (!TextUtils.isEmpty(aspectRatioInString))
                        if (arrVideos[i].videoRotation == 90f || arrVideos[i].videoRotation == 270f) {
                            Log.e("videoScaleConcatStr", "videoRotation if"+aspectRatioInString)
                            if (arrVideos[i].speed != 50f)
                                "[$i:v]trim=${arrVideos[i].startDuration.toDouble() / 1000}:${arrVideos[i].endDuration.toDouble() / 1000},setpts=PTS-STARTPTS[vtrim$i];[vtrim$i]setpts=${pairSpeed.first}[vspeed$i];[vspeed$i]scale='gte(iw/ih,$videoHeight/$videoWidth)*$videoHeight+lt(iw/ih,$videoHeight/$videoWidth)*(($videoWidth*iw)/ih):lte(iw/ih,$videoHeight/$videoWidth)*$videoWidth+gt(iw/ih,$videoHeight/$videoWidth)*(($videoHeight*ih)/iw)',pad='$videoHeight:$videoWidth:($videoHeight-gte(iw/ih,$videoHeight/$videoWidth)*$videoHeight-lt(iw/ih,$videoHeight/$videoWidth)*(($videoWidth*iw)/ih))/2:($videoWidth-lte(iw/ih,$videoHeight/$videoWidth)*$videoWidth-gt(iw/ih,$videoHeight/$videoWidth)*(($videoHeight*ih)/iw))/2:black',setdar=${aspectRatioStr}${rotationStr}[v$i];"
                            else
                                "[$i:v]trim=${arrVideos[i].startDuration.toDouble() / 1000}:${arrVideos[i].endDuration.toDouble() / 1000},setpts=${pairSpeed.first}[vtrim$i];[vtrim$i]scale='gte(iw/ih,$videoHeight/$videoWidth)*$videoHeight+lt(iw/ih,$videoHeight/$videoWidth)*(($videoWidth*iw)/ih):lte(iw/ih,$videoHeight/$videoWidth)*$videoWidth+gt(iw/ih,$videoHeight/$videoWidth)*(($videoHeight*ih)/iw)',pad='$videoHeight:$videoWidth:($videoHeight-gte(iw/ih,$videoHeight/$videoWidth)*$videoHeight-lt(iw/ih,$videoHeight/$videoWidth)*(($videoWidth*iw)/ih))/2:($videoWidth-lte(iw/ih,$videoHeight/$videoWidth)*$videoWidth-gt(iw/ih,$videoHeight/$videoWidth)*(($videoHeight*ih)/iw))/2:black',setdar=${aspectRatioStr}${rotationStr}[v$i];"
                        }
                        else {
                            Log.e("videoScaleConcatStr", "videoRotation ELLSE"+aspectRatioInString)
                            if (arrVideos[i].speed != 50f)
                                "[$i:v]trim=${arrVideos[i].startDuration.toDouble() / 1000}:${arrVideos[i].endDuration.toDouble() / 1000},setpts=PTS-STARTPTS[vtrim$i];[vtrim$i]setpts=${pairSpeed.first}[vspeed$i];[vspeed$i]scale='gte(iw/ih,$videoWidth/$videoHeight)*$videoWidth+lt(iw/ih,$videoWidth/$videoHeight)*(($videoHeight*iw)/ih):lte(iw/ih,$videoWidth/$videoHeight)*$videoHeight+gt(iw/ih,$videoWidth/$videoHeight)*(($videoWidth*ih)/iw)',pad='$videoWidth:$videoHeight:($videoWidth-gte(iw/ih,$videoWidth/$videoHeight)*$videoWidth-lt(iw/ih,$videoWidth/$videoHeight)*(($videoHeight*iw)/ih))/2:($videoHeight-lte(iw/ih,$videoWidth/$videoHeight)*$videoHeight-gt(iw/ih,$videoWidth/$videoHeight)*(($videoWidth*ih)/iw))/2:black',setdar=${aspectRatioInString}${rotationStr}[v$i];"
                            else
                                "[$i:v]trim=${arrVideos[i].startDuration.toDouble() / 1000}:${arrVideos[i].endDuration.toDouble() / 1000},setpts=${pairSpeed.first}[vtrim$i];[vtrim$i]scale='gte(iw/ih,$videoWidth/$videoHeight)*$videoWidth+lt(iw/ih,$videoWidth/$videoHeight)*(($videoHeight*iw)/ih):lte(iw/ih,$videoWidth/$videoHeight)*$videoHeight+gt(iw/ih,$videoWidth/$videoHeight)*(($videoWidth*ih)/iw)',pad='$videoWidth:$videoHeight:($videoWidth-gte(iw/ih,$videoWidth/$videoHeight)*$videoWidth-lt(iw/ih,$videoWidth/$videoHeight)*(($videoHeight*iw)/ih))/2:($videoHeight-lte(iw/ih,$videoWidth/$videoHeight)*$videoHeight-gt(iw/ih,$videoWidth/$videoHeight)*(($videoWidth*ih)/iw))/2:black',setdar=${aspectRatioInString}${rotationStr}[v$i];"
                        }
                    else
                        if (arrVideos[i].videoRotation == 90f || arrVideos[i].videoRotation == 270f)
                            if (arrVideos[i].isVideoPortrait) {
                                Log.e("videoScaleConcatStr", "1 if")
                                "[$i:v]trim=${arrVideos[i].startDuration.toDouble() / 1000}:${arrVideos[i].endDuration.toDouble() / 1000},setpts=${pairSpeed.first}[vtrim$i];[vtrim$i]scale='gte(iw/ih,1080/1920)*1080+lt(iw/ih,1080/1920)*((1920*iw)/ih):lte(iw/ih,1080/1920)*1920+gt(iw/ih,1080/1920)*((1080*ih)/iw)',pad='1080:1920:(1080-gte(iw/ih,1080/1920)*1080-lt(iw/ih,1080/1920)*((1920*iw)/ih))/2:(1920-lte(iw/ih,1080/1920)*1920-gt(iw/ih,1080/1920)*((1080*ih)/iw))/2:black',setdar=9/16,transpose=${if (arrVideos[i].videoRotation == 90f) 1 else 2}[v$i];"
                            } else {
                                Log.e("videoScaleConcatStr", "1 else")
                                "[$i:v]trim=${arrVideos[i].startDuration.toDouble() / 1000}:${arrVideos[i].endDuration.toDouble() / 1000},setpts=${pairSpeed.first}[vtrim$i];[vtrim$i]scale='gte(iw/ih,1080/1920)*1080+lt(iw/ih,1080/1920)*((1920*iw)/ih):lte(iw/ih,1080/1920)*1920+gt(iw/ih,1080/1920)*((1080*ih)/iw)',pad='1080:1920:(1080-gte(iw/ih,1080/1920)*1080-lt(iw/ih,1080/1920)*((1920*iw)/ih))/2:(1920-lte(iw/ih,1080/1920)*1920-gt(iw/ih,1080/1920)*((1080*ih)/iw))/2:black',setdar=9/16,transpose=${if (arrVideos[i].videoRotation == 90f) 1 else 2}[v$i];"
                            }
                        else if (arrVideos[i].videoRotation == 180f)
                            if (isLandScape || !arrVideos[i].isVideoPortrait) {
                                Log.e("videoScaleConcatStr", "2 if")
                                "[$i:v]trim=${arrVideos[i].startDuration.toDouble() / 1000}:${arrVideos[i].endDuration.toDouble() / 1000},setpts=${pairSpeed.first}[vtrim$i];[vtrim$i]scale='gte(iw/ih,1920/1080)*1920+lt(iw/ih,1920/1080)*((1080*iw)/ih):lte(iw/ih,1920/1080)*1080+gt(iw/ih,1920/1080)*((1920*ih)/iw)',pad='1920:1080:(1920-gte(iw/ih,1920/1080)*1920-lt(iw/ih,1920/1080)*((1080*iw)/ih))/2:(1080-lte(iw/ih,1920/1080)*1080-gt(iw/ih,1920/1080)*((1920*ih)/iw))/2:black',setdar=16/9,hflip,vflip[v$i];"
                            } else {
                                Log.e("videoScaleConcatStr", "2 else")
                                "[$i:v]trim=${arrVideos[i].startDuration.toDouble() / 1000}:${arrVideos[i].endDuration.toDouble() / 1000},setpts=${pairSpeed.first}[vtrim$i];[vtrim$i]scale='gte(iw/ih,1080/1920)*1080+lt(iw/ih,1080/1920)*((1920*iw)/ih):lte(iw/ih,1080/1920)*1920+gt(iw/ih,1080/1920)*((1080*ih)/iw)',pad='1080:1920:(1080-gte(iw/ih,1080/1920)*1080-lt(iw/ih,1080/1920)*((1920*iw)/ih))/2:(1920-lte(iw/ih,1080/1920)*1920-gt(iw/ih,1080/1920)*((1080*ih)/iw))/2:black',setdar=9/16,hflip,vflip[v$i];"
                            }
                        else {
                            if (isLandScape || !arrVideos[i].isVideoPortrait) {
                                Log.e("videoScaleConcatStr", "3 if")
                                "[$i:v]trim=${arrVideos[i].startDuration.toDouble() / 1000}:${arrVideos[i].endDuration.toDouble() / 1000},setpts=${pairSpeed.first}[vtrim$i];[vtrim$i]scale='gte(iw/ih,1920/1080)*1920+lt(iw/ih,1920/1080)*((1080*iw)/ih):lte(iw/ih,1920/1080)*1080+gt(iw/ih,1920/1080)*((1920*ih)/iw)',pad='1920:1080:(1920-gte(iw/ih,1920/1080)*1920-lt(iw/ih,1920/1080)*((1080*iw)/ih))/2:(1080-lte(iw/ih,1920/1080)*1080-gt(iw/ih,1920/1080)*((1920*ih)/iw))/2:black',setdar=16/9[v$i];"
                            } else {
                                Log.e("videoScaleConcatStr", "3 else")
                                "[$i:v]trim=${arrVideos[i].startDuration.toDouble() / 1000}:${arrVideos[i].endDuration.toDouble() / 1000},setpts=${pairSpeed.first}[vtrim$i];[vtrim$i]scale='gte(iw/ih,1080/1920)*1080+lt(iw/ih,1080/1920)*((1920*iw)/ih):lte(iw/ih,1080/1920)*1920+gt(iw/ih,1080/1920)*((1080*ih)/iw)',pad='1080:1920:(1080-gte(iw/ih,1080/1920)*1080-lt(iw/ih,1080/1920)*((1920*iw)/ih))/2:(1920-lte(iw/ih,1080/1920)*1920-gt(iw/ih,1080/1920)*((1080*ih)/iw))/2:black',setdar=9/16[v$i];"
                            }
                        }
                )
            if (isOneAudioAvail){
                videoScaleConcatStr.append(
                    if (arrVideos[i].isAudioAvail) {
                        if (arrVideos[i].speed != 50f) {
                            "[$i:a]atrim=${arrVideos[i].startDuration.toDouble() / 1000}:${arrVideos[i].endDuration.toDouble() / 1000},${pairSpeed.second},volume=${arrVideos[i].videoVolume.toFloat() / 100}[a$i];"
                        }   else {
                            "[$i:a]atrim=${arrVideos[i].startDuration.toDouble() / 1000}:${arrVideos[i].endDuration.toDouble() / 1000},${pairSpeed.second},volume=${arrVideos[i].videoVolume.toFloat() / 100}[a$i];"
                        }
                    } else {
                        "[${arrVideos.size}:a]atrim=${arrVideos[i].startDuration.toDouble() / 1000}:${arrVideos[i].endDuration.toDouble() / 1000},${pairSpeed.second},volume=${arrVideos[i].videoVolume.toFloat() / 100}[a$i];"
                    } )}
            else{
            videoScaleConcatStr.append("[${arrVideos.size}:a]atrim=${arrVideos[i].startDuration.toDouble() / 1000}:${arrVideos[i].endDuration.toDouble() / 1000},${pairSpeed.second},volume=${arrVideos[i].videoVolume.toFloat() / 100}[a$i];")
        }}

        Log.e("applyChanges", "(arrVideos[i].arrVideos: "+(arrVideos.size))
        for (i in 0 until arrVideos.size) {
            videoScaleConcatStr.append("[v$i][a$i]")
        }
        videoScaleConcatStr.append("concat=n=${arrVideos.size}:v=1:a=1[outv][outa]")

        if (arrTextData.isNotEmpty())
            videoScaleConcatStr.append(";")
        else if (arrMusicData.isNotEmpty())
            videoScaleConcatStr.append(";")
        /**
         * To Add images of text on videoatrim
         */
        if (arrTextData.isNotEmpty()) {
            for (i in arrTextData.indices) {
                arrTextData[i].x = (videoWidth * arrTextData[i].x / videoWidthRatio)
                arrTextData[i].y = (videoHeight * arrTextData[i].y / videoHeightRatio)
            }
            for (i in arrTextData.indices) {
                if (arrTextData[i].sticker is TextSticker)
                    videoScaleConcatStr.append("[${i + 2}]scale=${(videoWidth * ((arrTextData[i].sticker!! as TextSticker).textWidth / videoWidthRatio))}:${(videoHeight * ((arrTextData[i].sticker!! as TextSticker).textHeight / videoHeightRatio))},rotate=${arrTextData[i].sticker!!.currentAngle * PI / 180}:c=none:ow='rotw(${arrTextData[i].sticker!!.currentAngle * PI / 180})':oh='roth(${arrTextData[i].sticker!!.currentAngle * PI / 180})'[r$i];")


            }
            for (i in 0 until arrTextData.size) {
                videoScaleConcatStr.append(if (i == 0) "[outv]" else "[v$i]")
                videoScaleConcatStr.append(
                    "[r$i]overlay=${arrTextData[i].x}:${arrTextData[i].y}:enable='between(t,${arrTextData[i].startTime / 100},${arrTextData[i].endTime / 100})'[v${i + 1}]"
                )
                if (arrTextData.size > 1 && arrTextData.size > i + 1)
                    videoScaleConcatStr.append(";")
            }
        }

        if (arrMusicData.isNotEmpty()) {
            if (arrTextData.isNotEmpty())
                videoScaleConcatStr.append(";")
            for (i in 0 until arrMusicData.size) {
                /**
                 * Get all audio input variables
                 */
                if (arrMusicData[i].endTime - arrMusicData[i].startTime > 0) {
                    videoScaleConcatStr.append("[${arrTextData.size + arrVideos.size + 1 + i}:a]")
                }
            }
            /**
             * concat those variables
             */
            videoScaleConcatStr.append("concat=n=${arrMusicData.size}:v=0:a=1[outaa];")
            /**
             * split one variable into size of selected videos
             */
            videoScaleConcatStr.append("[outaa]asplit=${arrVideoTimeSlot.size - 1}")
            for (i in arrVideoTimeSlot.indices) {
                if (i < arrVideos.size) {
                    videoScaleConcatStr.append("[aa${i}]")
                    if (i == arrVideos.size - 1)
                        videoScaleConcatStr.append(";")
                }
            }
            /**
             * Now trim those split variables as per video duration and apply volume
             */
            Log.e("applyChanges", "arrVideoTimeSlot: "+ arrVideoTimeSlot.size +"  >>>> "  +arrVideos.size  )
            for (i in arrVideoTimeSlot.indices) {
                if (i < arrVideos.size)
                    videoScaleConcatStr.append("[aa${i}]atrim=${arrVideoTimeSlot[i].toDouble() / 1000}:${arrVideoTimeSlot[i + 1].toDouble() / 1000},asetpts=PTS-STARTPTS,volume=${arrVideos[i].audioVolume.toFloat() / 100}[outaa$i];")
            }
            /**
             * concat those variables after changing volume
             */
            for (i in 0 until arrVideos.size) {
                videoScaleConcatStr.append("[outaa$i]")
            }
            videoScaleConcatStr.append("concat=n=${arrVideos.size}:v=0:a=1[outaa];")
            /**
             * merge video sound and external selected audio file and make one variable
             */
//            if (isOneAudioAvail)
            videoScaleConcatStr.append("[outaa][outa]amix[audio]")
        }

        cmdList.append("-filter_complex")
        cmdList.append(videoScaleConcatStr)
        cmdList.append("-map")
        cmdList.append(if (arrTextData.isNotEmpty()) "[v${arrTextData.size}]" else "[outv]")
        cmdList.append("-shortest")
//        if (arrMusicData.isNotEmpty() || isOneAudioAvail) {
        cmdList.append("-map")
        cmdList.append(if (arrMusicData.isNotEmpty()) "[audio]" else if (arrMusicData.isNotEmpty()) "[outaa]" else "[outa]")
//        }
        cmdList.append("-qscale:v")
        cmdList.append("1")
        cmdList.append("-qscale:a")
        cmdList.append("1")
        cmdList.append("-preset")
        cmdList.append("ultrafast")

        cmdList.append(outputPath)
        onGetOutputPath.invoke(outputPath)
        try {
            Log.e("executeCommands", " $cmdList")
            execCmd(cmdList, totalVideoDuration * 1000, object :
                OnEditorListener {
                override fun onSuccess() {
                    Log.d("onSuccess", "onSuccess")
                    if (!isFilterApplyAll) {
                        val finalOutputPath = context.getVideoFinalFilePath()
                        if (moveFile(outputPath, finalOutputPath)) {
                            context.notifyItemAfterSaved(arrayOf(finalOutputPath))
                            context.deleteAllFileFromCacheFolder()
                            onSucceeded(finalOutputPath)
                        }
                    } else {
                        val pairVideoSize = context.getVideoWidthHeight(outputPath)
                        applyFilter(
                            VideoItem(
                                videoUrl = outputPath,
                                width = pairVideoSize.first,
                                height = pairVideoSize.second,
                                filter = arrVideos[0].filter
                            )
                        )
                    }
                }

                override fun onFailure() {
                    Log.e("onFailure", "onFailure")
                    deleteUnWantedFile(outputPath)
                    onFailed()
                }

                override fun onProgress(progress: Float) {
                    Log.d("onProgress", " $progress")
                    onProgressUpdate((progress * 100))
                }
            })
        } catch (e: Exception) {
            deleteUnWantedFile(outputPath)
            onFailed()
        }
    }

    private fun convertViewToBitmap(view: View): Bitmap {
        return view.drawingCache
    }

    /**
     * write bitmap in internal storage
     *
     * @param image
     * @return output path of written bitmap
     */
    private fun storeImage(
        image: Bitmap
    ): String {
        val outputPath = context.getImageInternalFilePath()
        try {
            val fos = FileOutputStream(outputPath)
            image.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return outputPath
    }

    /**
     * When one video speed changes for specific duration then need to add item in list again with
     * change in start and end duration as per data which are stored at time of speed changes.
     */
    private fun setSpeedData() {
        val arr = ArrayList<VideoItem>()
        arr.addAll(arrVideos)
        for (i in 0 until arr.size) {
            val videoItem = arr[i]
            if (videoItem.isStartSpeedChange || videoItem.isEndSpeedChange) {
                if (videoItem.isStartSpeedChange)
                    arrVideos.add(
                        if (i > 0) i else 0, VideoItem(
                            videoUrl = videoItem.videoUrl,
                            videoThumb = videoItem.videoThumb,
                            isSelected = false,
                            selectedItemCount = 0,
                            videoDuration = if (videoItem.isSplit) ((videoItem.speedStartDuration + videoItem.startDuration) - videoItem.startDuration) else videoItem.speedStartDuration - videoItem.startDuration,
                            isVideoPortrait = videoItem.isVideoPortrait,
                            isAudioAvail = videoItem.isAudioAvail,
                            isVideoProper = videoItem.isVideoProper,
                            width = videoItem.width,
                            height = videoItem.height,
                            videoUri = videoItem.videoUri,
                            startDuration = videoItem.startDuration,
                            endDuration = if (videoItem.isSplit) (videoItem.speedStartDuration + videoItem.startDuration) else videoItem.speedStartDuration,
                            isTrimmed = videoItem.isTrimmed,
                            speed = 50f,
                            isSplit = false,
                            videoRotation = videoItem.videoRotation,
                            orientation = videoItem.orientation,
                            filter = videoItem.filter,
                            videoVolume = videoItem.videoVolume,
                            audioVolume = videoItem.audioVolume,
                        )
                    )
                if (videoItem.isEndSpeedChange)
                    arrVideos.add(
                        if (videoItem.isStartSpeedChange) i + 2 else i + 1, VideoItem(
                            videoUrl = videoItem.videoUrl,
                            videoThumb = videoItem.videoThumb,
                            isSelected = false,
                            selectedItemCount = 0,
                            videoDuration = if (videoItem.isSplit) (videoItem.endDuration - (videoItem.speedEndDuration + videoItem.startDuration)) else videoItem.endDuration - videoItem.speedEndDuration,
                            isVideoPortrait = videoItem.isVideoPortrait,
                            isAudioAvail = videoItem.isAudioAvail,
                            isVideoProper = videoItem.isVideoProper,
                            width = videoItem.width,
                            height = videoItem.height,
                            videoUri = videoItem.videoUri,
                            startDuration = if (videoItem.isSplit) (videoItem.speedEndDuration + videoItem.startDuration) else videoItem.speedEndDuration,
                            endDuration = videoItem.endDuration,
                            isTrimmed = videoItem.isTrimmed,
                            speed = 50f,
                            isSplit = false,
                            videoRotation = videoItem.videoRotation,
                            orientation = videoItem.orientation,
                            filter = videoItem.filter,
                            videoVolume = videoItem.videoVolume,
                            audioVolume = videoItem.audioVolume,
                        )
                    )
                if (videoItem.isStartSpeedChange)
                    videoItem.startDuration = arrVideos[i].endDuration

                if (videoItem.isEndSpeedChange)
                    videoItem.endDuration =
                        arrVideos[if (videoItem.isStartSpeedChange) i + 2 else i + 1].startDuration
            }
        }
    }

    /**
     * Need to set video and audio speed as per FFMPEG command
     *
     * @param speed
     * @return
     */
    private fun setSpeedOfVideoAudio(speed: Float): Pair<String, String> {
        val videoSpeed: Float
        val audioSpeed: Float
        when (speed) {
            0f -> {
                videoSpeed = 2f
                audioSpeed = 0.5f
            }
            12.5f -> {
                videoSpeed = 1.6f
                audioSpeed = 0.625f
            }
            25f -> {
                videoSpeed = 1.333f
                audioSpeed = 0.75f
            }
            37.5f -> {
                videoSpeed = 1.1428f
                audioSpeed = 0.875f
            }
            50f -> {
                videoSpeed = 1f
                audioSpeed = 1f
            }
            62.5f -> {
                videoSpeed = 0.8f
                audioSpeed = 1.25f
            }
            75f -> {
                videoSpeed = 0.6667f
                audioSpeed = 1.50f
            }
            87.5f -> {
                videoSpeed = 0.5714f
                audioSpeed = 1.75f
            }
            100f -> {
                videoSpeed = 0.5f
                audioSpeed = 2f
            }
            else -> {
                videoSpeed = 1f
                audioSpeed = 1f
            }
        }
        /*
        *When speed if 1f(default value) then use setpts = PTS-STARTPTS, asetpts = PTS-STARTPTS
        * otherwise setpts = $videoSpeed * PTS, atempo = $audioSpeed
        */
        return Pair(
            if (videoSpeed != 1f) "'(${videoSpeed}*PTS)'" else "PTS-STARTPTS",
            if (audioSpeed != 1f) "atempo=$audioSpeed" else "asetpts=PTS-STARTPTS"
        )
    }

    fun applyVideoFilter() {
        if (isFilterApplyAll) {
            Log.e("VideoEditor", "applyChanges isFilterApplyAll $isFilterApplyAll")
            applyChanges()
        } else {
            for (i in 0 until arrVideos.size) {
                index = i
                if (arrVideos[i].filter != FilterType.DEFAULT) {
                    Log.e("VideoEditor", "for loop break")
                    isFilterUsed = true
                    applyFilter(arrVideos[i])
                    break
                }
                Log.e("VideoEditor", "for loop index $index")
            }
            if (index == arrVideos.size - 1 && !isFilterUsed) {
                Log.e("VideoEditor", "applyChanges")
                applyChanges()
            }
        }
    }



    private fun applyFilter(videoItem: VideoItem) {
        Log.e("VideoEditor", "applyFilter start"+videoItem.filter)
        val outputPath = context.getVideoFilePath()
        Log.e("VideoEditor", "applyFilter outputPath $outputPath")
        val mp4Composer = Mp4Composer(getFilePath(context.checkUrl(videoItem.videoUrl)), outputPath)
        onGetFilterObject.invoke(mp4Composer)
        mp4Composer
            .filter(FilterHelper.getFilter(videoItem.filter, context).setRotation(0))
            .listener(object : Mp4Composer.Listener {
                override fun onProgress(progress: Double) {
                    onProgressUpdate.invoke(progress.toFloat())
                }

                override fun onCompleted() {
                    Log.e("VideoEditor", "applyFilter onCompleted")
                    if (isFilterApplyAll)
                        deleteUnWantedFile(videoItem.videoUrl)
                    else {
                        videoItem.videoUrl = outputPath
                    }
                    if (isFilterApplyAll) {
                        val finalOutputPath = context.getVideoFinalFilePath()
                        if (moveFile(outputPath, finalOutputPath)) {
                            context.notifyItemAfterSaved(arrayOf(finalOutputPath))
                            context.deleteAllFileFromCacheFolder()
                            onSucceeded(finalOutputPath)
                        }
                    } else {
                        index++
                        Log.e("VideoEditor", "applyFilter index $index ${arrVideos.size}")
                        if (index < arrVideos.size) {
                            if (arrVideos[index].filter != FilterType.DEFAULT)
                                applyFilter(arrVideos[index])
                            else
                                applyChanges()
                        } else
                            applyChanges()
                    }
                }

                override fun onCanceled() {
                    deleteUnWantedFile(outputPath)
                    Log.e("VideoEditor", "applyFilter onCanceled")
                }

                override fun onFailed(exception: java.lang.Exception?) {
                    deleteUnWantedFile(outputPath)
                    Log.e("VideoEditor", "applyFilter onFailed"+exception!!.message.toString())
                }
            })
            .start()
    }

    private fun trimBitmap(sourceBitmap: Bitmap): Bitmap {
        val startTime = System.currentTimeMillis()
        var minX = sourceBitmap.width
        var minY = sourceBitmap.height
        var maxX = -1
        var maxY = -1

        loop1@ for (x in 0 until sourceBitmap.width) {
            loop2@ for (y in 0 until sourceBitmap.height) {
                val alpha = Color.alpha(sourceBitmap.getPixel(x, y))
                if (alpha > 0) {
                    minX = x
                    break@loop1
                }
            }
        }
        loop1@ for (x in sourceBitmap.width - 1 downTo 0) {
            loop2@ for (y in 0 until sourceBitmap.height) {
                val alpha = Color.alpha(sourceBitmap.getPixel(x, y))
                if (alpha > 0) {
                    maxX = x
                    break@loop1
                }
            }
        }

        loop1@ for (y in 0 until sourceBitmap.height) {
            loop2@ for (x in minX until maxX + 1) {
                val alpha = Color.alpha(sourceBitmap.getPixel(x, y))
                if (alpha > 0) {
                    minY = y
                    break@loop1
                }
            }
        }
        loop1@ for (y in sourceBitmap.height - 1 downTo 0) {
            loop2@ for (x in minX until maxX + 1) {
                val alpha = Color.alpha(sourceBitmap.getPixel(x, y))
                if (alpha > 0) {
                    maxY = y
                    break@loop1



                }
            }
        }


        val bitmap = if (maxX < minX || maxY < minY)
            sourceBitmap
        else Bitmap.createBitmap(
            sourceBitmap,
            minX,
            minY,
            maxX - minX + 1,
            maxY - minY + 1
        ) // Bitmap is entirely transparent
        Log.e("==>taken time for trim", "==>${System.currentTimeMillis() - startTime}")
        return bitmap
        // crop bitmap to non-transparent area and return:
    }
}