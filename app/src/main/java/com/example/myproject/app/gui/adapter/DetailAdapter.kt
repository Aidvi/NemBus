package com.example.myproject.app.gui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myproject.R
import com.example.myproject.app.model.Departures
import kotlinx.android.synthetic.main.cell_detail_departure.view.*

class DetailAdapter:

    RecyclerView.Adapter<DetailAdapter.DetailViewHolder>() {

    private lateinit var detailList: List<Departures>

    fun setDataSet(detailList: List<Departures>) {
        this.detailList = detailList
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = if (!::detailList.isInitialized) 0 else detailList.size

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailAdapter.DetailViewHolder {
        // create a new view
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cell_detail_departure, parent, false)
        // set the view's size, margins, paddings and layout parameters

        return DetailViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: DetailViewHolder, position: Int) {

        val detailDeparture = detailList[position]

        holder.itemView.busName.text = detailDeparture.name
        holder.itemView.time.text = detailDeparture.time
        if (detailDeparture.rtTime === null) {
            holder.itemView.rtTime.text = "-"
        } else{
            holder.itemView.rtTime.text = detailDeparture.rtTime
        }
        holder.itemView.direction.text = detailDeparture.direction
    }

    inner class DetailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {

        }
    }
}