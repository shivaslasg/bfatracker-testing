package sg.onemap.bfatracker.models.realm

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.RealmResults
import io.realm.annotations.LinkingObjects
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import io.realm.annotations.Required

@RealmClass
open class TrackGeoDetail(
    // You can put properties in the constructor as long as all of them are initialized with
    // default values. This ensures that an empty constructor is generated.
    // All properties are by default persisted.
    // Properties can be annotated with PrimaryKey or Index.
    // If you use non-nullable types, properties must be initialized with non-null values.
    @PrimaryKey var id: String = "",

    var latitude: Double = 0.0,

    var longitude: Double = 0.0,

    var datetime: String = "",

    var trackId: String = "",

    var order: Long = 0

//    @LinkingObjects("geoDetails")
//    var trackId: RealmResults<Track>? = null

) : RealmObject() {
    // The Kotlin compiler generates standard getters and setters.
    // Realm will overload them and code inside them is ignored.
    // So if you prefer you can also just have empty abstract methods.
}