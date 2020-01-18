package com.e.pictriptation.model

import android.graphics.Bitmap
import com.e.pictriptation.database.annotations.Column
import com.e.pictriptation.database.annotations.Table
import java.util.*


@Table("Trips")
class Trip {



    //region Constructors

    constructor() {

        this.id = 0
        this.timestamp = Date()
    }

    constructor(id: Long, title: String, text: String, image: Bitmap, timestamp: Date) {

        this.id = id
        this.title = title
        this.text = text
        this.image = image
        this.timestamp = timestamp
    }

    //endregion



    //region Properties

    @Column(Column.TYPE_INT,true)
    var id: Long

    @Column(Column.TYPE_TEXT)
    lateinit var title: String

    @Column(Column.TYPE_TEXT)
    lateinit var text: String

    @Column(Column.TYPE_BLOB)
    lateinit var image: Bitmap

    @Column(Column.TYPE_INT)
    var timestamp: Date

    //endregion


}