package idv.mistasy.hackernewsreader.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import idv.mistasy.hackernewsreader.R
import idv.mistasy.hackernewsreader.data.HackerNewsService
import idv.mistasy.hackernewsreader.data.model.Item
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subjects.PublishSubject

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
        recyclerView?.layoutManager = LinearLayoutManager(this)

        adapter = TopStoriesAdapter(mutableListOf<Long>())
        val subscription = adapter?.itemClicks?.asObservable()?.subscribe( {
            Log.d(TAG, "url2: ${it.url}")
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it.url)))
        })
        Log.d(TAG, "subscription: $subscription")

        recyclerView?.adapter = adapter

        hackerNewsService.getTopStoriesRx()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Log.d(TAG, "count: ${it.count()}")
                    adapter?.storyIds?.clear()
                    adapter?.storyIds?.addAll(it.subList(0, 10))
                    adapter?.notifyDataSetChanged()
                }

    }

    inner class TopStoriesAdapter(val storyIds: MutableList<Long>): RecyclerView.Adapter<TopStoriesViewHolder>() {

        val itemClicks: PublishSubject<Item> = PublishSubject.create()

        override fun getItemCount(): Int {
            return storyIds.count()
        }

        override fun onBindViewHolder(holder: TopStoriesViewHolder?, position: Int) {
            hackerNewsService.getItem(storyIds[position].toString())
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { item ->
                        holder?.title?.text = item.title
                        holder?.itemView?.setOnClickListener {
                            Log.d(TAG, "url: ${item.url}")
                            itemClicks.onNext(item)
                        }
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

