package me.heid.heidtools.main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import me.heid.heidtools.MaxWeight.MaxWeight
import me.heid.heidtools.MaxWeight.MaxWeightv2
import me.heid.heidtools.NoteCreator.NotCreator
import me.heid.heidtools.R
import me.heid.heidtools.speedApp.SpeedActivity
import me.heid.heidtools.speedApp.speedDurationPicker
import me.heid.heidtools.stream.encode.CamStream
import me.heid.heidtools.stream.chat.ChatClient
import me.heid.heidtools.work.WorkMain

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        setContentView(R.layout.activity_main)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        recycleView.setHasFixedSize(true)
        recycleView.layoutManager = LinearLayoutManager(this)


        data = ArrayList();
        data.add(
            MainRecycleViewData(
                "Simple Notification",
                R.drawable.dash,
                View.OnClickListener { v: View? ->
                    val i = Intent(
                        v!!.context,
                        NotCreator::class.java
                    ); startActivity(i)
                })
        )
        data.add(
            MainRecycleViewData(
                "Speed App",
                R.drawable.ic_launcher_foreground,
                View.OnClickListener { v: View? ->
                    val i = Intent(v!!.context, SpeedActivity::class.java); startActivity(i)
                })

        )
        data.add(
            MainRecycleViewData(
                "MaxWeight App",
                R.drawable.common_full_open_on_phone,
                View.OnClickListener { v: View? ->
                    val i = Intent(v!!.context, MaxWeight::class.java); startActivity(i)
                })
        )
        data.add(
            MainRecycleViewData(
                "MaxWeight2 App",
                R.drawable.common_full_open_on_phone,
                View.OnClickListener { v: View? ->
                    val i = Intent(v!!.context, MaxWeightv2::class.java); startActivity(i)
                })
        )
        data.add(
            MainRecycleViewData(
                "Chart App",
                R.drawable.common_full_open_on_phone,
                View.OnClickListener { v: View? ->
                    val i = Intent(v!!.context, speedDurationPicker::class.java); startActivity(i)
                })
        )
        data.add(
            MainRecycleViewData(
                "Wifi app",
                R.drawable.common_full_open_on_phone,
                View.OnClickListener { v: View? ->
                    val i = Intent(v!!.context, ChatClient::class.java); startActivity(i)
                })
        )
        data.add(
            MainRecycleViewData(
                "Cam app",
                R.drawable.common_full_open_on_phone,
                View.OnClickListener { v: View? ->
                    val i = Intent(v!!.context, CamStream::class.java); startActivity(i)
                })
        )
        data.add(
            MainRecycleViewData(
                "Work app",
                R.drawable.common_full_open_on_phone,
                View.OnClickListener { v: View? ->
                    val i = Intent(v!!.context, WorkMain::class.java); startActivity(i)
                })
        )
        // specify an viewAdapter (see also next example)
        recycleView.adapter = MyAdapter(data)

        recycleView.invalidate();
    }

    private lateinit var data:ArrayList<MainRecycleViewData>


}
