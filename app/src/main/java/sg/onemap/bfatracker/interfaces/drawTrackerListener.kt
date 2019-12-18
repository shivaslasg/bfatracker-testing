package sg.onemap.bfatracker.interfaces

import com.mapbox.mapboxsdk.geometry.LatLng

interface DrawTrackerListener {
    fun drawLinesFromTracker(latLng: LatLng)
    fun drawSymbolFromTracker(latLng: LatLng)
    fun addTrackRecord(latLng: LatLng, bearing:Double)
}