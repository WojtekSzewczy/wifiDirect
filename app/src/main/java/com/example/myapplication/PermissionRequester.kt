package com.example.myapplication

import android.Manifest
import android.app.Activity
import android.os.Build
import androidx.core.app.ActivityCompat

class PermissionRequester(activity: Activity) {
    val permissions = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S) arrayOf(
        Manifest.permission.CHANGE_WIFI_STATE,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.CHANGE_NETWORK_STATE,
        Manifest.permission.INTERNET,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
    ) else arrayOf(
        Manifest.permission.CHANGE_WIFI_STATE,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.CHANGE_NETWORK_STATE,
        Manifest.permission.INTERNET,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.MANAGE_EXTERNAL_STORAGE,
    )
    val mapa = HashMap<String, Int>()


    init {
        var counter = 0
        permissions.forEach {
            ActivityCompat.requestPermissions(activity, arrayOf(it), counter)
            counter++
        }
    }

}