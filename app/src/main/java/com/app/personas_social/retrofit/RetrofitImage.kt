package video.amaze.app.network

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.FileUtils
import android.util.Log
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.internal.notify
import okio.*
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.TimeUnit


object RetrofitImage {
    interface OnAttachmentDownloadListener {
        fun onAttachmentDownloadedSuccess()
        fun onAttachmentDownloadedError()
        fun onAttachmentDownloadedFinished()
        fun onAttachmentDownloadUpdate(percent: Int)
    }

    private fun provideRetrofit(listener: OnAttachmentDownloadListener): Retrofit {
        return Retrofit.Builder().baseUrl("https://google.com")
            .addConverterFactory(GsonConverterFactory.create())
            .client(getOkHttpDownloadClientBuilder(listener).build())
            .build()
    }

    private interface API {
        @GET
        fun getImageData(@Url url: String): Call<ResponseBody>

        @GET
        fun getVideoData(@Url url: String): Call<ResponseBody>
    }

    lateinit var setListener: OnAttachmentDownloadListener

    private val api: API by lazy { provideRetrofit(setListener).create(API::class.java) }

    fun getBitmapFrom(url: String, onComplete: (Bitmap?) -> Unit) {


        api.getImageData(url).enqueue(object : retrofit2.Callback<ResponseBody> {

            override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                onComplete(null)
            }

            override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
                if (response == null || !response.isSuccessful || response.body() == null || response.errorBody() != null) {
                    onComplete(null)
                    return
                }
                val bytes = response.body()!!.bytes()
                onComplete(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
            }
        })
    }

    fun getBitmapFrom(
        url: String,
        listener: OnAttachmentDownloadListener,
        onComplete: (Bitmap?) -> Unit
    ) {
        setListener = listener
        api.getImageData(url).enqueue(object : retrofit2.Callback<ResponseBody> {

            override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                onComplete(null)
            }

            override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
                if (response == null || !response.isSuccessful || response.body() == null || response.errorBody() != null) {
                    onComplete(null)
                    return
                }
                val bytes = response.body()!!.bytes()
                onComplete(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
            }
        })
    }

    fun getVideoFrom(url: String,   listener: OnAttachmentDownloadListener,onComplete: (InputStream?,) -> Unit) {
        Log.e("TAG", "onAttachmentDownloadedSuccess: SUCCESS" )
        setListener = listener
        api.getImageData(url).enqueue(object : retrofit2.Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                onComplete(null)
            }

            override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
                if (response == null || !response.isSuccessful || response.body() == null || response.errorBody() != null) {
                    onComplete(null)
                    return
                }
//                val bytes = response.body()!!.bytes()
//                onComplete(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))


                try {
                    onComplete(response.body()?.byteStream())
//                    val inputStream: InputStream = response
                } catch (e: IOException) {
                    e.printStackTrace()
                }


            }
        })
    }

    private class ProgressResponseBody(
        private val responseBody: ResponseBody,
        private val progressListener: OnAttachmentDownloadListener?
    ) :
        ResponseBody() {
        private var bufferedSource: BufferedSource? = null
        override fun contentType(): MediaType? {
            return responseBody.contentType()
        }

        override fun contentLength(): Long {
            return responseBody.contentLength()
        }

        override fun source(): BufferedSource {
            if (bufferedSource == null) {
                bufferedSource = source(responseBody.source()).buffer()
            }
            return bufferedSource!!
        }

        private fun source(source: Source): Source {
            return object : ForwardingSource(source) {
                var totalBytesRead = 0L

                @Throws(IOException::class)
                override fun read(sink: Buffer, byteCount: Long): Long {
                    val bytesRead = sink.let { super.read(it, byteCount) }
                    totalBytesRead += (if (bytesRead != -1L) bytesRead else 0)
                    val percent =
                        if (bytesRead == -1L) 100f else totalBytesRead.toFloat() / responseBody.contentLength()
                            .toFloat() * 100
                    progressListener?.onAttachmentDownloadUpdate(percent.toInt())
                    return bytesRead
                }
            }
        }
    }

    fun getOkHttpDownloadClientBuilder(progressListener: OnAttachmentDownloadListener?): OkHttpClient.Builder {
        val httpClientBuilder = OkHttpClient.Builder()

        // You might want to increase the timeout
        httpClientBuilder.connectTimeout(20, TimeUnit.SECONDS)
        httpClientBuilder.writeTimeout(0, TimeUnit.SECONDS)
        httpClientBuilder.readTimeout(5, TimeUnit.MINUTES)
        httpClientBuilder.addInterceptor(object : Interceptor {
            @Throws(IOException::class)
            override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
                if (progressListener == null) return chain.proceed(chain.request())
                val originalResponse: okhttp3.Response = chain.proceed(chain.request())
                return originalResponse.newBuilder()
                    .body(
                        originalResponse.body
                            ?.let { ProgressResponseBody(it, progressListener) })
                    .build()
            }
        })
        return httpClientBuilder
    }
}