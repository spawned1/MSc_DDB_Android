package com.e.pictriptation.model

import android.graphics.Bitmap
import com.e.pictriptation.database.annotations.Column
import com.e.pictriptation.database.annotations.ForeignKeyColumn
import com.e.pictriptation.database.annotations.Table
import com.google.android.gms.maps.model.LatLng
import java.util.*
import kotlin.collections.ArrayList


@Table("Routes")
class Route {



    //region Constructors

    constructor() {

        this.id = 0
        this.tripId = 0
        this.route = ""
        this.timestamp = Date()
    }

    constructor(id: Long, tripId: Long, route: String, timestamp: Date) {

        this.id = id
        this.tripId = tripId
        this.route = route
        this.timestamp = timestamp
    }

    //endregion



    //region Properties

    @Column(Column.TYPE_INT,true)
    var id: Long

    @ForeignKeyColumn(Trip::class)
    @Column(Column.TYPE_INT)
    var tripId: Long

    @Column(Column.TYPE_TEXT)
    lateinit var route: String

    @Column(Column.TYPE_INT)
    var timestamp: Date

    //endregion



    //region Methods

    fun setRoutePoints(points: ArrayList<LatLng>) {
        route = points.joinToString(separator = ";", transform = {it.latitude.toString() + "," + it.longitude.toString()})
    }

    fun getRoutePoints():  ArrayList<LatLng> {

        var list = ArrayList<LatLng>()
        if (route == "")
            return list

        val points = route.split(";")
        for (point in points) {

            val latlng = point.split(",")
            list.add(LatLng(latlng[0].toDouble(), latlng[1].toDouble()))
        }

        return list
    }

    //end region


}