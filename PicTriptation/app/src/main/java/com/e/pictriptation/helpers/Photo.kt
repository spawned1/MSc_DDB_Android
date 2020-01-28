package com.e.pictriptation.helpers

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.MediaStore

class Photo {

    public companion object Photo {


        //region Methods

        fun takePhoto(activity: Activity) {

            if (!Permission.checkPermission(activity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA), Permission.PHOTO_PERMISSION))
                return

            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
                intent.resolveActivity(activity.packageManager)?.also {
                    activity.startActivityForResult(intent, Permission.PHOTO_PERMISSION)
                }
            }
        }

        //endregion


    }
}