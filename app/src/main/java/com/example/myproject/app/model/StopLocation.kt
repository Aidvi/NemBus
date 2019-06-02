package com.example.myproject.app.model

import com.google.gson.annotations.Expose

data class StopLocation(
    @Expose val id: String,
    @Expose val name: String,
    @Expose val x: String,
    @Expose val y: String,
    @Expose val distance: String
)