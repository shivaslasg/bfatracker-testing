package sg.onemap.bfatracker.fragments

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.net.toUri
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.github.lhoyong.imagepickerview.ImagePickerView
import com.github.lhoyong.imagepickerview.core.config
import com.mapbox.mapboxsdk.Mapbox.getApplicationContext
import com.mapbox.mapboxsdk.geometry.LatLng
import sg.onemap.bfatracker.R
import sg.onemap.bfatracker.controllers.RealmController
import sg.onemap.bfatracker.interfaces.CommentFormListener
import sg.onemap.bfatracker.models.realm.TrackAdditional
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class CommentFormFragment(var trackId:String,
                          var currentLatLng:LatLng,
                          var mListener: CommentFormListener) : DialogFragment() {

    var IMAGE_REQUEST_CODE : String = "102"
    var commentTitle : EditText? = null
    var commentDesc : EditText? = null
    var commentImage: ImageView? = null
    var savedPath: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView =  inflater.inflate(R.layout.add_comment_form, container, false)
        commentTitle = rootView?.findViewById(R.id.commentTitle)
        commentDesc = rootView?.findViewById(R.id.commentDesc)
        commentImage = rootView?.findViewById(R.id.commentImage)

        var closeBtn:ImageButton? = rootView?.findViewById(R.id.closeBtn)
        closeBtn?.setOnClickListener{
            dismiss()
        }
        var addImageCommentBtn:ImageButton? = rootView?.findViewById(R.id.addImageCommentBtn)
        addImageCommentBtn?.setOnClickListener{
            ImagePickerView.Builder()
                .setup {
                    config {
                        name { IMAGE_REQUEST_CODE }
                        max { 1 }
                    }
                }
                .start(context!!)
        }
        val addCommentBtn: Button? = rootView?.findViewById(R.id.addCommentBtn)
        addCommentBtn?.setOnClickListener{
            var title:String = commentTitle?.text.toString()
            var desc:String = commentDesc?.text.toString()

            val trackAdditional = TrackAdditional()
            trackAdditional.trackId = trackId
            trackAdditional.title = title
            trackAdditional.comment = desc
            trackAdditional.commentLongitude = currentLatLng?.longitude
            trackAdditional.commentLatitude = currentLatLng?.latitude
            trackAdditional.image = savedPath!!

            mListener.addTrackAdditional(trackAdditional)
        }

        return rootView
    }

    override fun onStart() {
        super.onStart()
        val args = arguments

        //var trackId: String = args?.get("trackId").toString()
        //var currentLatLng: LatLng? = args?.getParcelable<LatLng>("latlng")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val images = data?.getParcelableArrayListExtra<Uri>(IMAGE_REQUEST_CODE)

            images?.forEach {
                var path: String = it?.path!!
                val uri = path.toUri()
                val filename:String = uri?.lastPathSegment!!
                val bitmap :Bitmap = BitmapFactory.decodeFile(path)
                savedPath = saveToInternalStorage(bitmap, filename)

                var savedFile = File(savedPath, filename)
                savedPath = savedPath+"/"+filename
                Glide.with(context!!).load(savedFile).into(commentImage!!)
                commentImage?.visibility = View.VISIBLE
            }

            /*images?.let {
                (recycler_view.adapter as ImageAdapter).submitList(it)
            }*/
        }
    }

    private fun saveToInternalStorage(bitmapImage: Bitmap, filename: String): String? {
        val cw = ContextWrapper(getApplicationContext())
        // path to /data/data/yourapp/app_data/imageDir
        val directory: File = cw.getDir("BFATRACKER", Context.MODE_PRIVATE)
        // Create imageDir
        val mypath = File(directory, filename)
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(mypath)
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                fos?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return directory.getAbsolutePath()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }
}