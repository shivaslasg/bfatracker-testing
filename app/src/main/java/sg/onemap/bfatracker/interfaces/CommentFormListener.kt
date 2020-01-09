package sg.onemap.bfatracker.interfaces

import sg.onemap.bfatracker.models.realm.TrackAdditional

interface CommentFormListener {
    fun addTrackAdditional(trackAdditional: TrackAdditional)
}