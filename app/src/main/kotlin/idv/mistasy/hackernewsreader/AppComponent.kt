package idv.mistasy.hackernewsreader

import dagger.Component
import idv.mistasy.hackernewsreader.data.DataModule
import idv.mistasy.hackernewsreader.ui.ItemActivity
import idv.mistasy.hackernewsreader.ui.MainActivity
import javax.inject.Singleton

/**
 * Created by shaocheng on 9/28/16.
 */
@Singleton
@Component(modules = arrayOf(AppModule::class, DataModule::class))
interface AppComponent {
    fun inject(mainActivity: MainActivity)

    fun inject(mainActivity: ItemActivity)
}
