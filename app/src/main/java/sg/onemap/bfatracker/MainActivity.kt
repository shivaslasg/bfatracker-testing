package sg.onemap.bfatracker

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.drawable.Drawable
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.widget.AdapterView
import android.widget.AutoCompleteTextView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.isVisible
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import kotlinx.android.synthetic.main.add_comment_form.view.*
import sg.onemap.bfatracker.adapters.SearchDataAdapter
import sg.onemap.bfatracker.interfaces.WebUtilityListener
import sg.onemap.bfatracker.interfaces.drawTrackerListener
import sg.onemap.bfatracker.models.*
import sg.onemap.bfatracker.utilities.*

class MainActivity : AppCompatActivity(), OnMapReadyCallback,
    WebUtilityListener, PermissionsListener, LocationListeningCallback.LocationListener, drawTrackerListener {

    var mapView: MapView? = null
    var mapboxMap: MapboxMap? = null

    var mapUtility: MapUtility? = null
    var webUtility: WebUtility? = null
    var utility : Utility? = null
    var locationUtility : LocationUtility? = null
    lateinit var motionDnaController: MotionDNAController

    var autoCompleteTextView : AutoCompleteTextView? = null
    var themeSymbolBtn : FloatingActionButton? = null
    var themePolylineBtn : FloatingActionButton? = null
    var themePolygonBtn : FloatingActionButton? = null
    var currentLocationBtn : FloatingActionButton? = null

    var remoteControlUIPanel : LinearLayout? = null
    var fixLocationBtn : FloatingActionButton? = null
    var uploadBtn : FloatingActionButton? = null
    var controlBtn : FloatingActionButton? = null
    var stopTrackBtn : FloatingActionButton? = null
    var pauseTrackBtn : FloatingActionButton? = null
    var recordTrackBtn : FloatingActionButton? = null

    var addCommentBtn : FloatingActionButton? = null
    var intervalBtn : FloatingActionButton? = null

    var latestChosenLocation : LatLng? = null

    private var permissionsManager: PermissionsManager = PermissionsManager(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
        setContentView(R.layout.activity_main)

        mapView = findViewById(R.id.mapView)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(this)

        utility = Utility(this@MainActivity)
        webUtility = WebUtility(
            this@MainActivity,
            this@MainActivity
        )

        autoCompleteTextView = findViewById(R.id.autoCompleteTextView)
        autoCompleteTextView?.addTextChangedListener(editor)

        autoCompleteTextView?.onItemClickListener = AdapterView.OnItemClickListener {
                parent,_view,position,_id->
            var addressItem: Address = parent.getItemAtPosition(position) as Address
            autoCompleteTextView?.setText(addressItem.BUILDING, false)
            autoCompleteTextView?.setSelection(addressItem.BUILDING?.length!!);
            mapUtility?.drawSearchMarker((addressItem))
            mapUtility?.zoomCameraToLocation(addressItem.LATITUDE?.toDouble()!!, addressItem.LONGITUDE?.toDouble()!!, 17.0)
            latestChosenLocation = LatLng(addressItem.LATITUDE?.toDouble()!!, addressItem.LONGITUDE?.toDouble()!!)

            //undo the GPS option
            if(currentLocationBtn!!.isSelected){
                currentLocationBtn!!.isSelected = false
            }
        }

        if(utility != null){
            if(utility!!?.checkTokenExpiryDate()){
                webUtility?.fetchToken()
            }
        }

        /*themeSymbolBtn = findViewById(R.id.themeSymbolBtn)
        themeSymbolBtn?.setOnClickListener{
            webUtility?.fetchThemeSymbolQuery()
        }

        themePolylineBtn = findViewById(R.id.themePolylineBtn)
        themePolylineBtn?.setOnClickListener{
            webUtility?.fetchThemePolylineQuery()
        }

        themePolygonBtn = findViewById(R.id.themePolygonBtn)
        themePolygonBtn?.setOnClickListener{
            webUtility?.fetchThemePolygonQuery()
        }*/

        remoteControlUIPanel = findViewById(R.id.remoteControlUIPanel)
        currentLocationBtn = findViewById(R.id.currentLocationBtn)
        currentLocationBtn?.setOnClickListener{
            //make sure recording is not in progress
            if(motionDnaController.getCurrentStateForMotionDNA() == motionDnaController.RECORDING_MOTIONDNA
                || motionDnaController.getCurrentStateForMotionDNA() == motionDnaController.PAUSE_MOTIONDNA) {
                val dialog = AlertDialog.Builder(this)
                dialog.apply {
                    setTitle("Ongoing Recording")
                    setMessage("Recording is in progress. Unable to set GPS location.")
                    setPositiveButton("Okay", { dialog,which -> dialog.dismiss()})
                }.show()
            } else {
                if(currentLocationBtn!!.isSelected){
                    locationUtility?.removeLocationUpdates()
                    currentLocationBtn!!.isSelected = false
                    mapUtility?.clearMapOfMarker()
                } else {
                    locationUtility?.requestLocationUpdates()
                }
            }
        }

        fixLocationBtn = findViewById(R.id.fixLocationBtn)

        fixLocationBtn?.setOnClickListener{
            if(fixLocationBtn!!.isSelected) {
                motionDnaController.fixMotionDnaLocation(latestChosenLocation!!)
                mapUtility?.drawMapPinSymbolForMotionDNA(latestChosenLocation!!)
            } else {
                if (latestChosenLocation != null) {
                    motionDnaController.fixMotionDnaLocation(latestChosenLocation!!)
                    fixLocationBtn!!.isSelected = true
                    controlBtn?.isEnabled = true
                    controlBtn?.drawable?.alpha = 225
                } else {
                    val dialog = AlertDialog.Builder(this)
                    dialog.apply {
                        setTitle("Location is not fixed")
                        setMessage("Kindly fix your location by either searching, GPS or reverse geocoding")
                        setPositiveButton("Okay", { dialog,which -> dialog.dismiss()})
                    }.show()
                }
            }
        }

        uploadBtn = findViewById(R.id.uploadBtn)
        controlBtn = findViewById(R.id.controlBtn)
        controlBtn?.isEnabled = false
        controlBtn?.drawable?.alpha = 125
        controlBtn?.setOnClickListener{
            if(controlBtn!!.isSelected) {
                //check whether there is a recording going on before stopping recording and closing
                if(motionDnaController.getCurrentStateForMotionDNA() == motionDnaController.RECORDING_MOTIONDNA
                    || motionDnaController.getCurrentStateForMotionDNA() == motionDnaController.PAUSE_MOTIONDNA) {
                    val dialog = AlertDialog.Builder(this)
                    dialog.apply {
                        setTitle("Ongoing Recording")
                        setMessage("Recording is in progress. Please stop recording in order to minimise control panel.")
                        setPositiveButton("Continue Recording", { dialog,which -> dialog.dismiss()})
                        setNegativeButton("Stop Recording", { _,_->
                            motionDnaController.stopMotionDna()
                            recordTrackBtn?.isSelected = false
                            remoteControlUIPanel!!.isVisible = false
                        })
                    }.show()
                } else {
                    remoteControlUIPanel!!.isVisible = false
                    controlBtn!!.isSelected = false
                    //reset all sub items' states
                    recordTrackBtn!!.isSelected = false
                    pauseTrackBtn!!.isSelected = false
                }
            } else {
                remoteControlUIPanel!!.isVisible = true
                controlBtn!!.isSelected = true
            }
        }

        recordTrackBtn?.setOnClickListener{
            if(recordTrackBtn!!.isSelected){

            } else {
                //make sure there is a current location selected
                if(latestChosenLocation!! !=null) {
                    motionDnaController.startMotionDna()
                    recordTrackBtn?.isSelected = true
                } else {
                    val dialog = AlertDialog.Builder(this)
                    dialog.apply {
                        setTitle("Location is not fixed")
                        setMessage("Kindly fix your location by either searching, GPS or reverse geocoding")
                        setPositiveButton("Okay", { dialog,which -> dialog.dismiss()})
                    }.show()
                }
            }
        }

        stopTrackBtn = findViewById(R.id.stopTrackBtn)
        stopTrackBtn?.setOnClickListener{
            if(motionDnaController.getCurrentStateForMotionDNA() == motionDnaController.PAUSE_MOTIONDNA) {
                val dialog = AlertDialog.Builder(this)
                dialog.apply {
                    setTitle("Ongoing Recording On Pause Mode")
                    setMessage("Recording is on pause.")
                    setPositiveButton("Continue pausing", { dialog,which -> dialog.dismiss()})
                    setNegativeButton("Stop Recording", { _,_->
                        motionDnaController.stopMotionDna()
                        recordTrackBtn?.isSelected = false
                        remoteControlUIPanel!!.isVisible = false
                    })
                }.show()
            } else {
                motionDnaController.stopMotionDna()
                recordTrackBtn?.isSelected = false
                //change back fix location
                fixLocationBtn!!.isSelected = false
            }
        }

        pauseTrackBtn = findViewById(R.id.pauseTrackBtn)
        pauseTrackBtn?.setOnClickListener{

            if(pauseTrackBtn!!.isSelected){
                motionDnaController.resumeMotionDna()
                pauseTrackBtn?.isSelected = false
            } else {
                motionDnaController.pauseMotionDna()
                pauseTrackBtn?.isSelected = true
            }
        }

        recordTrackBtn = findViewById(R.id.recordTrackBtn)
        recordTrackBtn?.setOnClickListener{
            motionDnaController.startMotionDna()
            recordTrackBtn?.isSelected = true
        }

        addCommentBtn = findViewById(R.id.addCommentBtn)
        addCommentBtn?.setOnClickListener{
            if(motionDnaController.getCurrentStateForMotionDNA() == motionDnaController.RECORDING_MOTIONDNA
            || motionDnaController.getCurrentStateForMotionDNA() == motionDnaController.PAUSE_MOTIONDNA) {
                val commentForm = LayoutInflater.from(this).inflate(R.layout.add_comment_form, null)
                val mBuilder = AlertDialog.Builder(this)
                    .setView(commentForm)
                    .setTitle("Comment Form")
                val mAlertDialog = mBuilder.show()
                commentForm.addCommentBtn.setOnClickListener{
                    mAlertDialog.dismiss()
                }
            } else {
                val dialog = AlertDialog.Builder(this)
                dialog.apply {
                    setTitle("Add Comment")
                    setMessage("Recording needs to be ongoing in order to add comments.")
                    setPositiveButton("Okay", { dialog,which -> dialog.dismiss()})
                }.show()
            }
        }



        intervalBtn = findViewById(R.id.intervalBtn)
        intervalBtn?.setOnClickListener{
            val builder = AlertDialog.Builder(this)
            with(builder)
            {
                setTitle("Intervals")
                setItems(motionDnaController.intervalItems) { dialog, which ->
                    motionDnaController.setInterval(motionDnaController.intervalItems[which])
                    Toast.makeText(applicationContext, motionDnaController.intervalItems[which] + " seconds set for interval", Toast.LENGTH_SHORT).show()
                    var chosenDrawable:Drawable? = null
                    when(which){
                        0 -> chosenDrawable=ResourcesCompat.getDrawable(this@MainActivity.resources,
                            R.drawable.icons_stopwatch_05s, this@MainActivity.getTheme())!!
                        1 -> chosenDrawable=ResourcesCompat.getDrawable(this@MainActivity.resources,
                            R.drawable.icons_stopwatch_1s, this@MainActivity.getTheme())!!
                        2 -> chosenDrawable=ResourcesCompat.getDrawable(this@MainActivity.resources,
                            R.drawable.icons_stopwatch_2s, this@MainActivity.getTheme())!!
                    }

                    intervalBtn?.setImageBitmap(chosenDrawable?.toBitmap())
                }
                setPositiveButton("OK", { dialog,which -> dialog.dismiss()})
                show()
            }
        }
    }



    private val editor = object : TextWatcher {
        var newMicro: Long = 0

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val text = s.toString().replace(".", "")
                .replace(",", "")
                .replace("$", "")

            try {
                newMicro = java.lang.Long.parseLong(text)
            } catch (e: Exception) {
                newMicro = 0
            }
        }

        override fun afterTextChanged(searchText: Editable?) {
            if(searchText?.length!! > 0) {
                webUtility?.searchData(searchText.toString())
            }
        }
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        mapboxMap.setStyle(Style.Builder().fromUri(
            "https://maps-json.onemap.sg/Default.json")) {

            mapUtility = MapUtility(
                mapView!!,
                mapboxMap,
                this@MainActivity
            )

            mapboxMap.addOnMapLongClickListener { point ->
                latestChosenLocation = LatLng(point.latitude, point.longitude)
                webUtility?.fetchRevgecode(point)
                true
            }

            enableLocationComponent(it)

            // Instantiating MotionDnaDataSource, passing in context, packagemanager and Navisens devkey
            motionDnaController = MotionDNAController(this@MainActivity,
                                                        packageManager,
                                                        getString(R.string.motiondnaid),
                                                this@MainActivity)
        }
    }

    override fun returnResultsForAutocomplete(addressResponse: AddressResponse) {
        try {
            var results = addressResponse.results!!
            if(results != null){
                var adapter : SearchDataAdapter? = null
                if(autoCompleteTextView?.adapter != null) {
                    adapter = autoCompleteTextView?.adapter as SearchDataAdapter
                    adapter.updateDataList(results)
                } else {
                    adapter = SearchDataAdapter(
                        this@MainActivity,
                        results
                    )
                    autoCompleteTextView?.setAdapter(adapter)
                }

                if(adapter != null)
                    adapter?.notifyDataSetChanged()
            }
        } catch (ex:Exception){
            Log.e("error", "Error occurred in returnResultsForAutocomplete : "+ex.message.toString())
        }
    }

    override fun returnResultsForRevgeocode(revgeocode: Revgeocode) {
        if(revgeocode != null){
            mapUtility?.drawRevgeoMarker(revgeocode)
        }
    }

    override fun returnResultsForThemeSymbol(themeSymbolResponse: ThemeSymbolResponse) {
        try {
            if(themeSymbolResponse != null
                && themeSymbolResponse.SrchResults?.size!! > 0) {
                mapUtility?.clearAllMappings()
                mapUtility?.drawThemeSymbolLayer(themeSymbolResponse)
                mapUtility?.zoomCameraToLocation(Utility.SharedPrefConstants.FIXED_LATITUDE, Utility.SharedPrefConstants.FIXED_LONGITUDE, 12.0)
            }
        } catch (ex:Exception){
            Log.e("error", "Error occurred in returnResultsForThemeSymbol : "+ex.message.toString())
        }
    }

    override fun returnResultsForThemePolyline(themPolylineResponse: ThemePolylineResponse) {
        try {
            if(themPolylineResponse != null
                && themPolylineResponse.SrchResults?.size!! > 0) {
                mapUtility?.clearAllMappings()
                mapUtility?.drawThemePolylineLayer(themPolylineResponse)
                mapUtility?.zoomCameraToLocation(Utility.SharedPrefConstants.FIXED_LATITUDE, Utility.SharedPrefConstants.FIXED_LONGITUDE, 12.0)
            }
        } catch (ex:Exception){
            Log.e("error", "Error occurred in returnResultsForThemePolyline : "+ex.message.toString())
        }
    }

    override fun returnResultsForThemePolygon(themePolygonResponse: ThemePolygonResponse) {
        try {
            if(themePolygonResponse != null
                && themePolygonResponse.SrchResults?.size!! > 0) {
                mapUtility?.clearAllMappings()
                mapUtility?.drawThemePolygonLayer(themePolygonResponse)
                mapUtility?.zoomCameraToLocation(Utility.SharedPrefConstants.FIXED_LATITUDE, Utility.SharedPrefConstants.FIXED_LONGITUDE, 10.0)
            }
        } catch (ex:Exception){
            Log.e("error", "Error occurred in returnResultsForThemePolygon : "+ex.message.toString())
        }
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapUtility?.mapManagersOnDestroy()
        locationUtility?.removeLocationUpdates()
        mapView?.onDestroy()
    }

    //region location permissions
    @SuppressLint("MissingPermission")
    private fun enableLocationComponent(loadedMapStyle: Style) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            locationUtility = LocationUtility(this@MainActivity, mapboxMap!!, this@MainActivity)
        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager.requestLocationPermissions(this)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onExplanationNeeded(permissionsToExplain: List<String>) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            enableLocationComponent(mapboxMap?.style!!)
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onObtainingLocation(latestLocation: Location) {
        //mapUtility?.addMarkerToMap("","", latestLocation.latitude, latestLocation.longitude)
        //turn on the location
        if(!currentLocationBtn!!.isSelected){
            currentLocationBtn!!.isSelected = true
        }
        latestChosenLocation = LatLng(latestLocation.latitude, latestLocation.longitude)
        mapUtility?.addMarkerToMap("","", latestLocation.latitude, latestLocation.longitude)
        mapUtility?.zoomCameraToLocation(latestLocation.latitude, latestLocation.longitude, 19.0)
        locationUtility?.removeLocationUpdates()
    }

    override fun drawLinesFromTracker(latLng: LatLng) {
        try {
            mapUtility?.drawPolyLineForMotionDNA(latLng)
          //  Toast.makeText(this, "drawLinesFromTracker at lat: "+latLng.latitude+", longitude: "+latLng.longitude, Toast.LENGTH_SHORT).show()
        }catch(ex:Exception){
            Log.e("error", "Error occurred in drawLinesFromTracker : "+ex.message.toString())
        }
    }

    override fun drawSymbolFromTracker(latLng: LatLng) {
        try {
            mapUtility?.drawSymbolForMotionDNA(latLng)
           // Toast.makeText(this, "drawSymbolFromTracker at lat: "+latLng.latitude+", longitude: "+latLng.longitude, Toast.LENGTH_SHORT).show()
        }catch(ex:Exception){
            Log.e("error", "Error occurred in drawSymbolFromTracker : "+ex.message.toString())
        }
    }

    //endregion
}
