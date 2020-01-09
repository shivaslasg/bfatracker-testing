package sg.onemap.bfatracker.controllers

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.mapbox.mapboxsdk.geometry.LatLng
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import io.realm.kotlin.where
import org.joda.time.format.DateTimeFormat
import sg.onemap.bfatracker.MainActivity
import sg.onemap.bfatracker.interfaces.RealmUtilityListener
import sg.onemap.bfatracker.models.realm.Track
import sg.onemap.bfatracker.models.realm.TrackAdditional
import sg.onemap.bfatracker.models.realm.TrackGeoDetail
import sg.onemap.bfatracker.models.realm.TrackReCalibrate
import java.util.*

class RealmController(var context: Context,
                      var mListener: RealmUtilityListener
) {

    var TAG : String = "RealmController"
    private lateinit var realm: Realm

    object TrackTaskConstants {
        const val TRACK_DRAW = "TRACK_DRAW"
        const val TRACK_DELETE = "TRACK_DELETE"
        const val TRACK_UPLOAD = "TRACK_UPLOAD"
        const val TRACK_DOWNLOAD = "TRACK_DOWNLOAD"
    }

    init{
        try {
            initRealm()
        } catch (ex:Exception) {
            Log.e(TAG, "Error occurred in RealmController init : "+ex.message.toString())
        }
    }

    fun initRealm() {
        // Open the realm for the UI thread.
        realm = Realm.getDefaultInstance()
       // deleteAll()
    }

    fun deleteAll() {
        realm.executeTransaction { realm ->
            realm.deleteAll()
        }
    }

    fun addNewTrack(latitude : Double, longitude: Double, bearing : Double) {
        // All writes must be wrapped in a transaction to facilitate safe multi threading
        var primarykey: String = ""
        var title: String = ""

        realm.executeTransactionAsync({ bgRealm ->
            primarykey = UUID.randomUUID().toString()
            val tracker: Track = Track()
            //tracker.id = primarykey

            var noofEntries = bgRealm.where<Track>().count()
            tracker.name = "Testing"+noofEntries
            title = tracker.name
            tracker.id = primarykey
            tracker.startLatitude = latitude
            tracker.startLongitude = longitude
            tracker.bearing = bearing

            val fmt: org.joda.time.format.DateTimeFormatter = DateTimeFormat.forPattern("dd-MM-yyyy hh:mm:ss a")

            val currentDateTime = org.joda.time.LocalDateTime.now().toDateTime()
            tracker.startDateTime = currentDateTime.toString(fmt)
            tracker.startTimestamp = currentDateTime.millis

            bgRealm.insert(tracker)
            //Toast.makeText(activity, "noofEntries :"+noofEntries, Toast.LENGTH_SHORT).show()
        }, {
            // Transaction was a success.
            Toast.makeText(context, "addNewTrack Transaction was a success", Toast.LENGTH_SHORT).show()
            mListener.addedRealmTrackRecord(primarykey, title)
        }) {
            // Transaction failed and was automatically canceled.
            Toast.makeText(context, "addNewTrack Transaction was cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    fun addTrackRecalibrate(trackId: String?, latLng: LatLng, bearing: Double){
        var primarykey: String = ""

        realm.executeTransactionAsync({ bgRealm ->
            primarykey = UUID.randomUUID().toString()
            val trackReCalibrate = TrackReCalibrate()
            trackReCalibrate.id = primarykey
            trackReCalibrate.recalibrateLatitude = latLng.latitude
            trackReCalibrate.recalibrateLongitude = latLng.longitude
            trackReCalibrate.recalibrateBearing = bearing
            trackReCalibrate.trackId = trackId.toString()


            bgRealm.insert(trackReCalibrate)
        }, {
            // Transaction was a success.
            Toast.makeText(context, "addTrackRecalibrate Transaction was a success", Toast.LENGTH_SHORT).show()
            //mListener.addedRealmTrackGeoDetailRecord(primarykey)
        }) {
            // Transaction failed and was automatically canceled.
            Toast.makeText(context, "addTrackRecalibrate Transaction was cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    fun addGeoTrackDetail(trackId: String?, latitude : Double, longitude: Double) {
        // All writes must be wrapped in a transaction to facilitate safe multi threading
        var primarykey: String = ""

        realm.executeTransactionAsync({ bgRealm ->
            primarykey = UUID.randomUUID().toString()
            val trackGeoDetail = TrackGeoDetail()
            trackGeoDetail.id = primarykey
            trackGeoDetail.latitude = latitude
            trackGeoDetail.longitude = longitude
            trackGeoDetail.trackId = trackId.toString()

            val fmt: org.joda.time.format.DateTimeFormatter = DateTimeFormat.forPattern("dd-MM-yyyy hh:mm:ss a")
            val currentTime = org.joda.time.LocalDateTime.now().toDateTime().toString(fmt)
            trackGeoDetail.datetime = currentTime


            val maxId = bgRealm.where<TrackGeoDetail>().equalTo("trackId", trackId).max("order")
            val nextId : Long =
                when(maxId) {
                    null -> { 1 }
                    else -> { maxId.toLong() + 1 }
                }
            trackGeoDetail.order = nextId

            bgRealm.insert(trackGeoDetail)
        }, {
            // Transaction was a success.
            //Toast.makeText(context, "addGeoTrackDetail Transaction was a success", Toast.LENGTH_SHORT).show()
            //mListener.addedRealmTrackGeoDetailRecord(primarykey)
        }) {
            // Transaction failed and was automatically canceled.
            Toast.makeText(context, "addGeoTrackDetail Transaction was cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    fun updateTrackEndTiming(trackId: String) {
        realm.executeTransactionAsync({ bgRealm ->
            var trackObj : Track? = bgRealm.where<Track>().equalTo("id", trackId).findFirst()

            val fmt: org.joda.time.format.DateTimeFormatter = DateTimeFormat.forPattern("dd-MM-yyyy hh:mm:ss a")

            val currentDateTime = org.joda.time.LocalDateTime.now().toDateTime()
            trackObj?.endDateTime = currentDateTime.toString(fmt)
            trackObj?.endTimestamp = currentDateTime.millis

            bgRealm.insertOrUpdate(trackObj)
        }, {
            // Transaction was a success.
            Toast.makeText(context, "updateTrackEndTiming Transaction was a success", Toast.LENGTH_SHORT).show()
            //mListener.addedRealmTrackGeoDetailRecord(primarykey)
        }) {
            // Transaction failed and was automatically canceled.
            Toast.makeText(context, "updateTrackEndTiming Transaction was cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    fun getTrack(trackId: String) : Track? {

        /*realm.executeTransactionAsync({ bgRealm ->
            var testingtracker: Track? = bgRealm.where<Track>().equalTo("id", trackId).findFirst()
            var startlat = testingtracker?.startLatitude
        }, {
            // Transaction was a success.
            Toast.makeText(context, "updateTrackEndTiming Transaction was a success", Toast.LENGTH_SHORT).show()
            //mListener.addedRealmTrackGeoDetailRecord(primarykey)
        }) {
            // Transaction failed and was automatically canceled.
            Toast.makeText(context, "updateTrackEndTiming Transaction was cancelled", Toast.LENGTH_SHORT).show()
        }*/

        return realm.where(Track::class.java).equalTo("id", trackId).findFirst()
        //return realm.where<Track>().equalTo("id", trackId).findAll()
    }

    fun getAllTrackGeoDetails(trackId : String) : RealmResults<TrackGeoDetail> {
        return realm.where<TrackGeoDetail>().equalTo("trackId", trackId).findAll()
    }

    fun getAllTrackAdditional(trackId : String) : RealmResults<TrackAdditional> {
        return realm.where<TrackAdditional>().equalTo("trackId", trackId).findAll()
    }

    fun getAllTrackReCalibrate(trackId : String) : RealmResults<TrackReCalibrate> {
        return realm.where<TrackReCalibrate>().equalTo("trackId", trackId).findAll()
    }

    fun deleteTrack(trackId : String) {
        realm.executeTransactionAsync({ bgRealm ->
            var results:RealmResults<TrackGeoDetail> = bgRealm.where<TrackGeoDetail>().equalTo("trackId", trackId).findAll()
            var deletedTrackGeoDetail:Boolean = results.deleteAllFromRealm()
            var track  = bgRealm.where<Track>().equalTo("id", trackId).findFirst()
            var deletedTrack = track?.deleteFromRealm()
        }, {
            // Transaction was a success.
            Toast.makeText(context, "Deleting track is a success", Toast.LENGTH_SHORT).show()
            //mListener.addedRealmTrackGeoDetailRecord(primarykey)
        }) {
            // Transaction failed and was automatically canceled.
            Toast.makeText(context, "deleteTrack Transaction was cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    fun getAllTracks() : RealmResults<Track> {

        //return realm.where(Track::class.java).findAll()
        var results: RealmResults<Track>? = null
        try {
            var pass:Long? = 0
            results = realm.where(Track::class.java).notEqualTo("endTimestamp", pass).findAll().sort("startTimestamp", Sort.DESCENDING)
        } catch (ex:Exception) {
            Toast.makeText(context, "getAllTracks Transaction caught exception", Toast.LENGTH_SHORT).show()
        }
        return results!!
    }

    fun addTrackAdditional(trackAdditional: TrackAdditional) {
        // All writes must be wrapped in a transaction to facilitate safe multi threading
        var primarykey: String = ""

        realm.executeTransactionAsync({ bgRealm ->
            primarykey = UUID.randomUUID().toString()
            trackAdditional.id = primarykey

            bgRealm.insert(trackAdditional)
        }, {
            // Transaction was a success.
            Toast.makeText(context, "addTrackAdditional Transaction was a success", Toast.LENGTH_SHORT).show()
            //mListener.addedRealmTrackGeoDetailRecord(primarykey)
        }) {
            // Transaction failed and was automatically canceled.
            Toast.makeText(context, "addTrackAdditional Transaction was cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    fun updateTrackTitle(trackId: String, trackTitle: String) {
        realm.executeTransactionAsync({ bgRealm ->
            var track = bgRealm.where<Track>().equalTo("id", trackId).findFirst()
            track?.name = trackTitle
            bgRealm.insertOrUpdate(track)
        }, {
            // Transaction was a success.
            Toast.makeText(context, "updateTrackTitle Transaction was a success", Toast.LENGTH_SHORT).show()
        }) {
            // Transaction failed and was automatically canceled.
            Toast.makeText(context, "updateTrackTitle Transaction was cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    fun updateDestinationLatLng(destinationLatLng: LatLng, trackId: String){
        realm.executeTransactionAsync({ bgRealm ->
            var trackObj : Track? = bgRealm.where<Track>().equalTo("id", trackId).findFirst()

            trackObj?.endLatitude = destinationLatLng.latitude
            trackObj?.endLongitude = destinationLatLng.longitude

            bgRealm.insertOrUpdate(trackObj)
        }, {
            // Transaction was a success.
            Toast.makeText(context, "updateDestinationLatLng Transaction was a success", Toast.LENGTH_SHORT).show()
            //mListener.addedRealmTrackGeoDetailRecord(primarykey)
        }) {
            // Transaction failed and was automatically canceled.
            Toast.makeText(context, "updateDestinationLatLng Transaction was cancelled", Toast.LENGTH_SHORT).show()
        }
    }

}