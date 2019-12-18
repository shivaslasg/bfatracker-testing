package sg.onemap.bfatracker.models.realm

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import io.realm.annotations.Required
import org.joda.time.LocalDateTime
import java.util.*

// Your model has to extend RealmObject. Furthermore, the class must be annotated with open (Kotlin classes are final
// by default).
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

    var bearing: Double = 0.0,

    var startDateTime: String = "",

    var endDateTime: String = "",

    // One-to-many relations is simply a RealmList of the objects which also subclass RealmObject
    var geoDetails: RealmList<TrackGeoDetail> = RealmList()

) : RealmObject() {
    // The Kotlin compiler generates standard getters and setters.
    // Realm will overload them and code inside them is ignored.
    // So if you prefer you can also just have empty abstract methods.
}