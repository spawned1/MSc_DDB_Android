package com.e.pictriptation.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.e.pictriptation.R
import com.e.pictriptation.database.Database
import com.e.pictriptation.helpers.Permission
import com.e.pictriptation.helpers.Photo
import com.e.pictriptation.model.Picture
import com.e.pictriptation.model.Trip
import kotlinx.android.synthetic.main.activity_maps.*
import java.util.*

class TripActivity : AppCompatActivity(), View.OnClickListener {


    private var trip = Trip()
    private lateinit var database: Database

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip)

        database = Database(super.getBaseContext())


        //trip erstellen
    }

    override fun onClick(v: View?) {

        //bei Klick auf Foto - Foto erstellen
        //Photo.takePhoto(this)

        //bei Klick auf apeichern - trip speichern
        //database.save(trip)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode != Permission.PHOTO_PERMISSION)
            return

        if (data == null)
            return
        if (resultCode != RESULT_OK || !data.hasExtra("data"))
            return

        val bitmap: Bitmap = data!!.extras!!["data"] as Bitmap
        if (bitmap == null)
            return

        trip.image = bitmap
    }



    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        if (requestCode == Permission.PHOTO_PERMISSION)
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Photo.takePhoto(this)
    }


}
