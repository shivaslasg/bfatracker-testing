package sg.onemap.bfatracker.utilities

import android.content.Context
import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.mapbox.mapboxsdk.geometry.LatLng
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import sg.onemap.bfatracker.R
import sg.onemap.bfatracker.restervices.SearchService
import sg.onemap.bfatracker.deserialisers.ThemePolygonDeserialiser
import sg.onemap.bfatracker.deserialisers.ThemePolylineDeserialiser
import sg.onemap.bfatracker.interfaces.WebUtilityListener
import sg.onemap.bfatracker.models.*

class WebUtility(var context: Context, var mListener: WebUtilityListener) {
    var BaseUrl: String = "https://developers.onemap.sg/"

    var retrofit : Retrofit? = null
    var service : SearchService? = null

    var utility = Utility(context)

    var theme_marker_queryname: String = "hawkercentre"
    var theme_polyline_queryname: String = "park_connector_loop"
    var theme_polygon_queryname: String = "dengue_cluster"

    fun searchData(searchText:String) {
        try {
            retrofit = Retrofit.Builder()
                .baseUrl(BaseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            service = retrofit?.create(SearchService::class.java)

            var call = service?.getSearchData(searchText, "Y", "Y")

            call?.enqueue(object : Callback<AddressResponse> {
                override fun onResponse(call: Call<AddressResponse>, response: Response<AddressResponse>) {
                    if (response.code() == 200) {
                        var addressResponse = response.body()!!
                        var found:Int = addressResponse.found!!
                        if(found > 0){
                            mListener?.returnResultsForAutocomplete(addressResponse)
                        }
                    }
                }
                override fun onFailure(call: Call<AddressResponse>, t: Throwable) {}
            })
        } catch (ex:Exception) {
            Log.e("error", "Error occured in searchData : "+ex.message.toString())
        }
    }

    fun fetchToken(){
        try {
            retrofit = Retrofit.Builder()
                .baseUrl(BaseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            service = retrofit?.create(SearchService::class.java)

            var jsonObject = JsonObject()
            jsonObject.addProperty("email", context?.getString(R.string.reg_email).toString())
            jsonObject.addProperty("password", context?.getString(R.string.password))
            var call = service?.getToken(jsonObject)
            call?.enqueue(object : Callback<TokenResponse> {
                override fun onResponse(call: Call<TokenResponse>, response: Response<TokenResponse>) {
                    if (response.code() == 200) {
                        var tokenResponse = response.body()!!
                        utility.saveToken(tokenResponse)
                    }
                }
                override fun onFailure(call: Call<TokenResponse>, t: Throwable) {}
            })
        } catch(ex: Exception){
            Log.e("error", "Error occured in fetchToken : "+ex.message.toString())
        }
    }

    fun fetchRevgecode(point: LatLng){
        try {
            retrofit = Retrofit.Builder()
                .baseUrl(BaseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            service = retrofit?.create(SearchService::class.java)

            var selectedLat : Double = point.latitude
            var selectedLng : Double = point.longitude
            var latLngStr: String = selectedLat?.toString()+","+selectedLng?.toString()
            var token: String? = utility?.getValueString(Utility.SharedPrefConstants.PRIVATE_TOKEN)

            var call = service?.revgeocode(latLngStr, token, 10, "All", "N")

            call?.enqueue(object : Callback<RevgeocodeResponse> {
                override fun onResponse(call: Call<RevgeocodeResponse>, response: Response<RevgeocodeResponse>) {
                    if (response.code() == 200) {
                        var revgeocodeResponse = response.body()!!
                        if(revgeocodeResponse != null){
                            val revgeocode = revgeocodeResponse.GeocodeInfo
                            if(revgeocode != null &&
                                revgeocode.size > 0) {
                                mListener.returnResultsForRevgeocode(revgeocode.get(0))
                            }

                        }
                    }
                }
                override fun onFailure(call: Call<RevgeocodeResponse>, t: Throwable) {}
            })
        } catch(ex: Exception){
            Log.e("error", "Error occured in fetchRevgecode : "+ex.message.toString())
        }
    }

    fun fetchThemeSymbolQuery() {
        try {
            var token: String? = utility?.getValueString(Utility.SharedPrefConstants.PRIVATE_TOKEN)

            retrofit = Retrofit.Builder()
                .baseUrl(BaseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            service = retrofit?.create(SearchService::class.java)

            var call = service?.getThemeSymbolData(theme_marker_queryname, token)

            call?.enqueue(object : Callback<ThemeSymbolResponse> {
                override fun onResponse(call: Call<ThemeSymbolResponse>, response: Response<ThemeSymbolResponse>) {
                    if (response.code() == 200) {
                        if(response.body() != null){
                            var themeMarkerResponse: ThemeSymbolResponse = response.body()!!
                            mListener.returnResultsForThemeSymbol(themeMarkerResponse)
                        }
                    }
                }
                override fun onFailure(call: Call<ThemeSymbolResponse>, t: Throwable) {}
            })
        } catch(ex: Exception){
            Log.e("error", "Error occured in fetchThemeMarkerQuery : "+ex.message.toString())
        }
    }

    fun fetchThemePolylineQuery() {
        try {
            var token: String? = utility?.getValueString(Utility.SharedPrefConstants.PRIVATE_TOKEN)

            var themePolylineDeserialiser =
                GsonBuilder().registerTypeAdapter(ThemePolylineResponse::class.java, ThemePolylineDeserialiser()).create()

            retrofit = Retrofit.Builder()
                .baseUrl(BaseUrl)
                .addConverterFactory(GsonConverterFactory.create(themePolylineDeserialiser))
                .build()

            service = retrofit?.create(SearchService::class.java)

            var call = service?.getThemePolylineData(theme_polyline_queryname, token)

            call?.enqueue(object : Callback<ThemePolylineResponse> {
                override fun onResponse(call: Call<ThemePolylineResponse>, response: Response<ThemePolylineResponse>) {
                    if (response.code() == 200) {
                        if(response.body() != null){
                            var themePolylineResponse : ThemePolylineResponse = response.body()!!
                            mListener.returnResultsForThemePolyline(themePolylineResponse)
                        }
                    }
                }
                override fun onFailure(call: Call<ThemePolylineResponse>, t: Throwable) {}
            })
        } catch(ex: Exception){
            Log.e("error", "Error occured in fetchThemePolylineQuery : "+ex.message.toString())
        }
    }

    fun fetchThemePolygonQuery() {
        try {
            var token: String? = utility?.getValueString(Utility.SharedPrefConstants.PRIVATE_TOKEN)

            var themePolygonDeserialiser =
                GsonBuilder().registerTypeAdapter(ThemePolygonResponse::class.java, ThemePolygonDeserialiser()).create()

            retrofit = Retrofit.Builder()
                .baseUrl(BaseUrl)
                .addConverterFactory(GsonConverterFactory.create(themePolygonDeserialiser))
                .build()

            service = retrofit?.create(SearchService::class.java)

            // var service = retrofit?.create(SearchService::class.java)
            var call = service?.getThemePolygonData(theme_polygon_queryname, token)

            call?.enqueue(object : Callback<ThemePolygonResponse> {
                override fun onResponse(call: Call<ThemePolygonResponse>, response: Response<ThemePolygonResponse>) {
                    if (response.code() == 200) {
                        if(response.body() != null){
                            var themePolygonResponse : ThemePolygonResponse = response.body()!!
                            mListener.returnResultsForThemePolygon(themePolygonResponse)
                        }
                    }
                }
                override fun onFailure(call: Call<ThemePolygonResponse>, t: Throwable) {}
            })
        } catch(ex: Exception){
            Log.e("error", "Error occurred in fetchThemePolygonQuery : "+ex.message.toString())
        }
    }
}