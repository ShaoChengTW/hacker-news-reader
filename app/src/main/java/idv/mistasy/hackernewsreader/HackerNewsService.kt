package idv.mistasy.hackernewsreader

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import rx.Observable

interface HackerNewsService {
    @GET("topstories.json")
    fun getTopStoriesRx(): Observable<List<Long>>

    @GET("topstories.json")
    fun getTopStories(): Call<List<Long>>

    @GET("item/{item_id}.json")
    fun getItem(@Path("item_id") itemId: String): Observable<Item>
}

