package cu.control.queue.repository.retrofit

import cu.control.queue.BuildConfig
import retrofit2.Call
import retrofit2.http.*

interface APIService {

    @GET
    fun checkVersion(@Url url: String = CHECK_VERSION_URL): Call<String>

    @POST
    fun hiPorter(@Url url: String = HI_PORTER_URL, @Body data: String, @HeaderMap headers : Map<String, String>): Call<String>

    companion object {
        private const val BASE_URL = BuildConfig.BASE_URL
        private const val HI_PORTER_URL = "porter/hi"
        private const val CHECK_VERSION_URL = BuildConfig.BASE_URL
        val apiService: APIService
            get() = RetrofitClient.get(BASE_URL)!!.create(APIService::class.java)
    }
}