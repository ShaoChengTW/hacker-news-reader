package idv.mistasy.hackernewsreader

import android.app.Application
import io.realm.Realm
import timber.log.Timber

/**
 * Created by shaocheng on 9/28/16.
 */

class HnrApp: Application() {
    companion object {
        lateinit var appComponent: AppComponent
    }

    override fun onCreate() {
        super.onCreate()

        Realm.init(this)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            // TODO
        }

        appComponent = DaggerAppComponent.builder()
                .appModule(AppModule(this))
                .build()

        appComponent.dataManager().mainRealm = Realm.getInstance(appComponent.realmConfig())
    }
}
