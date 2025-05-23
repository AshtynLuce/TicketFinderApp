package com.example.finalproject.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.finalproject.WebAdapter
import com.example.finalproject.databinding.FragmentNotificationsBinding


class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: WebAdapter
    private lateinit var notificationsViewModel: NotificationsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize ViewModel early
        notificationsViewModel = ViewModelProvider(this)[NotificationsViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupRecyclerView()

        // Observe favorites
        notificationsViewModel.siteList.observe(viewLifecycleOwner) { siteList ->
            adapter.updateData(siteList ?: emptyList())
            binding.tvNoResults2.visibility =
                if (siteList.isNullOrEmpty()) View.VISIBLE else View.GONE
        }

        return root
    }

    override fun onResume() {
        super.onResume()
        if (this::notificationsViewModel.isInitialized) {
            notificationsViewModel.fetchFavorites()
        }
    }

    private fun setupRecyclerView() {
        adapter = WebAdapter(ArrayList(), isFavoritesMode = true) {
            notificationsViewModel.fetchFavorites()
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}