package cu.control.queue.repository.retrofit

import cu.control.queue.BuildConfig
import retrofit2.Call
import retrofit2.http.*

interface APIService {

    @POST
    fun hiPorter(@Url url: String = HI_PORTER_URL, @Body data: String, @HeaderMap headers : Map<String, String>): Call<String>

    @POST
    fun sendActions(@Url url: String = ACTIONS_PORTER_URL, @Body payload: String, @HeaderMap headers : Map<String, String>): Call<String>

    companion object {
        private const val BASE_URL = BuildConfig.BASE_URL
        private const val HI_PORTER_URL = "hi"
        private const val ACTIONS_PORTER_URL = "actions"
        val apiService: APIService
            get() = RetrofitClient.get(BASE_URL)!!.create(APIService::class.java)
    }
}