package sg.onemap.bfatracker.models

import com.google.gson.annotations.SerializedName

class ThemeSymbolResponse {
    @SerializedName("FeatCount")
    var FeatCount: Int? = null

    @SerializedName("Theme_Name")
    var Theme_Name: String? = null

    @SerializedName("Category")
    var Category: String? = null

    @SerializedName("Owner")
    var Owner: String? = null

    @SerializedName("SrchResults")
    var SrchResults: List<ThemeSymbol>? = null
}