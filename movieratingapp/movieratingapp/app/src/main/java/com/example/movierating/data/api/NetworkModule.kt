package com.example.movierating.data.api

import android.util.Log
import com.example.movierating.data.database.RatingDao
import com.example.movierating.data.server.VulnerableDispatcher // Make sure this import matches where you put the Dispatcher file
import okhttp3.CertificatePinner
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.tls.HandshakeCertificates
import okhttp3.tls.HeldCertificate
import okhttp3.mockwebserver.MockWebServer
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.InetAddress
import java.util.concurrent.TimeUnit

object NetworkModule {

    private var mockWebServer: MockWebServer? = null
    private const val SERVER_PORT = 45678
    private const val SERVER_ADDRESS = "10.0.2.2" // host ip address in android studio emulator
    private var serverCert: HeldCertificate? = null

    private fun getServerCertificate(): HeldCertificate {
        if (serverCert == null) {
            serverCert = HeldCertificate.Builder()
                .addSubjectAlternativeName("localhost")
                .addSubjectAlternativeName("127.0.0.1")
                .addSubjectAlternativeName(SERVER_ADDRESS)
                .addSubjectAlternativeName("movieapp.local") // Matches your hosts file!
                .build()
        }
        return serverCert!!
    }

    private fun getHandshakeCertificates(): HandshakeCertificates {
        return HandshakeCertificates.Builder()
            .heldCertificate(getServerCertificate()) // The server's ID
            .addTrustedCertificate(getServerCertificate().certificate) // Trust it as a Root CA
            .build()
    }


    fun provideOkHttpClient(
        enableLogging: Boolean = false,
        certificatePinner: CertificatePinner? = null,
        extraInterceptors: List<Interceptor> = emptyList(),
        useCustomTrust: Boolean = false
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)

        certificatePinner?.let { builder.certificatePinner(it) } // SSL PINNING APPLIED HERE

        // HTTPS TRUST SETUP
        if (useCustomTrust) {
            val certificates = getHandshakeCertificates()
            builder.sslSocketFactory(certificates.sslSocketFactory(), certificates.trustManager)
        }

        if (enableLogging) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY // JSON response in logs
            }
            builder.addInterceptor(loggingInterceptor)
        }

        extraInterceptors.forEach { builder.addInterceptor(it) } // extra interceptors (token)

        return builder.build() // builds client
    }

    // retrofit handles networking, converting gson into data classes
    fun provideRetrofit(baseUrl: String, client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    inline fun <reified T> createApi(retrofit: Retrofit): T = retrofit.create(T::class.java)

    // THE ACTUAL CALLING TO THE API BOUND WITH RETROFIT (for real TMDB data)
    fun provideTmdbService(): TmdbApiService {
        val client = provideOkHttpClient(enableLogging = true)
        val retrofit = provideRetrofit("https://api.themoviedb.org/3/", client)
        return retrofit.create(TmdbApiService::class.java)
    }

    // USED FOR IDOR / LOOPBACK SERVER  - accepts RatingDao because the "Server" needs to read/write the DB!
    fun provideMovieApiService(ratingDao: RatingDao, userToken: String): MovieApiService {

        if (mockWebServer == null) {
            val server = MockWebServer()
            server.dispatcher = VulnerableDispatcher(ratingDao)

            // enable https on server
            val serverHandshake = getHandshakeCertificates()
            server.useHttps(serverHandshake.sslSocketFactory(), false)

            // Start on background thread with FIXED PORT
            Thread {
                try {
                    server.start(InetAddress.getByName("0.0.0.0"), SERVER_PORT)
                    Log.d("NetworkModule", "Loopback Server started on port $SERVER_PORT")
                } catch (e: Exception) {
                    Log.e("NetworkModule", "Server failed to start: ${e.message}")
                }
            }.start()

            mockWebServer = server
        }

        val localUrl = "https://$SERVER_ADDRESS:$SERVER_PORT/"

        val pin = CertificatePinner.pin(getServerCertificate().certificate) //pinning implementation - extracting the SHA-256 hash from our generated cert

        Log.d("NetworkModule", "Enforcing SSL Pin: $pin")

        val pinner = CertificatePinner.Builder()
            .add("movieapp.local", pin) // Pinning the domain to this specific key
            .add(SERVER_ADDRESS, pin)
            .build()

        val interceptor = RequestInterceptor(userToken)

        val client = provideOkHttpClient(
            enableLogging = true,
            certificatePinner = pinner, // Attach the Pinner
            extraInterceptors = listOf(interceptor),
            useCustomTrust = true // Trust our self-signed cert
        )

        val retrofit = provideRetrofit(localUrl, client)
        return retrofit.create(MovieApiService::class.java)
    }
}

// sets up headers for idor like /movies requests
class RequestInterceptor(private val userToken: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        // everything under /api/ gets the token, and the vulnerability is that
        // the server IGNORES the token and uses the query param instead
        if (request.url.pathSegments.contains("api")) {
            return chain.proceed(
                request.newBuilder()
                    .addHeader("Authorization", "Bearer $userToken")
                    .build()
            )
        }

        return chain.proceed(request)
    }
}