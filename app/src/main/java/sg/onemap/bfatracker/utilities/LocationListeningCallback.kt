package sg.onemap.bfatracker.utilities

import android.location.Location
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineResult
import sg.onemap.bfatracker.MainActivity
import java.lang.ref.WeakReference

class LocationListeningCallback internal constructor(activity: MainActivity) :
    LocationEngineCallback<LocationEngineResult> {

    private val activityWeakReference: WeakReference<MainActivity>
    private lateinit var mListener:LocationListener;

    init {this.activityWeakReference = WeakReference(activity)}

    override fun onSuccess(result: LocationEngineResult) {

        // The LocationEngineCallback interface's method which fires when the device's location has changed.
        var currentLocation : Location = result.getLastLocation()!!
        if(currentLocation != null){
            if(mListener != null){
                mListener.onObtainingLocation(currentLocation)
            }
        }
    }

    /**
     * The LocationEngineCallback interface's method which fires when the device's location can not be captured
     *
     * @param exception the exception message
     */
    override fun onFailure(exception: Exception) {
        // The LocationEngineCallback interface's method which fires when the device's location can not be captured
    }

    fun setmListener(mListener: LocationListener) {
        this.mListener = mListener
    }

    interface LocationListener {
        fun onObtainingLocation(latestLocation: Location)
    }
}
