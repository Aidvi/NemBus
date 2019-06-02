package com.example.myproject.app.gui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myproject.R
import com.example.myproject.app.model.StopLocation
import kotlinx.android.synthetic.main.cell_stop.view.*

class StopLocationAdapter:

    RecyclerView.Adapter<StopLocationAdapter.LocationViewHolder>() {

    private var stopLocationList = ArrayList<StopLocation>()

    fun setDataSet(stopLocationList: ArrayList<StopLocation>) {
        this.stopLocationList = stopLocationList
        notifyDataSetChanged()
    }
    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = stopLocationList.size

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StopLocationAdapter.LocationViewHolder {
        // create a new view
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cell_stop, parent, false)
        // set the view's size, margins, paddings and layout parameters

        return LocationViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        //holder.textView.text = myDataset[position]

        val stop = stopLocationList[position]

        holder.itemView.stopName.text = stop.name
        holder.itemView.dist.text = stop.distance + "m"

    }
    inner class LocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {
                listener.onItemClick(stopLocationList[adapterPosition])
            }
        }
    }
    lateinit var listener: OnItemClickListener<StopLocation>
}