package idv.mistasy.hackernewsreader.data

import android.content.Context
import android.util.Log
import dagger.Module
import dagger.Provides
import io.realm.RealmConfiguration
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

/**
 * Created by shaocheng on 9/28/16.
 */
@Module
class DataModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val interceptor = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger { Log.d("DataModule", it) })
        interceptor.level = HttpLoggingInterceptor.Level.BASIC

        return OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build()
    }

    @Provides
    @Singleton
    fun provideHackerNewsService(okHttpClient: OkHttpClient): HackerNewsService {
        val retrofit = Retrofit.Builder()
                .baseUrl("https://hacker-news.firebaseio.com/v0/")
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build()

        return retrofit.create(HackerNewsService::class.java)
    }

    @Provides
    @Singleton
    fun provideDataManager(hackerNewsService: HackerNewsService,
                           realmConfig: RealmConfiguration): DataManager {
        return DataManager(hackerNewsService, realmConfig)
    }

    @Provides
    @Singleton
    fun provideRealmConfig(context: Context): RealmConfiguration {
        return RealmConfiguration.Builder()
            .schemaVersion(0)
            .build()
    }
}
