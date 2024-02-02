package me.heid.heidtools.work

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionInflater
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_speed.*
import kotlinx.android.synthetic.main.fragment_work_browse.*
import kotlinx.android.synthetic.main.fragment_work_view.*
import me.heid.heidtools.R
import org.json.JSONObject
import java.io.File
import java.time.LocalDate
import java.util.*
import kotlin.collections.ArrayList

class WorkBrowse : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inflater = TransitionInflater.from(requireContext())
        exitTransition = inflater.inflateTransition(R.transition.fade)

    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        val listView = WorkBrowseRecyclerView as RecyclerView
        val cd = LocalDate.now()
        var date = "${cd.year}-${cd.monthValue}-${cd.dayOfMonth}"
        arguments?.let {
            date = it.getString("date",date)
        }

        val filepath = (date.toString()).replace("-","/")
        var file = File(context?.filesDir?.path, "data/$filepath")

        var files = file.list()
        if(files != null) {

            val list = ArrayList<WorkBrowseAdapter.AdapterDataHolder>()
            for (cf in files) {
                var serial = ""
                var plate = ""
                var created = 0L

                val f = File(file, "$cf/data.json")
                if (f.exists()) {
                    val text = f.readText()
                    if (text.isNotEmpty()) {
                        val json = JSONObject(text)
                        if (json.has("Serial")) {
                            serial = json.getString("Serial")
                        }
                        if (json.has("Km")) {
                            plate = json.getString("Km")
                        }
                        if (json.has("Created")) {
                            created = json.getLong("Created")
                        }
                    }
                }
                list.add(WorkBrowseAdapter.AdapterDataHolder(cf, plate, serial, created))
            }

            Collections.sort(list,Collections.reverseOrder())
            listView.adapter = WorkBrowseAdapter(list,file.path,date)
        }
        listView.layoutManager = LinearLayoutManager(requireContext())


        listView.invalidate()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_work_browse, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val calendar = WorkBrowseCurrentDate as TextView
        val listView = WorkBrowseRecyclerView as RecyclerView
        val addButton = WorkBrowseAddButton as FloatingActionButton


        val cd = LocalDate.now()
        var date = "${cd.year}-${cd.monthValue}-${cd.dayOfMonth}"
        arguments?.let {
           date = it.getString("date",date)
        }



        WorkBrowseCurrentDate.text = date
        WorkBrowseCurrentDate.setOnClickListener{
            parentFragmentManager.beginTransaction().setReorderingAllowed(true).replace(R.id.fragment_container_view,WorkCalendarPicker::class.java,null).commit()
        }

        addButton.setOnClickListener( View.OnClickListener {
                v ->
            parentFragmentManager.beginTransaction().setReorderingAllowed(true).replace(R.id.fragment_container_view,WorkAddCamera2::class.java,null).commit()
        })
    }


}