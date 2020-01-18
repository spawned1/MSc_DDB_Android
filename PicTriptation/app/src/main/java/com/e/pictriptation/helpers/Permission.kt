package com.e.pictriptation.helpers

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

class Permission {

    public companion object Permission {


        const val LOCATION_PERMISSION = 1
        const val PHOTO_PERMISSION = 2

        //region Methods

        public fun checkPermission(activity: Activity, permissions :Array<String>, permissionId :Int) :Boolean {

            var selfPermission = PackageManager.PERMISSION_GRANTED
            for (permission in permissions)
                if (ActivityCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED)
                    selfPermission = PackageManager.PERMISSION_DENIED

            if (selfPermission == PackageManager.PERMISSION_GRANTED)
                return true

            ActivityCompat.requestPermissions(
                activity,
                permissions,
                permissionId
            )

            return false
        }

        //endregion


    }
}