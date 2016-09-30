package idv.mistasy.hackernewsreader.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import idv.mistasy.hackernewsreader.HnrApp
import idv.mistasy.hackernewsreader.R
import idv.mistasy.hackernewsreader.data.DataManager
import idv.mistasy.hackernewsreader.data.HackerNewsService
import idv.mistasy.hackernewsreader.data.model.Item
import io.realm.RealmList
import io.realm.RealmResults
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subjects.PublishSubject
import timber.log.Timber
import javax.inject.Inject

class MainActivity: Activity() {
    private val TAG: String = "MainActivity"

    @Inject
    lateinit var dataManager: DataManager

    @Inject
    lateinit var hackerNewsService: HackerNewsService

    private var adapter: TopStoriesAdapter? = null
    private var recyclerView: RecyclerView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        HnrApp.appComponent.inject(this)

        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recycler_view) as RecyclerView?
        recyclerView?.layoutManager = LinearLayoutManager(this)

        adapter = TopStoriesAdapter(null)
        val subscription = adapter?.itemClicks?.asObservable()?.subscribe( {
            Log.d(TAG, "url2: ${it.url}")
            val intent = Intent(this, ItemActivity::class.java)
            intent.putExtra(ItemActivity.INTENT_EXTRA_URL, it.url)
            startActivity(intent)
        })

        recyclerView?.adapter = adapter

        Timber.d("sync")
        dataManager.sync()

        dataManager.getItems()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Log.d(TAG, "count: ${it.count()}")
                    adapter?.stories = it
                    adapter?.notifyDataSetChanged()
                }

    }

    inner class TopStoriesAdapter(var stories: RealmResults<Item>?): RecyclerView.Adapter<TopStoriesViewHolder>() {

        val itemClicks: PublishSubject<Item> = PublishSubject.create()

        override fun getItemCount(): Int {
            return stories?.count() ?: 0
        }

        override fun onBindViewHolder(holder: TopStoriesViewHolder?, position: Int) {
            val item = stories?.get(position) ?: return

            holder?.title?.text = item.title
            holder?.itemView?.setOnClickListener {
                Log.d(TAG, "url: ${item.url}")
                itemClicks.onNext(item)
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

