package me.heid.heidtools.speedApp

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.IBinder
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationServices.getFusedLocationProviderClient
import me.heid.heidtools.R
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.lang.Exception
import java.lang.RuntimeException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.coroutines.coroutineContext


class SpeedService : Service() {
    var dataStore: DatabaseReference? =null;
    var database: FirebaseDatabase? = null;
    var str = "";
    val dt = DateTimeFormatter.ofPattern("yyyy/MM/dd/HH/mm/ss")
    override fun onBind(intent: Intent?): IBinder? {
        println("test")
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.

    }

    override fun onCreate() {
        super.onCreate()
        // Write a message to the database
        database = FirebaseDatabase.getInstance()
        database!!.setPersistenceEnabled(true)

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val ld =LocalDateTime.now()
        dataStore = database!!.getReference("users/"+uid+"/data/gps/"+ld.format(dt))
        str = "users/$uid/data/gps/"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        var nc = NotificationChannel("SpeedService","Speed Service", NotificationManager.IMPORTANCE_DEFAULT)
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(nc)
        var not = Notification.Builder(this,"SpeedService").setContentTitle("Speed service")
            .setContentText("running").setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(intent?.getParcelableExtra("intent")).setTicker("tick").build()
        startForeground(234,not)

        setup()
        return super.onStartCommand(intent, flags, startId)

    }

    override fun onDestroy() {
        Log.w("Dest","ONDESTROY")
        getFusedLocationProviderClient(this).removeLocationUpdates(callback)
        super.onDestroy()

    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.w("Dest","ONUNBIND")
        return super.onUnbind(intent)
    }
    internal var callback:LocationCallback = object : LocationCallback() {

        override fun onLocationResult(locationResult: LocationResult) {
            if(locationResult != null){
                val la = locationResult.lastLocation;
                val dbin = SpeedDbFormat(la!!.time,la.latitude,la.longitude,la.altitude,la.speed,la.bearing,la.accuracy)
                val ld = LocalDateTime.now()
                database!!.getReference(str+"/"+ld.format(dt)).setValue(dbin)

            }
            onUpdate(locationResult!!.lastLocation!!)
            super.onLocationResult(locationResult)
            super.onLocationResult(locationResult)
        }
    }



    fun setup(){
        var locreq = LocationRequest()
        locreq.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        locreq.setInterval(15)
        locreq.setFastestInterval(1000)
        // Create LocationSettingsRequest object using location request
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(locreq)
        val locationSettingsRequest = builder.build()

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        val settingsClient = LocationServices.getSettingsClient(this)
        settingsClient.checkLocationSettings(locationSettingsRequest)


        Log.w("location", "LOCATION Satisfied")
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this,"NO GPS PERMISSIONS",Toast.LENGTH_LONG).show();
            Log.e("SPEEDSERVICE","NO GPS PERMISSION")
            return
        }
        getFusedLocationProviderClient(this).requestLocationUpdates(locreq,callback,mainLooper)

    }
    fun onUpdate(lastLocation: Location) {
        var intent = Intent("me.tubs.text")
        intent.putExtra("lat",lastLocation.latitude.toString())
        intent.putExtra("lon",lastLocation.longitude.toString())
        intent.putExtra("speed",lastLocation.speed.toString())
        intent.putExtra("head",lastLocation.bearing.toString())
        intent.putExtra("height",lastLocation.altitude.toString())
        sendBroadcast(intent)

    }

}