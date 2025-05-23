package com.example.finalproject

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface TicketMasterService {

    @GET(".")
    fun getEvents(@Query("keyword") keyword: String,
                  @Query("city") city: String,
                  @Query("sort") sort: String,
                  @Query("apikey") apikey: String) : Call<SiteData>
}