package com.e.pictriptation.activities

import android.Manifest
import android.content.*
import android.graphics.*
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.e.pictriptation.R
import com.e.pictriptation.database.Database
import com.e.pictriptation.helpers.Permission
import com.e.pictriptation.model.Picture
import com.e.pictriptation.model.Route
import com.e.pictriptation.model.Trip
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.activity_maps.*
import kotlin.collections.ArrayList


class MapsActivity : AppCompatActivity(), View.OnClickListener, DialogInterface.OnClickListener, OnMapReadyCallback {


    //region Declarations

    private lateinit var geocoder: Geocoder
    private lateinit var map: GoogleMap
    private var mapReady: Boolean = false

    private var mapZoom: Float = 14f


    private var lastLocationMarker: Marker? = null
    private lateinit var lastLocation: LatLng
    private var currentLocations: ArrayList<LatLng> = ArrayList()
    private var currentLocationsPolyline: Polyline? = null
    private var currentLocationsPolylineMarker: Marker? = null

    private lateinit var locationManager: LocationManager
    private var locationSettingsBroadcastReceiver: BroadcastReceiver? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private var trackPosition: Boolean = false

    private var locationRequestFastestInterval: Long = 5000
    private var locationRequestInterval: Long = 1000

    private lateinit var database: Database
    private var trip: Trip? = null
    private var route: Route = Route()

    //endregion



    //region Activity Listener Methods
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_maps)

        geocoder = Geocoder(this)

        locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = createCurrentLocationCallback()
        registerLocationSettingsBroadcastReceiver()

        database = Database(super.getBaseContext())

        initializeTrip()
        initilizeMap()

        photoButton.setOnClickListener(this)
        startButton.setOnClickListener(this)
        stopButton.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()

        loadPictures()
        loadRoute()
    }

    override fun onBackPressed() {
        super.onBackPressed()

        unregisterCurrentLocationUdates()
        unregisterLocationSettingsBroadcastReceiver()
    }

    override fun onClick(v: View?) {

        if (v == photoButton)
            takePhoto()

        if (v == startButton)
            enableLocationTracking()

        if (v == stopButton)
            disableLocationTracking()
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {

        //Öffnen der Einstellungen um die Standortdienste zu aktivieren
        startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }

    //endregion



    //region Permission Listener Methods

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        if (requestCode == Permission.LOCATION_PERMISSION)
            if (Permission.isPermisssionGranted(grantResults))
                registerCurrentLocationUpdates()

        //Wenn beim Starten des Positionstrackings die Berechtigung abgelehnt wird muss die Aufzeichnung wieder gestoppt werden
        if (requestCode == Permission.ROUTE_PERMISSION)
            if (Permission.isPermisssionGranted(grantResults))
                enableLocationTracking()
    }

    //endregion



    //region Map Listener Methods

    override fun onMapReady(googleMap: GoogleMap) {

        mapReady = true

        map = googleMap
        map.setOnMapClickListener { _ ->
            showDistanceInfo()
        }
        map.setOnMarkerClickListener { marker ->

            showDistanceInfo()

            if (marker == null || marker.tag == null)
                return@setOnMarkerClickListener false

            val picture = marker.tag as Picture
            if (picture == null)
                return@setOnMarkerClickListener false

            //zwischenspeichern der aktuellen Standortverlaufs
            route.setRoutePoints(currentLocations)

            val pictureIntent = Intent(this@MapsActivity, PictureActivity::class.java)
            pictureIntent.putExtra("id", picture.id)
            this@MapsActivity.startActivity(pictureIntent)

            return@setOnMarkerClickListener false
        }

        registerCurrentLocationUpdates()

        loadPictures()
        loadRoute()
    }

    //endregion





    //region Location Service Methods

    fun checkLocationProviderEnabled(): Boolean {

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            return true

        AlertDialog.Builder(this)
            .setTitle(R.string.maps_locationservices_title)
            .setMessage(R.string.maps_locationservices_disabled)
            .setNegativeButton(R.string.button_dialog_no, null)
            .setPositiveButton(R.string.button_dialog_yes, this)
            .show()

        return false
    }

    fun createCurrentLocationCallback() : LocationCallback {

        return object: LocationCallback() {

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
                createLocationMarkerAndMove()

                if(!trackPosition)
                    return

                currentLocations.add(lastLocation)

                createLocationMarkerAndMove()
                createLocationRoute()
            }
        }
    }

    fun registerCurrentLocationUpdates() {

        //entfernt alle eventuell registrierten LoationRequests
        unregisterCurrentLocationUdates()

        if (!Permission.checkPermission(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), Permission.LOCATION_PERMISSION))
            return
        if (!checkLocationProviderEnabled())
            return

        val locationRequest = LocationRequest().apply {
            priority = PRIORITY_HIGH_ACCURACY
            fastestInterval = locationRequestFastestInterval
            interval = locationRequestInterval
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    fun unregisterCurrentLocationUdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    //registriert einen receiver um festzustellen ob sich die Eistellungen zu den Standortdiensten ändern
    fun registerLocationSettingsBroadcastReceiver() {

        if (locationSettingsBroadcastReceiver != null)
            return

        val intentFilter = IntentFilter()
        intentFilter.addAction("android.location.PROVIDERS_CHANGED");
        locationSettingsBroadcastReceiver = object: BroadcastReceiver() {

            override fun onReceive(context: Context?, intent: Intent?) {

                if (intent == null)
                    return

                val action = intent.getAction();
                if (action == null || !action.equals("android.location.PROVIDERS_CHANGED"))
                    return

                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                    return

                registerCurrentLocationUpdates()
            }
        }

        registerReceiver(locationSettingsBroadcastReceiver, intentFilter)
    }

    fun unregisterLocationSettingsBroadcastReceiver() {

        if (locationSettingsBroadcastReceiver != null)
            return

        unregisterReceiver(locationSettingsBroadcastReceiver)
    }

    //endregion



    //region Location Methods

    fun enableLocationTracking() {

        if (!Permission.checkPermission(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), Permission.ROUTE_PERMISSION))
            return
        if (!checkLocationProviderEnabled())
            return

        trackPosition = true
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        stopButton.visibility = View.VISIBLE
        startButton.visibility = View.GONE
    }
    fun disableLocationTracking() {

        trackPosition = false
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        startButton.visibility = View.VISIBLE
        stopButton.visibility = View.GONE

        route.setRoutePoints(currentLocations)
        database.save(route)
    }

    fun createLocationMarkerAndMove() {

        var currentMapZoom = mapZoom
        if (lastLocationMarker != null) {

            lastLocationMarker!!.remove()
            currentMapZoom = map.getCameraPosition().zoom;
        }

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLocation, currentMapZoom))
        lastLocationMarker = map.addMarker(MarkerOptions().position(lastLocation))
    }
    fun createLocationRoute() {

        if (currentLocationsPolyline != null)
            currentLocationsPolyline!!.remove()
        if (currentLocationsPolylineMarker != null)
            currentLocationsPolylineMarker!!.remove()


        var polylineOptions = PolylineOptions()
        polylineOptions.addAll(currentLocations)
        polylineOptions.width(12f);
        polylineOptions.color(Color.BLUE);
        polylineOptions.geodesic(true);


        currentLocationsPolyline = map.addPolyline(polylineOptions)

        if (currentLocations.count() == 0)
            return

        val currentLocationsPolylineMarkerOptions = MarkerOptions()
            .position(currentLocations.last())
            .title("Distanz %.2f m".format(calculateDistance()))
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.transarent))

        currentLocationsPolylineMarker = map.addMarker(currentLocationsPolylineMarkerOptions)
        showDistanceInfo()
    }

    fun createMarker(picture: Picture) {

        map.addMarker(
            MarkerOptions()
                .position(LatLng(picture.latitude, picture.longitude))
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



    //region Methods

    fun initilizeMap() {

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    fun initializeTrip() {

        val bundle = getIntent().getExtras()
        if (bundle == null)
            return

        trip = database.selectById<Trip>(bundle.getLong("id", 0L))
        if (trip == null)
            return

        val routes = database.selectByForeignKey<Route, Trip>(trip!!)
        if (routes.count() > 0) {
            route = routes[0]
        }
        else {
            route.tripId = trip!!.id
        }

        val imageView = findViewById<ImageView>(R.id.tvTripsImage)
        imageView.setImageBitmap(trip!!.image)

        val textView = findViewById<TextView>(R.id.tvTripsTitle)
        textView.text = trip!!.title
    }

    fun takePhoto() {

        if (!Permission.checkPermission(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), Permission.LOCATION_PERMISSION))
            return
        if (!checkLocationProviderEnabled())
            return

        if (!::lastLocation.isInitialized) {

            Toast.makeText(this, R.string.maps_no_location, Toast.LENGTH_LONG).show()
            return
        }

        //zwischenspeichern der aktuellen Punkte
        route.setRoutePoints(currentLocations)

        //Öffnen der Aktivität um ein neues Foto hinzuzufügen
        val mainIntent = Intent(this@MapsActivity, PictureNewActivity::class.java)
        mainIntent.putExtra("id", trip!!.id)
        mainIntent.putExtra("lat", lastLocation.latitude)
        mainIntent.putExtra("lng", lastLocation.longitude)

        val addresses = geocoder.getFromLocation(lastLocation.latitude, lastLocation.longitude, 1);
        if (addresses.count() != 0)
            mainIntent.putExtra("city", addresses[0].locality)

        this@MapsActivity.startActivity(mainIntent)
    }

    fun loadPictures() {

        if (!mapReady)
            return

        map.clear()

        for (picture in database.selectByForeignKey<Picture, Trip>(trip!!))
            createMarker(picture)
    }

    fun loadRoute() {

        if (!mapReady)
            return

        currentLocations = route.getRoutePoints()
        createLocationRoute()
    }

    fun calculateDistance(): Float {

        var distance = 0.0f
        var previous: LatLng? = null

        for (point in currentLocations) {

            if (previous == null) {

                previous = point
                continue
            }

            val results = FloatArray(1)
            Location.distanceBetween(previous.latitude, previous.longitude, point.latitude, point.longitude, results);

            distance += results[0]
            previous = point
        }

        return distance
    }

    fun showDistanceInfo() {

        if (currentLocationsPolylineMarker == null)
            return

        currentLocationsPolylineMarker!!.showInfoWindow()
    }

    //endregion


}
