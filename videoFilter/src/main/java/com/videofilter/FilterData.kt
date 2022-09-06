package com.videofilter

import android.graphics.Bitmap

class FilterData(
    var filterType: FilterType,
    var position: Int = 0,
    var filterThumb: Bitmap ,
    var filterName: String = "",
    var isSelected: Boolean = false
)