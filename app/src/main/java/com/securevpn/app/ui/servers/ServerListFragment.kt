package com.securevpn.app.ui.servers

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.securevpn.app.R
import com.securevpn.app.adapter.ServerAdapter
import com.securevpn.app.data.model.ServerItem
import com.securevpn.app.databinding.FragmentServerListBinding
import com.securevpn.app.ui.home.HomeViewModel
import com.securevpn.app.utils.Resource
import com.securevpn.app.utils.hide
import com.securevpn.app.utils.show

class ServerListFragment : Fragment() {

    private var _binding: FragmentServerListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ServerListViewModel by viewModels()

    // Shared with HomeFragment to pass selected server
    private val homeViewModel: HomeViewModel by activityViewModels()

    private lateinit var serverAdapter: ServerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentServerListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearch()
        observeViewModel()
    }

    // ─────────────────────────────────────────────
    // RecyclerView Setup
    // ─────────────────────────────────────────────

    private fun setupRecyclerView() {
        serverAdapter = ServerAdapter(
            onServerClick = { server -> onServerSelected(server) },
            onFastestClick = {
                // Pick the lowest-ping server automatically
                val fastest = (viewModel.filteredServers.value as? Resource.Success)
                    ?.data?.minByOrNull { it.ping }
                fastest?.let { onServerSelected(it) }
            }
        )

        binding.rvServers.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = serverAdapter
            setHasFixedSize(false)
        }
    }

    // ─────────────────────────────────────────────
    // Search
    // ─────────────────────────────────────────────

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.filterServers(s?.toString() ?: "")
            }
        })
    }

    // ─────────────────────────────────────────────
    // Observe
    // ─────────────────────────────────────────────

    private fun observeViewModel() {
        viewModel.filteredServers.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> {
                    binding.progressServers.show()
                    binding.rvServers.hide()
                    binding.tvError.hide()
                }
                is Resource.Success -> {
                    binding.progressServers.hide()
                    binding.rvServers.show()
                    binding.tvError.hide()
                    val selectedId = homeViewModel.selectedServer.value?.id
                    serverAdapter.submitServers(result.data, selectedId)
                }
                is Resource.Error -> {
                    binding.progressServers.hide()
                    binding.rvServers.hide()
                    binding.tvError.show()
                    binding.tvError.text = result.message
                }
            }
        }

        // Keep adapter in sync with current selected server
        homeViewModel.selectedServer.observe(viewLifecycleOwner) { server ->
            serverAdapter.updateSelectedServer(server?.id)
        }
    }

    // ─────────────────────────────────────────────
    // Server Selected
    // ─────────────────────────────────────────────

    private fun onServerSelected(server: ServerItem) {
        homeViewModel.selectServer(server)
        viewModel.selectServer(server.id)
        // Navigate back to HomeFragment
        findNavController().navigate(R.id.homeFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
