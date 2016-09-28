package idv.mistasy.hackernewsreader.data

import rx.Observable

/**
 * Created by shaocheng on 9/28/16.
 */

class DataManager(val hackerNewsService: HackerNewsService) {
    fun getTopStories(): Observable<List<Long>> {
        return hackerNewsService.getTopStoriesRx()
    }
}