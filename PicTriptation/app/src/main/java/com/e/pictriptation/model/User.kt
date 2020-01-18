package com.e.pictriptation.model

import android.graphics.Bitmap
import com.e.pictriptation.database.annotations.Column
import com.e.pictriptation.database.annotations.Table
import java.util.*


@Table("Users")
class User {



    //region Constructors

    constructor() {

        this.id = 0
        this.timestamp = Date()
    }

    constructor(id: Long, name: String, image: Bitmap, timestamp: Date) {

        this.id = id
        this.name = name
        this.image = image
        this.timestamp = timestamp
    }

    //endregion



    //region Properties

    @Column(Column.TYPE_INT,true)
    var id: Long

    @Column(Column.TYPE_TEXT)
    lateinit var name: String

    @Column(Column.TYPE_BLOB)
    lateinit var image: Bitmap

    @Column(Column.TYPE_INT)
    var timestamp: Date

    //endregion


}