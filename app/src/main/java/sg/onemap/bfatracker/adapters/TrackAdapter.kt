package sg.onemap.bfatracker.adapters

import android.content.ClipData.Item
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter
import sg.onemap.bfatracker.R
import sg.onemap.bfatracker.RealmListActivity
import sg.onemap.bfatracker.models.realm.Track


class TrackAdapter (data: OrderedRealmCollection<Track?>?, var mListener: RealmListActivity) :
    RealmRecyclerViewAdapter<Track?, TrackAdapter.TrackHolder?>(data, true) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TrackHolder {
        val itemView: View =
            LayoutInflater.from(parent.context).inflate(R.layout.track_row, parent, false)
        return TrackHolder(itemView)
    }

    override fun onBindViewHolder(
        holder: TrackHolder,
        position: Int
    ) {
        val tryoutobj : Track? = data?.get(position)
        //val obj = getItem(position)
        holder.title.setText(tryoutobj?.name)
        holder.timing.setText("From : "+tryoutobj?.startDateTime+" till "+tryoutobj?.endDateTime)
        holder.loadBtn.setOnClickListener {
            mListener.assignTrackTask(tryoutobj?.id.toString(), 1)
        }
        /*
        holder.deletedCheckBox.isChecked = countersToDelete.contains(itemId)
        if (inDeletionMode) {
            holder.deletedCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    countersToDelete.add(itemId)
                } else {
                    countersToDelete.remove(itemId)
                }
            }
        } else {
            holder.deletedCheckBox.setOnCheckedChangeListener(null)
        }
        holder.deletedCheckBox.visibility = if (inDeletionMode) View.VISIBLE else View.GONE
        */
    }


    class TrackHolder(view : View) : RecyclerView.ViewHolder(view) {
        var title: TextView
        var timing: TextView
        var loadBtn: ImageButton
        var downloadBtn: ImageButton
        var uploadBtn: ImageButton
        var deleteBtn: ImageButton
        var data: Item? = null

        init {
            title = view.findViewById(R.id.title)
            timing = view.findViewById(R.id.timing)
            loadBtn = view.findViewById(R.id.loadBtn)
            downloadBtn = view.findViewById(R.id.downloadBtn)
            uploadBtn = view.findViewById(R.id.uploadBtn)
            deleteBtn = view.findViewById(R.id.deleteBtn)
        }
    }
}