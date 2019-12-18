package sg.onemap.bfatracker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.realm.Realm
import sg.onemap.bfatracker.adapters.TrackAdapter
import sg.onemap.bfatracker.interfaces.TrackAdapterListener
import sg.onemap.bfatracker.models.realm.Track


class RealmListActivity : AppCompatActivity(), TrackAdapterListener {
    private lateinit var linearLayoutManager: LinearLayoutManager
    var trackRecyclerView : RecyclerView? = null
    private var adapter: TrackAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_realm_list)

        trackRecyclerView = findViewById(R.id.trackRecyclerView)
        linearLayoutManager = LinearLayoutManager(this)
        trackRecyclerView?.layoutManager = linearLayoutManager
        var realm = Realm.getDefaultInstance()
        adapter = TrackAdapter(realm.where(Track::class.java).findAll(),  this)

        trackRecyclerView?.setAdapter(adapter)
        //trackRecyclerView?.setHasFixedSize(true)
        trackRecyclerView?.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

    }

    override fun assignTrackTask(trackId: String, taskId: Int) {
        val mIntent = Intent()
        mIntent.putExtra("trackId", trackId)
        mIntent.putExtra("taskId", taskId)
        setResult(Activity.RESULT_OK, mIntent)
        finish()
    }
}
