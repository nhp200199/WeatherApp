package com.example.weatherapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : BaseActivity() {
    private val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    private lateinit var fusedLocationLocation: FusedLocationProviderClient
    private var coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(isLocationEnabled()) {
            Toast.makeText(this, "Location enabled", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Location not enabled", Toast.LENGTH_SHORT).show()
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }

        val ivImage: ImageView = findViewById(R.id.ivImage)
        ivImage.setImageResource(R.drawable.sb_alexa_logo1)

        requestUserPermission(permissions)
    }

    private fun requestUserPermission(permissions: Array<String>) {
        val permissionsNeedToGrant = permissions.filter { !hasPermission(it) }

        if (permissionsNeedToGrant.isEmpty()) {
            if (hasInternetConnection(this)) {
                Toast.makeText(this, "Internet connected", Toast.LENGTH_SHORT).show()
                fusedLocationLocation = LocationServices.getFusedLocationProviderClient(this)
                try {
                    fusedLocationLocation.lastLocation
                        .addOnSuccessListener { location: Location? ->
                            if (location == null) {
                                Toast.makeText(this, "empty location", Toast.LENGTH_SHORT).show()
                            } else {
                                coroutineScope.launch {
                                    showDialog("this is title", "this is mesage", null, null)
                                    val response = withContext(Dispatchers.Main) {
                                        getWeatherData(location.latitude, location.longitude)
                                    }
                                    hideDialog()
                                    Log.i("TAG", response.toString())
                                }
                            }
                        }
                } catch (e: SecurityException) {
                    Toast.makeText(this, "exception: dont have permission", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Internet not connected", Toast.LENGTH_SHORT).show()
            }
            return
        }

        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            showRequestPermissionRationaleDialog()
        } else {
            requestPermissions(permissionsNeedToGrant.toTypedArray(), REQ_LOCATION)
        }
    }

    private suspend fun getWeatherData(lat: Double, lon: Double): WeatherResponse {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ApiService::class.java)
        return service.getWeather(lat, lon, resources.getString(R.string.API_KEY))
    }

    private fun showRequestPermissionRationaleDialog() {
        AlertDialog.Builder(this)
            .setMessage("We really need this permission, please grant it to us to use this feature")
            .setNegativeButton("Deny"
            ) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Understood") {_, _ ->
                requestPermissions(permissions, REQ_LOCATION)
            }
            .show()
    }

    private fun hasPermission(permission: String): Boolean {
        return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_LOCATION) {
            var locationsGranted = true;
            for (i in permissions.indices) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) locationsGranted = false
            }
            if (locationsGranted) {
                Toast.makeText(this, "Location granted", Toast.LENGTH_SHORT).show()
                if (hasInternetConnection(this)) {
                    Toast.makeText(this, "Internet connected", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Internet not connected", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Location not granted", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun hasInternetConnection(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

             return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            return connectivityManager.activeNetworkInfo ?.isConnectedOrConnecting ?: return false
        }
    }

    companion object {
        const val REQ_LOCATION = 111
    }
}