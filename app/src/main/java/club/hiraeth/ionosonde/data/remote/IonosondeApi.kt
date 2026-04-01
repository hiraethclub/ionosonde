package club.hiraeth.ionosonde.data.remote

import retrofit2.http.GET

interface IonosondeApi {
    @GET("solar101vhf.php")
    suspend fun getSolarXml(): String

    companion object {
        const val BASE_URL = "https://www.hamqsl.com/"
    }
}

interface NoaaApi {
    @GET("products/noaa-planetary-k-index.json")
    suspend fun getKIndexHistory(): String

    @GET("text/27-day-outlook.txt")
    suspend fun getSfiForecast(): String

    @GET("products/noaa-planetary-k-index-forecast.json")
    suspend fun getKIndexForecast(): String

    companion object {
        const val BASE_URL = "https://services.swpc.noaa.gov/"
    }
}
