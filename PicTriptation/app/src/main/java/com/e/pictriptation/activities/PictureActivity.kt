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

class PictureActivity : AppCompatActivity(), View.OnClickListener {


    private var picture = Picture()
    private lateinit var database: Database


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picture)

        database = Database(super.getBaseContext())


        //picture erstellen
    }

    override fun onClick(v: View?) {

        //bei Klick auf Foto - Foto erstellen
        //Photo.takePhoto(this)

        //bei Klick auf apeichern - picture speichern
        //database.save(picture)
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
    }



    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        if (requestCode == Permission.PHOTO_PERMISSION)
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Photo.takePhoto(this)
    }


}
