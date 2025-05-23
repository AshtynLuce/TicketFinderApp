# TicketFinderApp
The TicketFinderApp is a Kotlin-based Android application that helps users discover live events in any city by querying the Ticketmaster API.
Users can search events by city and event type, view results in a dynamic list, and authenticate via Firebase.
Each user can also favorite events and view them later in a dedicated favorites fragment.

Features:
  - Search Events by city and event type (event type in a spinner)
  - Ticketmaster API Integration using Retrofit
  - RecyclerView to display event results
  - Firebase Authentication (email/password)
  - User Favorites stored with Firebase Realtime Database or Firestore
  - Fragment Navigation between search, favorites, and a profile page to show person information

Tech Stack
  - Kotlin Primary language
  - Retrofit HTTP client for consuming the Ticketmaster API
  - Firebase Authentication and database (Realtime Database or Firestore)
  - RecyclerView Efficiently displays a list of events
  - ViewModel For managing UI-related data
  - Navigation Component – Handles fragment transitions

![TicketFinder Login Screen]([http://url/to/img.png](https://imgur.com/a/E1MHQeG)) ![TicketFinder Search Screen]([http://url/to/img.png](https://imgur.com/a/u9mPubk)) ![TicketFinder Favorites Screen]([http://url/to/img.png](https://imgur.com/a/oPrFgdc))
