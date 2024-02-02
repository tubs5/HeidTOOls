package me.heid.heidtools.speedApp

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.android.synthetic.main.activity_speed.*
import me.heid.heidtools.Toolbar.BaseToolbarActivity


class SpeedActivity() : BaseToolbarActivity() {


    var lon: TextView? = null;
    var lan: TextView? = null;
    var head: TextView? = null;
    var speed: TextView? = null;
    var height: TextView? = null;



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(me.heid.heidtools.R.layout.activity_speed)
        setSupportActionBar(myTb as Toolbar)

        lon = longt
        lan = langt
        head = heading
        speed = speedtv
        height = heightg
        registerReceiver(br, IntentFilter("me.tubs.text"))

       var  mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "id123")
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "name.nameson")
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "image")
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)


    }

    override fun onStart() {
        super.onStart()

        var penint = PendingIntent.getActivity(this,0,this.intent,PendingIntent.FLAG_MUTABLE)
        var myIntent= Intent(applicationContext, SpeedService::class.java)
        myIntent.putExtra("intent",penint)

        button2.setOnClickListener{
            startForegroundService(myIntent)
            Toast.makeText(this,"Started Service",Toast.LENGTH_SHORT).show();
        }
        button.setOnClickListener {
            var inf = IntentFilter()
            if(stopService(myIntent)){
                Toast.makeText(this,"Stopped Service",Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this,"FAILED to Stop Service",Toast.LENGTH_LONG).show();
            }

        }
        history.setOnClickListener {
        val intent = Intent(this,SpeedhistoryActivity::class.java);
        startActivity(intent);
        }

    }


    internal var br: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            lon?.text =   intent.getStringExtra("lon")
            lan?.text =   intent.getStringExtra("lat")
            head?.text =   intent.getStringExtra("head")
            speed?.text =   intent.getStringExtra("speed")
            height?.text =   intent.getStringExtra("height")

        }
    }

}
