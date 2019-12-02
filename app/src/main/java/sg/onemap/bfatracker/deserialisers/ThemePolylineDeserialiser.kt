package sg.onemap.bfatracker.deserialisers

import android.util.Log
import com.google.gson.*
import com.mapbox.mapboxsdk.geometry.LatLng
import sg.onemap.bfatracker.models.ThemePolyline
import sg.onemap.bfatracker.models.ThemePolylineResponse
import java.lang.reflect.Type

class ThemePolylineDeserialiser : JsonDeserializer<ThemePolylineResponse> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): ThemePolylineResponse {
        var themePolylineResponses = ThemePolylineResponse()
        var themePolylineList = mutableListOf<ThemePolyline>()
        var gson = Gson()
        try{
            if(!json?.isJsonNull!!){
                var jsArray: JsonArray = json.asJsonObject["SrchResults"] as JsonArray
                var position : Int = 0
                for(eachItem in jsArray){
                    if(position == 0) {
                        themePolylineResponses.Category = eachItem.asJsonObject["Category"]?.asString
                        themePolylineResponses.Theme_Name = eachItem.asJsonObject["Theme_Name"]?.asString
                        themePolylineResponses.Owner = eachItem.asJsonObject["Owner"]?.asString
                        themePolylineResponses.FeatCount = eachItem.asJsonObject["FeatCount"]?.asInt
                    } else {
                        var themePolylineEntity = gson?.fromJson(eachItem.toString().trim(), ThemePolyline::class.java)
                        var listLatLng = themePolylineEntity.LatLng!!
                        var listValues: List<String> = listLatLng?.split("|")
                        val latLngList = mutableListOf<LatLng>()
                        for(listValue in listValues) {
                            var listValueArray: List<String> = listValue?.split(",")
                            if(listValueArray.size > 1) {
                                var eachLatLng = LatLng(
                                    listValueArray.get(0).toDouble(),
                                    listValueArray.get(1).toDouble())
                                latLngList?.add(eachLatLng)
                            }
                        }
                        themePolylineEntity.LatLngList = latLngList
                        themePolylineList.add(themePolylineEntity)
                    }
                    position = position + 1;
                }

                if(themePolylineList.size > 0){
                    themePolylineResponses.SrchResults = themePolylineList
                }
            }
        } catch(ex: Exception) {
            Log.e("Error", "Error occurred")
        }

        return themePolylineResponses
    }
}