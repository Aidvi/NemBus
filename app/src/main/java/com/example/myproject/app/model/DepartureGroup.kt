package com.example.myproject.app.model


import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class DepartureGroup(val name: String, val departures: List<Departures>): Parcelable