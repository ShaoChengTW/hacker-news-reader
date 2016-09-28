package idv.mistasy.hackernewsreader

import android.app.Application

/**
 * Created by shaocheng on 9/28/16.
 */

class HnrApp: Application() {
    companion object {
        lateinit var appComponent: AppComponent
    }

    override fun onCreate() {
        super.onCreate()

        appComponent = DaggerAppComponent.builder()
                .appModule(AppModule(this))
                .build()
    }
}
