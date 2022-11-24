package com.example.weatherapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

class MainActivity : AppCompatActivity() {
    private val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

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