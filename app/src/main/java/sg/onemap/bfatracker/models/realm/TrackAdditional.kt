package sg.onemap.bfatracker.models.realm

import android.os.Parcelable
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@RealmClass
open class TrackAdditional(
    // You can put properties in the constructor as long as all of them are initialized with
    // default values. This ensures that an empty constructor is generated.
    // All properties are by default persisted.
    // Properties can be annotated with PrimaryKey or Index.
    // If you use non-nullable types, properties must be initialized with non-null values.
    @PrimaryKey var id: String = "",

    var title: String = "",

    var comment: String = "",

//    var image: ByteArray = byteArrayOf(),
    var image: String = "",

    var commentLatitude: Double = 0.0,

    var commentLongitude: Double = 0.0,

    var trackId: String = ""

    //@LinkingObjects("geoDetails")
    //val trackGeoDetail: RealmResults<TrackGeoDetail>? = null

) : RealmObject(), Parcelable {
    // The Kotlin compiler generates standard getters and setters.
    // Realm will overload them and code inside them is ignored.
    // So if you prefer you can also just have empty abstract methods.
}