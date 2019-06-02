package com.example.myproject.app.services

import com.example.myproject.app.config.ApiConfig
import com.example.myproject.app.model.DeparturesResponse
import com.example.myproject.app.model.LocationResponse
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query


interface MoviaApiService {


    companion object {

        fun create(config: ApiConfig): MoviaApiService {
            val client: OkHttpClient.Builder = OkHttpClient.Builder()
                    .addInterceptor(DebugInterceptor().setPrintResponseBody(true))

            val gson: Gson = GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .setDateFormat("YYYY-MM-DD'T'HH:mm:ss")
                .create()

            return Retrofit.Builder()
                .client(client.build())
                .baseUrl(config.baseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(MoviaApiService::class.java)
        }
    }

    @GET("stopsNearby")
    fun getAllStops(
        @Query("coordX") latitude: Double,
        @Query("coordY") longtitude: Double,
        @Query("maxRadius") maxRadius: Int,
        @Query("maxNumber") maxNumber: Int,
        @Query("useTog") useTog: Int = 0,
        @Query("useMetro") useMetro: Int = 0,
        @Query("format") format: String = "json"
        ): Call<LocationResponse>

    @GET("departureBoard")
    fun getDepartures(
        @Query("id") id: String,
        @Query("date") date: String,
        @Query("time") time: String,
        @Query("useTog") useTog: Int = 0,
        @Query("useMetro") useMetro: Int = 0,
        @Query("format") format: String = "json"
    ): Call<DeparturesResponse>

    @GET("departureBoard")
    fun getTrainDepartures(
        @Query("id") id: String,
        @Query("date") date: String,
        @Query("time") time: String,
        @Query("useBus") useBus: Int = 0,
        @Query("useTog") useTog: Int = 1,
        @Query("useMetro") useMetro: Int = 1,
        @Query("format") format: String = "json"
    ): Call<DeparturesResponse>
}