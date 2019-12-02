package sg.onemap.bfatracker.models

import com.google.gson.annotations.SerializedName

class TokenResponse {
    @SerializedName("access_token")
    var access_token:String? = null

    @SerializedName("expiry_timestamp")
    var expiry_timestamp:String? = null
}