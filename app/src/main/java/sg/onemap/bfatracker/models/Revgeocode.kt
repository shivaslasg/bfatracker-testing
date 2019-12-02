package sg.onemap.bfatracker.models

import com.google.gson.annotations.SerializedName

class Revgeocode {

    @SerializedName("BUILDINGNAME")
    var BUILDINGNAME: String? = null

    @SerializedName("BLOCK")
    var BLOCK: String? = null

    @SerializedName("ROAD")
    var ROAD: String? = null

    @SerializedName("POSTALCODE")
    var POSTALCODE: String? = null

    @SerializedName("XCOORD")
    var XCOORD: String? = null

    @SerializedName("YCOORD")
    var YCOORD: String? = null

    @SerializedName("LATITUDE")
    var LATITUDE: String? = null

    @SerializedName("LONGITUDE")
    var LONGITUDE: String? = null
}