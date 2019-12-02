package sg.onemap.bfatracker.utilities

import android.app.Activity
import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.plugins.annotation.*
import sg.onemap.bfatracker.R
import sg.onemap.bfatracker.models.*
import java.util.ArrayList

class MapUtility (mapView: MapView, mapboxMap: MapboxMap, activity: Activity) {

    lateinit var mapboxMap: MapboxMap
    lateinit var mapView: MapView
    var activity: Activity? = null

    var THEME_SYMBOL = "theme-symbol"
    var symbolManager : SymbolManager? = null
    var lineManager : LineManager? = null
    var fillManager : FillManager? = null

    var TRACKER_SYMBOL = "tracker-symbol"
    var MAP_PIN_SYMBOL = "map-pin-symbol"

    var mapPinSymbolManager : SymbolManager? = null

    init{
        try {
            this.mapboxMap = mapboxMap
            this.mapView = mapView
            this.activity = activity

            //Disable mapbox logo
            mapboxMap.uiSettings.isAttributionEnabled = false
            mapboxMap.uiSettings.isLogoEnabled = false

            //Position of compass in case map rotates with user gestures
            mapboxMap.uiSettings.setCompassMargins(0, 280, 30, 0)

            //Zoom to current location (PSA Building)
            // latitude : 1.273750
            // longitude : 103.801514
            zoomCameraToLocation(1.273750, 103.801514, 17.0)


            var hawker_drawable: Drawable = ResourcesCompat.getDrawable(activity.getResources(),
                R.drawable.mk_thm_hawkercentre, activity.getTheme())!!;
            var hawker_bitmap = hawker_drawable.toBitmap()

            mapboxMap.style?.addImage(THEME_SYMBOL,  hawker_bitmap)

            var tracker_drawable: Drawable = ResourcesCompat.getDrawable(activity.getResources(),
                R.drawable.ic_circular_shape_silhouette, activity.getTheme())!!;
            var tracker_bitmap = tracker_drawable.toBitmap()

            mapboxMap.style?.addImage(TRACKER_SYMBOL,  tracker_bitmap)

            var map_pin_drawable: Drawable = ResourcesCompat.getDrawable(activity.getResources(),
                R.drawable.icons_map_pin, activity.getTheme())!!;
            var map_pin_bitmap = map_pin_drawable.toBitmap()

            mapboxMap.style?.addImage(MAP_PIN_SYMBOL,  map_pin_bitmap)

            symbolManager = SymbolManager(mapView!!, mapboxMap, mapboxMap.style!!)
            symbolManager?.iconAllowOverlap = true
            symbolManager?.textAllowOverlap = true
            lineManager = LineManager(mapView!!, mapboxMap, mapboxMap.style!!)
            fillManager = FillManager(mapView!!, mapboxMap, mapboxMap.style!!)

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

    fun drawRevgeoMarker(revgeocode: Revgeocode){
        try{
            if(revgeocode != null) {
                var buildingName : String = revgeocode?.BUILDINGNAME!!
                if(buildingName != null && buildingName == "null")
                    buildingName = revgeocode?.BLOCK!!
                var addressDetail :String = revgeocode?.ROAD!!+"\n"+revgeocode?.LATITUDE!!+",\n"+revgeocode?.LONGITUDE!!
                var lat: Double = revgeocode?.LATITUDE!!.toDouble()
                var lng: Double = revgeocode?.LONGITUDE!!.toDouble()
                addMarkerToMap(buildingName, addressDetail, lat, lng)
            }
        } catch(ex:Exception){
            Log.e("error", "Error occured in drawRevgeoMarker : "+ex.message.toString())
        }
    }

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

    fun clearAllMappings(){
        try {
            symbolManager?.deleteAll()
            lineManager?.deleteAll()
            fillManager?.deleteAll()
        } catch(ex:Exception) {
            Log.e("error", "Error occurred in clearAllMappings : "+ex.message.toString())
        }
    }

    fun mapManagersOnDestroy() {
        try {
            symbolManager?.onDestroy()
            fillManager?.onDestroy()
            lineManager?.onDestroy()
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
            Toast.makeText(activity, "Error occurred in drawPolyLineForMotionDNA at lat: "+latLng.latitude+", longitude: "+latLng.longitude, Toast.LENGTH_SHORT).show()
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
            Toast.makeText(activity, "Error occurred in drawSymbolForMotionDNA at lat: "+latLng.latitude+", longitude: "+latLng.longitude, Toast.LENGTH_SHORT).show()
        }
    }

    fun drawMapPinSymbolForMotionDNA(latLng: LatLng){
        try{
            val symbolOptions = SymbolOptions()
                .withLatLng(latLng)
                .withIconImage(MAP_PIN_SYMBOL)
                .withIconSize(0.02f)
                .withDraggable(false)

            mapPinSymbolManager?.create(symbolOptions)
        }catch(ex:Exception) {
            Log.e("error", "Error occurred in drawMapPinSymbolForMotionDNA : "+ex.message.toString())
            Toast.makeText(activity, "Error occurred in drawMapPinSymbolForMotionDNA at lat: "+latLng.latitude+", longitude: "+latLng.longitude, Toast.LENGTH_SHORT).show()
        }
    }

}