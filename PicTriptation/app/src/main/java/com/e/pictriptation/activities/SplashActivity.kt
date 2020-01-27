package com.e.pictriptation.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Handler
import com.e.pictriptation.database.Database
import com.e.pictriptation.model.Picture
import com.e.pictriptation.model.Route
import com.e.pictriptation.model.Trip
import com.e.pictriptation.model.User
import java.util.*


class SplashActivity : AppCompatActivity() {


    //region Declarations

    private lateinit var database: Database

    //endregion



    //region Activity Listener Methods

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(com.e.pictriptation.R.layout.activity_splash)


        database = Database(super.getBaseContext())

        var users = database.select<User>()
        if (users.count() == 0) {

            Handler().postDelayed(Runnable {

                val mainIntent = Intent(this@SplashActivity, UserActivity::class.java)
                this@SplashActivity.startActivityForResult(mainIntent, 1)
            }, 2000)
        }
        else {

            Handler().postDelayed(Runnable {

                val mainIntent = Intent(this@SplashActivity, TripsActivity::class.java)
                this@SplashActivity.startActivity(mainIntent)
                this@SplashActivity.finish()
            }, 2000)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode != 1)
            return

        val mainIntent = Intent(this@SplashActivity, TripsActivity::class.java)
        this@SplashActivity.startActivity(mainIntent)
        this@SplashActivity.finish()
    }

    //endregion


}