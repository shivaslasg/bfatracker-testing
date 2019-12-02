package sg.onemap.bfatracker.models

import com.google.gson.annotations.SerializedName

class AddressResponse {
    @SerializedName("found")
    var found: Int? = null

    @SerializedName("totalNumPages")
    var totalNumPages: Int? = null

    @SerializedName("pageNum")
    var pageNum: Int? = null

    @SerializedName("results")
    var results: List<Address>? = null
}