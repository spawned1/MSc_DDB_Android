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
import kotlinx.android.synthetic.main.activity_picture.*
import java.text.SimpleDateFormat

class PictureActivity : AppCompatActivity(), View.OnClickListener {


    private var picture: Picture? = Picture()
    private lateinit var database: Database


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picture)

        database = Database(super.getBaseContext())


        val bundle = intent.extras
        if (bundle != null) {
            picture = database.selectById<Picture>(bundle.getLong("id", 0L))


            //picture!!.image

            ivIcon2.setImageBitmap( picture!!.image )

            val format = SimpleDateFormat("dd.MM.yyyy").format(picture!!.timestamp)
            etDatum.setText(format)

            tvDescription.setText( picture!!.description)
            tvLocation.setText( picture!!.location )
            etCity.setText( picture!!.city )

            if (picture!!.tripId > 0) {

                val trip = database.selectById<Trip>(picture!!.tripId)
                ivIcon1.setImageBitmap( trip!!.image )
            }

        }



        btnShare.setOnClickListener(this)


    }

    override fun onClick(v: View?) {



        picture!!.description = tvDescription.text.toString()
        picture!!.location = tvLocation.text.toString()
        picture!!.city = etCity.text.toString()

        database.save(picture!!)
        finish()
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

        picture!!.image = bitmap
        ivIcon2.setImageBitmap(bitmap)




    }



    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        if (requestCode == Permission.PHOTO_PERMISSION)
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Photo.takePhoto(this)
    }


}
