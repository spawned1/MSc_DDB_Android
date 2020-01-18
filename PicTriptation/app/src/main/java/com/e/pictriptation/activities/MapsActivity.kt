package com.e.pictriptation.activities

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.e.pictriptation.R
import com.e.pictriptation.database.Database
import com.e.pictriptation.helpers.Permission
import com.e.pictriptation.helpers.Photo
import com.e.pictriptation.map.CenterTextInfoWindowAdapter
import com.e.pictriptation.model.Picture
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.activity_maps.*
import java.util.*
import kotlin.collections.ArrayList


class MapsActivity : AppCompatActivity(), View.OnClickListener, DialogInterface.OnClickListener, OnMapReadyCallback {


    //region Declarations

    private lateinit var map :GoogleMap
    private var mapZoom :Float = 14f

    private var lastLocationMarker :Marker? = null
    private lateinit var lastLocation :LatLng
    private var currentLocations :ArrayList<LatLng> = ArrayList()
    private var currentLocationsPolyline :Polyline? = null

    private lateinit var fusedLocationClient :FusedLocationProviderClient
    private lateinit var locationCallback :LocationCallback
    private var locationCallbackRunning :Boolean = false

    private var locationRequestFastestInterval :Long = 5000
    private var locationRequestInterval :Long = 1000

    private var photoActivityRunning :Boolean = false

    private lateinit var database : Database

    //endregion



    //region Activity Listener Methods
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(com.e.pictriptation.R.layout.activity_maps)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = createCurrentLocationCallback()

        database = Database(super.getBaseContext())

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        photoButton.setOnClickListener(this)
        startButton.setOnClickListener(this)
    }

    override fun onPause() {
        super.onPause()

        //Wenn gerade ein Foto aufgenommen wird sollen die LocationUpdates nicht beendet werden
        if (photoActivityRunning)
            return;

        fusedLocationClient.removeLocationUpdates(locationCallback)
        locationCallbackRunning = false
    }

    override fun onClick(v: View?) {

        if (v == photoButton)
            Photo.takePhoto(this)

        if (v == startButton)
            registerCurrentLocationUpdates()
    }
    override fun onClick(dialog: DialogInterface?, which: Int) {
        startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode != Permission.PHOTO_PERMISSION)
            return

        photoActivityRunning = false

        if (data == null)
            return
        if (resultCode != RESULT_OK || !data.hasExtra("data"))
            return

        val bitmap: Bitmap = data!!.extras!!["data"] as Bitmap
        if (bitmap == null)
            return

        createMarker(
            database.save(
                Picture(0, 0, bitmap, "", "", "", lastLocation.latitude, lastLocation.longitude, Date())
            )
        )
    }

    //endregion



    //region Permission Listener Methods

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        if (requestCode == Permission.LOCATION_PERMISSION)
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                getCurrentLocation()

        if (requestCode == Permission.PHOTO_PERMISSION)
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Photo.takePhoto(this)
    }

    //endregion



    //regin Map Listener Methods

    override fun onMapReady(googleMap: GoogleMap) {

        map = googleMap
        map.setInfoWindowAdapter(CenterTextInfoWindowAdapter(this))
        map.setOnMarkerClickListener { marker ->

            if (marker == null)
                return@setOnMarkerClickListener false

            val picture = marker!!.tag as Picture
            if (picture == null)
                return@setOnMarkerClickListener false

            val pictureIntent = Intent(this@MapsActivity, PictureActivity::class.java)
            pictureIntent.putExtra("id", picture.id)
            this@MapsActivity.startActivity(pictureIntent)

            return@setOnMarkerClickListener false
        }

        getCurrentLocation()

        for (picture in database.select<Picture>())
            createMarker(picture)
    }

    //endregion





    //region Location Service Methods

    fun createCurrentLocationCallback() : LocationCallback {

        return object : LocationCallback() {

            override fun onLocationResult(locationResult: LocationResult?) {

                if (locationResult == null)
                    return

                var mostAccurateLocation :Location? = null
                for (location in locationResult.locations){

                    if (mostAccurateLocation != null && mostAccurateLocation.accuracy <= location.accuracy)
                        continue

                    mostAccurateLocation = location
                }

                if (mostAccurateLocation == null)
                    return

                lastLocation = LatLng(mostAccurateLocation.latitude, mostAccurateLocation.longitude)
                map.moveCamera(CameraUpdateFactory.newLatLng(lastLocation))

                createCurrentLocationMarkerAndMove()
                createCurrentLocationRoute()

                Log.d("Location", lastLocation.latitude.toString() + " - " + lastLocation.longitude.toString())
            }
        }
    }

    fun getCurrentLocation() {

        if (!Permission.checkPermission(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), Permission.LOCATION_PERMISSION))
            return

        fusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->

            if (task.result == null)
                return@addOnCompleteListener

            lastLocation = LatLng(task.result!!.latitude, task.result!!.longitude)
            createCurrentLocationMarkerAndMove()
        }
    }

    fun registerCurrentLocationUpdates() {

        if (!Permission.checkPermission(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), Permission.LOCATION_PERMISSION))
            return

        val locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            AlertDialog.Builder(this)
                .setTitle(R.string.maps_locationservices_title)
                .setMessage(R.string.maps_locationservices_disabled)
                .setNegativeButton(R.string.button_dialog_no, null)
                .setPositiveButton(R.string.button_dialog_yes, this)
                .show()

            return
        }

        val locationRequest = LocationRequest().apply {
            priority = PRIORITY_HIGH_ACCURACY
            fastestInterval = locationRequestFastestInterval
            interval = locationRequestInterval
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        locationCallbackRunning = true
    }

    //endregion



    //region Location Methods

    fun createCurrentLocationMarkerAndMove() {

        var currentMapZoom = mapZoom
        if (lastLocationMarker != null) {

            lastLocationMarker!!.remove()
            currentMapZoom = map.getCameraPosition().zoom;
        }

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLocation, currentMapZoom))
        lastLocationMarker = map.addMarker(MarkerOptions().position(lastLocation))

        currentLocations.add(lastLocation)
    }
    fun createCurrentLocationRoute() {

        if (currentLocationsPolyline != null)
            currentLocationsPolyline!!.remove()

        var polylineOptions = PolylineOptions()
        polylineOptions.addAll(currentLocations)
        polylineOptions.width(12f);
        polylineOptions.color(Color.BLUE);
        polylineOptions.geodesic(true);

        currentLocationsPolyline = map.addPolyline(polylineOptions)
    }

    fun createMarker(picture: Picture) {

        map.addMarker(
            MarkerOptions()
                .position(LatLng(picture.latitude!!, picture.longitude!!))
                .icon(BitmapDescriptorFactory.fromBitmap(createMarkerIcon(picture)))
        ).apply {
            tag = picture
        }.showInfoWindow()
    }
    fun createMarkerIcon(picture: Picture) : Bitmap {

        val valueInPixels = getResources().getDimension(R.dimen.mapsMarker).toInt()

        val resizedBitmap = Bitmap.createScaledBitmap(
            picture.image,
            valueInPixels,
            valueInPixels,
            false
        )

        val circlePaint = Paint().apply {
            isAntiAlias = true
        }
        val circleRadius = Math.max(valueInPixels, valueInPixels) / 2f

        // output bitmap
        val outputBitmapPaint = Paint(circlePaint).apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN) }
        val outputBounds = Rect(0, 0, valueInPixels, valueInPixels)
        val output = Bitmap.createBitmap(valueInPixels + 10, valueInPixels + 10, Bitmap.Config.ARGB_8888)

        return Canvas(output).run {
            drawCircle(circleRadius, circleRadius, circleRadius, circlePaint)
            drawBitmap(resizedBitmap, outputBounds, outputBounds, outputBitmapPaint)
            output
        }
    }

    //endregion


}
