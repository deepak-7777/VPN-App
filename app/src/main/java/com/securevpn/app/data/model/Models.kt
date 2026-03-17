package com.securevpn.app.data.model

import com.google.gson.annotations.SerializedName

data class BaseApiResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: T?
)

// -----------------------------
// Server models
// -----------------------------

data class ServerItem(
    @SerializedName("id") val id: String,
    @SerializedName("country") val country: String,
    @SerializedName("city") val city: String,
    @SerializedName("countryCode") val countryCode: String,
    @SerializedName("flag") val flag: String,
    @SerializedName("ping") val ping: Int,
    @SerializedName("isFree") val isFree: Boolean = true,
    @SerializedName("isRecommended") val isRecommended: Boolean = false,
    @SerializedName("signalLevel") val signalLevel: Int = 3
)

// -----------------------------
// Health models
// -----------------------------

data class HealthPayload(
    @SerializedName("status") val status: String,
    @SerializedName("version") val version: String?,
    @SerializedName("app") val app: String?
)

// -----------------------------
// Connect / Disconnect models
// -----------------------------

data class ConnectRequest(
    @SerializedName("serverId") val serverId: Long,
    @SerializedName("deviceType") val deviceType: String = "android",
    @SerializedName("deviceId") val deviceId: String
)

data class DisconnectRequest(
    @SerializedName("sessionToken") val sessionToken: String?,
    @SerializedName("deviceId") val deviceId: String
)

data class ConnectData(
    @SerializedName("sessionToken") val sessionToken: String?,
    @SerializedName("assignedIp") val assignedIp: String?,
    @SerializedName("server") val server: ServerItem?,
    @SerializedName("wireGuard") val wireGuard: WireGuardConfigDto?
)

data class WireGuardConfigDto(
    @SerializedName("serverPublicKey") val serverPublicKey: String?,
    @SerializedName("clientPrivateKey") val clientPrivateKey: String?,
    @SerializedName("endpointHost") val endpointHost: String?,
    @SerializedName("endpointPort") val endpointPort: Int?,
    @SerializedName("clientAddress") val clientAddress: String?,
    @SerializedName("dnsServers") val dnsServers: List<String>?,
    @SerializedName("allowedIps") val allowedIps: List<String>?,
    @SerializedName("persistentKeepalive") val persistentKeepalive: Int?
)