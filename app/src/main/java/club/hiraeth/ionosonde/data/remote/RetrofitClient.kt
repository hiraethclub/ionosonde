package club.hiraeth.ionosonde.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val hamqslApi: IonosondeApi by lazy {
        Retrofit.Builder()
            .baseUrl(IonosondeApi.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
            .create(IonosondeApi::class.java)
    }

    val noaaApi: NoaaApi by lazy {
        Retrofit.Builder()
            .baseUrl(NoaaApi.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
            .create(NoaaApi::class.java)
    }
}
