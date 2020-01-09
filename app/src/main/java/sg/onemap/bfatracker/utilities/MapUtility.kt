package sg.onemap.bfatracker.utilities

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.Toast
import androidx.collection.LongSparseArray
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.exceptions.InvalidLatLngBoundsException
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.plugins.annotation.*
import org.json.JSONException
import org.json.JSONObject
import sg.onemap.bfatracker.interfaces.ViewCommentListener
import sg.onemap.bfatracker.models.*
import sg.onemap.bfatracker.models.realm.TrackAdditional
import sg.onemap.bfatracker.R


class MapUtility (var mapView: MapView,
                  var mapboxMap: MapboxMap,
                  var context: Context,
                  var mListener: ViewCommentListener) { //, activity: Activity
    /*
    lateinit var mapboxMap: MapboxMap
    lateinit var mapView: MapView
    var activity: Activity? = null
    */

    var THEME_SYMBOL = "theme-symbol"
    var lineManager : LineManager? = null
    var fillManager : FillManager? = null

    var TRACKER_SYMBOL = "tracker-symbol"
    var MAP_PIN_SYMBOL = "map-pin-symbol"
    var MAP_UNPIN_SYMBOL = "map-unpin-symbol"
    var TRACKER_ADDITIONAL_SYMBOL = "tracker-additional-symbol"

    var SOURCE_SYMBOL = "source-symbol"
    var DESTINATION_SYMBOL = "destination-symbol"

    var symbolManager : SymbolManager? = null
    var mapPinSymbolManager : SymbolManager? = null
    var mapUnPinSymbolManager : SymbolManager? = null
    var trackAdditionalSymbolManager : SymbolManager? = null
    var sourceManager : SymbolManager? =null
    var destinationManager : SymbolManager? =null

    init{
        try {
            /*
            this.mapboxMap = mapboxMap
            this.mapView = mapView
            this.activity = activity
            */

            //Disable mapbox logo
            mapboxMap.uiSettings.isAttributionEnabled = false
            mapboxMap.uiSettings.isLogoEnabled = false

            //Position of compass in case map rotates with user gestures
            mapboxMap.uiSettings.setCompassMargins(0, 280, 30, 0)

            //Zoom to current location (PSA Building)
            // latitude : 1.273750
            // longitude : 103.801514
            zoomCameraToLocation(1.273750, 103.801514, 17.0)

            var hawker_drawable: Drawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.mk_thm_hawkercentre, context.getTheme())!!
            var hawker_bitmap = hawker_drawable.toBitmap()

            mapboxMap.style?.addImage(THEME_SYMBOL,  hawker_bitmap)

            var tracker_drawable: Drawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_circular_shape_silhouette, context.getTheme())!!
            var tracker_bitmap = tracker_drawable.toBitmap()

            mapboxMap.style?.addImage(TRACKER_SYMBOL,  tracker_bitmap)


            var map_pin_drawable: Drawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.icons_map_pin, context.getTheme())!!
            var map_pin_bitmap = map_pin_drawable.toBitmap()

            mapboxMap.style?.addImage(MAP_PIN_SYMBOL,  map_pin_bitmap)

            var comment_drawable: Drawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.icons_comment, context.getTheme())!!
            var comment_bitmap = comment_drawable.toBitmap()

            mapboxMap.style?.addImage(TRACKER_ADDITIONAL_SYMBOL,  comment_bitmap)

            var map_unpin_drawable: Drawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.icons_map_unpin, context.getTheme())!!
            var map_unpin_bitmap = map_unpin_drawable.toBitmap()

            mapboxMap.style?.addImage(MAP_UNPIN_SYMBOL,  map_unpin_bitmap)

            var source_drawable: Drawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.icons_source, context.getTheme())!!
            var source_bitmap = source_drawable.toBitmap()

            mapboxMap.style?.addImage(SOURCE_SYMBOL,  source_bitmap)

            var destination_drawable: Drawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.icons_destination, context.getTheme())!!
            var destination_bitmap = destination_drawable.toBitmap()

            mapboxMap.style?.addImage(DESTINATION_SYMBOL,  destination_bitmap)

            symbolManager = SymbolManager(mapView!!, mapboxMap, mapboxMap.style!!)
            symbolManager?.iconAllowOverlap = true
            symbolManager?.textAllowOverlap = true
            lineManager = LineManager(mapView!!, mapboxMap, mapboxMap.style!!)
            fillManager = FillManager(mapView!!, mapboxMap, mapboxMap.style!!)


            trackAdditionalSymbolManager = SymbolManager(mapView!!, mapboxMap, mapboxMap.style!!)
            trackAdditionalSymbolManager?.iconAllowOverlap = true
            trackAdditionalSymbolManager?.textAllowOverlap = true

            mapPinSymbolManager = SymbolManager(mapView!!, mapboxMap, mapboxMap.style!!)
            mapPinSymbolManager?.iconAllowOverlap = true
            mapPinSymbolManager?.textAllowOverlap = true

            mapUnPinSymbolManager = SymbolManager(mapView!!, mapboxMap, mapboxMap.style!!)
            mapUnPinSymbolManager?.iconAllowOverlap = true
            mapUnPinSymbolManager?.textAllowOverlap = true

            sourceManager = SymbolManager(mapView!!, mapboxMap, mapboxMap.style!!)
            sourceManager?.iconAllowOverlap = true
            sourceManager?.textAllowOverlap = true

            destinationManager = SymbolManager(mapView!!, mapboxMap, mapboxMap.style!!)
            destinationManager?.iconAllowOverlap = true
            destinationManager?.textAllowOverlap = true

        } catch (ex:Exception) {
            Log.e("error", "Error occurred in maputility init : "+ex.message.toString())
        }
    }

    fun drawSearchMarker(address: Address){
        try{
            var buildingName : String = address.BUILDING!!
            var addressDetail :String = address.ADDRESS!!
            var lat: Double = address.LATITUDE!!.toDouble()
            var lng: Double = address.LONGITUDE!!.toDouble()
            addMarkerToMap(buildingName, addressDetail, lat, lng)
        } catch(ex:Exception){
            Log.e("error", "Error occurred in drawSearchMarker : "+ex.message.toString())
        }
    }

    fun addMarkerToMap(buildingName: String, addressDetail: String, lat: Double, lng: Double){
        mapboxMap.clear()
        var markerOptions: MarkerOptions = MarkerOptions()
        markerOptions.title = buildingName
        markerOptions.snippet = addressDetail
        markerOptions.position = LatLng(lat,lng)
        mapboxMap.addMarker(markerOptions)
    }

    fun clearMapOfMarker(){
        mapboxMap.clear()
    }

    /**
     * Zooms map camera to given latitude, longitude at specific zoom level
     * @param lat latitude value in double
     * @param lng longitude value in double
     * @param zoomLevel zoom level in double
     */
    fun zoomCameraToLocation(lat:Double, lng:Double, zoomLevel:Double) {
        try {
            var zoomLocation = LatLng(lat, lng)
            var position = CameraPosition.Builder()
                .target(zoomLocation)
                .zoom(zoomLevel) // Sets the zoom
                .build() // Creates a CameraPosition from the builder
            mapboxMap.animateCamera(
                CameraUpdateFactory
                    .newCameraPosition(position), 500
            )
        } catch(ex:Exception) {
            Log.e("error", "Error occurred in zoomCameraToLocation : "+ex.message.toString())
        }
    }

    fun drawRevgeoMarker(revgeocode: Revgeocode, originalPoint: LatLng){
        try{
            if(revgeocode != null) {
                var buildingName : String = revgeocode?.BUILDINGNAME!!
                if(buildingName != null && buildingName == "null")
                    buildingName = revgeocode?.BLOCK!!
                var addressDetail :String = revgeocode?.ROAD!!+"\n"+revgeocode?.LATITUDE!!+",\n"+revgeocode?.LONGITUDE!!
                //var lat: Double = revgeocode?.LATITUDE!!.toDouble()
                //var lng: Double = revgeocode?.LONGITUDE!!.toDouble()
                var lat: Double = originalPoint.latitude
                var lng: Double = originalPoint.longitude
                addMarkerToMap(buildingName, addressDetail, lat, lng)
            }
        } catch(ex:Exception){
            Log.e("error", "Error occured in drawRevgeoMarker : "+ex.message.toString())
        }
    }

    /*
    fun drawThemeSymbolLayer(themeMarkerResponse: ThemeSymbolResponse){
        try {
            var themeMarkerSymbolOptions = ArrayList<SymbolOptions>()

            for(themeMarkerLocation in themeMarkerResponse?.SrchResults!!){
                if(themeMarkerLocation.NO_OF_FOOD_STALLS != null) {
                    var lat: Double = themeMarkerLocation.LATITUDE!!.toDouble()
                    var lng: Double = themeMarkerLocation.LONGITUDE!!.toDouble()
                    var latLng: LatLng = LatLng(lat, lng)
                    val symbolOptions = SymbolOptions()
                        .withLatLng(latLng)
                        .withIconImage(THEME_SYMBOL)
                        .withIconSize(0.3f)
                        .withDraggable(false)

                    themeMarkerSymbolOptions.add(symbolOptions)
                }
            }

            if(themeMarkerSymbolOptions.size > 0) {
                symbolManager?.create(themeMarkerSymbolOptions)
            }
        } catch(ex:Exception){
            Log.e("error", "Error occurred in drawThemeMarkerLayer : "+ex.message.toString())
        }
    }

    fun drawThemePolylineLayer(themePolylineResponse : ThemePolylineResponse) {
        try {
            var themePolylineList: List<ThemePolyline> = themePolylineResponse?.SrchResults!!
            // var parkConnectorLatLngList = mutableListOf<LatLng>()
            var lineOptionsList = ArrayList<LineOptions>()
            for (parkConnector in themePolylineList) {
                var themePolylineLatLng : List<LatLng> = parkConnector?.LatLngList!!
                var lineOptions = LineOptions()
                    .withLatLngs(themePolylineLatLng)
                    .withLineColor("#a10d1c")
                    .withLineWidth(2.0f)
                lineOptionsList.add(lineOptions)
            }

            lineManager?.create(lineOptionsList)

        } catch(ex:Exception) {
            Log.e("error", "Error occured in drawThemePolylineLayer : "+ex.message.toString())
        }
    }

    fun drawThemePolygonLayer(themePolygonResponse: ThemePolygonResponse) {
        try {
            var themePolygonList: List<ThemePolygon> = themePolygonResponse?.SrchResults!!
            var themePolygonLatLngList = mutableListOf<List<LatLng>>()
            for (themePolygon in themePolygonList) {
                var themePolygonLatLng = themePolygon?.LatLngList
                if(themePolygonLatLng != null){
                    themePolygonLatLngList.add(themePolygonLatLng!!)
                }
            }

            var fillOptions = FillOptions()
                .withLatLngs(themePolygonLatLngList)
                .withFillColor("#a10d1c")
                .withFillOpacity(0.60f)

            fillManager?.create(fillOptions)
        } catch(ex:Exception) {
            Log.e("error", "Error occurred in drawThemePolygonLayer : "+ex.message.toString())
        }
    }
     */

    fun clearAllMappings(){
        try {
            symbolManager?.deleteAll()
            lineManager?.deleteAll()
            fillManager?.deleteAll()
            trackAdditionalSymbolManager?.deleteAll()
            mapPinSymbolManager?.deleteAll()
            mapUnPinSymbolManager?.deleteAll()
            sourceManager?.deleteAll()
            destinationManager?.deleteAll()
        } catch(ex:Exception) {
            Log.e("error", "Error occurred in clearAllMappings : "+ex.message.toString())
        }
    }

    fun mapManagersOnDestroy() {
        try {
            symbolManager?.onDestroy()
            fillManager?.onDestroy()
            lineManager?.onDestroy()
            trackAdditionalSymbolManager?.onDestroy()
            mapPinSymbolManager?.onDestroy()
            mapUnPinSymbolManager?.onDestroy()
            sourceManager?.onDestroy()
            destinationManager?.onDestroy()
        } catch(ex:Exception) {
            Log.e("error", "Error occurred in mapManagersOnDestroy : "+ex.message.toString())
        }
    }

    fun drawPolyLineForMotionDNA(latLng: LatLng){
        try{
            val arrayList = ArrayList<LatLng>()
            arrayList.add(latLng)

            var lineOptions = LineOptions()
                .withLatLngs(arrayList)
                .withLineColor("#a10d1c")
                .withLineWidth(2.0f)
            lineManager?.create(lineOptions)
        }catch(ex:Exception) {
            Log.e("error", "Error occurred in drawPolyLineForMotionDNA : "+ex.message.toString())
            Toast.makeText(context, "Error occurred in drawPolyLineForMotionDNA at lat: "+latLng.latitude+", longitude: "+latLng.longitude, Toast.LENGTH_SHORT).show()
        }
    }

    fun drawSymbolForMotionDNA(latLng: LatLng){
        try{
            val symbolOptions = SymbolOptions()
                .withLatLng(latLng)
                .withIconImage(TRACKER_SYMBOL)
                .withIconSize(0.02f)
                .withDraggable(false)

            symbolManager?.create(symbolOptions)
        }catch(ex:Exception) {
            Log.e("error", "Error occurred in drawSymbolForMotionDNA : "+ex.message.toString())
            Toast.makeText(context, "Error occurred in drawSymbolForMotionDNA at lat: "+latLng.latitude+", longitude: "+latLng.longitude, Toast.LENGTH_SHORT).show()
        }
    }

    fun drawSource(latLng: LatLng){
        try{
            sourceManager?.deleteAll()
            val symbolOptions = SymbolOptions()
                .withLatLng(latLng)
                .withIconImage(SOURCE_SYMBOL)
                .withIconSize(0.6f)
                .withDraggable(false)

            sourceManager?.create(symbolOptions)
        }catch(ex:Exception) {
            Log.e("error", "Error occurred in sourceManager : "+ex.message.toString())
            Toast.makeText(context, "Error occurred in sourceManager at lat: "+latLng.latitude+", longitude: "+latLng.longitude, Toast.LENGTH_SHORT).show()
        }
    }

    fun drawDestination(latLng: LatLng){
        try{
            destinationManager?.deleteAll()
            val symbolOptions = SymbolOptions()
                .withLatLng(latLng)
                .withIconImage(DESTINATION_SYMBOL)
                .withIconSize(0.6f)
                .withDraggable(false)

            destinationManager?.create(symbolOptions)
        }catch(ex:Exception) {
            Log.e("error", "Error occurred in destinationManager : "+ex.message.toString())
            Toast.makeText(context, "Error occurred in destinationManager at lat: "+latLng.latitude+", longitude: "+latLng.longitude, Toast.LENGTH_SHORT).show()
        }
    }

    fun drawMapUnPinSymbolForMotionDNA(latLng: LatLng){
        try{
            mapUnPinSymbolManager?.deleteAll()
            val symbolOptions = SymbolOptions()
                .withLatLng(latLng)
                .withIconImage(MAP_UNPIN_SYMBOL)
                .withIconSize(0.6f)
                .withDraggable(false)

            mapUnPinSymbolManager?.create(symbolOptions)
        }catch(ex:Exception) {
            Log.e("error", "Error occurred in drawMapUnPinSymbolForMotionDNA : "+ex.message.toString())
            Toast.makeText(context, "Error occurred in drawMapUnPinSymbolForMotionDNA at lat: "+latLng.latitude+", longitude: "+latLng.longitude, Toast.LENGTH_SHORT).show()
        }
    }

    fun drawMapPinSymbolForMotionDNA(latLng: LatLng){
        try{
            mapUnPinSymbolManager?.deleteAll()
            val symbolOptions = SymbolOptions()
                .withLatLng(latLng)
                .withIconImage(MAP_PIN_SYMBOL)
                .withIconSize(0.6f)
                .withDraggable(false)

            mapPinSymbolManager?.create(symbolOptions)
        }catch(ex:Exception) {
            Log.e("error", "Error occurred in drawMapPinSymbolForMotionDNA : "+ex.message.toString())
            Toast.makeText(context, "Error occurred in drawMapPinSymbolForMotionDNA at lat: "+latLng.latitude+", longitude: "+latLng.longitude, Toast.LENGTH_SHORT).show()
        }
    }

    fun drawTrackAdditional(trackAdditional: TrackAdditional, isData: Boolean){
        try{
            var latLng = LatLng(trackAdditional.commentLatitude, trackAdditional.commentLongitude)
            val symbolOptions = SymbolOptions()
                .withLatLng(latLng)
                .withIconImage(TRACKER_ADDITIONAL_SYMBOL)
                .withIconSize(0.4f)
                .withDraggable(false)

            if(isData) {
                val additionalInfoJson = JSONObject()
                var element: JsonElement? = null
                try {
                    if (trackAdditional.title != null) additionalInfoJson.put("title", trackAdditional.title)
                    if (trackAdditional.comment != null) additionalInfoJson.put("message", trackAdditional.comment)
                    if (trackAdditional.image != null) additionalInfoJson.put("imageLocation", trackAdditional.image)

                    val gson = Gson()
                    element = gson.fromJson(
                        additionalInfoJson.toString(),
                        JsonElement::class.java
                    )
                    symbolOptions.withData(element)
                } catch (e: JSONException) {
                    Log.e("TEST", "", e)
                }
            }

            trackAdditionalSymbolManager?.create(symbolOptions)

            trackAdditionalSymbolManager?.addClickListener { symbol ->
                try {
                    val data = symbol.data
                    val title = data!!.asJsonObject["title"].asString
                    var message = data.asJsonObject["message"].asString
                    var imageLocation = data.asJsonObject["imageLocation"].asString
                    mListener.showComment(title, message, imageLocation)
                } catch (ex: java.lang.Exception) {
                    Log.e("TEST", "Error School marker", ex)
                }
            }
        }catch(ex:Exception) {
            Log.e("error", "Error occurred in drawTrackAdditional : "+ex.message.toString())
            Toast.makeText(context, "Error occurred in drawTrackAdditional at lat: "+trackAdditional.commentLatitude+", longitude: "+trackAdditional.commentLongitude, Toast.LENGTH_SHORT).show()
        }
    }

    fun zoomToTrackMap(paddingLeft: Int, paddingTop: Int, paddingRight: Int, paddingBottom: Int) {
        try {
            val allPoints = ArrayList<LatLng>()
            if (symbolManager != null) {
                val list: LongSparseArray<*> = symbolManager!!.getAnnotations()
                for (i in 0 until list.size()) {
                    val symbol = list[i.toLong()] as Symbol?
                    allPoints.add(symbol!!.latLng)
                }
            }
            zoomToBoundingBox(allPoints, paddingLeft, paddingTop, paddingRight, paddingBottom)
        } catch(ex:Exception) {

        }
    }

    fun zoomToBoundingBox(latLngs: List<LatLng?>, paddingLeft: Int, paddingTop: Int, paddingRight: Int, paddingBottom: Int) {
        var paddingLeft = paddingLeft
        var paddingTop = paddingTop
        var paddingRight = paddingRight
        var paddingBottom = paddingBottom
        Log.i("Test", "zoomToBoundingBox")
        //Bug if too many coordinates
        if (latLngs.size < 2) return
        try {
            val latLngBounds = LatLngBounds.Builder().includes(latLngs).build()

            mapboxMap?.easeCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, paddingLeft, paddingTop, paddingRight, paddingBottom), 1000)
        } catch (e: InvalidLatLngBoundsException) {
            return
        }
    }

}