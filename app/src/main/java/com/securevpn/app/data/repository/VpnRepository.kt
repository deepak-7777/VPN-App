package com.securevpn.app.data.repository

import android.content.Context
import android.provider.Settings
import com.securevpn.app.data.model.BaseApiResponse
import com.securevpn.app.data.model.ConnectData
import com.securevpn.app.data.model.ConnectRequest
import com.securevpn.app.data.model.DisconnectRequest
import com.securevpn.app.data.model.ServerItem
import com.securevpn.app.data.remote.RetrofitClient
import com.securevpn.app.utils.NetworkUtils
import com.securevpn.app.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VpnRepository(private val context: Context) {

    private val api = RetrofitClient.apiService
    private var currentSessionToken: String? = null

    suspend fun getServers(): Resource<List<ServerItem>> = withContext(Dispatchers.IO) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            return@withContext Resource.Error("No internet connection")
        }

        try {
            val response = api.getServers()
            if (!response.isSuccessful) {
                return@withContext Resource.Error("Server error: ${response.code()}")
            }

            val body = response.body()
            if (body?.success == true && !body.data.isNullOrEmpty()) {
                Resource.Success(body.data)
            } else {
                Resource.Error(body?.message ?: "Unable to fetch servers")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unable to fetch servers")
        }
    }

    suspend fun connectVpn(serverId: String): Resource<ConnectData> = withContext(Dispatchers.IO) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            return@withContext Resource.Error("No internet connection")
        }

        val serverIdLong = serverId.toLongOrNull()
            ?: return@withContext Resource.Error("Invalid server id")

        try {
            val request = ConnectRequest(
                serverId = serverIdLong,
                deviceId = getDeviceId()
            )

            val response = api.connectVpn(request)
            if (!response.isSuccessful) {
                return@withContext Resource.Error("Server error: ${response.code()}")
            }

            val body = response.body()
            if (body?.success != true || body.data == null) {
                return@withContext Resource.Error(body?.message ?: "Connection failed")
            }

            val connectData = body.data
            if (connectData.wireGuard == null) {
                return@withContext Resource.Error("WireGuard config missing from backend")
            }

            currentSessionToken = connectData.sessionToken
            Resource.Success(connectData)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Connection failed")
        }
    }

    suspend fun disconnectVpn(): Resource<BaseApiResponse<Void?>> = withContext(Dispatchers.IO) {
        try {
            val request = DisconnectRequest(
                sessionToken = currentSessionToken,
                deviceId = getDeviceId()
            )

            val response = api.disconnectVpn(request)
            currentSessionToken = null

            if (!response.isSuccessful) {
                return@withContext Resource.Error("Server error: ${response.code()}")
            }

            val body = response.body()
            if (body?.success == true) {
                Resource.Success(body)
            } else {
                Resource.Error(body?.message ?: "Disconnect failed")
            }
        } catch (e: Exception) {
            currentSessionToken = null
            Resource.Error(e.message ?: "Disconnect failed")
        }
    }

    fun getCurrentSessionToken(): String? = currentSessionToken

    fun clearSession() {
        currentSessionToken = null
    }

    private fun getDeviceId(): String {
        return Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: "unknown-device"
    }
}