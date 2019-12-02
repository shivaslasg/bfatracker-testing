package sg.onemap.bfatracker.restervices

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import sg.onemap.bfatracker.models.*

interface SearchService {
    @GET("commonapi/search")
    fun getSearchData(@Query("searchVal") searchVal: String,
                      @Query("returnGeom") returnGeom: String,
                      @Query("getAddrDetails") getAddrDetails: String): Call<AddressResponse>

    @POST("privateapi/auth/post/getToken")
    fun getToken(@Body body : JsonObject): Call<TokenResponse>

    @GET("privateapi/commonsvc/revgeocode")
    fun revgeocode(@Query("location") location: String,
                   @Query("token") token: String?,
                   @Query("buffer") buffer: Int,
                   @Query("addressType") addressType: String,
                   @Query("otherFeatures") otherFeatures: String): Call<RevgeocodeResponse>

    @GET("privateapi/themesvc/retrieveTheme")
    fun getThemeSymbolData(@Query("queryName") queryName: String,
                           @Query("token") token: String?): Call<ThemeSymbolResponse>

    @GET("privateapi/themesvc/retrieveTheme")
    fun getThemePolylineData(@Query("queryName") queryName: String,
                             @Query("token") token: String?): Call<ThemePolylineResponse>

    @GET("privateapi/themesvc/retrieveTheme")
    fun getThemePolygonData(@Query("queryName") queryName: String,
                            @Query("token") token: String?): Call<ThemePolygonResponse>
}