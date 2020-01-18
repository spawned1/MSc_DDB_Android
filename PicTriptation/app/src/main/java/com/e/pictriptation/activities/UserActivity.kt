package com.e.pictriptation.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.e.pictriptation.R
import com.e.pictriptation.database.Database
import com.e.pictriptation.model.User

class UserActivity : AppCompatActivity(), View.OnClickListener {


    private var user = User()
    private lateinit var database: Database


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        database = Database(super.getBaseContext())


        //user erstellen
    }


    override fun onClick(v: View?) {

        //bei Klick auf apeichern - Benutzer speichern
        //database.save(user)
    }

}
