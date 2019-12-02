package sg.onemap.bfatracker.interfaces

import sg.onemap.bfatracker.models.*

interface WebUtilityListener {
    fun returnResultsForAutocomplete(addressResponse: AddressResponse)
    fun returnResultsForRevgeocode(revgeocode: Revgeocode)
    fun returnResultsForThemeSymbol(themeSymbolResponse: ThemeSymbolResponse)
    fun returnResultsForThemePolyline(themPolylineResponse: ThemePolylineResponse)
    fun returnResultsForThemePolygon(themePolygonResponse: ThemePolygonResponse)
}