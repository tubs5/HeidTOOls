package me.heid.heidtools.speedApp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.speedhistory.*
import me.heid.heidtools.NoteCreator.NotCreator
import me.heid.heidtools.R
import me.heid.heidtools.main.MainRecycleViewData
import me.heid.heidtools.main.MyAdapter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SpeedhistoryActivity : AppCompatActivity() {

    var UPDATE_INTERVALL:Long = 5*1000; // time between updates
    var dataStore: DatabaseReference? =null;
    var database: FirebaseDatabase? = null;
    var str = "";
    val dt = DateTimeFormatter.ofPattern("yyyy/MM/dd/HH/mm/ss")

    private lateinit var data:ArrayList<MainRecycleViewData>

    override fun onCreate(savedInstanceState: Bundle?) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        var path:String;
        if(intent.extras != null){
           path = intent.extras?.getString("path","users/"+uid+"/data/gps/").toString()
        }else{

            path = "users/"+uid+"/data/gps/"
        }
        str = path;


        loadDB(path!!)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.speedhistory)

        recycleView2.setHasFixedSize(true)
        recycleView2.layoutManager = LinearLayoutManager(this)


        data = ArrayList();


        // specify an viewAdapter (see also next example)
        recycleView2.adapter = MyAdapter(data)

        recycleView2.invalidate();




    }


    fun loadDB(path: String) {
        database = FirebaseDatabase.getInstance()

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val ld = LocalDateTime.now()
        //dataStore = database!!.getReference("users/"+uid+"/data/gps/2022/01/19/17")
        dataStore = database!!.getReference(path)
        dataStore!!.get().addOnSuccessListener {
            var value = it.value as HashMap<*, *>
            for (a in value) {

                data.add(
                    MainRecycleViewData(
                        a.key.toString(),
                        R.drawable.dash,
                        View.OnClickListener { v: View? ->

                            Log.d("TAG", "loadDB:" +str.split("/").count())
                            if(str.split("/").count() < 8) {


                                val i = Intent(
                                    v!!.context,
                                    SpeedhistoryActivity::class.java
                                );
                                i.putExtra("path", str + "/" + a.key.toString())
                                Log.d("loadDB", str + "/" + a.key.toString())
                                startActivity(i)
                            }


                        })
                )
            }
            recycleView2.adapter = MyAdapter(data)

            recycleView2.invalidate();
            Log.d("aaaaaaa", it.value.toString());
        }




    }



}