package sg.onemap.bfatracker.utilities

import io.realm.annotations.RealmModule
import sg.onemap.bfatracker.models.realm.Track
import sg.onemap.bfatracker.models.realm.TrackAdditional
import sg.onemap.bfatracker.models.realm.TrackGeoDetail
import sg.onemap.bfatracker.models.realm.TrackReCalibrate

// Create the module
@RealmModule(classes = [Track::class, TrackGeoDetail::class, TrackAdditional::class, TrackReCalibrate::class])
class BfaTrackerModule