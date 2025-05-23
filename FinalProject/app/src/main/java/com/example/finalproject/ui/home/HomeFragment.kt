package com.example.finalproject.ui.home

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.finalproject.databinding.FragmentHomeBinding
import com.example.finalproject.WebAdapter

class HomeFragment : Fragment(), AdapterView.OnItemSelectedListener {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val homeViewModel: HomeViewModel by activityViewModels()

    private lateinit var eventType: String
    private lateinit var adapter: WebAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupSpinner()
        setupRecyclerView()

        // Observe the siteList LiveData from the ViewModel
        homeViewModel.siteList.observe(viewLifecycleOwner, Observer { siteList ->
            // Update the adapter with new data
            if (siteList != null) {
                adapter.updateData(siteList)
            }
            if (siteList != null) {
                if (siteList.isEmpty()) {
                    binding.tvNoResults.visibility = View.VISIBLE
                } else {
                    binding.tvNoResults.visibility = View.GONE
                }
            }
        })

        binding.buttonSearch.setOnClickListener {
            val selectedEvent = eventType.lowercase()
            val city = binding.etEventLocation.text.toString().lowercase()

            if (selectedEvent == "choose an event category" || city.isEmpty()) {
                when {
                    selectedEvent == "choose an event category" && city.isEmpty() -> {
                        AlertDialog.Builder(requireContext())
                            .setTitle("Invalid Category and Missing City")
                            .setIcon(android.R.drawable.ic_delete)
                            .setMessage("Please select a valid event category and enter a city")
                            .setPositiveButton("OKAY", null)
                            .show()
                    }
                    selectedEvent == "choose an event category" -> {
                        AlertDialog.Builder(requireContext())
                            .setTitle("Invalid Category")
                            .setIcon(android.R.drawable.ic_delete)
                            .setMessage("Please select a valid event category.")
                            .setPositiveButton("OKAY", null)
                            .show()
                    }
                    city.isEmpty() -> {
                        AlertDialog.Builder(requireContext())
                            .setTitle("Missing City")
                            .setIcon(android.R.drawable.ic_delete)
                            .setMessage("Please enter a city to search for events.")
                            .setPositiveButton("OKAY", null)
                            .show()
                    }
                }
                return@setOnClickListener
            }

            // Valid input
            homeViewModel.fetchEvents(selectedEvent, city)
        }

        return root
    }

    override fun onResume() {
        super.onResume()
        homeViewModel.refreshFavorites()
    }

    private fun setupSpinner() {
        val spinner: Spinner = binding.spinnerEventType

        val eventTypes = listOf(
            "Choose an event category",
            "Music",
            "Sports",
            "Theater",
            "Family",
            "Arts & Theater",
            "Concerts",
            "Comedy",
            "Dance"
        )

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            eventTypes
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = this
    }

    private fun setupRecyclerView() {
        adapter = WebAdapter(ArrayList())
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        eventType = parent?.getItemAtPosition(position).toString()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        Toast.makeText(requireContext(), "Nothing is selected!", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}