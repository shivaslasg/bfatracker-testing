package sg.onemap.bfatracker

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import sg.onemap.bfatracker.adapters.TrackAdapter
import sg.onemap.bfatracker.controllers.RealmController
import sg.onemap.bfatracker.interfaces.RealmUtilityListener
import sg.onemap.bfatracker.interfaces.TrackAdapterListener
import sg.onemap.bfatracker.models.realm.Track
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class RealmListActivity : AppCompatActivity(), TrackAdapterListener, RealmUtilityListener {
    private lateinit var linearLayoutManager: LinearLayoutManager
    var trackRecyclerView : RecyclerView? = null
    private var adapter: TrackAdapter? = null
    private lateinit var realmController: RealmController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_realm_list)

        trackRecyclerView = findViewById(R.id.trackRecyclerView)
        linearLayoutManager = LinearLayoutManager(this)
        trackRecyclerView?.layoutManager = linearLayoutManager
        realmController = RealmController(this, this)
        var tracks = realmController.getAllTracks()
        adapter = TrackAdapter(tracks,  this)

        trackRecyclerView?.setAdapter(adapter)
        trackRecyclerView?.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

    }

    override fun assignTrackTask(track: Track?, taskId: String) {
        if(taskId.equals(RealmController.TrackTaskConstants.TRACK_DELETE)) {
            realmController.deleteTrack(track!!.id)
        } else {
            val mIntent = Intent()
            mIntent.putExtra("track", track)
            //mIntent.putExtra("trackId", trackId)
            mIntent.putExtra("taskId", taskId)
            setResult(3000, mIntent)
            //setResult(Activity.RESULT_OK, mIntent)
            finish()
        }
    }

    //useless for now
    override fun addedRealmTrackRecord(primarykey: String, title: String) {}
}
