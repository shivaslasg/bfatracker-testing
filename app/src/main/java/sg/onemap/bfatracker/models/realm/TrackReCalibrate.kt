package sg.onemap.bfatracker.models.realm

import android.os.Parcelable
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@RealmClass
open class TrackReCalibrate (
    @PrimaryKey
    var id: String = "",

    var recalibrateLatitude: Double = 0.0,

    var recalibrateLongitude: Double = 0.0,

    var recalibrateBearing: Double = 0.0,

    var trackId: String = ""
) : RealmObject(), Parcelable {
    // The Kotlin compiler generates standard getters and setters.
    // Realm will overload them and code inside them is ignored.
    // So if you prefer you can also just have empty abstract methods.
}
