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
import android.widget.Toast
import com.e.pictriptation.R
import com.e.pictriptation.database.Database
import com.e.pictriptation.helpers.Permission
import com.e.pictriptation.helpers.Photo
import com.e.pictriptation.model.Picture
import kotlinx.android.synthetic.main.activity_picture_new.*
import kotlinx.android.synthetic.main.activity_trips.*
import java.text.SimpleDateFormat
import java.util.*


class PictureNewActivity : AppCompatActivity(), View.OnClickListener {

    private var picture = Picture()
    private lateinit var database: Database




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picture_new)

        database = Database(super.getBaseContext())

        // Button Listener
        picTake.setOnClickListener(this)
        picSave.setOnClickListener(this)
        picCancel.setOnClickListener(this)

    }

    override fun onClick(v: View?) {

        //bei Klick auf Foto - Foto aufnhemen
        if (v == picTake) {
            Photo.takePhoto(this)

            // Laden Location (GPS) und Stadt
            picture!!.location = picGpsCord.text.toString()
            picture!!.city = picCity.text.toString()
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

        picture.image = bitmap
        picture.timestamp = Date()

        picTaken.setImageBitmap(bitmap)
        picDate.setText(SimpleDateFormat("dd.MM.yyyy").format(picture!!.timestamp))
    }



    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        if (requestCode == Permission.PHOTO_PERMISSION)
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Photo.takePhoto(this)
    }



}
