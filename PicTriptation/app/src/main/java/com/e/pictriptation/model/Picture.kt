package com.e.pictriptation.model

import android.graphics.Bitmap
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
        this.description = ""
        this.city = ""
        this.latitude = 0.0
        this.longitude = 0.0
        this.timestamp = Date()
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
    var description: String

    @Column(Column.TYPE_TEXT)
    var city: String

    @Column(Column.TYPE_DOUBLE)
    var latitude: Double

    @Column(Column.TYPE_DOUBLE)
    var longitude: Double

    @Column(Column.TYPE_INT)
    var timestamp: Date

    //endregion



    //region Methods

    public fun isImageInitialized(): Boolean {
        return ::image.isInitialized
    }


}