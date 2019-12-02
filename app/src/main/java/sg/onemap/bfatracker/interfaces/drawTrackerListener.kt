package sg.onemap.bfatracker.interfaces

import com.mapbox.mapboxsdk.geometry.LatLng

interface drawTrackerListener {
    fun drawLinesFromTracker(latLng: LatLng)
    fun drawSymbolFromTracker(latLng: LatLng)
}