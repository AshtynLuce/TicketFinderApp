package com.example.finalproject

data class SiteData(
    val _embedded: EmbeddedEvents? = null
)

data class EmbeddedEvents(
    val events: List<Site>? = null
)

data class Site(
    val name: String? = null,
    val images: List<Images> = emptyList(),
    val dates: Dates = Dates(),
    val url: String? = null,
    val _embedded: EmbeddedVenue = EmbeddedVenue(),

    var isFavorited: Boolean = false // Used only in UI, not saved to Firestore
)

data class Images(
    val url: String? = null,
    val width: Int = 0,
    val height: Int = 0
)

data class Dates(
    val start: Start = Start(),
    val status: Status? = null
)

data class Start(
    val localDate: String = "",
    val localTime: String = ""
)

data class Status(
    val code: String? = null
)

data class EmbeddedVenue(
    val venues: List<Venue> = emptyList()
)

data class Venue(
    val name: String? = null,
    val city: City = City(),
    val state: State? = State(),
    val address: Address? = Address()
)

data class City(
    val name: String? = null
)

data class State(
    val name: String? = null
)

data class Address(
    val line1: String? = null
)