package com.e.pictriptation.model

import android.graphics.Bitmap
import com.e.pictriptation.database.annotations.Column
import com.e.pictriptation.database.annotations.ForeignKeyColumn
import com.e.pictriptation.database.annotations.Table
import java.util.*


@Table("Routes")
class Route {



    //region Constructors

    constructor() {

        this.id = 0
        this.tripId = 0
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


}