package com.app.personas_social.viewmodel

import android.graphics.drawable.Drawable
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

abstract class BaseViewModel : ViewModel() {
    val currentStatus = MutableLiveData<Any>()
    val strToolbarTitle = ObservableField<String>()
    val titleRightDrawable = ObservableField<Drawable>()
    val isBtnFinishVisible = ObservableBoolean(false)
    val navigationIcon = ObservableField<Drawable>()


    val strTopTitle = ObservableField<String>()
    val strTopNum = ObservableField<String>()
    val isSearchVisible = ObservableBoolean(false)
    val isFilterVisible = ObservableBoolean(false)
    val isTabVisible = ObservableBoolean(false)
    val isMyCartVisible = ObservableBoolean(false)

    var isClick = false

    var exportProgress = ObservableInt(0)
    var progressText = ObservableField<String>()
    var isShowCancel = ObservableBoolean(true)


    fun setSearchVisibility(isVisible: Boolean) {
        isSearchVisible.set(isVisible)
    }

    fun setFilterVisibility(isVisible: Boolean) {
        isFilterVisible.set(isVisible)
    }

    fun setTabVisibility(isVisible: Boolean) {
        isTabVisible.set(isVisible)
    }

    fun setMyCartVisibility(isVisible: Boolean) {
        isMyCartVisible.set(isVisible)
    }

    fun setTopTitle(title: String) {
        strTopTitle.set(title)
    }

    fun setTitleRightDrawable(icon: Drawable?) {
        titleRightDrawable.set(icon)
    }

    fun setBtnFinishVisibility(isVisible: Boolean) {
        isBtnFinishVisible.set(isVisible)
    }

    fun setNavigationIcon(iconId: Drawable?) {
        navigationIcon.set(iconId)
    }



    fun setTopNum(num: String) {
        strTopNum.set(num)
    }

    fun isValidPassword(password: String?): Boolean {
        password?.let {
            val passwordPattern =
                "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[-!@#$%^&*+=_])(?=\\S+$).{8,20}$"
            val passwordMatcher = Regex(passwordPattern)
            return passwordMatcher.find(password) != null
        } ?: return false
    }


}