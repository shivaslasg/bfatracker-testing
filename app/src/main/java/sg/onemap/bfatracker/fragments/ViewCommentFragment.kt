package sg.onemap.bfatracker.fragments

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import sg.onemap.bfatracker.R

class ViewCommentFragment(var title:String,
                          var message:String,
                          var imageLocation: String) : DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView =  inflater.inflate(R.layout.view_comment, container, false)

        var commentTitle: TextView = rootView?.findViewById(R.id.commentTitle)!!
        var commentMsg: TextView = rootView?.findViewById(R.id.commentMsg)!!
        var commentImage: ImageView = rootView?.findViewById(R.id.commentImage)!!

        commentTitle.setText(title)
        commentMsg.setText(message)

        val uri = imageLocation.toUri()
        //val filename:String = uri?.lastPathSegment!!

        //var savedFile = File(savedPath, filename)

        Glide.with(context!!).load(uri).into(commentImage!!)

        var closeBtn: ImageButton? = rootView?.findViewById(R.id.closeBtn)
        closeBtn?.setOnClickListener{
            dismiss()
        }
        return rootView
    }
}