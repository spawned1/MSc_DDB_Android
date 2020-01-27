package com.e.pictriptation.activities

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.e.pictriptation.R
import com.e.pictriptation.database.Database
import com.e.pictriptation.helpers.Permission
import com.e.pictriptation.helpers.Photo
import com.e.pictriptation.model.Picture
import com.e.pictriptation.model.Trip
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.activity_picture.*
import kotlinx.android.synthetic.main.activity_picture_new.*
import kotlinx.android.synthetic.main.activity_trips.*
import java.text.SimpleDateFormat
import java.util.*


class PictureNewActivity : AppCompatActivity(), View.OnClickListener {

    private var trip: Trip? = null
    private var picture = Picture()
    private lateinit var database: Database




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picture_new)

        database = Database(super.getBaseContext())


        val bundle = intent.extras
        if (bundle != null) {

            trip = database.selectById<Trip>(bundle.getLong("id", 0L))
            if (trip != null) {

                val imageView = findViewById<ImageView>(R.id.tvTripsImage)
                imageView.setImageBitmap(trip!!.image)

                val textView = findViewById<TextView>(R.id.tvTripsTitle)
                textView.text = trip!!.title
            }

            picture.latitude = bundle.getDouble("lat", 0.0)
            picture.longitude = bundle.getDouble("lng", 0.0)
            picture.city = bundle.getString("city", "")
        }

        // Button Listener
        picTaken.setOnClickListener(this)
        picSave.setOnClickListener(this)
        picCancel.setOnClickListener(this)

    }

    override fun onClick(v: View?) {

        //bei Klick auf Foto - Foto aufnhemen
        if (v == picTaken) {
            Photo.takePhoto(this)
        }

        //bei Klick auf OK - Eintrag erstellen
        if (v == picSave) {
            val picNote = picDescription.text.toString()

            // Abfrage ob schon Foto gemacht wurde
            if (!picture.isImageInitialized()) {


                // Alert Dialog, wenn kein Foto vorhanden
                val dialogBuilder = AlertDialog.Builder(this)
                dialogBuilder.setMessage(R.string.take_foto_first)
                    .setPositiveButton(
                        R.string.alert_button,
                        DialogInterface.OnClickListener { dialog, id ->
                            dialog.cancel()
                        })

                // Dialogbox erstellen
                val alert = dialogBuilder.create()
                alert.setTitle(R.string.missing_foto)
                // Dialogbox anzeigen
                alert.show()
                }

            else {
                picture.description = picNote
                database.save(picture)

                finish()
            }

        }



        //bei Klick auf Abbrechen - Eintrag abbrechen bzw. Zurück
        if (v == picCancel) {
            Toast.makeText(this, R.string.pic_note_creation, Toast.LENGTH_LONG).show()
            //* val intent = Intent(this, TripActivity::class.java)
            //* startActivity(intent)
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

        picture.tripId = trip!!.id
        picture.image = bitmap
        picture.timestamp = Date()

        picTaken.setImageBitmap(bitmap)
        picGpsCord.setText(picture.latitude.toString() + ";" + picture.longitude.toString())
        picCity.setText(picture!!.city)
        picDate.setText(SimpleDateFormat("dd.MM.yyyy").format(picture!!.timestamp))
    }



    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        if (requestCode == Permission.PHOTO_PERMISSION)
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Photo.takePhoto(this)
    }



}
