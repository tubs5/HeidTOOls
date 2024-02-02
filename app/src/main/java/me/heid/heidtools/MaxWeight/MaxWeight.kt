package me.heid.heidtools.MaxWeight

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import androidx.core.view.get
import com.google.android.gms.tasks.Task
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_max_weight.*
import kotlinx.android.synthetic.main.activity_speed.myTb
import me.heid.heidtools.R
import me.heid.heidtools.Toolbar.BaseToolbarActivity


class MaxWeight : BaseToolbarActivity() {
    var dataStore: DatabaseReference? = null;
    var database: FirebaseDatabase? = null;

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        setContentView(R.layout.activity_max_weight)
        database = FirebaseDatabase.getInstance()
        database!!.setPersistenceEnabled(true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        setContentView(R.layout.activity_max_weight)
        val myTb = findViewById<View>(R.id.myTb) as Toolbar

        setSupportActionBar(myTb)
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "id123")
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "name.nameson")
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "image")
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)

    }

    override fun onStart() {
        database = FirebaseDatabase.getInstance()
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        LoadView()
    }

    private fun LoadView() {
        if (FirebaseAuth.getInstance().currentUser != null) {
            database = FirebaseDatabase.getInstance()

            val uid = FirebaseAuth.getInstance().currentUser?.uid
            dataStore = database!!.getReference("users/" + uid + "/data/weight/")
            dataStore!!.get().addOnSuccessListener {
                Log.i("MaxWeight", "Got value ${it.value}")
                if (it.childrenCount == 0.toLong()) {
                    Log.w("MaxWeight", "user has no data, Creating basics")
                    setupUser()
                } else {
                    Log.w("MaxWeight", "Data found loading basics")
                    setupWorkoutTypes(it.children, "users/" + uid + "/data/weight/");
                }
            }.addOnFailureListener {
                Log.e("MaxWeight", "Error getting data", it)
                Toast.makeText(baseContext, "Failed To load data", Toast.LENGTH_LONG).show()
            }
        }
    }

    @SuppressLint("NewApi")
    private fun setupWorkoutTypes(
        children: Iterable<DataSnapshot>,
        path: String
    ) {

        var data = ArrayList<String?>();
        children.forEach { dataSnapshot: DataSnapshot -> data.add(String(Base64.decode(dataSnapshot.key,Base64.NO_WRAP))) }
        data.add("Create Another Workout Type")
        var arrayad = ArrayAdapter(this, android.R.layout.simple_spinner_item, data)
        mwwt.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View,
                pos: Int,
                l: Long
            ) {
                if (pos == data.size-1) {
                    createNewWorkoutType(path)
                } else {
                    setupWorkoutMaxes(data[pos])
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {
                return
            }
        }
        mwwt.adapter = arrayad;
        mwwt.invalidate()
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun onButtonShowPopupWindowClick(
        view: View,
        path: String,
        hint: String,
        inputType: Int,
        value:Boolean  = false
    ){
        val (popupWindow, text, button) = setupPopup(view)
        text.hint = hint;
        text.inputType = inputType
        text.invalidate()

        button.setOnClickListener { v: View? ->
            popupWindow.dismiss();
            if (FirebaseAuth.getInstance().currentUser != null) {
                database = FirebaseDatabase.getInstance()
                var da: Task<Void>
                if(value){
                    val vae = mapOf<String,String>(System.currentTimeMillis().toString() to text.text.toString())
                     da= database!!.getReference(path).updateChildren(vae)
                }else{
                     da= database!!.getReference(path+ "/"+ Base64.encodeToString(text.text.toString().toByteArray(),Base64.NO_WRAP)).setValue(0)
                }


                    da.addOnSuccessListener { void ->
                    Log.d(
                        "MaxWeight",
                        "onButtonShowPopupWindowClick: Added "+ text.text.toString() + "\t"+path
                    )
                    var id = FirebaseAuth.getInstance().uid
                    if (id != null) {
                        var str = path.substring(path.indexOf("weight")).split("/")
                        if(str.size == 1){
                            if (str[1] != ""){
                                LoadView()

                            }else{
                                setupWorkoutMaxes(String(Base64.decode(str[1],Base64.NO_WRAP)),text.text.toString())
                            }

                        }else if (str.size == 2){
                            setupWorkoutMaxes(String(Base64.decode(str[1],Base64.NO_WRAP)),text.text.toString())
                        }else{
                            setupData(String(Base64.decode(str[1],Base64.NO_WRAP)),String(Base64.decode(str[2],Base64.NO_WRAP)));
                        }
                    }
                }

            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun setupPopup(view: View): Triple<PopupWindow, EditText, Button> {
        val inflater =
            getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView: View = inflater.inflate(R.layout.simplepopup, null)


        val width = LinearLayout.LayoutParams.WRAP_CONTENT
        val height = LinearLayout.LayoutParams.WRAP_CONTENT
        val focusable = true // lets taps outside the popup also dismiss it

        val popupWindow = PopupWindow(popupView, width, height, focusable)

        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        val text = popupView.requireViewById<EditText>(R.id.spText)
        val button = popupView.requireViewById<Button>(R.id.spButton)
        return Triple(popupWindow, text, button)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun createNewWorkoutType(s: String) {

        onButtonShowPopupWindowClick(findViewById(R.id.mwLayout),s,"New Workout Type Name",EditorInfo.TYPE_CLASS_TEXT);
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun createNewWorkout(path:String) {
        onButtonShowPopupWindowClick(findViewById(R.id.mwLayout),path,"new Workout Name",EditorInfo.TYPE_CLASS_TEXT);
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun setupWorkoutMaxes(s: String?, current:String = "") {
        if (FirebaseAuth.getInstance().currentUser != null) {
            database = FirebaseDatabase.getInstance()
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            dataStore =
                database!!.getReference("users/" + uid + "/data/weight/" +  Base64.encodeToString(s?.toByteArray(),Base64.NO_WRAP))
            dataStore!!.get().addOnSuccessListener {
                loadWorkoutMaxes(it.children,s,"users/" + uid + "/data/weight/" +  Base64.encodeToString(
                    s?.toByteArray(),Base64.NO_WRAP),current)

            }.addOnFailureListener {
                Log.e("MaxWeight", "Error getting data: setupWorkoutMaxes", it)
                Toast.makeText(baseContext, "Failed To load data", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupUser() {
        val wt = arrayOf<String>("1 Rep Max", "3 Rep Max", "5 Rep Max","Custom")

        database = FirebaseDatabase.getInstance()

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        dataStore =
            database!!.getReference("users/" + uid + "/data/weight")
        for (s in wt){
            database!!.getReference("users/" + uid + "/data/weight/"+ Base64.encodeToString(s.toByteArray(),Base64.NO_WRAP) ).setValue(0).addOnSuccessListener {
                Log.d(
                    "Maxweight",
                    "setupUser: "+ s + ": added"
                ) }
        }
        LoadView()
    }


    @RequiresApi(Build.VERSION_CODES.P)
    private fun loadWorkoutMaxes(children: MutableIterable<DataSnapshot>, s: String?, path:String,current:String = "") {
        var data = ArrayList<String?>();
        children.forEach { dataSnapshot: DataSnapshot -> data.add(String(Base64.decode(dataSnapshot.key,Base64.NO_WRAP))) }
        data.add("Create Another Workout Type")
        var arrayad = ArrayAdapter(this, android.R.layout.simple_spinner_item, data)
        mwwn.adapter = arrayad;

        mwwn.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View,
                pos: Int,
                l: Long
            ) {
                if (pos == data.size-1) {
                    createNewWorkout(path)
                } else {
                    mwsn.setOnClickListener {  v ->
                        onButtonShowPopupWindowClick(findViewById(R.id.mwLayout),
                            path!! + "/"+ Base64.encodeToString(arrayad.getItem(pos)?.toByteArray(),Base64.NO_WRAP) ,"Enter new max",EditorInfo.TYPE_CLASS_NUMBER or EditorInfo.TYPE_NUMBER_FLAG_DECIMAL,value = true);
                    }
                    setupData(s,data[pos])
                }
            }


            override fun onNothingSelected(adapterView: AdapterView<*>?) {
                return
            }
        }

        mwwn.invalidate()
        mwwn.setSelection(arrayad.getPosition(current))
    }

    private fun setupData(s: String?,s2: String?) {
        if (FirebaseAuth.getInstance().currentUser != null) {
            database = FirebaseDatabase.getInstance()

            val uid = FirebaseAuth.getInstance().currentUser?.uid
            dataStore =
                database!!.getReference("users/" + uid + "/data/weight/" +  Base64.encodeToString(s?.toByteArray(),Base64.NO_WRAP) +"/"+ Base64.encodeToString(
                    s2?.toByteArray(),Base64.NO_WRAP))
            dataStore!!.get().addOnSuccessListener {
                displayData(it.children, s ,s2)

            }.addOnFailureListener {
                Log.e("MaxWeight", "Error getting data: setupWorkoutMaxes", it)
                Toast.makeText(baseContext, "Failed To load data", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun displayData(children: Iterable<DataSnapshot>, s: String?, s2: String?) {
        var s = StringBuilder();
        children.forEach { dataSnapshot: DataSnapshot -> s.append(dataSnapshot.key + ":" + dataSnapshot.value + "\n") }
        mwnmTEXTVIEW.setText(s.toString())
        mwnmTEXTVIEW.invalidate()
    }


}