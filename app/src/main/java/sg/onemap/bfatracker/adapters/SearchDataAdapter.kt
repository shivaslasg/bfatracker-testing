package sg.onemap.bfatracker.adapters

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import sg.onemap.bfatracker.R
import sg.onemap.bfatracker.models.Address

class SearchDataAdapter(private var activity: Activity, private var items: List<Address>):
    ArrayAdapter<Address>(activity, 0, items) {

    private class ViewHolder(row: View?) {
        var txtBuildingName: TextView? = null
        var txtAddress: TextView? = null

        init {
            this.txtBuildingName = row?.findViewById<TextView>(R.id.result_building_name)
            this.txtAddress = row?.findViewById<TextView>(R.id.result_address)
        }
    }

    fun updateDataList(newItems: List<Address>){
        this.items = newItems
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View?
        val viewHolder: ViewHolder
        if (convertView == null) {
            val inflater = activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.search_row, null)
            viewHolder =
                ViewHolder(view)
            view?.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        var listAddress = items[position]
        viewHolder.txtBuildingName?.text = listAddress.BUILDING?.toUpperCase()
        viewHolder.txtAddress?.text = listAddress.ADDRESS?.toUpperCase()

        return view as View
    }

    override fun getItem(i: Int): Address {
        return items[i]
    }

    override fun getItemId(i: Int): Long {
        return i.toLong()
    }

    override fun getCount(): Int {
        return items.size
    }
}