package video.amaze.app.network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*
import video.amaze.app.network.ApiConstants.API_GET_ALLPEXELS
import video.amaze.app.network.ApiConstants.API_GET_ALL_VIDEO
import video.amaze.app.network.ApiConstants.API_GET_SEARCH_PEXELS
import video.amaze.app.network.ApiConstants.HEADER_AUTHORIZATION
import video.amaze.app.network.ApiConstants.PARAM_API_KEY
import video.amaze.app.network.ApiConstants.PARAM_CLIENT_ID
import video.amaze.app.network.ApiConstants.PARAM_KEY
import video.amaze.app.network.ApiConstants.PARAM_LIMIT
import video.amaze.app.network.ApiConstants.PARAM_OFFSET
import video.amaze.app.network.ApiConstants.PARAM_PAGE
import video.amaze.app.network.ApiConstants.PARAM_PER_PAGE
import video.amaze.app.network.ApiConstants.PARAM_Q
import video.amaze.app.network.ApiConstants.PARAM_QUERY
//import video.amaze.app.utilities.model.*
//import video.amaze.app.utilities.model.pexels.PexelsModel
//import video.amaze.app.utilities.model.pexels.PexelsVideoModel


interface RetrofitService {

//    @Headers("$HEADER_AUTHORIZATION ${BuildConfig.PexelsKey}")
//    @GET(API_GET_ALLPEXELS)
//    suspend fun getAllPexels(
//        @Query(PARAM_PAGE) currentPage: String, @Query(PARAM_PER_PAGE) pageLimit: String
//    ): Response<PexelsModel>

//    @Headers("$HEADER_AUTHORIZATION ${BuildConfig.PexelsKey}")
//    @GET(API_GET_ALL_VIDEO)
//    suspend fun getAllVideoPexels(
//        @Query(PARAM_PAGE) currentPage: String, @Query(PARAM_PER_PAGE) pageLimit: String
//    ): Response<PexelsVideoModel>
//
//    @Headers("$HEADER_AUTHORIZATION ${BuildConfig.PexelsKey}")
//    @GET(API_GET_ALL_VIDEO)
//    suspend fun getSearchVideoPexels(
//        @Query(PARAM_QUERY) searchText: String,
//        @Query(PARAM_PAGE) currentPage: String,
//        @Query(PARAM_PER_PAGE) pageLimit: String
//    ): Response<PexelsVideoModel>
//
//
//    /*@Headers("$HEADER_AUTHORIZATION ${BuildConfig.PexelsKey}")*/
//    @Headers("$HEADER_AUTHORIZATION ${BuildConfig.PexelsKey}")
//    @GET(API_GET_SEARCH_PEXELS)
//    suspend fun getSearchPexels(
//        @Query(PARAM_QUERY) searchText: String,
//        @Query(PARAM_PAGE) currentPage: String,
//        @Query(PARAM_PER_PAGE) pageLimit: String
//    ): Response<PexelsModel>
//
//
//    @GET(ApiConstants.API_ALL_UNSPLASH_PHOTOS)
//    suspend fun getAllUnsplash(
//        @Query(PARAM_CLIENT_ID) clientId: String = BuildConfig.UnsplashKey,
//        @Query(PARAM_PAGE) page: String,
//        @Query(PARAM_PER_PAGE) perPage: String
//    ): Response<UnsplashModel>
//
//    @GET
//    suspend fun getAllGif(
//        @Url url: String
//    ): Response<GiphyModel>
//
//
//    @GET(ApiConstants.API_GET_TRENDING_GIF)
//    suspend fun getGifTrending(@Query(PARAM_API_KEY) apiKey:String,@Query(PARAM_OFFSET)offSet:String,@Query(
//        PARAM_LIMIT) limit:String): Response<GiphyModel>
//
//    @GET(ApiConstants.API_GET_SEARCH_GIF)
//    suspend fun getGifSearch(@Query(PARAM_API_KEY) apiKey:String,@Query(PARAM_OFFSET)offSet:String,@Query(
//        PARAM_LIMIT) limit:String,@Query(PARAM_Q) searchText: String): Response<GiphyModel>
//
//
//    @GET(ApiConstants.API_GET_SEARCH_GIF)
//    suspend fun getGreetingsGif(@Query(PARAM_API_KEY) apiKey:String,@Query(PARAM_OFFSET)offSet:String,@Query(
//        PARAM_LIMIT) limit:String,@Query(PARAM_Q)query:String): Response<GiphyModel>
//
//    @GET(ApiConstants.API_GET_EMOJIS)
//    suspend fun getEmojis(@Query(PARAM_OFFSET)offSet:String,@Query(
//        PARAM_LIMIT) limit:String,@Query(PARAM_API_KEY)apiKey:String) : Response<GiphyModel>
//
//
//    @GET(ApiConstants.API_GET_EMOJIS)
//    suspend fun getSearchEmojis(@Query(PARAM_API_KEY)apiKey:String,@Query(PARAM_OFFSET)offSet:String,@Query(
//        PARAM_LIMIT) limit:String,@Query(PARAM_Q)query:String): Response<GiphyModel>
//
//
//
//
//
//
//
//
//
//    @GET(ApiConstants.API_SEARCH_UNSPLASH_PHOTOS)
//    suspend fun getSearchUnsplash(
//        @Query(PARAM_QUERY) searchText: String,
//        @Query(PARAM_CLIENT_ID) clientId: String,
//        @Query(PARAM_PAGE) page: String,
//        @Query(PARAM_PER_PAGE) perPage: String
//
//    ): Response<UnsplashSearchModel>
//
//
//    @GET("api/")
//    suspend fun getAllPixabay(
//        @Query("q") searchText: String,
//        @Query(PARAM_KEY) key: String,
//        @Query(PARAM_PAGE) page: String,
//        @Query(
//            PARAM_PER_PAGE
//        ) perPage: String
//    ): Response<PixabayModel>
//
//    @GET("api/videos/")
//    suspend fun getAllVideoPixabay(
//        @Query("q") searchText: String,
//        @Query(PARAM_KEY) key: String,
//        @Query(PARAM_PAGE) page: String,
//        @Query(
//            PARAM_PER_PAGE
//        ) perPage: String
//    ): Response<PixabayVideoModel>
//
//
//    @Streaming
//    @GET
//    fun downloadFile(@Url fileUrl: String): Response<Response<ResponseBody>>


    //    pexelsKey = "563492ad6f917000010000012813df801a05489d91a211a979576910"
    companion object
}