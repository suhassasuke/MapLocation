package com.wnet.maplocation

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener {

    private lateinit var mMap: GoogleMap
    private var mCurrentLocation: Location? = null
    private lateinit var locationManager: LocationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 400, 1000.0f, this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = false
        initMap()
    }

    fun initMap(){
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            checkLocationPermission()
            return
        }
        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.setOnCircleClickListener {
            Toast.makeText(baseContext, "radius clicked", Toast.LENGTH_LONG).show()
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, this)
//        mMap.addCircle(
//            CircleOptions()
//                .center(LatLng(12.886101,77.626191))
//                .fillColor(Color.argb(64, 0, 0, 0))
//                .strokeColor(Color.RED)
//                .strokeWidth(1.0f)
//                .radius(10000.0)
//        )
        mMap.setOnMapClickListener {
            val targetLocation = Location("") //provider name is unnecessary

            targetLocation.latitude = it.latitude

            targetLocation.longitude = it.longitude
            if (getDistanceFromLatLonInKm(mCurrentLocation!!,targetLocation)) {
                mMap.addMarker(MarkerOptions().position(it))
            }else{
                Toast.makeText(baseContext, "Marker OutSide the Radius Not Allowed...", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun getDistanceFromLatLonInKm(loc1: Location, loc2: Location): Boolean {
        val R = 6371; // Radius of the earth in km
//        var dLat = deg2rad(lat2-lat1);  // deg2rad below
//        var dLon = deg2rad(lon2-lon1);
        val dLat = deg2rad(loc2.latitude - loc1.latitude);  // deg2rad below
        val dLon = deg2rad(loc2.longitude - loc1.longitude);
        val a =
            Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(deg2rad(loc1.latitude)) * Math.cos(
                deg2rad(loc2.latitude)
            ) * Math.sin(dLon / 2) * Math.sin(dLon / 2)

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        val d = R * c; // Distance in km
        Log.i("Distance in KM", d.toString())
        return d <= (mCurrentLocation!!.accuracy.toDouble() / 1000)
    }

    fun deg2rad(deg: Double): Double {
        return deg * (Math.PI / 180)
    }

    private fun checkLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            1
        );
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if ((ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED)
                    ) {
                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                        initMap()
                    }
                } else {
                    Toast.makeText(this, "Can't use this app without the permission...", Toast.LENGTH_SHORT).show()
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        1
                    );
                }
                return
            }
        }
    }

    override fun onLocationChanged(location: Location?) {
        if (mCurrentLocation == null) {
            mCurrentLocation = location
            val latLng = LatLng(location!!.latitude, location.longitude)
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 18f)
            mMap.moveCamera(cameraUpdate)
//        locationManager.removeUpdates(this)
            mMap.addCircle(
                CircleOptions()
                    .center(latLng)
                    .fillColor(R.color.lime)
                    .strokeColor(Color.RED)
                    .strokeWidth(1.0f)
                    .radius(mCurrentLocation!!.accuracy.toDouble())
            )
        }
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        //("Not yet implemented")
    }

    override fun onProviderEnabled(provider: String?) {
        //("Not yet implemented")
    }

    override fun onProviderDisabled(provider: String?) {
        //("Not yet implemented")
    }
}