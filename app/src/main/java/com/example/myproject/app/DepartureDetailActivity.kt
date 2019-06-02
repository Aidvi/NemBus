package com.example.myproject.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myproject.R
import com.example.myproject.app.gui.adapter.DetailAdapter
import com.example.myproject.app.model.Departures
import kotlinx.android.synthetic.main.activity_main.*

class DepartureDetailActivity : AppCompatActivity() {


    // Recycler View
    private lateinit var viewAdapter: DetailAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_departure_detail)

        with(recyclerView) {
            setHasFixedSize(true)
            viewAdapter = DetailAdapter()
            adapter = viewAdapter
            layoutManager = LinearLayoutManager(context)
        }

        val departureList = intent.getParcelableArrayListExtra<Departures>("departureList")

        viewAdapter.setDataSet(departureList)
    }
}
