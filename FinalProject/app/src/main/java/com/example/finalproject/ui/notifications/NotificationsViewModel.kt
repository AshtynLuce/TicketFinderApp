package com.example.finalproject.ui.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.finalproject.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class NotificationsViewModel : ViewModel() {

    private val _siteList = MutableLiveData<List<Site>>()
    val siteList: LiveData<List<Site>> get() = _siteList

    private val db = FirebaseFirestore.getInstance()

    init {
        fetchFavorites()
    }

    fun fetchFavorites() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("users")
            .document(userId)
            .collection("favorites")
            .get()
            .addOnSuccessListener { result ->
                val favoriteEvents = result.mapNotNull { it.toObject(FavoriteEvent::class.java) }

                val sites = favoriteEvents.map { fav ->
                    Site(
                        name = fav.eventName ?: "",
                        url = fav.eventUrl ?: "",
                        images = listOf(
                            Images(
                                url = fav.imageUrl ?: "",
                                width = 0,
                                height = 0
                            )
                        ),
                        dates = Dates(
                            start = Start(
                                localDate = fav.date ?: "",
                                localTime = fav.time ?: ""
                            ),
                            status = null
                        ),
                        _embedded = EmbeddedVenue(
                            venues = listOf(
                                Venue(
                                    name = fav.venueName ?: "",
                                    city = City(fav.city ?: ""),
                                    state = fav.state?.let { State(it) },
                                    address = fav.address?.let { Address(it) }
                                )
                            )
                        ),
                        isFavorited = true
                    )
                }

                _siteList.value = sites
            }
            .addOnFailureListener {
                _siteList.value = emptyList()
            }
    }
}