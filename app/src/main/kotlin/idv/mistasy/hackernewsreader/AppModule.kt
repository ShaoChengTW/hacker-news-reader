package idv.mistasy.hackernewsreader

import android.content.Context
import dagger.Module
import dagger.Provides
import idv.mistasy.hackernewsreader.HnrApp
import javax.inject.Singleton

/**
 * Created by shaocheng on 9/28/16.
 */

@Module
class AppModule(val app: HnrApp) {

    @Provides
    @Singleton
    fun provideContext(): Context {
        return app
    }

    @Provides
    @Singleton
    fun provideApplication(): HnrApp {
        return app
    }
}

