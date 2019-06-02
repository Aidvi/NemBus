package com.example.myproject.app


import android.Manifest
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myproject.R
import com.example.myproject.app.config.ApiConfig
import com.example.myproject.app.gui.adapter.OnItemClickListener
import com.example.myproject.app.gui.adapter.StopLocationAdapter
import com.example.myproject.app.model.LocationResponse
import com.example.myproject.app.model.StopLocation
import com.example.myproject.app.services.MoviaApiService
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Response

open class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, OnItemClickListener<StopLocation> {

    // TAG
    private val TAG = "MainActivity"

    // Api Service
    private val apiConfig = ApiConfig()
    private val apiService = MoviaApiService.create(apiConfig)

    // Location
    private lateinit var locationManager: LocationManager
    private lateinit var mLocation: Location
    // private val listener: com.google.android.gms.location.LocationListener? = null
    private var mLocationRequest: LocationRequest? = null
    private var mLocationManager: LocationManager? = null
    private val updateInterval = (2 * 1000).toLong()  /* 10 secs */
    private val intervalSpeed: Long = 2000 /* 2 sec */

    // Google Maps
    private lateinit var mMap: GoogleMap
    private lateinit var mGoogleApiClient: GoogleApiClient

    // Recycler view
    private lateinit var viewAdapter: StopLocationAdapter

    // Call
    private var stopLocationCall: Call<LocationResponse>? = null

    // Response from API
    //private lateinit var myDataset: ArrayList<StopLocation>


    override fun onStart() {
        super.onStart()
        mGoogleApiClient.connect()
    }

    override fun onStop() {
        super.onStop()
        if (mGoogleApiClient.isConnected) {
            mGoogleApiClient.disconnect()
        }
    }

    override fun onConnectionSuspended(p0: Int) {

        Log.i(TAG, "Connection Suspended")
        mGoogleApiClient.connect()
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.i(TAG, "Connection failed. Error: " + connectionResult.errorCode)
    }

    override fun onLocationChanged(location: Location) {

        //var msg = "Updated Location: Latitude " + location.longitude.toString() + location.longitude;

        //  var utm = Deg2UTM(location.latitude, location.longitude)
        // val easting = utm.easting
        // val northing = utm.northing

        //Log.e(TAG, "UTM Easting: " + easting.toString())
        //Log.e(TAG, "UTM Northing: " + northing.toString())

        //Log.e(TAG, "LATITUDE: " + location.latitude.toString())
        //Log.e(TAG, "LONGTITUDE: " + location.longitude.toString())

        //txt_latitude.setText(""+location.latitude);
        //txt_longitude.setText(""+location.longitude);
        //Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    override fun onConnected(p0: Bundle?) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 8)
        }
        startLocationUpdates()

        val fusedLocationProviderClient:
                FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationProviderClient.lastLocation
                .addOnSuccessListener(this) { location ->
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        // Logic to handle location object
                        mLocation = location

                        onReady()
                    }
                }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()

        mLocationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        with(recyclerView) {
            setHasFixedSize(true)
            viewAdapter = StopLocationAdapter()
            adapter = viewAdapter
            viewAdapter.listener = this@MainActivity
            layoutManager = LinearLayoutManager(context)
        }

        swipeRefreshLayout.setOnRefreshListener {

            apiCall()
        }

        checkLocation()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style_map))
            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            } else {
                Log.e(TAG, "Map style success")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }
    }

    private fun onReady() {

        if (::mMap.isInitialized && ::mLocation.isInitialized) {

            val myLocation = LatLng(mLocation.latitude, mLocation.longitude)
            //mMap.addMarker(MarkerOptions().position(myLocation).title("Marker"))
            mMap.clear()
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 17.toFloat()))

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            mMap.isMyLocationEnabled = true

            apiCall()
        }
    }

    private fun apiCall() {

        stopLocationCall = apiService.getAllStops(mLocation.latitude, mLocation.longitude, 500, 30).also {
            it.enqueue(object : retrofit2.Callback<LocationResponse> {
                override fun onFailure(call: Call<LocationResponse>, t: Throwable) {
                    Log.e(TAG, "FAIL API REJSEPlANEN: ")
                    t.printStackTrace()
                }
                override fun onResponse(call: Call<LocationResponse>, response: Response<LocationResponse>) {
                    response.body()?.let { r ->
                        val myDataSet = r.locationList.StopLocation
                        viewAdapter.setDataSet(myDataSet)

                        swipeRefreshLayout.isRefreshing = false
                    }
                    stopLocationCall = null
                }
            })
        }
    }

    override fun onDestroy() {
        stopLocationCall?.let {
            it.cancel()
            stopLocationCall = null
        }
        super.onDestroy()
    }


    override fun onItemClick(item: StopLocation) {
        val intent = Intent(this@MainActivity, StopInfoActivity::class.java)
        val options = ActivityOptions.makeSceneTransitionAnimation(this)
        intent.putExtra("stopLocationId", item.id)
        startActivity(intent, options.toBundle())
    }

    private fun checkLocation(): Boolean {
        if (!isLocationEnabled()) {
            println("Fail")
        }
        return isLocationEnabled()
    }

    private fun isLocationEnabled(): Boolean {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun startLocationUpdates() {

        // Create the location request
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(updateInterval)
                .setFastestInterval(intervalSpeed)
        // Request location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this)
    }
}