package com.securevpn.app.data.remote

import com.securevpn.app.data.model.BaseApiResponse
import com.securevpn.app.data.model.ConnectData
import com.securevpn.app.data.model.ConnectRequest
import com.securevpn.app.data.model.DisconnectRequest
import com.securevpn.app.data.model.HealthPayload
import com.securevpn.app.data.model.ServerItem
import com.securevpn.app.utils.Constants
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @GET(Constants.ENDPOINT_SERVERS)
    suspend fun getServers(): Response<BaseApiResponse<List<ServerItem>>>

    @POST(Constants.ENDPOINT_CONNECT)
    suspend fun connectVpn(
        @Body request: ConnectRequest
    ): Response<BaseApiResponse<ConnectData>>

    @POST(Constants.ENDPOINT_DISCONNECT)
    suspend fun disconnectVpn(
        @Body request: DisconnectRequest
    ): Response<BaseApiResponse<Void?>>

    @GET(Constants.ENDPOINT_HEALTH)
    suspend fun healthCheck(): Response<BaseApiResponse<HealthPayload>>
}