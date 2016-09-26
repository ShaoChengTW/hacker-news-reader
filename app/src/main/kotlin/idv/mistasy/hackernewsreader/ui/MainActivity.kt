package idv.mistasy.hackernewsreader.ui

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import idv.mistasy.hackernewsreader.R
import idv.mistasy.hackernewsreader.data.HackerNewsService
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.jetbrains.anko.*
import org.jetbrains.anko.appcompat.v7.toolbar
import org.jetbrains.anko.recyclerview.v7.recyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import rx.Observable
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.functions.Action1
import rx.functions.Func1
import rx.schedulers.Schedulers

class MainActivity: Activity() {
    private val TAG: String = "MainActivity"

    val hackerNewsService: HackerNewsService by lazy {
        val interceptor = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger { Log.d(TAG, it) })
        interceptor.level = HttpLoggingInterceptor.Level.BASIC

        val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build()

        val retrofit = Retrofit.Builder()
                .baseUrl("https://hacker-news.firebaseio.com/v0/")
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build()

        retrofit.create(HackerNewsService::class.java)
    }

    private var adapter: TopStoriesAdapter? = null
    private var recyclerView: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recycler_view) as RecyclerView?
        recyclerView?.setHasFixedSize(true)
        recyclerView?.layoutManager = LinearLayoutManager(this)

        adapter = TopStoriesAdapter(emptyList<Long>())

        hackerNewsService.getTopStoriesRx()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Log.d(TAG, "count: ${it.count()}")
                    recyclerView?.adapter = TopStoriesAdapter(it.subList(0, 10))
                }

    }

    inner class TopStoriesAdapter(val storyIds: List<Long>): RecyclerView.Adapter<TopStoriesViewHolder>() {

        override fun getItemCount(): Int {
            return storyIds.count()
        }

        override fun onBindViewHolder(holder: TopStoriesViewHolder?, position: Int) {
            hackerNewsService.getItem(storyIds[position].toString())
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        holder?.title?.text = it.title
                    }
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): TopStoriesViewHolder {
            val view = LayoutInflater.from(this@MainActivity).inflate(R.layout.item_story, parent, false)
            return TopStoriesViewHolder(view)
        }
    }
}


class TopStoriesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var title: TextView? = null

    init {
        title = itemView.findViewById(R.id.item_title) as TextView?
    }
}

