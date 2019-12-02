package sg.onemap.bfatracker.models

import com.google.gson.annotations.SerializedName
import com.mapbox.mapboxsdk.geometry.LatLng

class ThemePolyline {
    @SerializedName("NAME")
    var NAME: String? = null

    @SerializedName("DESCRIPTION")
    var DESCRIPTION: String? = null

    @SerializedName("HYPERLINK")
    var HYPERLINK: String? = null

    @SerializedName("MAPTIP")
    var MAPTIP: String? = null

    @SerializedName("SYMBOLCOLOR")
    var SYMBOLCOLOR: String? = null

    @SerializedName("LatLng")
    var LatLng: String? = null

    @SerializedName("LatLngList")
    var LatLngList: List<LatLng>? = null
}