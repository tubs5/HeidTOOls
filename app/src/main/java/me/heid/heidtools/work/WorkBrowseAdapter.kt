package me.heid.heidtools.work

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.findFragment
import androidx.recyclerview.widget.RecyclerView
import me.heid.heidtools.R
import java.io.File


class WorkBrowseAdapter(val data:ArrayList<AdapterDataHolder>,val basePath:String,val date:String) : RecyclerView.Adapter<WorkBrowseAdapter.ViewHolder>() {

    class AdapterDataHolder(val id:String,val plate:String,val serial:String,val created:Long) :Comparable<AdapterDataHolder>{
        override fun compareTo(other: AdapterDataHolder): Int {
            return this.created.compareTo(other.created)
        }

    }

    class ViewHolder(itemView: View,val basePath:String, val date:String) : RecyclerView.ViewHolder(itemView) {
        private var id:TextView
        private var plate:TextView
        private var serial:TextView

        init {
            id = itemView.findViewById(R.id.WorkBrowseListViewItemID)
            serial = itemView.findViewById(R.id.WorkBrowseListViewItemSerial)
            plate = itemView.findViewById(R.id.WorkBrowseListViewItemPlate)

            itemView.setOnClickListener{
                val bundle = Bundle()
                bundle.putString("path", basePath+"/"+id.text)
                bundle.putString("date",date)
                itemView.findFragment<WorkBrowse>().parentFragmentManager.beginTransaction().setReorderingAllowed(true).replace(R.id.fragment_container_view,WorkView::class.java,bundle).commit()
            }
        }

        fun getIdView():TextView{
            return id
        }
        fun getPlateView():TextView{
            return plate
        }
        fun getSerialView():TextView{
            return serial
        }


    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkBrowseAdapter.ViewHolder {
        val view: View = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.listviewitem_work_browse, parent, false)
        return ViewHolder(view,basePath,date)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.getIdView().text = data[position].id
        holder.getSerialView().text = data[position].serial
        holder.getPlateView().text = data[position].plate
    }

    override fun getItemCount(): Int {
        return data.size;
    }


}