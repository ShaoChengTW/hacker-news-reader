package idv.mistasy.hackernewsreader.ui

import android.app.Activity
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.widget.TextView
import idv.mistasy.hackernewsreader.HnrApp
import idv.mistasy.hackernewsreader.R
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject

/**
 * Created by shaocheng on 9/27/16.
 */

class ItemActivity: Activity() {
    companion object {
        val INTENT_EXTRA_URL = "intent.extra.url"
    }

    @Inject
    lateinit var okHttpClient: OkHttpClient

    private val TAG = "ItemActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        HnrApp.appComponent.inject(this)

        Log.d(TAG, "has url: ${intent.hasExtra(INTENT_EXTRA_URL)}")

        val url = intent.getStringExtra(INTENT_EXTRA_URL)
        Log.d(TAG, "url: $url")

        setContentView(R.layout.activity_item)

        val textView = findViewById(R.id.text) as TextView?

        Observable.fromCallable {
            val httpUrl = HttpUrl.parse("http://boilerpipe-web.appspot.com/extract").newBuilder()
                    .addEncodedQueryParameter("url", url)
                    .addEncodedQueryParameter("output", "htmlFragment")
                    .build()

            val request = Request.Builder()
                    .url(httpUrl)
                    .build()

            okHttpClient.newCall(request).execute().body().string()
        }
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
            val a = it.replace(Regex("<style[\\s\\S]*</style>"), "")
            textView?.text = Html.fromHtml(a) }
    }
}
