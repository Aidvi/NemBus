package com.example.myproject.app.gui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myproject.R
import com.example.myproject.app.model.DepartureGroup
import kotlinx.android.synthetic.main.cell_departure.view.*
import kotlinx.android.synthetic.main.cell_strain_metro_departure.view.*
import org.jetbrains.anko.backgroundColorResource

class DeparturesAdapter :

    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class Type {
        Train,
        Regional,
        Bus,
    }

    lateinit var type: Type

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long = groupedList[position].name.hashCode().toLong()

    private val groupedList = ArrayList<DepartureGroup>()

    fun setDataSet(groupedList: List<DepartureGroup>) {
        this.groupedList.addAll(groupedList)

        notifyItemRangeInserted(0, groupedList.size)
    }

    fun removeItems() {
        val start = this.groupedList.size
        this.groupedList.clear()

        notifyItemRangeRemoved(0, start)
    }

    // Return the size of your dataSet (invoked by the layout manager)
    override fun getItemCount() = groupedList.size

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // create a new view
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        // set the view's size, margins, paddings and layout parameters

        return when (type) {
            Type.Train -> TrainDepartureViewHolder(view)
            Type.Regional -> RegionalDepartureViewHolder(view)
            Type.Bus -> DepartureViewHolder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {

        return when (type) {
            Type.Train -> R.layout.cell_strain_metro_departure
            Type.Regional -> TODO() // R.layout.RegionalDeparture
            Type.Bus -> R.layout.cell_departure
        }
    }
    

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (holder::class.java) {
            DepartureViewHolder::class.java -> {
                val groupDeparture = groupedList[position]

                val color = when {
                    groupDeparture.name.contains("S") -> R.color.busModelS
                    groupDeparture.name.contains("C") -> R.color.busModelC
                    groupDeparture.name.contains("A") -> R.color.busModelA
                    else -> R.color.busStandardModel
                }

                holder.itemView.viewColorCard.backgroundColorResource = color
                holder.itemView.departureName.text = groupDeparture.name
            }

            TrainDepartureViewHolder::class.java -> {
                val groupedSTrainMetroDeparture = groupedList[position]
                // TESTING ONLY ATM
                holder.itemView.trainName.text = ""
                groupedSTrainMetroDeparture.departures.forEach {
                    holder.itemView.trainName.text =
                        holder.itemView.trainName.text.toString() + " " + it.name
                }
            }

            RegionalDepartureViewHolder::class.java -> TODO()
        }
    }

    inner class DepartureViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {
                listener.onItemClick(groupedList[adapterPosition])
            }
        }
    }

    inner class TrainDepartureViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)


    inner class RegionalDepartureViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)


    lateinit var listener: OnItemClickListener<DepartureGroup>
}