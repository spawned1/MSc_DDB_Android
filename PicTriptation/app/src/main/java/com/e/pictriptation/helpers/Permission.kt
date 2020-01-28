package com.e.pictriptation.helpers

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

class Permission {

    public companion object Permission {


        //region Constants

        const val LOCATION_PERMISSION = 1
        const val ROUTE_PERMISSION = 2
        const val PHOTO_PERMISSION = 3

        //endregion



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

        fun isPermisssionGranted(grantResults: IntArray): Boolean {

            if (grantResults.isEmpty())
                return false;
            if (grantResults.count() < 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED)
                return false;
            if (grantResults.count() < 2 || grantResults[1] != PackageManager.PERMISSION_GRANTED)
                return false;
            if (grantResults.count() < 3 || grantResults[2] != PackageManager.PERMISSION_GRANTED)
                return false;

            return true
        }

        //endregion


    }
}