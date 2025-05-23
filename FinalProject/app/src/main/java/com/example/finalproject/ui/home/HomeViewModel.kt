package com.example.finalproject.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.finalproject.Site
import com.example.finalproject.SiteData
import com.example.finalproject.TicketMasterService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory

class HomeViewModel : ViewModel() {

    private val TAG = "HomeViewModel"
    private val BASE_URL = "https://app.ticketmaster.com/discovery/v2/events/"

    private val _siteList = MutableLiveData<List<Site>?>()
    val siteList: MutableLiveData<List<Site>?> = _siteList

    private val db = FirebaseFirestore.getInstance()

    // Fetch favorites from Firestore
    private fun fetchFavorites(): LiveData<List<String>> {
        val favoriteUrls = MutableLiveData<List<String>>()

        db.collection("favorites")
            .get()
            .addOnSuccessListener { result ->
                val favoriteEventUrls = result.mapNotNull { it.getString("eventUrl") }
                favoriteUrls.value = favoriteEventUrls
            }
            .addOnFailureListener {
                Log.e(TAG, "Failed to fetch favorites")
                favoriteUrls.value = emptyList()
            }

        return favoriteUrls
    }

    // Fetch events based on category and city
    fun fetchEvents(eventType: String, city: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://app.ticketmaster.com/discovery/v2/events/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val ticketMasterAPI = retrofit.create(TicketMasterService::class.java)

        ticketMasterAPI.getEvents(
            eventType, city, "date,asc",
            "ApiKeyHere" //Removed for security
        ).enqueue(object : Callback<SiteData> {
            override fun onResponse(call: Call<SiteData>, response: Response<SiteData>) {
                val body = response.body()
                if (body == null) {
                    _siteList.postValue(emptyList())
                    return
                }

                val events = body._embedded?.events ?: emptyList()

                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId == null) {
                    _siteList.postValue(events)
                    return
                }

                // Check which events are favorited by the current user
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .collection("favorites")
                    .get()
                    .addOnSuccessListener { favoritesSnapshot ->
                        val favoriteUrls =
                            favoritesSnapshot.documents.mapNotNull { it.getString("eventUrl") }

                        val updatedEvents = events.map { event ->
                            event.isFavorited = favoriteUrls.contains(event.url)
                            event
                        }

                        _siteList.postValue(updatedEvents)
                    }
                    .addOnFailureListener {
                        // If Firestore fails, fallback to just show events without favorite status
                        _siteList.postValue(events)
                    }
            }

            override fun onFailure(call: Call<SiteData>, t: Throwable) {
                _siteList.postValue(emptyList())
            }
        })
    }
    fun refreshFavorites() {
        val currentEvents = _siteList.value ?: return
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("favorites")
            .get()
            .addOnSuccessListener { snapshot ->
                val favoriteUrls = snapshot.documents.mapNotNull { it.getString("eventUrl") }
                val updatedEvents = currentEvents.map { event ->
                    event.isFavorited = favoriteUrls.contains(event.url)
                    event
                }
                _siteList.postValue(updatedEvents)
            }
    }
}
