package com.e.pictriptation.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Handler
import com.e.pictriptation.database.Database
import com.e.pictriptation.model.Picture
import com.e.pictriptation.model.Route
import com.e.pictriptation.model.Trip
import java.util.*


class SplashActivity : AppCompatActivity() {


    //region Activity Listener Methods

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(com.e.pictriptation.R.layout.activity_splash)



        /*
        var database = Database(this)

        //erstellen eines Trips
        var trip = Trip()
        trip.id = 0

        //speichern eines Trips
        trip = database.save(trip)

        //aktualisieren eines Tripps
        trip.title = "title"
        trip.text = "text"
        database.save(trip)



        val trip = database.selectById<Trip>(12)

        //laden aller Bilder eines Trips
        var pictures = database.selectByForeignKey<Picture, Trip>(trip!!)

        //löschen eines Bildes
        val picture = pictures[0]
        database.delete(picture)

         */





        Handler().postDelayed(Runnable {

            val mainIntent = Intent(this@SplashActivity, MapsActivity::class.java)
            this@SplashActivity.startActivity(mainIntent)
            this@SplashActivity.finish()
        }, 2000)
    }

    //endregion


}