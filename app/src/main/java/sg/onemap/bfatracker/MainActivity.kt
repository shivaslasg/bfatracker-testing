package sg.onemap.bfatracker

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.Drawable
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import org.json.JSONArray
import org.json.JSONObject
import sg.onemap.bfatracker.adapters.SearchDataAdapter
import sg.onemap.bfatracker.controllers.MotionDNAController
import sg.onemap.bfatracker.controllers.RealmController
import sg.onemap.bfatracker.fragments.CommentFormFragment
import sg.onemap.bfatracker.fragments.HeadingFormFragment
import sg.onemap.bfatracker.fragments.ViewCommentFragment
import sg.onemap.bfatracker.interfaces.*
import sg.onemap.bfatracker.models.*
import sg.onemap.bfatracker.models.realm.Track
import sg.onemap.bfatracker.models.realm.TrackAdditional
import sg.onemap.bfatracker.models.realm.TrackGeoDetail
import sg.onemap.bfatracker.models.realm.TrackReCalibrate
import sg.onemap.bfatracker.utilities.*
import java.io.File

class MainActivity : AppCompatActivity(), OnMapReadyCallback,
    WebUtilityListener, PermissionsListener, LocationListeningCallback.LocationListener, DrawTrackerListener,
    RealmUtilityListener, CommentFormListener, ViewCommentListener, HeadingFormListener {

    var mapView: MapView? = null
    var mapboxMap: MapboxMap? = null

    var mapUtility: MapUtility? = null
    var webUtility: WebUtility? = null
    var utility : Utility? = null
    var logUtility: LogFileUtility? = null
    var locationUtility : LocationUtility? = null
    lateinit var motionDnaController: MotionDNAController
    lateinit var realmController : RealmController

    var autoCompleteTextView : AutoCompleteTextView? = null
    var themeSymbolBtn : FloatingActionButton? = null
    var themePolylineBtn : FloatingActionButton? = null
    var themePolygonBtn : FloatingActionButton? = null
    var currentLocationBtn : FloatingActionButton? = null

    var remoteControlUIPanel : LinearLayout? = null
    //var fixLocationBtn : FloatingActionButton? = null
    var reportBtn : FloatingActionButton? = null
    var controlBtn : FloatingActionButton? = null
    var stopTrackBtn : FloatingActionButton? = null
    var pauseTrackBtn : FloatingActionButton? = null
    var recordTrackBtn : FloatingActionButton? = null

    var addCommentBtn : FloatingActionButton? = null
    var intervalBtn : FloatingActionButton? = null

    var commentContainer : FrameLayout? =null

    var latestChosenLocation : LatLng? = null //passed for fixing location
    var latestHeading : Float? = null

    private var permissionsManager: PermissionsManager = PermissionsManager(this)

    var REQUEST_CODE : Int = 101
    var IMAGE_REQUEST_CODE : String = "102"
    var IMAGE_REQUEST_CODE_INT : Int = 3030

    var currentLatLng : LatLng? = null

    val commentFormFragmentTag: String = "commentFormFragmentTag"
    val viewCommentFragmentTag: String = "viewCommentFragmentTag"
    val headingFormFragmentTag: String = "heaingFormFragmentTag"

    var fixLocationHeadingBtn : FloatingActionButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
        setContentView(R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        mapView = findViewById(R.id.mapView)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(this)

        utility = Utility(this@MainActivity)
        webUtility = WebUtility(
            this@MainActivity,
            this@MainActivity
        )
        logUtility = LogFileUtility(this)

        autoCompleteTextView = findViewById(R.id.autoCompleteTextView)
        autoCompleteTextView?.addTextChangedListener(editor)

        autoCompleteTextView?.onItemClickListener = AdapterView.OnItemClickListener {
                parent,_view,position,_id->
            val addressItem: Address = parent.getItemAtPosition(position) as Address
            autoCompleteTextView?.setText(addressItem.BUILDING, false)
            autoCompleteTextView?.setSelection(addressItem.BUILDING?.length!!)
            /*if(fixLocationBtn!!.isSelected) {
                var readjustLatLng = LatLng(addressItem.LATITUDE?.toDouble()!!,addressItem.LONGITUDE?.toDouble()!!)
                mapUtility?.drawMapUnPinSymbolForMotionDNA(readjustLatLng!!)
                latestHeading = 0.0f
            } else {
                mapUtility?.drawSearchMarker((addressItem))
            }*/

            if(fixLocationHeadingBtn!!.isSelected) {
                var readjustLatLng = LatLng(addressItem.LATITUDE?.toDouble()!!,addressItem.LONGITUDE?.toDouble()!!)
                mapUtility?.drawMapUnPinSymbolForMotionDNA(readjustLatLng!!)
                latestHeading = 0.0f
            } else {
                mapUtility?.drawSearchMarker((addressItem))
            }

            mapUtility?.zoomCameraToLocation(addressItem.LATITUDE?.toDouble()!!, addressItem.LONGITUDE?.toDouble()!!, 17.0)
            latestChosenLocation = LatLng(addressItem.LATITUDE?.toDouble()!!, addressItem.LONGITUDE?.toDouble()!!)

            //undo the GPS option
            if(currentLocationBtn!!.isSelected){
                currentLocationBtn!!.isSelected = false
                latestHeading = 0.0f
            }
        }

        if(utility != null){
            if(utility!!.checkTokenExpiryDate()){
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
            /*if(motionDnaController.getCurrentStateForMotionDNA() == motionDnaController.RECORDING_MOTIONDNA
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
                    latestChosenLocation = null
                } else {
                    locationUtility?.requestLocationUpdates()
                }
            }*/
            if(currentLocationBtn!!.isSelected){
                locationUtility?.removeLocationUpdates()
                currentLocationBtn!!.isSelected = false
                if(motionDnaController.getCurrentStateForMotionDNA() != motionDnaController.RECORDING_MOTIONDNA
                    && motionDnaController.getCurrentStateForMotionDNA() != motionDnaController.PAUSE_MOTIONDNA) {
                    mapUtility?.clearMapOfMarker()
                }
                latestChosenLocation = null
            } else {
                locationUtility?.requestLocationUpdates()
            }
        }

        //fixLocationBtn = findViewById(R.id.fixLocationBtn)

        /*fixLocationBtn?.setOnClickListener{
            if(fixLocationBtn!!.isSelected) {
                motionDnaController.fixMotionDnaLocation(latestChosenLocation!!)
                val trackId :String? = utility?.sharedPref?.getString(Utility.SharedPrefConstants.TRACK_PRIMARYKEY,"")
                mapUtility?.drawMapPinSymbolForMotionDNA(latestChosenLocation!!)
                /*if(latestHeading != null && latestHeading!!.toDouble() > 0.0) {
                    motionDnaController.fixMotionDnaLocationWithHeading(latestChosenLocation!!, latestHeading!!)
                } else {
                    latestHeading = 0.0f
                    motionDnaController.fixMotionDnaLocation(latestChosenLocation!!)
                }*/
                realmController?.addTrackRecalibrate(trackId, latestChosenLocation!!, latestHeading!!.toDouble())
            } else {

                mapUtility?.clearAllMappings()
                if (latestChosenLocation != null) {
                    motionDnaController.fixMotionDnaLocation(latestChosenLocation!!)
                    /*if(latestHeading != null) {
                        motionDnaController.fixMotionDnaLocationWithHeading(latestChosenLocation!!, latestHeading!!)
                    } else {
                        motionDnaController.fixMotionDnaLocation(latestChosenLocation!!)
                    }*/
                    remoteControlUIPanel!!.isVisible = true
                    controlBtn!!.isSelected = true
                    fixLocationHeadingBtn?.isVisible = false
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

         */

        fixLocationHeadingBtn = findViewById(R.id.fixLocationHeadingBtn)
        fixLocationHeadingBtn?.setOnClickListener{
            if(fixLocationHeadingBtn!!.isSelected) {
                mapUtility?.drawMapPinSymbolForMotionDNA(latestChosenLocation!!)
                showHeadingFormFragment()
            } else {

                mapUtility?.clearAllMappings()
                if (latestChosenLocation != null) {
                    //motionDnaController.fixMotionDnaLocation(latestChosenLocation!!)
                    if(latestHeading == null) {
                        showHeadingFormFragment()
                    }
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

        reportBtn = findViewById(R.id.reportBtn)
        reportBtn?.setOnClickListener{
            if(motionDnaController.getCurrentStateForMotionDNA() == motionDnaController.RECORDING_MOTIONDNA
                || motionDnaController.getCurrentStateForMotionDNA() == motionDnaController.PAUSE_MOTIONDNA) {
                val dialog = AlertDialog.Builder(this)
                dialog.apply {
                    setTitle("Ongoing Recording")
                    setMessage("Recording is in progress. Please stop recording in order to view reports.")
                    setPositiveButton("Continue Recording", { dialog,which -> dialog.dismiss()})
                    setNegativeButton("Stop Recording", { _,_->
                        stopRecording()
                    })
                }.show()
            } else {
                val intent = Intent(this, RealmListActivity::class.java)
                intent.putExtra("SHOW_WELCOME", true)
                startActivityForResult(intent, REQUEST_CODE)
            }
        }

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
                            stopRecording()
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

        recordTrackBtn = findViewById(R.id.recordTrackBtn)
        recordTrackBtn?.setOnClickListener{
            if(recordTrackBtn!!.isSelected){

            } else {
                //make sure there is a current location selected
                if(latestChosenLocation!! !=null) {
                    if(latestHeading == null)
                        latestHeading = 0.0f

                    realmController.addNewTrack(latestChosenLocation!!.latitude, latestChosenLocation!!.longitude, latestHeading!!.toDouble())
                    motionDnaController.startMotionDna()
                    recordTrackBtn?.isSelected = true
                    //fixLocationBtn?.setImageResource(R.drawable.icons_map_pin)
                    fixLocationHeadingBtn?.setImageResource(R.drawable.icons_map_pin)
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
                        stopRecording()
                    })
                }.show()
            } else
            {
                stopRecording()
            }
        }

        pauseTrackBtn = findViewById(R.id.pauseTrackBtn)
        pauseTrackBtn?.setOnClickListener{

            if(motionDnaController.getCurrentStateForMotionDNA() == motionDnaController.RECORDING_MOTIONDNA
                || motionDnaController.getCurrentStateForMotionDNA() == motionDnaController.PAUSE_MOTIONDNA) {
                if(pauseTrackBtn!!.isSelected){
                    motionDnaController.resumeMotionDna()
                    pauseTrackBtn?.isSelected = false
                } else {
                    motionDnaController.pauseMotionDna()
                    pauseTrackBtn?.isSelected = true
                }
            } else {
                val dialog = AlertDialog.Builder(this)
                dialog.apply {
                    setTitle("Start recording to pause/resume")
                    setMessage("Kindly start recording in order to pasue or resume")
                    setPositiveButton("Cancel", { dialog,which -> dialog.dismiss()})
                    setNegativeButton("Start Recording", { _,_->
                        recordTrackBtn?.performClick()
                    })
                }.show()
            }
        }

        commentContainer = findViewById(R.id.comment_container)
        addCommentBtn = findViewById(R.id.addCommentBtn)
        addCommentBtn?.setOnClickListener{
            if(motionDnaController.getCurrentStateForMotionDNA() == motionDnaController.RECORDING_MOTIONDNA
            || motionDnaController.getCurrentStateForMotionDNA() == motionDnaController.PAUSE_MOTIONDNA) {

                val trackId :String? = utility?.sharedPref?.getString(Utility.SharedPrefConstants.TRACK_PRIMARYKEY,"")
                // Create an instance of ExampleFragment
                val commentFormFragment = CommentFormFragment(trackId!!, currentLatLng!!, this)

                supportFragmentManager.beginTransaction()
                    .add(R.id.comment_container, commentFormFragment, commentFormFragmentTag).commit()
                commentContainer?.isVisible = true

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

        realmController = RealmController(this, this)
    }

    fun showHeadingFormFragment() {
        locationUtility?.removeLocationUpdates()
        if(currentLocationBtn!!.isSelected)
            currentLocationBtn!!.isSelected = false

        val headFormFragment = HeadingFormFragment(this)

        supportFragmentManager.beginTransaction()
            .add(R.id.comment_container, headFormFragment, headingFormFragmentTag).commit()
        commentContainer?.isVisible = true
    }

    fun fixMotionDnaLocationWithHeading() {
        if(latestHeading != null && latestHeading!! > 0.0f) {
            motionDnaController.fixMotionDnaLocationWithHeading(latestChosenLocation!!, latestHeading!!)
        } else {
            Toast.makeText(this, "Latest heading is 0.0", Toast.LENGTH_SHORT).show()
            motionDnaController.fixMotionDnaLocation(latestChosenLocation!!)
        }
        remoteControlUIPanel!!.isVisible = true
        controlBtn!!.isSelected = true
        //fixLocationBtn?.isVisible = false
    }


    fun stopRecording() {
            motionDnaController.stopMotionDna()
            //fixLocationBtn?.setImageResource(R.drawable.icons_location_calibrate)
            fixLocationHeadingBtn?.setImageResource(R.drawable.icons_location_calibrate_heading)

            val trackId :String? = utility?.sharedPref?.getString(Utility.SharedPrefConstants.TRACK_PRIMARYKEY,"")
            val trackTitle :String? = utility?.sharedPref?.getString(Utility.SharedPrefConstants.TRACK_TITLE,"")
            realmController.updateTrackEndTiming(trackId.toString())

            if(currentLatLng != null){
                realmController.updateDestinationLatLng(currentLatLng!!, trackId.toString())
            }

            var editText = EditText(this)
            editText.setText(trackTitle)
            val builder = AlertDialog.Builder(this)
            // Set the dialog title
            builder.setTitle("Save Title")
                .setMessage("Change title of recording")
                .setView(editText)
                // Set the action buttons
                .setPositiveButton("Save",
                    DialogInterface.OnClickListener { dialog, id ->
                        // User clicked OK, so save the selectedItems results somewhere
                        // or return them to the component that opened the dialog
                        realmController.updateTrackTitle(trackId!!, editText.text.toString())
                    })
                .setNegativeButton("Cancel",
                    DialogInterface.OnClickListener { dialog, id ->
                        dialog.dismiss()
                    })

            builder.create()
            builder.show()

            recordTrackBtn?.isSelected = false
            //fixLocationBtn!!.isSelected = false
            fixLocationHeadingBtn!!.isSelected = false
            pauseTrackBtn!!.isSelected = false

            //fixLocationBtn!!.visibility = View.VISIBLE
            fixLocationHeadingBtn!!.visibility = View.VISIBLE

            remoteControlUIPanel!!.isVisible = false
            controlBtn!!.isSelected = false
            //remove all drawings
            mapUtility?.clearAllMappings()
            latestHeading = null
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
                this@MainActivity,
                this@MainActivity
            )

            mapboxMap.addOnMapLongClickListener { point ->
                latestChosenLocation = LatLng(point.latitude, point.longitude)
                webUtility?.fetchRevgecode(point)
                true
            }

            enableLocationComponent(it)

            // Instantiating MotionDnaDataSource, passing in context, packagemanager and Navisens devkey
            motionDnaController =
                MotionDNAController(
                    this@MainActivity,
                    packageManager,
                    getString(R.string.motiondnaid),
                    this@MainActivity
                )
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

    override fun returnResultsForRevgeocode(revgeocode: Revgeocode, originalPoint: LatLng) {
        if(revgeocode != null){
            //latestHeading = 0.0f
            /*if(fixLocationBtn!!.isSelected) {
                mapUtility?.drawMapUnPinSymbolForMotionDNA(originalPoint)
            } else{
                mapUtility?.drawRevgeoMarker(revgeocode, originalPoint)
            }*/

            //if(fixLocationHeadingBtn!!.isSelected || fixLocationBtn!!.isSelected) {
            if(fixLocationHeadingBtn!!.isSelected) {
                mapUtility?.drawMapUnPinSymbolForMotionDNA(originalPoint)
            } else{
                mapUtility?.drawRevgeoMarker(revgeocode, originalPoint)
            }
        }
    }

    override fun returnResultsForThemeSymbol(themeSymbolResponse: ThemeSymbolResponse) {
        /*
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
        */
    }

    override fun returnResultsForThemePolyline(themPolylineResponse: ThemePolylineResponse) {
        /*
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
        */
    }

    override fun returnResultsForThemePolygon(themePolygonResponse: ThemePolygonResponse) {
        /*
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
        */
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

        //var headingInDegrees : Float? = locationUtility?.getHeadings()
        //latestHeading = headingInDegrees
        latestHeading = null

        //check whether this is to recalibrate
        //if(fixLocationHeadingBtn!!.isSelected || fixLocationBtn!!.isSelected) {
        if(fixLocationHeadingBtn!!.isSelected) {
            mapUtility?.drawMapUnPinSymbolForMotionDNA(LatLng(latestLocation.latitude, latestLocation.longitude))
        } else {
            mapUtility?.addMarkerToMap("","", latestLocation.latitude, latestLocation.longitude)
            mapUtility?.zoomCameraToLocation(latestLocation.latitude, latestLocation.longitude, 19.0)
        }


        //Toast.makeText(this, "Headings in degree: "+headingInDegrees, Toast.LENGTH_SHORT).show()
        //locationUtility?.removeLocationUpdates()
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
            currentLatLng =latLng

            mapUtility?.drawSymbolForMotionDNA(latLng)

            val trackId :String? = utility?.sharedPref?.getString(Utility.SharedPrefConstants.TRACK_PRIMARYKEY,"")

            realmController?.addGeoTrackDetail(trackId, latLng.latitude, latLng.longitude)
           // Toast.makeText(this, "drawSymbolFromTracker at lat: "+latLng.latitude+", longitude: "+latLng.longitude, Toast.LENGTH_SHORT).show()
        }catch(ex:Exception){
            Log.e("error", "Error occurred in drawSymbolFromTracker : "+ex.message.toString())
        }
    }

    override fun addTrackRecord(latLng: LatLng, heading: Double) {
        //DO NOT ADD UNTIL IT STARTS RECORDING
        //realmController.addNewTrack(latLng.latitude, latLng.longitude, heading)
    }

    override fun addedRealmTrackRecord(primarykey: String, trackTitle: String) {
        //store primary key
        utility?.save(Utility.SharedPrefConstants.TRACK_PRIMARYKEY, primarykey)
        utility?.save(Utility.SharedPrefConstants.TRACK_TITLE, trackTitle)
        //fixLocationBtn!!.isSelected = true
        fixLocationHeadingBtn!!.isSelected = true
        controlBtn?.isEnabled = true
        controlBtn?.drawable?.alpha = 225
    }


    //endregion

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == 3000) {
            var bundle:Bundle = data?.extras!!
            var track:Track? = bundle?.getParcelable("track")
            var taskId = bundle?.getString("taskId")
            if(taskId.equals(RealmController.TrackTaskConstants.TRACK_DRAW)){
                redrawResults(track)
            } else if(taskId.equals(RealmController.TrackTaskConstants.TRACK_DOWNLOAD)) {
                downloadGeoJson(track)
            } else if(taskId.equals(RealmController.TrackTaskConstants.TRACK_UPLOAD)) {
                //downloadGeoJson(track)
            }
//            var trackId:String = bundle?.getString("trackId")!!
//            var taskId = bundle?.getString("taskId")
//            redrawResults(trackId)
        } else {
            var commentFormFragment : Fragment? = supportFragmentManager.findFragmentByTag(commentFormFragmentTag)
            commentFormFragment?.onActivityResult(requestCode, resultCode, data)
        }
    }

    fun downloadGeoJson(track: Track?){
        var trackId = track?.id
        var results = realmController.getAllTrackGeoDetails(trackId!!)

        var trackObject = JSONObject()
        trackObject.put("id", trackId)
        trackObject.put("startLatitude", track?.startLatitude)
        trackObject.put("startLongitude", track?.startLongitude)
        trackObject.put("startDateTime", track?.startDateTime)
        trackObject.put("endDateTime", track?.endDateTime)
        trackObject.put("heading", track?.heading)
        trackObject.put("endLatitude", track?.endLatitude)
        trackObject.put("endLongitude", track?.endLongitude)

        var trackGeoDetailList = JSONArray()

        for(target:TrackGeoDetail in results){
            var trackGeoDetailObject = JSONObject()
            trackGeoDetailObject.put("id", target.id)
            trackGeoDetailObject.put("trackId", target.trackId)
            trackGeoDetailObject.put("latitude", target.latitude)
            trackGeoDetailObject.put("longitude", target.longitude)
            trackGeoDetailObject.put("datetime", target.datetime)
            trackGeoDetailObject.put("order", target.order)
            trackGeoDetailList.put(trackGeoDetailObject)
        }

        trackObject.put("Tracks", trackGeoDetailList)

        var reCalibrateTrackList = JSONArray()

        var reCalibrateResults = realmController.getAllTrackReCalibrate(trackId!!)
        for(recalibrate: TrackReCalibrate in reCalibrateResults){
            var reCalibrateTrackObject = JSONObject()
            reCalibrateTrackObject.put("id", recalibrate.id)
            reCalibrateTrackObject.put("trackId", recalibrate.trackId)
            reCalibrateTrackObject.put("recalibrateLatitude", recalibrate.recalibrateLatitude)
            reCalibrateTrackObject.put("recalibrateLongitude", recalibrate.recalibrateLongitude)
            reCalibrateTrackObject.put("recalibrateHeading", recalibrate.recalibrateHeading)
            reCalibrateTrackList.put(reCalibrateTrackObject)
        }

        //trackObject.put("RecalibrateTracks", reCalibrateTrackList)

        var stringtosee: String = trackObject.toString()
        Toast.makeText(this, stringtosee, Toast.LENGTH_LONG).show()

        var filename: File? = logUtility?.createFolderFile(track?.id)
        logUtility?.setWriteToFile(stringtosee)
        //downloadFile(filename)

        // File file = chosenFile.getAbsoluteFile();
// Uri uri = Uri.fromFile(file);
        val intent = Intent(Intent.ACTION_VIEW)
        //intent.setDataAndType(uri, "text/plain");
        //intent.setDataAndType(uri, "text/plain");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        val apkURI = FileProvider.getUriForFile(
            this@MainActivity,
            this@MainActivity.applicationContext.packageName + ".provider", filename!!
        )
        intent.setDataAndType(apkURI, "text/plain")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        this@MainActivity.startActivity(intent)
    }

    fun downloadFile(DownloadUrl : String?) {
        var request1: DownloadManager.Request  = DownloadManager.Request(Uri.parse(DownloadUrl))
        request1.setDescription("Download")   //appears the same in Notification bar while downloading
        request1.setTitle("Downloading...")
        //request1.setVisibleInDownloadsUi(false)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            request1.allowScanningByMediaScanner()
            request1.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
        }
        request1.setDestinationInExternalFilesDir(getApplicationContext(), "/File", "Question1.mp3")

        var manager1  = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        //var downloadManager:DownloadManager= (DownloadManager) getSystemService(DOWNLOAD_SERVICE)
        manager1.enqueue(request1)
        //Objects.requireNonNull(manager1).enqueue(request1);
        if (DownloadManager.STATUS_SUCCESSFUL == 8) {
        //DownloadSuccess();
        }
    }

    fun redrawResults(track: Track?) {

        mapUtility?.clearAllMappings()
        mapUtility?.clearMapOfMarker()

        var trackId = track?.id
        mapUtility?.drawSource(LatLng(track?.startLatitude!!, track?.startLongitude!!))
        mapUtility?.drawDestination(LatLng(track?.endLatitude!!, track?.endLongitude!!))
        //var trackResults = realmController.getAllTracks()
        /*for(eachTrack:Track in trackResults){
            if(eachTrack.id.equals(trackId)){
                mapUtility?.drawSource(LatLng(eachTrack.startLatitude, eachTrack.startLongitude))
                mapUtility?.drawDestination(LatLng(eachTrack.endLatitude, eachTrack.endLongitude))
                break;
            }
        }*/

        var results = realmController.getAllTrackGeoDetails(trackId!!)
        var resultsAdditional = realmController.getAllTrackAdditional(trackId!!)

        latestChosenLocation = null
        for(target:TrackGeoDetail in results){
            var lat:Double = target.latitude
            var lng:Double = target.longitude
            var passLatLng = LatLng(lat, lng)
            mapUtility?.drawSymbolForMotionDNA(passLatLng)
        }
        mapUtility?.zoomToTrackMap(100,100,100,100)
        for(trackAdditional:TrackAdditional in resultsAdditional){
            mapUtility?.drawTrackAdditional(trackAdditional, true)
        }
        var resultsReCalibrate = realmController.getAllTrackReCalibrate(trackId)
        for(trackReCalibrate: TrackReCalibrate in resultsReCalibrate){
            mapUtility?.drawMapPinSymbolForMotionDNA(LatLng(trackReCalibrate.recalibrateLatitude, trackReCalibrate.recalibrateLongitude))
        }
    }

    override fun addTrackAdditional(trackAdditional: TrackAdditional) {
        try {
            realmController?.addTrackAdditional(trackAdditional)
            var latlng:LatLng = LatLng(trackAdditional.commentLatitude, trackAdditional.commentLongitude)
            mapUtility?.drawTrackAdditional(trackAdditional, false)
            var commentFormFragment : Fragment? = supportFragmentManager.findFragmentByTag(commentFormFragmentTag)
            supportFragmentManager.beginTransaction().remove(commentFormFragment!!).commit()

            // Toast.makeText(this, "drawSymbolFromTracker at lat: "+latLng.latitude+", longitude: "+latLng.longitude, Toast.LENGTH_SHORT).show()
        }catch(ex:Exception){
            Log.e("error", "Error occurred in addTrackAdditional : "+ex.message.toString())
        }
    }

    override fun showComment(title: String, message: String, imageLocation: String) {
        val viewCommentFragment = ViewCommentFragment(title, message, imageLocation)
        supportFragmentManager.beginTransaction()
            .add(sg.onemap.bfatracker.R.id.comment_container, viewCommentFragment, viewCommentFragmentTag).commit()
        commentContainer?.isVisible = true
    }

    override fun updateHeading(heading: Float) {
        latestHeading = heading


        if(motionDnaController.getCurrentStateForMotionDNA() != motionDnaController.RECORDING_MOTIONDNA
            && motionDnaController.getCurrentStateForMotionDNA() != motionDnaController.PAUSE_MOTIONDNA) {
            fixMotionDnaLocationWithHeading()
        } else {
            /*if(latestHeading != null && latestHeading!!.toDouble() > 0.0) {
                motionDnaController.fixMotionDnaLocationWithHeading(latestChosenLocation!!, latestHeading!!)
            } else {
                latestHeading = 0.0f
                Toast.makeText(this, "Latest heading is 0.0", Toast.LENGTH_SHORT).show()
                motionDnaController.fixMotionDnaLocation(latestChosenLocation!!)
            }*/

            if(latestHeading != null) {
                motionDnaController.fixMotionDnaLocationWithHeading(latestChosenLocation!!, latestHeading!!)
            }
            val trackId :String? = utility?.sharedPref?.getString(Utility.SharedPrefConstants.TRACK_PRIMARYKEY,"")
            realmController?.addTrackRecalibrate(trackId, latestChosenLocation!!, latestHeading!!.toDouble())
        }

        var commentFormFragment : Fragment? = supportFragmentManager.findFragmentByTag(headingFormFragmentTag)
        supportFragmentManager.beginTransaction().remove(commentFormFragment!!).commit()
    }


}
