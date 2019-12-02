package sg.onemap.bfatracker.models

import com.google.gson.annotations.SerializedName

class RevgeocodeResponse {
    @SerializedName("GeocodeInfo")
    var GeocodeInfo: List<Revgeocode>? = null
}