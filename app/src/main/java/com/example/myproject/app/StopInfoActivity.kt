package com.example.myproject.app

//import kotlinx.android.synthetic.main.activity_main.*
import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.transition.*
import com.example.myproject.R
import com.example.myproject.app.config.ApiConfig
import com.example.myproject.app.gui.adapter.DeparturesAdapter
import com.example.myproject.app.gui.adapter.OnItemClickListener
import com.example.myproject.app.model.DepartureGroup
import com.example.myproject.app.model.Departures
import com.example.myproject.app.model.DeparturesResponse
import com.example.myproject.app.services.DateFormats
import com.example.myproject.app.services.GridSpacingItemDecoration
import com.example.myproject.app.services.MoviaApiService
import kotlinx.android.synthetic.main.activity_stop_info.*
import org.jetbrains.anko.dip
import org.joda.time.DateTime
import retrofit2.Call
import retrofit2.Response


class StopInfoActivity : AppCompatActivity(), OnItemClickListener<DepartureGroup> {

    // Api Service
    private val apiConfig = ApiConfig()
    private val apiService = MoviaApiService.create(apiConfig)

    private var isFabOpen: Boolean = false

    // Main Bus List
    private var departureBusMap = LinkedHashMap<String, ArrayList<Departures>>()

    // Main Train List
    private var departureTrainMap = LinkedHashMap<String, ArrayList<Departures>>()

    // GridLayout
    private val gridLayoutManager = GridLayoutManager(this, 3)


    // StopLocationId From MainActivity
    private lateinit var stopLocationId: String

    // Recycler View
    private lateinit var viewAdapter: DeparturesAdapter

    // Call
    private var departuresCall: Call<DeparturesResponse>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stop_info)
        stopLocationId = intent.getStringExtra("stopLocationId")
        //fabSort = findViewById(R.id.fabSort)

        with(recyclerView) {
            setHasFixedSize(true)
            addItemDecoration(GridSpacingItemDecoration(3, dip(15)))
            viewAdapter = DeparturesAdapter()
            adapter = viewAdapter
            viewAdapter.listener = this@StopInfoActivity
            layoutManager = gridLayoutManager
        }

        swipeRefreshLayout.setOnRefreshListener {
            departureBusMap.clear()
            apiCallBus(DateTime.now())
        }

        fab_main.setOnClickListener {
            if (!isFabOpen) {
                showFabMenu()
            } else {
                closeFabMenu()
            }
        }

        fab_tog.setOnClickListener {
            closeFabMenu()
            apiCallSTrainMetro(DateTime.now())
            fab_main.setImageDrawable(
                ContextCompat.getDrawable(
                    applicationContext,
                    R.drawable.ic_train_black
                )
            )
        }

        fab_metro.setOnClickListener {
            fab_main.setImageDrawable(
                ContextCompat.getDrawable(
                    applicationContext,
                    R.drawable.ic_metro_final
                )
            )
            closeFabMenu()
        }
        fab_bus.setOnClickListener {
            fab_main.setImageDrawable(
                ContextCompat.getDrawable(
                    applicationContext,
                    R.drawable.ic_bus
                )
            )
            closeFabMenu()
        }

        bg_fab_menu.setOnClickListener {
            closeFabMenu()
        }

        apiCallBus(DateTime.now())
    }

    private fun showFabMenu() {
        isFabOpen = true
        val constraintSet = ConstraintSet()
        fab_metro.visibility = View.VISIBLE
        fab_tog.visibility = View.VISIBLE
        fab_bus.visibility = View.VISIBLE
        bg_fab_menu.visibility = View.VISIBLE

        TransitionManager.go(Scene(stopInfoConstraintLayout))
        fab_main.animate().rotation(90f)
        bg_fab_menu.animate().alpha(1f)
        constraintSet.clone(stopInfoConstraintLayout)
        constraintSet.connect(
            fab_tog.id,
            ConstraintSet.BOTTOM,
            fab_main.id,
            ConstraintSet.TOP,
            dip(20)
        )
        constraintSet.clear(fab_tog.id, ConstraintSet.TOP)
        constraintSet.connect(
            fab_metro.id,
            ConstraintSet.BOTTOM,
            fab_tog.id,
            ConstraintSet.TOP,
            dip(10)
        )
        constraintSet.clear(fab_metro.id, ConstraintSet.TOP)
        constraintSet.connect(
            fab_bus.id,
            ConstraintSet.BOTTOM,
            fab_metro.id,
            ConstraintSet.TOP,
            dip(10)
        )
        constraintSet.clear(fab_bus.id, ConstraintSet.TOP)
        constraintSet.applyTo(stopInfoConstraintLayout)
    }

    private fun closeFabMenu() {
        isFabOpen = false
        val constraintSet = ConstraintSet()

        TransitionManager.go(Scene(stopInfoConstraintLayout), AutoTransition().apply {
            addListener(object : TransitionListenerAdapter() {
                override fun onTransitionEnd(transition: Transition) {
                    fab_metro.visibility = View.GONE
                    fab_tog.visibility = View.GONE
                    fab_bus.visibility = View.GONE
                    bg_fab_menu.visibility = View.GONE
                }
            })
        })

        fab_main.animate().rotation(0f)
        bg_fab_menu.animate().alpha(0f)
        constraintSet.clone(stopInfoConstraintLayout)
        constraintSet.connect(
            fab_tog.id,
            ConstraintSet.BOTTOM,
            fab_main.id,
            ConstraintSet.BOTTOM,
            dip(0)
        )
        constraintSet.connect(
            fab_metro.id,
            ConstraintSet.BOTTOM,
            fab_main.id,
            ConstraintSet.BOTTOM,
            dip(0)
        )
        constraintSet.connect(
            fab_bus.id,
            ConstraintSet.BOTTOM,
            fab_main.id,
            ConstraintSet.BOTTOM,
            dip(0)
        )
        constraintSet.applyTo(stopInfoConstraintLayout)
    }

    override fun onItemClick(item: DepartureGroup) {
        val intent = Intent(this@StopInfoActivity, DepartureDetailActivity::class.java)
        val options = ActivityOptions.makeSceneTransitionAnimation(this)
        intent.putParcelableArrayListExtra("departureList", ArrayList(item.departures))
        startActivity(intent, options.toBundle())
    }

    private fun apiCallBus(date: DateTime) {

        val time = DateFormats.timeFormat.format(date.toDate())
        val date = DateFormats.dateFormat.format(date.toDate())

        departuresCall = apiService.getDepartures(stopLocationId, date, time).also {
            it.enqueue(object : retrofit2.Callback<DeparturesResponse> {

                override fun onFailure(call: Call<DeparturesResponse>, t: Throwable) {
                    Log.e("TAG", "FAIL API REJSEPLANEN: ")
                    t.printStackTrace()
                    departuresCall = null
                }

                override fun onResponse(
                    call: Call<DeparturesResponse>,
                    response: Response<DeparturesResponse>
                ) {
                    response.body()?.let { r ->

                        val myDataSet = r.departureBoard.Departure
                        myDataSet.forEach { departure ->
                            if (!departureBusMap.containsKey(departure.name)) {
                                departureBusMap.put(departure.name, ArrayList())
                            }
                            departureBusMap[departure.name]?.add(departure)
                        }

                        val lastDeparture = myDataSet.last()

                        val combinedString = lastDeparture.date + " " + lastDeparture.time
                        val finalDate = DateTime(DateFormats.dateTimeFormat.parse(combinedString))
                        if (finalDate.minusHours(1).isBeforeNow) {
                            apiCallBus(finalDate)
                        } else {
                            val groupedList: ArrayList<DepartureGroup> = ArrayList()
                            swipeRefreshLayout.isRefreshing = false
                            progressBar1.visibility = View.GONE
                            departureBusMap.entries.forEach { entry ->
                                val departureGroup = DepartureGroup(entry.key, entry.value)
                                groupedList.add(departureGroup)

                                // TODO SKAL HAVE SKREVET STATIONER UD PÅ VIEW
                            }
                            viewAdapter.removeItems()
                            viewAdapter.type = DeparturesAdapter.Type.Bus
                            gridLayoutManager.spanCount = 3
                            viewAdapter.setDataSet(groupedList)
                        }
                    }
                    departuresCall = null
                }
            })
        }
    }

    private fun apiCallSTrainMetro(date: DateTime) {

        val time = DateFormats.timeFormat.format(date.toDate())
        val date = DateFormats.dateFormat.format(date.toDate())

        departuresCall = apiService.getTrainDepartures(stopLocationId, date, time).also {
            it.enqueue(object : retrofit2.Callback<DeparturesResponse> {

                override fun onFailure(call: Call<DeparturesResponse>, t: Throwable) {
                    Log.e("TAG", "FAIL API REJSEPLANEN: ")
                    t.printStackTrace()
                    departuresCall = null
                }

                override fun onResponse(
                    call: Call<DeparturesResponse>,
                    response: Response<DeparturesResponse>
                ) {
                    response.body()?.let { r ->

                        val myDataSet = r.departureBoard.Departure
                        myDataSet.forEach { departure ->
                            if (!departureTrainMap.containsKey(departure.type)) {
                                departureTrainMap.put(departure.type, ArrayList())
                            }
                            departureTrainMap[departure.type]?.add(departure)
                        }

                        val groupedSTrainMetroList: ArrayList<DepartureGroup> = ArrayList()

                        departureTrainMap.entries.forEach { entry ->
                            val departureGroup = DepartureGroup(entry.key, entry.value)
                            groupedSTrainMetroList.add(departureGroup)
                            // TODO SKAL HAVE SKREVET STATIONER UD PÅ VIEW
                        }

                        groupedSTrainMetroList.forEach {
                            if (it.name == "Regional") {
                                groupedSTrainMetroList.remove(it)
                            }
                        }

                        groupedSTrainMetroList.forEach {
                            Log.e("TaG", it.departures.toString())
                        }
                        viewAdapter.removeItems()
                        viewAdapter.type = DeparturesAdapter.Type.Train
                        gridLayoutManager.spanCount = 1
                        viewAdapter.setDataSet(groupedSTrainMetroList)
                    }
                    departuresCall = null
                }
            })
        }
    }

    override fun onDestroy() {
        departuresCall?.let {
            it.cancel()
            departuresCall = null
        }
        super.onDestroy()
    }
}
