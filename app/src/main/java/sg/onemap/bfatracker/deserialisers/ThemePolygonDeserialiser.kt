package sg.onemap.bfatracker.deserialisers

import android.util.Log
import com.google.gson.*
import com.mapbox.mapboxsdk.geometry.LatLng
import sg.onemap.bfatracker.models.ThemePolygon
import sg.onemap.bfatracker.models.ThemePolygonResponse
import java.lang.reflect.Type

class ThemePolygonDeserialiser : JsonDeserializer<ThemePolygonResponse> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): ThemePolygonResponse {
        var themePolygonResponses = ThemePolygonResponse()
        var themePolygonList = mutableListOf<ThemePolygon>()
        var gson = Gson()
        try{
            if(!json?.isJsonNull!!){
                var jsArray: JsonArray = json.asJsonObject["SrchResults"] as JsonArray
                var position : Int = 0
                for(eachItem in jsArray){
                    if(position == 0) {
                        themePolygonResponses.Category = eachItem.asJsonObject["Category"]?.asString
                        themePolygonResponses.Theme_Name = eachItem.asJsonObject["Theme_Name"]?.asString
                        themePolygonResponses.Owner = eachItem.asJsonObject["Owner"]?.asString
                        themePolygonResponses.FeatCount = eachItem.asJsonObject["FeatCount"]?.asInt
                    } else {
                        var themePolygonEntity = gson?.fromJson(eachItem.toString(), ThemePolygon::class.java)
                        var listLatLng = themePolygonEntity.LatLng!!
                        var listValues: List<String> = listLatLng?.split("|")
                        val latLngList = mutableListOf<LatLng>()
                        for(listValue in listValues) {
                            var listValueArray: List<String> = listValue?.split(",")
                            if(listValueArray.size > 1) {
                                var eachLatLng = LatLng(
                                    listValueArray.get(0).toDouble(),
                                    listValueArray.get(1).toDouble()
                                )
                                latLngList?.add(eachLatLng)
                            }
                        }
                        themePolygonEntity.LatLngList = latLngList
                        themePolygonList.add(themePolygonEntity)
                    }
                    position = position + 1;
                }

                if(themePolygonList.size > 0){
                    themePolygonResponses.SrchResults = themePolygonList
                }
            }
        } catch(ex: Exception) {
            Log.e("Error", "Error occurred")
        }

        return themePolygonResponses
    }

}