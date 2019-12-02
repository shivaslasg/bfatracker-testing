package sg.onemap.bfatracker.models

import com.google.gson.annotations.SerializedName
import com.mapbox.mapboxsdk.geometry.LatLng

class ThemePolygon {

    @SerializedName("Theme_Name")
    var Theme_Name: String? = null

    @SerializedName("DESCRIPTION")
    var DESCRIPTION: String? = null

    @SerializedName("MAPTIP")
    var MAPTIP: String? = null

    @SerializedName("SYMBOLCOLOR")
    var SYMBOLCOLOR: String? = null

    @SerializedName("ICON_NAME")
    var ICON_NAME: String? = null

    @SerializedName("LatLng")
    var LatLng: String? = null

    @SerializedName("LatLngList")
    var LatLngList: List<LatLng>? = null
}