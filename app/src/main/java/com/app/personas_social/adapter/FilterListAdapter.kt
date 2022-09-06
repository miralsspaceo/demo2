package com.app.personas_social.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.util.Log
import androidx.core.content.ContextCompat
import com.app.personas_social.R
import com.app.personas_social.databinding.RawFilterItemBinding
import com.bumptech.glide.Glide
import com.videofilter.*
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.filter.*
import jp.co.cyberagent.android.gpuimage.filter.FilterType

import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


class FilterListAdapter (
    private val  context : Context,
    private val arrVideo: ArrayList<FilterData>,
    private val callBack: (position: Int) -> Unit
) :
    BaseAdapter(arrVideo) {
    private var mSelectedItem = 0
    private val mBitmaps: ArrayList<Bitmap>? = ArrayList()
    override fun getItemViewType(position: Int) = R.layout.raw_filter_item

    override fun onBindViewHolder(holder: BaseAdapter.ItemViewHolder, @SuppressLint("RecyclerView") position: Int) {
        if (holder.binding is RawFilterItemBinding) {



//            Glide.with(context)
//                .load(arrVideo[position].filterThumb)
//                .centerCrop()
//                .into(holder.binding.ivFilter);

//            var bitmap = savebitmap(arrVideo[position].filterThumb)
            mBitmaps!!.clear()
            when(position){
                0 ->{
                    val imageFilters: List<GPUImageFilter> = listOf(GPUImageFilter())
                    GPUImage.getBitmapForMultipleFilters(
                        arrVideo[position].filterThumb,
                        imageFilters
                    ) { item ->
                        holder.binding.ivFilter.setImageBitmap(item)
                    }
                }
                1->{
                    val imageFilters: List<GPUImageFilter> = listOf(IF1977Filter(context))
                    GPUImage.getBitmapForMultipleFilters(
                        arrVideo[position].filterThumb,
                        imageFilters
                    ) { item ->
                        holder.binding.ivFilter.setImageBitmap(item)
                    }
                }
                2->{
                    val imageFilters: List<GPUImageFilter> = listOf(IFAmaroFilter(context))
                    GPUImage.getBitmapForMultipleFilters(
                        arrVideo[position].filterThumb,
                        imageFilters
                    ) { item ->
                        holder.binding.ivFilter.setImageBitmap(item)
                    }
                }
                3->{
                    val imageFilters: List<GPUImageFilter> = listOf(IFInkwellFilter(context))

                    GPUImage.getBitmapForMultipleFilters(
                        arrVideo[position].filterThumb,
                        imageFilters
                    ) { item ->
                        holder.binding.ivFilter.setImageBitmap(item)
                    }
                }
                4->{
                    val imageFilters: List<GPUImageFilter> = listOf(IFEarlybirdFilter(context))
                    GPUImage.getBitmapForMultipleFilters(
                        arrVideo[position].filterThumb,
                        imageFilters
                    ) { item ->
                        holder.binding.ivFilter.setImageBitmap(item)
                    }
                }
                5->{
                    val imageFilters: List<GPUImageFilter> = listOf(IFHudsonFilter(context))
                    GPUImage.getBitmapForMultipleFilters(
                        arrVideo[position].filterThumb,
                        imageFilters
                    ) { item ->
                        holder.binding.ivFilter.setImageBitmap(item)
                    }
                }
                6->{
                    val imageFilters: List<GPUImageFilter> = listOf(IFHefeFilter(context))
                    GPUImage.getBitmapForMultipleFilters(
                        arrVideo[position].filterThumb,
                        imageFilters
                    ) { item ->
                        holder.binding.ivFilter.setImageBitmap(item)
                    }
                }
                7->{
                    val imageFilters: List<GPUImageFilter> = listOf( IFLomoFilter(context))
                    GPUImage.getBitmapForMultipleFilters(
                        arrVideo[position].filterThumb,
                        imageFilters
                    ) { item ->
                        holder.binding.ivFilter.setImageBitmap(item)
                    }
                }
                8->{
                    val imageFilters: List<GPUImageFilter> = listOf(IFLordKelvinFilter(context))
                    GPUImage.getBitmapForMultipleFilters(
                        arrVideo[position].filterThumb,
                        imageFilters
                    ) { item ->
                        holder.binding.ivFilter.setImageBitmap(item)
                    }
                }
                9->{
                    val imageFilters: List<GPUImageFilter> = listOf( IFNashvilleFilter(context))
                    GPUImage.getBitmapForMultipleFilters(
                        arrVideo[position].filterThumb,
                        imageFilters
                    ) { item ->
                        holder.binding.ivFilter.setImageBitmap(item)
                    }
                }
                10->{
                    val imageFilters: List<GPUImageFilter> = listOf( IFRiseFilter(context))
                    GPUImage.getBitmapForMultipleFilters(
                        arrVideo[position].filterThumb,
                        imageFilters
                    ) { item ->
                        holder.binding.ivFilter.setImageBitmap(item)
                    }
                }
                11->{
                    val imageFilters: List<GPUImageFilter> = listOf(IFXprollFilter(context))
                    GPUImage.getBitmapForMultipleFilters(
                        arrVideo[position].filterThumb,
                        imageFilters
                    ) { item ->
                        holder.binding.ivFilter.setImageBitmap(item)
                    }
                }

            }


            Log.e("onBindViewHolder", "onBindViewHolder: "+position )
            if (arrVideo[position].isSelected) {
                holder.binding.tvFilterName.setTextColor(ContextCompat.getColor(context,R.color.pink))
            }else{
                holder.binding.tvFilterName.setTextColor(ContextCompat.getColor(context,R.color.black))

            }

            holder.binding.tvFilterName.text = arrVideo[position].filterName

            holder.itemView.setOnClickListener {
                mSelectedItem = position;
                notifyDataSetChanged()
                callBack(position)
            }

        }
    }


}