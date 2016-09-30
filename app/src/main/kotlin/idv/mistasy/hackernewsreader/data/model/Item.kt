package idv.mistasy.hackernewsreader.data.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Item: RealmObject() {

    @PrimaryKey
    open var id: Long? = 0

    open var title: String? = null

    open var text: String? = null

    open var url: String? = null
}
