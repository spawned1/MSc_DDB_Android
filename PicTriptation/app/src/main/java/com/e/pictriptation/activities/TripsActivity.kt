package com.e.pictriptation.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.e.pictriptation.R
import com.e.pictriptation.database.Database
import com.e.pictriptation.model.Trip

class TripsActivity : AppCompatActivity(), View.OnClickListener {


    private var trips = ArrayList<Trip>()
    private lateinit var database: Database

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trips)

        database = Database(super.getBaseContext())
    }


    override fun onClick(v: View?) {

        //bei Klick auf einen - Trip TripActivity öffnen
        //Photo.takePhoto(this)
    }

    override fun onResume() {
        super.onResume()

        //trips laden und über TripAdapter anzeigen
    }

}
