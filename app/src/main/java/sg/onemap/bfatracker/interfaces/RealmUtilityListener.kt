package sg.onemap.bfatracker.interfaces

import com.mapbox.mapboxsdk.geometry.LatLng

interface RealmUtilityListener {
    fun addedRealmTrackRecord(primarykey: String, title: String)
}