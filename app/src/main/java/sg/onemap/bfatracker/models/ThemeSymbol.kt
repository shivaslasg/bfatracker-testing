package sg.onemap.bfatracker.models

import com.google.gson.annotations.SerializedName

class ThemeSymbol {
    @SerializedName("NAME")
    var NAME: String? = null

    @SerializedName("DESCRIPTION")
    var DESCRIPTION: String? = null

    @SerializedName("ADDRESSBUILDINGNAME")
    var ADDRESSBUILDINGNAME: String? = null

    @SerializedName("ADDRESSBLOCKHOUSENUMBER")
    var ADDRESSBLOCKHOUSENUMBER: String? = null

    @SerializedName("ADDRESSPOSTALCODE")
    var ADDRESSPOSTALCODE: String? = null

    @SerializedName("ADDRESSSTREETNAME")
    var ADDRESSSTREETNAME: String? = null

    @SerializedName("NO_OF_FOOD_STALLS")
    var NO_OF_FOOD_STALLS: String? = null

    @SerializedName("OPERATING_HOURS")
    var OPERATING_HOURS: String? = null

    @SerializedName("LATITUDE")
    var LATITUDE: String? = null

    @SerializedName("LONGITUDE")
    var LONGITUDE: String? = null

    @SerializedName("XVAL")
    var XVAL: String? = null

    @SerializedName("YVAL")
    var YVAL: String? = null

    @SerializedName("LatLng")
    var LatLng: String? = null

    @SerializedName("ICON_NAME")
    var ICON_NAME: String? = null
}