package me.heid.heidtools.speedApp

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import com.anychart.AnyChart
import com.anychart.AnyChartView
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.charts.Cartesian
import com.anychart.core.cartesian.series.Line
import com.anychart.data.Mapping
import com.anychart.data.Set
import com.anychart.enums.Anchor
import com.anychart.enums.MarkerType
import com.anychart.enums.TooltipPositionMode
import com.anychart.graphics.vector.Stroke
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_speed_duration_picker.*
import me.heid.heidtools.R
import java.net.URLDecoder
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime

/*
"lat",l
"lon",l
"speed"
"head",
"height
 */

class speedDurationPicker : AppCompatActivity() {
    var dataStore: DatabaseReference? = null
    var database: FirebaseDatabase? = null
    var str = ""
    var data:Data = Data


    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_speed_duration_picker)
        web_view2.webViewClient = MyBrowser

        web_view2.loadDataWithBaseURL(
            "text://apa/",
            "<html>\n" +
                    "<body>\n" +
                    "<form action=\"/action_page.php\">\n" +
                    "  <label for=\"startTime\">Start:</label>\n" +
                    "  <input type=\"datetime-local\" id=\"startTime\" name=\"startTime\">\n <br>" +
                    "  <label for=\"endTime\">End:</label>\n" +
                    "  <input type=\"datetime-local\" id=\"endTime\" name=\"endTime\">\n<br>" +
                    "  <input type=\"submit\">\n" +
                    "</form>\n" +
                    "</body>\n" +
                    "</html>", "text/html; charset=UTF-8", null, ""
        )
        web_view2.invalidate()


        textView14.setOnClickListener {
            Log.d("TAG", "onCreate,Text: " + textView14.text.toString())
            val text = textView14.text.toString()

            val parse = URLDecoder.decode(text.substring(text.indexOf("?"))).split("&")
            val start = parse[0].substring(parse[0].indexOf("=") + 1)
            val startdate =
                start.substring(0, start.indexOf("T")).replace("-", "/") + "/" + start.substring(
                    start.indexOf("T") + 1
                ).replace(":", "/")
            val stop = parse[1].substring(parse[1].indexOf("=") + 1)
            val stopdate = stop.substring(0, stop.indexOf("T"))
                .replace("-", "/") + "/" + stop.substring(stop.indexOf("T") + 1).replace(":", "/")

            val startArray = ArrayList<Int>()
            for (a in startdate.split("/")) {
                startArray.add(a.toInt())
            }

            var endDate = ArrayList<Int>()
            for (a in stopdate.split("/")) {
                endDate.add(a.toInt())
            }



            val startIns = Instant.parse(start+":00Z")
            val stopIns = Instant.parse(stop+":00Z")
            //Log.d(TAG, "onCreate: "+Duration.between(,).toMillis())


            val uid = FirebaseAuth.getInstance().currentUser?.uid
            loadDB2(startIns,stopIns,0,"")







            /* seriesData.add(CustomDataEntry("1986", 3.6, 2.3, 2.8))
             seriesData.add(CustomDataEntry("1987", 7.1, 4.0, 4.1))
             seriesData.add(CustomDataEntry("1988", 8.5, 6.2, 5.1))
             seriesData.add(CustomDataEntry("1989", 9.2, 11.8, 6.5))
             seriesData.add(CustomDataEntry("1990", 10.1, 13.0, 12.5))
             seriesData.add(CustomDataEntry("1991", 11.6, 13.9, 18.0))
             seriesData.add(CustomDataEntry("1992", 16.4, 18.0, 21.0))
             seriesData.add(CustomDataEntry("1993", 18.0, 23.3, 20.3))
             seriesData.add(CustomDataEntry("1994", 13.2, 24.7, 19.2))
             seriesData.add(CustomDataEntry("1995", 12.0, 18.0, 14.4))
             seriesData.add(CustomDataEntry("1996", 3.2, 15.1, 9.2))
             seriesData.add(CustomDataEntry("1997", 4.1, 11.3, 5.9))
             seriesData.add(CustomDataEntry("1998", 6.3, 14.2, 5.2))
             seriesData.add(CustomDataEntry("1999", 9.4, 13.7, 4.7))
             seriesData.add(CustomDataEntry("2000", 11.5, 9.9, 4.2))
             seriesData.add(CustomDataEntry("2001", 13.5, 12.1, 1.2))
             seriesData.add(CustomDataEntry("2002", 14.8, 13.5, 5.4))
             seriesData.add(CustomDataEntry("2003", 16.6, 15.1, 6.3))
             seriesData.add(CustomDataEntry("2004", 18.1, 17.9, 8.9))
             seriesData.add(CustomDataEntry("2005", 17.0, 18.9, 10.1))
             seriesData.add(CustomDataEntry("2006", 16.6, 20.3, 11.5))
             seriesData.add(CustomDataEntry("2007", 14.1, 20.7, 12.2))
             seriesData.add(CustomDataEntry("2008", 15.7, 21.6, 10))
             seriesData.add(CustomDataEntry("2009", 12.0, 22.5, 8.9))*/




        }
    }



    fun DATAFUNNEL(path:Any) {
        val b = path as HashMap<String,*>

       // Log.d(TAG, "DATAFUNNEL: "+path.toString())
        data.head.add(b.get("head") as Long)
        data.lon.add(b.get("lon") as Double)
        data.time.add(b.get("time")as Long)
        data.lat.add(b.get("lat")as Double)
        //data.speed.add(b.get("speed").)
        data.height.add(b.get("height") as Long)



        val seriesData: MutableList<DataEntry> = ArrayList()


        for (i in 0 .. data.speed.size-1){
            seriesData.add(CustomDataEntry2(data.time.get(i).toString(), data.height.get(i), data.lon.get(i), data.lat.get(i)))
        }




        val set: Set = Set.instantiate()
        set.data(seriesData)
        val series1Mapping: Mapping = set.mapAs("{ x: 'x', value: 'value' }")
        val series2Mapping: Mapping = set.mapAs("{ x: 'x', value: 'value2' }")
        val series3Mapping: Mapping = set.mapAs("{ x: 'x', value: 'value3' }")



        val cartesian: Cartesian = AnyChart.line()

        cartesian.animation(true)

        cartesian.padding(10.0, 20.0, 5.0, 20.0)

        cartesian.crosshair().enabled(true)
        cartesian.crosshair()
            .yLabel(true) // TODO ystroke
            .yStroke(null as Stroke?, null, null, null as String?, null as String?)

        cartesian.tooltip().positionMode(TooltipPositionMode.POINT)

        cartesian.title("DATA")

        cartesian.yAxis(0).title("Speed(km/h)")
        cartesian.xAxis(0).labels().padding(5.0, 5.0, 5.0, 5.0)

        val series1: Line = cartesian.line(series1Mapping)
        series1.name("Brandy")
        series1.hovered().markers().enabled(true)
        series1.hovered().markers()
            .type(MarkerType.CIRCLE)
            .size(4.0)
        series1.tooltip()
            .position("right")
            .anchor(Anchor.LEFT_CENTER)
            .offsetX(5.0)
            .offsetY(5.0)



        cartesian.legend().enabled(true)
        cartesian.legend().fontSize(13.0)
        cartesian.legend().padding(0.0, 0.0, 10.0, 0.0)

        any_chart_view2.setChart(cartesian)
        any_chart_view2.invalidate()


    }

    fun loadDB2(startdate: Instant, endDate: Instant, layer: Int,addon:String) {


        database = FirebaseDatabase.getInstance()



        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val ld = LocalDateTime.now()
        //dataStore = database!!.getReference("users/"+uid+"/data/gps/2022/01/19/17")
        dataStore = database!!.getReference( "users/" + uid + "/data/gps/"+addon)



        dataStore!!.get().addOnSuccessListener {
            var value = it.value as HashMap<*, *>
            for (b in value) {
                var a = b.key
                var tal = (a as String).toIntOrNull()

                if(layer>4){
                    DATAFUNNEL(b.value)
                }else{




                if (tal != null) {
                    val MAX = "9999-12-31"
                    var ins1 = addon
                    if(addon==""){
                        ins1=""+b.key
                    }else
                    {
                        ins1 += "/"+b.key
                    }

                    if(ins1.length>10){
                        ins1 = ins1.substring(0, 10) + 'T' + ins1.substring(10 + 1)
                    }

                    val ins2 = ins1.split("T")
                    val size = ins2[0].length

                    val min = "1900-01-01"


                    var time = "23:59:59Z"
                    var time2 = "00:00:00Z"
                    if(ins2.size>1){
                        val size2 = ins2[1].length
                        time= ins2[1].replace("/",":")+time.substring(size2)
                        time2= ins2[1].replace("/",":")+time2.substring(size2)
                    }


                    val date = ins2[0].replace("/","-")+ MAX.substring(size)+"T"

                    val maxDateTime = Instant.parse(date+time)


                    val date2 = ins2[0].replace("/","-")+ min.substring(size)+"T"

                    val minDateTime = Instant.parse(date2+time2)

                    if( ((Duration.between(startdate,maxDateTime).toMillis() > 0) && (Duration.between(minDateTime,endDate).toMillis()>0))){
                        if(addon != ""){
                            loadDB2(startdate,endDate,layer+1,addon+"/"+b.key)
                        }else{
                            loadDB2(startdate,endDate,layer+1, ""+b.key)
                        }
}
                    }else{
                        Log.d(TAG, "loadDB: NOTHING")
                    }
                }
                }


            }


        }

    fun loadDB(startdate: ArrayList<Int>, endDate: ArrayList<Int>, layer: Int,path:String) {


        database = FirebaseDatabase.getInstance()



        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val ld = LocalDateTime.now()
        //dataStore = database!!.getReference("users/"+uid+"/data/gps/2022/01/19/17")
        dataStore = database!!.getReference(path)
        dataStore!!.get().addOnSuccessListener {
            var value = it.value as HashMap<*, *>
            for (b in value) {
                var a = b.key
                var tal = (a as String).toIntOrNull()
                if (tal != null) {
                      if( (startdate[layer] <= tal && tal <= endDate[layer]) ){
                          loadDB(startdate,endDate,layer+1,path+"/"+b.key)
                      }else{
                          Log.d(TAG, "loadDB: NOTHING")
                      }
                }else{
                    println("SAVE ME")
                }


            }


        }
    }
}

object MyBrowser : WebViewClient() {
    @Deprecated("Deprecated in Java")
    override fun shouldOverrideUrlLoading(view: WebView, url: String?): Boolean {
        Log.d(TAG, "shouldOverrideUrlLoading: "+url)
        for ( a in (view.parent as ConstraintLayout).children){
            if(a is Button){
                a.performClick()
                a.isPressed = true
            }
            else if(a is TextView){
                a.text = url
                a.performClick()
                a.isPressed = true
            }

        }
        return true
    }

}
object Data{
    var lat:ArrayList<Double> = ArrayList()
    var lon:ArrayList<Double> = ArrayList()
    var speed:ArrayList<Number> = ArrayList()
    var head:ArrayList<Long> = ArrayList()
    var height:ArrayList<Long> = ArrayList()
    var time:ArrayList<Long> = ArrayList()
}

private class CustomDataEntry2(
    x: String?,
    value: Number?,
    value2: Number?,
    value3: Number?
) :
    ValueDataEntry(x, value) {
    init {
        setValue("value2", value2)
        setValue("value3", value3)
    }
}