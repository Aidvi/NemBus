package com.example.myproject.app.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class LocationResponse(
        @SerializedName("LocationList")
        @Expose
        val locationList: LocationList
)
{
    data class LocationList(
            @Expose val StopLocation: ArrayList<StopLocation>
    )
}