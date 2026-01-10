package Network

import Model.JamendoResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface JamendoApi {

    @GET("tracks/")
    suspend fun getTracks(
        @Query("client_id") clientId: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 20,
        @Query("audioformat") audioFormat: String = "mp32"
    ): JamendoResponse
}