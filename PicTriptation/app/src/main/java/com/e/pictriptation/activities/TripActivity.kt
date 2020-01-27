package com.e.pictriptation.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.e.pictriptation.R
import com.e.pictriptation.database.Database
import com.e.pictriptation.helpers.Permission
import com.e.pictriptation.helpers.Photo
import com.e.pictriptation.model.Picture
import com.e.pictriptation.model.Trip
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.activity_trip.*
import java.util.*

class TripActivity : AppCompatActivity(), View.OnClickListener {


    private var trip = Trip()
    private lateinit var database: Database

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip)

        database = Database(super.getBaseContext())


        PhotoPlus.setOnClickListener(this)
        xButton.setOnClickListener(this)
        okButton.setOnClickListener(this)

    }

    override fun onClick(v: View?) {

        if(v == okButton) {

            trip.title = InputReiseziel.text.toString()
            trip.tripFrom = InputDatum.text.toString();
            trip.tripTo = InputEnde.text.toString();
            trip.timestamp = Date()


            database.save(trip)
            finish()
        }

        if(v == PhotoPlus) {

            Photo.takePhoto(this)
        }

        if(v == xButton) {
            finish()
        }


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
        PhotoPlus.setImageBitmap(bitmap)
        PhotoPlus.scaleType = ImageView.ScaleType.CENTER_CROP
    }



    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        if (requestCode == Permission.PHOTO_PERMISSION)
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Photo.takePhoto(this)
    }


}
