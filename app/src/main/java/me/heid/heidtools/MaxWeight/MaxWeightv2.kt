package me.heid.heidtools.MaxWeight

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import com.google.android.gms.tasks.Task
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_max_weightv2.*
import me.heid.heidtools.R
import me.heid.heidtools.Toolbar.BaseToolbarActivity
import java.time.format.DateTimeFormatter
import kotlin.collections.ArrayList


class MaxWeightv2 : BaseToolbarActivity() {
    var dataStore: DatabaseReference? = null;
    var database: FirebaseDatabase? = null;
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        setContentView(R.layout.activity_max_weightv2)
        super.onCreate(savedInstanceState)

        database = FirebaseDatabase.getInstance()
        database!!.setPersistenceEnabled(true)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_max_weightv2)
        setSupportActionBar(myTb as Toolbar)
        super.onCreate(savedInstanceState)
        var mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);



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
        mwsn2.visibility = Button.INVISIBLE
        LoadView()
    }
    private fun LoadView() {
        if (FirebaseAuth.getInstance().currentUser != null) {
            database = FirebaseDatabase.getInstance()

            val uid = FirebaseAuth.getInstance().currentUser?.uid
            dataStore = database!!.getReference("users/" + uid + "/data/weight")
            dataStore!!.get().addOnSuccessListener {
                Log.i("MaxWeight", "Got value ${it.value}")
                if (it.childrenCount == 0.toLong()) {
                    Log.w("MaxWeight", "user has no data")
                    //setupUser()
                } else {
                    Log.w("MaxWeight", "Data found loading basics")

                }
                setupWorkoutTypes(it.children, "users/" + uid + "/data/weight");
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
        var dataEncoded = ArrayList<DataSnapshot?>();
        children.forEach { dataSnapshot: DataSnapshot -> data.add(String(
            Base16.decode(dataSnapshot.key,
                Base16.NO_WRAP)))
            dataEncoded.add(dataSnapshot)
            Log.d("MAX WEIGHT", "setupWorkoutTypes: " +String(
                    Base16.decode(dataSnapshot.key,
                        Base16.NO_WRAP)))
        }

        var arrayad = ArrayAdapter(this, android.R.layout.select_dialog_item, data)
        MWE.nextFocusDownId =  R.id.MWR;
        MWE.setAdapter(arrayad);
        MWE.onItemClickListener = AdapterView.OnItemClickListener { parent, view, pos, id ->
            setupWorkoutMaxes(dataEncoded[pos],path+"/"+ dataEncoded[pos]!!.key)
        }
        MWE.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            val s = data.find { s ->  s!!.toLowerCase() == v.text.toString().toLowerCase() }
            if(s == null){
                setupWorkoutMaxes(null,path+"/"+ Base16.encodeToString(v.text.toString().toByteArray(),
                    Base16.NO_WRAP));
            }
            else{
                val doe = dataEncoded[data.indexOf(s)]
                setupWorkoutMaxes(doe,path+"/"+ doe!!.key)
            }

            true
        })

        MWE.invalidate()
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
                    da= database!!.getReference(path+ "/"+ Base16.encodeToString(text.text.toString().toByteArray(),
                        Base16.NO_WRAP)).setValue(0)
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
                        //        setupWorkoutMaxes(String(Base16.decode(str[1],Base16.NO_WRAP)),text.text.toString())
                            }

                        }else if (str.size == 2){
                      //      setupWorkoutMaxes(String(Base16.decode(str[1],Base16.NO_WRAP)),text.text.toString())
                        }else{
                            setupData(String(
                                Base16.decode(str[1],
                                    Base16.NO_WRAP)),String(
                                Base16.decode(str[2],
                                    Base16.NO_WRAP)));
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
        text.requestFocus()
        val button = popupView.requireViewById<Button>(R.id.spButton)
        return Triple(popupWindow, text, button)
    }
    @RequiresApi(Build.VERSION_CODES.P)
    private fun setupWorkoutMaxes(data: DataSnapshot?, path:String) {

        loadWorkoutMaxes(data?.children,path)

    }


    @RequiresApi(Build.VERSION_CODES.P)
    private fun loadWorkoutMaxes(children: MutableIterable<DataSnapshot>?, path:String) {
        var data = ArrayList<String?>();
        val dataEncoded = ArrayList<DataSnapshot>();
        if(children != null) {
            children.forEach { dataSnapshot: DataSnapshot ->
                data.add(
                    String(
                        Base16.decode(
                            dataSnapshot.key,
                            Base16.NO_WRAP
                        )
                    )
                )
                dataEncoded.add(dataSnapshot)
                Log.d("MAX WEIGHT V2", "loadWorkoutMaxes: "+ String(
                    Base16.decode(
                        dataSnapshot.key,
                        Base16.NO_WRAP
                    )
                ))
            }

            var arrayad = ArrayAdapter(this, android.R.layout.simple_spinner_item, data)
            MWR.setAdapter(arrayad)

            MWR.setOnItemClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
                val item = dataEncoded.find { dataSnapshot -> String(
                    Base16.decode(dataSnapshot.key,
                        Base16.NO_WRAP)).toLowerCase() == data[position]!!.toLowerCase()}
                OnRepsFilled(path+"/"+ String(Base16.encode((view as AppCompatTextView).text.toString().toByteArray(),
                    Base16.NO_WRAP)), item)
            }
        }
        MWR.setOnFocusChangeListener { v, hasFocus ->
            if(hasFocus){
                MWR.showDropDown()
            }
        }
        MWR.requestFocus()
        MWR.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View,
                pos: Int,
                l: Long
            ) {
                val item = dataEncoded.find { dataSnapshot -> String(
                    Base16.decode(dataSnapshot.key,
                        Base16.NO_WRAP)).toLowerCase() == data[pos]!!.toLowerCase()}
                OnRepsFilled(path+"/"+ String(Base16.encode((view as AppCompatTextView).text.toString().toByteArray(),
                    Base16.NO_WRAP)), item)

            }
            override fun onNothingSelected(adapterView: AdapterView<*>?) {

                return
            }
        }
        MWR.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            val item = dataEncoded.find { dataSnapshot -> String(
                Base16.decode(dataSnapshot.key,
                    Base16.NO_WRAP)).toLowerCase() == v.text.toString().toLowerCase()}

            OnRepsFilled(path+"/"+ String(Base16.encode(MWR.text.toString().toByteArray(),
                Base16.NO_WRAP)),item)

            //OnRepsFilled(path+"/"+ Base16.encodeToString(MWR.text.toString().toByteArray(), Base16.NO_WRAP), MWR.text.toString(),"")

            true
        })

        MWR.invalidate()
        //MWR.setSelection(arrayad.getPosition(current))

    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun OnRepsFilled(
        path: String,
        data: DataSnapshot?
    ) {
        createNewMaxButton(path)
        setupData(data,path)
        removeKeyboard()
    }
    @RequiresApi(Build.VERSION_CODES.P)
    private fun OnRepsFilled(
        path: String,
        pos: String?,
        s: String?
    ) {
        createNewMaxButton(path)
        setupData(s, pos)
        removeKeyboard()
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun createNewMaxButton(path: String) {
        mwsn2.setOnClickListener { v ->
            onButtonShowPopupWindowClick(
                findViewById(R.id.mwLayout2),
                path,
                "Enter new max",
                EditorInfo.TYPE_CLASS_NUMBER or EditorInfo.TYPE_NUMBER_FLAG_DECIMAL,
                value = true
            );
        }
        mwsn2.visibility = EditText.VISIBLE;
        mwsn2.invalidate()
    }

    private fun removeKeyboard() {
        val imm =
            this.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view: View? = this.getCurrentFocus()
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(this)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun setupData(s: String?,s2: String?) {
        if (FirebaseAuth.getInstance().currentUser != null) {
            database = FirebaseDatabase.getInstance()

            val uid = FirebaseAuth.getInstance().currentUser?.uid
            dataStore =
                database!!.getReference("users/" + uid + "/data/weight/" +  Base16.encodeToString(s!!.toByteArray(),
                    Base16.NO_WRAP) +"/"+ Base16.encodeToString(
                    s2!!.toByteArray(), Base16.NO_WRAP))
            dataStore!!.get().addOnSuccessListener {
                displayData(it)

            }.addOnFailureListener {
                Log.e("MaxWeight", "Error getting data: setupWorkoutMaxes", it)
                Toast.makeText(baseContext, "Failed To load data", Toast.LENGTH_LONG).show()
            }
        }
    }
    private fun setupData(data: DataSnapshot?,path: String){
        if(data!= null) {
            displayData(data)
        }else Log.e("MaxWeight2", "setupData: Data is nonexistant" )
    }

    private fun displayData(children: DataSnapshot?) {
        val dt = DateTimeFormatter.ofPattern("yyyy/MM/dd")
        var s = StringBuilder();
        val values =  children!!.value as HashMap<String,String>
        for (it in values){
       // children.forEach {

                    var instance = java.time.Instant.ofEpochMilli(it.key!!.toLong());
                    var localDateTime = java.time.LocalDateTime
                        .ofInstant(instance, java.time.ZoneId.systemDefault());
                    s.append(localDateTime.format(dt))
                    s.append(":")
                    s.append(it.value)
                    s.append("\n")
        }
        MWSD.setText(s.toString())
        MWSD.invalidate()
    }
}