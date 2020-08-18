package cu.control.queue.repository.retrofit

import cu.control.queue.BuildConfig
import cu.control.queue.repository.dataBase.entitys.payload.Queue
import retrofit2.Call
import retrofit2.http.*

interface APIService {

    @POST
    fun hiPorter(
        @Url url: String = HI_PORTER_URL,
        @Body data: String,
        @HeaderMap headers: Map<String, String>
    ): Call<String>

    @GET
    fun getQueue(
        @Url url: String = GET_QUEUE_PORTER_URL,
        @HeaderMap headers: Map<String, String>
    ): Call<Queue>

    @PUT
    fun putCollaborator(
        @Url url: String = COLABORATOR_PORTER_URL,
        @HeaderMap headers: Map<String, String>,
        @Body data: String
    ): Call<String>

    @HTTP(method = "DELETE", hasBody = true)
    fun deleteCollaborator(
        @Url url: String = COLABORATOR_PORTER_URL,
        @HeaderMap headers: Map<String, String>,
        @Body data: String
    ): Call<String>

    @POST
    fun sendActions(
        @Url url: String = ACTIONS_PORTER_URL,
        @Body payload: String,
        @HeaderMap headers: Map<String, String>
    ): Call<String>

    companion object {
        private const val BASE_URL = BuildConfig.BASE_URL
        private const val HI_PORTER_URL = "hi"
        private const val COLABORATOR_PORTER_URL = "collaborator"
        private const val ACTIONS_PORTER_URL = "actions"
        private const val GET_QUEUE_PORTER_URL = "queue"
        val apiService: APIService
            get() = RetrofitClient.get(BASE_URL)!!.create(APIService::class.java)
    }
}