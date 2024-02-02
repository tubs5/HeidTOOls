package me.heid.heidtools.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView
import me.heid.heidtools.R

class MyAdapter(private val myDataset: ArrayList<MainRecycleViewData>) :



    RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    class MyViewHolder( val frameLayout: FrameLayout): RecyclerView.ViewHolder(frameLayout){
        val textView: TextView = frameLayout[0].findViewById(R.id.textView2);
        val imageView: ImageView = frameLayout[0].findViewById(R.id.imageView2);
        val constraintLayout: ConstraintLayout= frameLayout[0].findViewById(R.id.con1);

    }


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): MyViewHolder {
        // create a new view
        val fl = LayoutInflater.from(parent.context)
            .inflate(R.layout.main_list_item_view, parent, false) as FrameLayout
        return MyViewHolder(fl)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.textView.text = myDataset[position].text
        holder.imageView.setImageDrawable(holder.imageView.context.getDrawable(myDataset[position].icon))
        holder.frameLayout.setOnClickListener(myDataset[position].clkListener)
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myDataset.size
}
class MainRecycleViewData(val text:String,val icon:Int,val clkListener: View.OnClickListener)