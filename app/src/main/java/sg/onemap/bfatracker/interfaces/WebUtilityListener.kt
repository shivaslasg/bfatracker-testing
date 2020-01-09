package sg.onemap.bfatracker.interfaces

import com.mapbox.mapboxsdk.geometry.LatLng
import sg.onemap.bfatracker.models.*

interface WebUtilityListener {
    fun returnResultsForAutocomplete(addressResponse: AddressResponse)
    fun returnResultsForRevgeocode(revgeocode: Revgeocode, originalPoint: LatLng)
    fun returnResultsForThemeSymbol(themeSymbolResponse: ThemeSymbolResponse)
    fun returnResultsForThemePolyline(themPolylineResponse: ThemePolylineResponse)
    fun returnResultsForThemePolygon(themePolygonResponse: ThemePolygonResponse)
}