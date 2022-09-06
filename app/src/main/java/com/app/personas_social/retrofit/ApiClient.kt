//package video.amaze.app.network
//
//import com.localebro.okhttpprofiler.OkHttpProfilerInterceptor
//import okhttp3.OkHttpClient
//import okhttp3.logging.HttpLoggingInterceptor
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
//
//object ApiClient {
//    fun getUnsplashService(): RetrofitService {
//        val interceptor = OkHttpProfilerInterceptor()
//
//
//        val client: OkHttpClient =
//            OkHttpClient.Builder().addInterceptor(interceptor).build()
//
//        val retrofit = Retrofit.Builder()
//            .client(client)
//            .baseUrl(ApiConstants.BASE_UNSPLASH)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//        return retrofit.create(RetrofitService::class.java)
//    }
//    fun getPexals(): RetrofitService {
//            val interceptor = OkHttpProfilerInterceptor()
//        val client: OkHttpClient =
//            OkHttpClient.Builder().addInterceptor(interceptor).build()
//
//        val retrofit = Retrofit.Builder()
//            .client(client)
//            .baseUrl(ApiConstants.BASE_PEXELS)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//    return retrofit.create(RetrofitService::class.java)
//
//    }
//    fun getPixaBay(): RetrofitService {
//            val interceptor = OkHttpProfilerInterceptor()
//        val client: OkHttpClient =
//            OkHttpClient.Builder().addInterceptor(interceptor).build()
//
//        val retrofit = Retrofit.Builder()
//            .client(client)
//            .baseUrl(ApiConstants.BASE_PIXABAY)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//        return retrofit.create(RetrofitService::class.java)
//    }
//
//    fun getGIF():RetrofitService{
//        val interceptor=OkHttpProfilerInterceptor()
//        val client: OkHttpClient =
//            OkHttpClient.Builder().addInterceptor(interceptor).build()
//        val retrofit = Retrofit.Builder()
//            .client(client)
//            .baseUrl(ApiConstants.BASE_GIPHY)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//        return retrofit.create(RetrofitService::class.java)
//
//    }
//}