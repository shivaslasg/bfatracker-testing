package sg.onemap.bfatracker.utilities

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.android.core.location.LocationEngineRequest
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import sg.onemap.bfatracker.MainActivity
import sg.onemap.bfatracker.R

class LocationUtility (var activity: MainActivity, var mapboxMap: MapboxMap,
                       var mListener: LocationListeningCallback.LocationListener ) {
    private val callback = LocationListeningCallback(activity)
    val DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L
    val DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5
    lateinit var locationEngine : LocationEngine
    lateinit var locationRequest:LocationEngineRequest
    var TAG = "LocationUtility"

    init{
        try {
            enableLocationComponent(mapboxMap)
            initLocationEngine()
        } catch (ex:Exception) {
            Log.e(TAG, "Error occurred in LocationUtility init : "+ex.message.toString())
        }
    }


    fun enableLocationComponent(mapboxMap: MapboxMap){
        try {
            // Create and customize the LocationComponent's options
            val customLocationComponentOptions = LocationComponentOptions.builder(activity)
                .trackingGesturesManagement(true)
                .accuracyColor(ContextCompat.getColor(activity, android.R.color.holo_blue_bright))
                .build()

            val locationComponentActivationOptions = LocationComponentActivationOptions.builder(activity, mapboxMap?.style!!)
                .locationComponentOptions(customLocationComponentOptions)
                .build()

            // Get an instance of the LocationComponent and then adjust its settings
            mapboxMap?.locationComponent.apply {

                // Activate the LocationComponent with options
                activateLocationComponent(locationComponentActivationOptions)

                // Enable to make the LocationComponent visible
                isLocationComponentEnabled = true

                // Set the LocationComponent's camera mode
                cameraMode = CameraMode.TRACKING

                // Set the LocationComponent's render mode
                renderMode = RenderMode.COMPASS
            }

            if (isLocationEnabled(activity)) {
                //zoom in
                mapboxMap?.locationComponent.zoomWhileTracking(19.0)
            }
        }catch (ex : Exception){
            Log.e(TAG, "error occurred in enabling location component")
        }
    }

    fun initLocationEngine() {
        try {
            locationEngine = LocationEngineProvider.getBestLocationEngine(activity)
            locationRequest = LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME)
                .build()

        } catch (ex:Exception) {
            Log.e(TAG, "Error occurred in init location engine : "+ex.message.toString())
        }
    }

    fun requestLocationUpdates() {
        try {
            if (!isLocationEnabled(activity)) {
                showAlert("Location Manager", "Unable to get current location.\nPlease enable location for application")
            } else {
                callback.setmListener(mListener)
                locationEngine.requestLocationUpdates(
                    locationRequest,
                    callback,
                    Looper.getMainLooper()
                )
                locationEngine.getLastLocation(callback)
            }

        } catch (ex:Exception) {
            Log.e(TAG, "Error occurred in requestLocationUpdates : "+ex.message.toString())
        }
    }

    fun removeLocationUpdates(){
        try {
            if (mapboxMap.getLocationComponent() != null
                && mapboxMap.getLocationComponent().isLocationComponentActivated()
                && locationEngine != null
                && callback != null
            ) {
                locationEngine.removeLocationUpdates(callback)
                //mapboxMap.getLocationComponent().getLocationEngine()!!.removeLocationUpdates(callback)
            }
        } catch (ex:Exception) {
            Log.e(TAG, "Error occurred in removeLocationUpdates : "+ex.message.toString())
        }
    }


    private fun isLocationEnabled(mContext: Context): Boolean {
        val lm = mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER)
    }

    private fun showAlert(pTitle: String, pMessage: String) {
        val dialog = AlertDialog.Builder(activity)
        dialog.apply {
            setTitle(pTitle)
            setMessage(pMessage)
            setPositiveButton(activity.getString(R.string.location_settings),
                { _, _ ->
                    val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    activity.startActivity(myIntent)
                })
        }.show()
    }


}