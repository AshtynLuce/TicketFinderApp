package com.example.finalproject

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class WebAdapter(
    private val events: ArrayList<Site>,
    private val isFavoritesMode: Boolean = false,
    private val onFavoritesChanged: (() -> Unit)? = null
) : RecyclerView.Adapter<WebAdapter.MyViewHolder>() {

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val eventImage = itemView.findViewById<ImageView>(R.id.eventImage)
        val eventName = itemView.findViewById<TextView>(R.id.tvEventName)
        val venue = itemView.findViewById<TextView>(R.id.tvVenue)
        val address = itemView.findViewById<TextView>(R.id.tvAddress)
        val dateTime = itemView.findViewById<TextView>(R.id.tvDateTime)
        val ticketLink = itemView.findViewById<Button>(R.id.buttonTicketLink)
        val switchFavorites = itemView.findViewById<Switch>(R.id.switch_favorites)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.site_row, parent, false)
        return MyViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = events[position]

        holder.eventName.text = currentItem.name

        val venue = currentItem._embedded.venues.firstOrNull()
        holder.venue.text = venue?.let {
            "${it.name}, ${it.state?.name ?: "Unknown State"}"
        } ?: "No venue info"

        holder.address.text = venue?.let {
            "${it.address?.line1 ?: "No address"}, ${it.city.name}, ${it.state?.name ?: ""}"
        } ?: "No venue info"

        val formattedDateTime = try {
            val date = LocalDate.parse(currentItem.dates.start.localDate)
            val time = LocalTime.parse(currentItem.dates.start.localTime)
            val dateFormatter = DateTimeFormatter.ofPattern("M/d/yyyy")
            val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
            "${date.format(dateFormatter)} @ ${time.format(timeFormatter)}"
        } catch (e: Exception) {
            "${currentItem.dates.start.localDate} @ ${currentItem.dates.start.localTime}"
        }

        holder.dateTime.text = formattedDateTime

        val highestQualityImage = currentItem.images.maxByOrNull {
            it.width.toInt() * it.height.toInt()
        }

        val context = holder.itemView.context

        Glide.with(context)
            .load(highestQualityImage?.url)
            .placeholder(R.drawable.ticketmaster)
            .into(holder.eventImage)

        holder.ticketLink.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentItem.url))
            context.startActivity(intent)
        }

        holder.switchFavorites.setOnCheckedChangeListener(null)
        holder.switchFavorites.isChecked = currentItem.isFavorited
        holder.switchFavorites.text = if (currentItem.isFavorited) "Favorited" else "Unfavorited"

        holder.switchFavorites.setOnCheckedChangeListener { _, isChecked ->
            val db = FirebaseFirestore.getInstance()
            val userId = FirebaseAuth.getInstance().currentUser?.uid

            if (userId == null) {
                Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
                holder.switchFavorites.isChecked = false
                return@setOnCheckedChangeListener
            }

            val userFavoritesRef = db.collection("users")
                .document(userId)
                .collection("favorites")

            holder.switchFavorites.text = if (isChecked) "Favorited" else "Unfavorited"

            val venue = currentItem._embedded.venues.firstOrNull()
            val imageUrl = currentItem.images.firstOrNull()?.url

            val favoriteEvent = hashMapOf(
                "eventName" to (currentItem.name ?: ""),
                "eventUrl" to (currentItem.url ?: ""),
                "date" to (currentItem.dates.start.localDate ?: ""),
                "time" to (currentItem.dates.start.localTime ?: ""),
                "imageUrl" to (imageUrl ?: ""),
                "venueName" to (venue?.name ?: ""),
                "address" to (venue?.address?.line1 ?: ""),
                "city" to (venue?.city?.name ?: ""),
                "state" to (venue?.state?.name ?: "")
            )

            if (isChecked) {
                userFavoritesRef
                    .add(favoriteEvent)
                    .addOnSuccessListener { /* success */ }
                    .addOnFailureListener { e -> /* error */ }
            } else {
                userFavoritesRef
                    .whereEqualTo("eventUrl", currentItem.url)
                    .get()
                    .addOnSuccessListener { result ->
                        for (document in result) {
                            userFavoritesRef.document(document.id).delete()
                        }
                        if (isFavoritesMode) {
                            removeItem(holder.adapterPosition)
                            onFavoritesChanged?.invoke()
                        }
                    }
                    .addOnFailureListener { e -> /* error */ }
            }
        }
    }

    override fun getItemCount(): Int = events.size

    fun updateData(newEvents: List<Site>) {
        events.clear()
        events.addAll(newEvents)
        notifyDataSetChanged()
    }

    fun removeItem(position: Int) {
        events.removeAt(position)
        notifyItemRemoved(position)
    }
}