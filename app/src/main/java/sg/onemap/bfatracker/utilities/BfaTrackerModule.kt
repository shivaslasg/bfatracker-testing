package sg.onemap.bfatracker.utilities

import io.realm.annotations.RealmModule
import sg.onemap.bfatracker.models.realm.Track
import sg.onemap.bfatracker.models.realm.TrackAdditional
import sg.onemap.bfatracker.models.realm.TrackGeoDetail

// Create the module
@RealmModule(classes = [Track::class, TrackGeoDetail::class, TrackAdditional::class])
class BfaTrackerModule