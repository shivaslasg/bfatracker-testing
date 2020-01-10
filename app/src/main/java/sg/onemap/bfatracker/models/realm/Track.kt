package sg.onemap.bfatracker.models.realm

import android.os.Parcelable
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import kotlinx.android.parcel.Parcelize

// Your model has to extend RealmObject. Furthermore, the class must be annotated with open (Kotlin classes are final
// by default).
@Parcelize
@RealmClass
open class Track(
    // You can put properties in the constructor as long as all of them are initialized with
    // default values. This ensures that an empty constructor is generated.
    // All properties are by default persisted.
    // Properties can be annotated with PrimaryKey or Index.
    // If you use non-nullable types, properties must be initialized with non-null values.
    @PrimaryKey var id: String = "",

    var name: String = "",

    var startLatitude: Double = 0.0,

    var startLongitude: Double = 0.0,

    var heading: Double = 0.0,

    var startDateTime: String = "",

    var endDateTime: String = "",

    var startTimestamp : Long = 0,

    var endTimestamp : Long = 0,

    var endLatitude: Double = 0.0,

    var endLongitude: Double = 0.0

) : RealmObject(), Parcelable {
    // The Kotlin compiler generates standard getters and setters.
    // Realm will overload them and code inside them is ignored.
    // So if you prefer you can also just have empty abstract methods.
}