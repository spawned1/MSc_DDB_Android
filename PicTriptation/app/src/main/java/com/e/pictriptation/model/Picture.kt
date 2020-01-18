package com.e.pictriptation.model

import android.graphics.Bitmap
import android.util.EventLogTags
import com.e.pictriptation.database.annotations.Column
import com.e.pictriptation.database.annotations.ForeignKeyColumn
import com.e.pictriptation.database.annotations.Table
import java.util.*


@Table("Pictures")
class Picture {



    //region Constructors

    constructor() {

        this.id = 0
        this.tripId = 0
        this.latitude = null
        this.longitude = null
        this.timestamp = Date()
    }

    constructor(id: Long, tripId: Long, image: Bitmap, description: String, city: String, location: String, latitude: Double?, longitude: Double?, timestamp: Date) {

        this.id = id
        this.tripId = tripId
        this.image = image
        this.description = description
        this.city = city
        this.location = location
        this.latitude = latitude
        this.longitude = longitude
        this.timestamp = timestamp
    }

    //endregion



    //region Properties

    @Column(Column.TYPE_INT,true)
    var id: Long

    @ForeignKeyColumn(Trip::class)
    @Column(Column.TYPE_INT)
    var tripId: Long

    @Column(Column.TYPE_BLOB)
    lateinit var image: Bitmap

    @Column(Column.TYPE_TEXT)
    lateinit var description: String

    @Column(Column.TYPE_TEXT)
    lateinit var city: String

    @Column(Column.TYPE_TEXT)
    lateinit var location: String

    @Column(Column.TYPE_DOUBLE)
    var latitude: Double?

    @Column(Column.TYPE_DOUBLE)
    var longitude: Double?

    @Column(Column.TYPE_INT)
    var timestamp: Date

    //endregion



    //region Methods

    public fun isImageInitialized(): Boolean {
        return ::image.isInitialized
    }


}