package sg.onemap.bfatracker.controllers

import android.util.Log
import android.widget.Toast
import io.realm.Realm
import io.realm.RealmResults
import io.realm.kotlin.where
import org.joda.time.format.DateTimeFormat
import sg.onemap.bfatracker.MainActivity
import sg.onemap.bfatracker.interfaces.RealmUtilityListener
import sg.onemap.bfatracker.models.realm.Track
import sg.onemap.bfatracker.models.realm.TrackGeoDetail
import java.util.*

class RealmController(var activity: MainActivity,
                      var mListener: RealmUtilityListener
) {

    var TAG : String = "RealmController"
    private lateinit var realm: Realm

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

        realm.executeTransactionAsync({ bgRealm ->
            primarykey = UUID.randomUUID().toString()
            val tracker: Track = Track()
            //tracker.id = primarykey

            var noofEntries = bgRealm.where<Track>().count()
            tracker.name = "Testing"+noofEntries
            tracker.id = primarykey
            tracker.startLatitude = latitude
            tracker.startLongitude = longitude
            tracker.bearing = bearing

            val fmt: org.joda.time.format.DateTimeFormatter = DateTimeFormat.forPattern("dd-MM-yyyy hh:mm:ss a")
            val currentTime = org.joda.time.LocalDateTime.now().toDateTime().toString(fmt)
            tracker.startDateTime = currentTime

            bgRealm.insert(tracker)
            //Toast.makeText(activity, "noofEntries :"+noofEntries, Toast.LENGTH_SHORT).show()

        }, {
            // Transaction was a success.
            Toast.makeText(activity, "addNewTrack Transaction was a success", Toast.LENGTH_SHORT).show()
            mListener.addedRealmTrackRecord(primarykey)
        }) {
            // Transaction failed and was automatically canceled.
            Toast.makeText(activity, "addNewTrack Transaction was cancelled", Toast.LENGTH_SHORT).show()
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
            //Toast.makeText(activity, "addGeoTrackDetail Transaction was a success", Toast.LENGTH_SHORT).show()
            //mListener.addedRealmTrackGeoDetailRecord(primarykey)
        }) {
            // Transaction failed and was automatically canceled.
            Toast.makeText(activity, "addGeoTrackDetail Transaction was cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    fun updateTrackEndTiming(trackId: String) {
        realm.executeTransactionAsync({ bgRealm ->
            var trackObj : Track? = bgRealm.where<Track>().equalTo("id", trackId).findFirst()

            val fmt: org.joda.time.format.DateTimeFormatter = DateTimeFormat.forPattern("dd-MM-yyyy hh:mm:ss a")
            val currentTime = org.joda.time.LocalDateTime.now().toDateTime().toString(fmt)
            trackObj?.endDateTime = currentTime
            bgRealm.insertOrUpdate(trackObj)
        }, {
            // Transaction was a success.
            Toast.makeText(activity, "updateTrackEndTiming Transaction was a success", Toast.LENGTH_SHORT).show()
            //mListener.addedRealmTrackGeoDetailRecord(primarykey)
        }) {
            // Transaction failed and was automatically canceled.
            Toast.makeText(activity, "updateTrackEndTiming Transaction was cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    fun getAllTrackGeoDetails(trackId : String) : RealmResults<TrackGeoDetail> {
        return realm.where(TrackGeoDetail::class.java).findAll()
    }

}