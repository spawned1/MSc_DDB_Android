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

    //endregion



    //region Properties

    @Column(Column.TYPE_INT,true)
    var id: Long

    @Column(Column.TYPE_TEXT)
    lateinit var title: String

    @Column(Column.TYPE_TEXT)
    lateinit var tripFrom: String

    @Column(Column.TYPE_TEXT)
    lateinit var tripTo: String

    @Column(Column.TYPE_BLOB)
    lateinit var image: Bitmap

    @Column(Column.TYPE_INT)
    var timestamp: Date

    //endregion


}