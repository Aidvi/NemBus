package com.example.myproject.app.model

import android.os.Parcelable
import com.google.gson.annotations.Expose
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Departures(
   @Expose val name: String,
   @Expose val type: String,
   @Expose val stop: String,
   @Expose val time: String,
   @Expose val date: String,
   @Expose val messages: String,
   @Expose val cancelled: String?,
   @Expose val track: String?,
   @Expose val rtTrack: String?,
   @Expose val rtTime: String?,
   @Expose val rtDate: String?,
   @Expose val finalStop: String,
   @Expose val direction: String
): Parcelable