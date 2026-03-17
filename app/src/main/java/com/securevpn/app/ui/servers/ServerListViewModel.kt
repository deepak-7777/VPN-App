package com.securevpn.app.ui.servers

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.securevpn.app.data.model.ServerItem
import com.securevpn.app.data.repository.VpnRepository
import com.securevpn.app.utils.Resource
import kotlinx.coroutines.launch

/**
 * ViewModel for ServerListFragment.
 * Handles server list fetching, filtering, and selection.
 */
class ServerListViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = VpnRepository(application)

    // Full unfiltered list
    private val _allServers = MutableLiveData<List<ServerItem>>()

    // Filtered list shown in RecyclerView
    private val _filteredServers = MutableLiveData<Resource<List<ServerItem>>>()
    val filteredServers: LiveData<Resource<List<ServerItem>>> = _filteredServers

    // Currently selected server
    private val _selectedServerId = MutableLiveData<String?>()
    val selectedServerId: LiveData<String?> = _selectedServerId

    init {
        fetchServers()
    }

    // ─────────────────────────────────────────────
    // Fetch servers from backend (or fallback)
    // ─────────────────────────────────────────────

    fun fetchServers() {
        viewModelScope.launch {
            _filteredServers.value = Resource.Loading
            val result = repository.getServers()
            when (result) {
                is Resource.Success -> {
                    _allServers.value = result.data
                    _filteredServers.value = Resource.Success(result.data)
                }
                is Resource.Error -> {
                    _filteredServers.value = Resource.Error(result.message)
                }
                else -> {}
            }
        }
    }

    // ─────────────────────────────────────────────
    // Filter by search query
    // ─────────────────────────────────────────────

    fun filterServers(query: String) {
        val all = _allServers.value ?: return
        if (query.isBlank()) {
            _filteredServers.value = Resource.Success(all)
            return
        }
        val q = query.trim().lowercase()
        val filtered = all.filter {
            it.country.lowercase().contains(q) || it.city.lowercase().contains(q)
        }
        _filteredServers.value = Resource.Success(filtered)
    }

    // ─────────────────────────────────────────────
    // Select a server
    // ─────────────────────────────────────────────

    fun selectServer(serverId: String) {
        _selectedServerId.value = serverId
    }

    fun getServerById(id: String): ServerItem? =
        _allServers.value?.find { it.id == id }
}
