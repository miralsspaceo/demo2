package com.app.personas_social.app


sealed class ResponseObserver<out T> {
    class Loading(val isLoading: Boolean = false) : ResponseObserver<Boolean>()
    class DisplayAlert(val msgId: Int = 0, val msg: String = "") : ResponseObserver<Any>()
    class PerformAction(val data: Any, val nextData: Any? = null) : ResponseObserver<Any>()
}