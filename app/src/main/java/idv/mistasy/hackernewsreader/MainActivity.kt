package idv.mistasy.hackernewsreader

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.jetbrains.anko.*
import org.jetbrains.anko.appcompat.v7.toolbar
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val text = findViewById(R.id.text) as TextView
        text.setText("Hello World")

        hackerNewsService.getTopStoriesRx()
                .flatMap { Observable.from(it) }
                .limit(3)
                .flatMap { hackerNewsService.getItem(it.toString()) }
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { Log.d(TAG, "onNext, ${it.title}") }
    }
}

class MainActivityUI : AnkoComponent<MainActivityUI> {
    override fun createView(ui: AnkoContext<MainActivityUI>) = with(ui) {
        verticalLayout {
            toolbar {
                background = context.attrAsColorDrawable(R.attr.colorPrimary)
                elevation = dip(4).toFloat()
                theme =
            }.lparams(width = matchParent, height = context.attrAsDimen(R.attr.actionBarSize))
        }
    }

}

fun Context.attribute(value : Int) : TypedValue {
    var ret = TypedValue()
    theme.resolveAttribute(value, ret, true)
    return ret
}

fun Context.attrAsDimen(value : Int) : Int{
    return TypedValue.complexToDimensionPixelSize(attribute(value).data, getResources().getDisplayMetrics())
}

fun Context.attrAsColorDrawable(value: Int) = ColorDrawable(attribute(value).data)
