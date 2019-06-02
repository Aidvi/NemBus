package com.example.myproject.app.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class DeparturesResponse(
    @SerializedName("DepartureBoard")
    @Expose
    val departureBoard: DepartureBoard
)
{
    data class DepartureBoard(
        @Expose val Departure: List<Departures>
    )
}
