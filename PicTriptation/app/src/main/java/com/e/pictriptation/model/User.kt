package com.e.pictriptation.model

import com.e.pictriptation.database.annotations.Column
import com.e.pictriptation.database.annotations.Table
import java.util.*


@Table("Users")
class User {



    //region Constructors

    constructor() {

        this.id = 0
        this.name = ""
        this.mail = ""
        this.timestamp = Date()
    }

    //endregion



    //region Properties

    @Column(Column.TYPE_INT,true)
    var id: Long

    @Column(Column.TYPE_TEXT)
    var name: String

    @Column(Column.TYPE_TEXT)
    var mail: String

    @Column(Column.TYPE_INT)
    var timestamp: Date

    //endregion


}