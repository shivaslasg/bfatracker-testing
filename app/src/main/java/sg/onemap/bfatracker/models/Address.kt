package sg.onemap.bfatracker.models

import com.google.gson.annotations.SerializedName

class Address {
    @SerializedName("SEARCHVAL")
    var SEARCHVAL: String? = null

    @SerializedName("BLK_NO")
    var BLK_NO: String? = null

    @SerializedName("ROAD_NAME")
    var ROAD_NAME: String? = null

    @SerializedName("BUILDING")
    var BUILDING: String? = null

    @SerializedName("ADDRESS")
    var ADDRESS: String? = null

    @SerializedName("POSTAL")
    var POSTAL: String? = null

    @SerializedName("X")
    var X: String? = null

    @SerializedName("Y")
    var Y: String? = null

    @SerializedName("LATITUDE")
    var LATITUDE: String? = null

    @SerializedName("LONGITUDE")
    var LONGITUDE: String? = null
}