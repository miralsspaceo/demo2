//package video.amaze.app.network
//
//class MainRepository {
//
//    suspend fun getAllPexels(currentPage: String, pageLimit: String) =
//        ApiClient.getPexals().getAllPexels(
//            currentPage,
//            pageLimit
//        )
//
//    suspend fun getAllVideoPexels(currentPage: String, pageLimit: String) =
//        ApiClient.getPexals().getAllVideoPexels(
//            currentPage,
//            pageLimit
//        )
//
//    suspend fun getSearchPexels(searchText: String, currentPage: String, pageLimit: String) =
//        ApiClient.getPexals().getSearchPexels(
//            searchText,
//            currentPage,
//            pageLimit
//        )
//
//    suspend fun getSearchVideoPexels(searchText: String, currentPage: String, pageLimit: String) =
//        ApiClient.getPexals().getSearchVideoPexels(
//            searchText,
//            currentPage,
//            pageLimit
//        )
//
//
//    suspend fun getAllGif(apiKey:String,offSet:String,limit:String) = ApiClient.getGIF().getGifTrending(apiKey, offSet, limit)
//    suspend fun getGifSearch(apiKey:String,offSet:String,limit:String,searchText: String) = ApiClient.getGIF().getGifSearch(apiKey, offSet, limit,searchText)
//
//    suspend fun getAllGifEmojis(offSet:String,limit:String,apiKey: String)=ApiClient.getGIF().getEmojis(offSet, limit, apiKey)
//    suspend fun getSearchEmojis(apiKey:String,offSet: String,limit: String,searchText: String)=ApiClient.getGIF().getSearchEmojis(apiKey,offSet,limit,searchText)
//
//    suspend fun getGreetingsEmojis(apiKey: String,offSet: String,limit: String,query:String)=ApiClient.getGIF().getGreetingsGif(apiKey, offSet, limit, query)
//    suspend fun getSearchGreetingsEmojis(apiKey: String,offSet: String,limit: String,query:String)=ApiClient.getGIF().getGreetingsGif(apiKey, offSet, limit, query)
//
//
//    suspend fun getAllGif(apiKey:String) = ApiClient.getGIF().getAllGif(apiKey)
//
//
//    suspend fun getAllUnsplash(unSplashClientId: String, page: String, perPage: String) =
//        ApiClient.getUnsplashService().getAllUnsplash(
//            unSplashClientId, page, perPage
//        )
//
//
//    suspend fun getSearchUnsplash(queryPhoto: String, unSplashClientId: String, page:String, perPage: String) =         ApiClient.getUnsplashService().getSearchUnsplash(
//      queryPhoto,  unSplashClientId, page, perPage
//    )
//
//    suspend fun getAllPixabay(searchText: String, pixaBayKey: String, page:String, perPage: String) = ApiClient.getPixaBay().getAllPixabay(
//        searchText,pixaBayKey,page,perPage
//    )
//
//    suspend fun getAllVideoPixabay(searchText: String, pixaBayKey: String, page:String, perPage: String) = ApiClient.getPixaBay().getAllVideoPixabay(
//        searchText,pixaBayKey,page,perPage
//    )
//    suspend fun getdownloadFile(url: String) = ApiClient.getUnsplashService().downloadFile(
//        url
//    )
//}