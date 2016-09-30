package idv.mistasy.hackernewsreader.data

import idv.mistasy.hackernewsreader.data.model.Item
import io.realm.*
import rx.Observable
import rx.Scheduler
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import timber.log.Timber

/**
 * Created by shaocheng on 9/28/16.
 */

class DataManager(val hackerNewsService: HackerNewsService,
                  val realmConfig: RealmConfiguration) {

    var mainRealm: Realm? = null

    var realmChangeListener: RealmChangeListener<RealmResults<Item>>? = null

    fun sync() {
        Timber.i("Sync begins")

        hackerNewsService.getTopStoriesRx()
                .subscribeOn(Schedulers.newThread())
                .flatMapIterable { it -> it }
                .limit(10)
                .flatMap { hackerNewsService.getItem(it.toString()) }
                .subscribe({ item ->
                    transactRealm(realmConfig, {
                        it.copyToRealmOrUpdate(item)
                    })
                }, { error -> Timber.e(error, "Sync fails") })
    }

    fun getItems(): Observable<RealmResults<Item>> {
        Timber.d("getItems() begin")

        checkNotNull(mainRealm)

        val subject = BehaviorSubject.create<RealmResults<Item>>()

        val items = mainRealm?.where(Item::class.java)?.findAll()
        Timber.d("Num of items: %d", items?.count())

        if (items != null) {
            subject.onNext(items)
        }

        realmChangeListener = RealmChangeListener<RealmResults<Item>> {
            Timber.d("Realm Changed")

            if (it != null) {
                subject.onNext(it)
            }
        }
        items?.addChangeListener(realmChangeListener)


        return subject.asObservable()
    }
}

inline fun <T> useRealm(realmConfig: RealmConfiguration, block: (realm: Realm) -> T ): T {
    var realm: Realm? = null
    try {
        realm = Realm.getInstance(realmConfig)

        return block(realm)
    } finally {
        realm?.close()
    }
}

inline fun transactRealm(realmConfig: RealmConfiguration, crossinline block: (realm: Realm) -> Unit ): Unit {
    var realm: Realm? = null
    try {
        realm = Realm.getInstance(realmConfig)

        realm.executeTransaction {
            block(it)
        }
    } finally {
        realm?.close()
    }
}