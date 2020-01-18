package com.e.pictriptation.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import com.e.pictriptation.R
import com.e.pictriptation.adapters.TripAdapter
import com.e.pictriptation.database.Database
import com.e.pictriptation.helpers.Photo
import com.e.pictriptation.model.Trip
import kotlinx.android.synthetic.main.activity_trips.*

class TripsActivity : AppCompatActivity(), View.OnClickListener, AdapterView.OnItemClickListener {


    private var trips = ArrayList<Trip>()
    private lateinit var database: Database

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trips)

        database = Database(super.getBaseContext())

        btnTrip.setOnClickListener(this)
    }


    override fun onClick(v: View?) {

        if (v == btnTrip) {

            val mainIntent = Intent(this@TripsActivity, TripActivity::class.java)
            this@TripsActivity.startActivity(mainIntent)
        }


    }
    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        //Open Trip
        val intent = Intent(this, MapsActivity::class.java)
        intent.putExtra("id", id)
        startActivity(intent)

    }


    override fun onResume() {
        super.onResume()

        //trips laden und über TripAdapter anzeigen
        val All_trips = database.select<Trip>()

        //INIT Adapter
        val adapter = TripAdapter( this, All_trips)
        lvTrips.adapter = adapter
        lvTrips.onItemClickListener = this

    }

}
