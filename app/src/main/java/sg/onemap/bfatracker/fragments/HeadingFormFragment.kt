package sg.onemap.bfatracker.fragments

import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.fragment.app.DialogFragment
import sg.onemap.bfatracker.R
import sg.onemap.bfatracker.interfaces.HeadingFormListener
import sg.onemap.bfatracker.utilities.DecimalDigitsInputFilter


class HeadingFormFragment(var mListener: HeadingFormListener) : DialogFragment() {

    //var primaryHeading : EditText? = null
    //var secondaryHeading : EditText? = null
    var headingText : EditText? = null
    var updateHeadingBtn : Button? =null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView =  inflater.inflate(R.layout.heading_form, container, false)
        //primaryHeading = rootView?.findViewById(R.id.primaryHeading)
        //secondaryHeading = rootView?.findViewById(R.id.secondaryHeading)
        headingText = rootView?.findViewById(R.id.headingText)
        updateHeadingBtn = rootView?.findViewById(R.id.updateHeadingBtn)
        headingText?.setFilters(arrayOf<InputFilter>(DecimalDigitsInputFilter(5, 2)))

        var closeBtn: ImageButton? = rootView?.findViewById(R.id.closeBtn)
        closeBtn?.setOnClickListener{
            dismiss()
        }

        updateHeadingBtn?.setOnClickListener{
            /*
            var primaryHeadingValue:String = primaryHeading?.text.toString()
            var secondaryHeadingValue:String = secondaryHeading?.text.toString()

            var conactString: String = primaryHeadingValue+"."+secondaryHeadingValue
            */

            var headingStr:String = headingText?.text.toString()
            if(headingStr.equals("")) {
                headingStr = "0.0"
            }
            mListener.updateHeading(headingStr.toFloat())

//            primaryHeading?.setError("testing")
//            secondaryHeading?.setError("testing")
        }

        return rootView
    }
}