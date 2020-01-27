package com.e.pictriptation.map

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import android.widget.TextView
import com.e.pictriptation.R
import com.e.pictriptation.model.Picture
import java.text.SimpleDateFormat


class CenterTextInfoWindowAdapter : GoogleMap.InfoWindowAdapter {

    private val context :Context


    constructor(context: Context) :super() {
        this.context = context
    }


    override fun getInfoWindow(p0: Marker?): View? {
        return null
    }

    override fun getInfoContents(marker: Marker?): View? {

        if (marker == null || marker!!.tag == null)
            return null

        var picture = marker!!.tag as Picture

        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.maps_marker_infowindow, null)


        val title = view.findViewById(R.id.map_marker_title) as TextView
        title.text = SimpleDateFormat("dd.MM.yyyy").format(picture.timestamp)


        val snippet = view.findViewById(R.id.map_marker_snippet) as TextView
        snippet.text = picture.description

        return view
    }

}