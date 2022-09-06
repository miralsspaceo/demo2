package video.amaze.app.network

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider


//class MyViewModelFactory constructor(private val repository: MainRepository) :
//    ViewModelProvider.Factory {

//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        return if (modelClass.isAssignableFrom(TemplateEditViewModel::class.java)) {
//            TemplateEditViewModel() as T
//
//        } else if (modelClass.isAssignableFrom(GraphicsViewModel::class.java)) {
//            GraphicsViewModel(this.repository) as T
//
//        } else if (modelClass.isAssignableFrom(GraphicsViewModelNew::class.java)) {
//            GraphicsViewModelNew(this.repository) as T
//
//        } else if (modelClass.isAssignableFrom(MediaViewModel::class.java)) {
//            MediaViewModel(this.repository) as T

//        } else {
//            throw IllegalArgumentException("ViewModel Not Found")
//        }
//    }
//}