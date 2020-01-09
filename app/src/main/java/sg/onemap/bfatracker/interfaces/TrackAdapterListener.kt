package sg.onemap.bfatracker.interfaces

import sg.onemap.bfatracker.models.realm.Track

interface TrackAdapterListener {
    fun assignTrackTask(track: Track?, taskId : String)
}