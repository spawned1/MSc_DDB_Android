package com.e.pictriptation.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.e.pictriptation.R
import com.e.pictriptation.model.Trip


class TripAdapter : BaseAdapter {

    private lateinit var context: Context
    private lateinit var trips: ArrayList<Trip>

    constructor(c: Context, n: ArrayList<Trip>) : super() {
        context = c
        trips = n
    }


    override fun getItem(position: Int): Any {
        return trips[position]
    }

    override fun getItemId(position: Int): Long {
        return trips[position].id
    }

    override fun getCount(): Int {
        return trips.size
    }


    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val view: View
        val holder: ViewHolder
        if (convertView == null) {

            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.activity_tripitem, parent, false) as View //Id des Views

            holder = ViewHolder()
            holder.title = view.findViewById(R.id.tvTripsTitle) as TextView //Id des TextView
            holder.image = view.findViewById(R.id.tvTripsImage) as ImageView //Id des ImageView

            view.tag = holder
        }
        else {

            view = convertView
            holder = convertView.tag as ViewHolder

        }

        val trip = trips.get(position)

        holder.title.text = trip.title
        holder.image.setImageBitmap(trip.image)

        return view
    }

    private class ViewHolder {
        lateinit var title: TextView
        lateinit var image: ImageView
    }

}