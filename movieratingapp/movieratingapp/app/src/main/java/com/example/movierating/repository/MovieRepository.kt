package com.example.movierating.repository

import android.util.Log
import com.example.movierating.data.api.MovieApiService
import com.example.movierating.data.api.TmdbApiService
import com.example.movierating.data.database.MovieDao
import com.example.movierating.data.database.MovieEntity
import com.example.movierating.data.database.MovieWithRating
import com.example.movierating.data.database.RatingDao
import com.example.movierating.data.database.RatingEntity
import com.example.movierating.util.Base64Utils


class MovieRepository(
    private val movieDao: MovieDao,
    private val ratingDao: RatingDao,
    private val userId: Int,
    private val tmdbService: TmdbApiService, // Retrofit interface
    private val movieService: MovieApiService, // Loopback Server connection
) {
    companion object {
        val encodedApiKey: String = "YmZlYTYxOTNlMmVhM2Q3YWM5MGI5M2U3YjVkMDhiNWE="
    }


    // get all movies, Check cache first, call from API only if empty
    suspend fun getAllMovies(): List<MovieWithRating> {

        val cachedMovies = movieDao.getAllMovies()
        if (cachedMovies.isNotEmpty()) {
            return fetchUserRatingsFromNetworkOrDb(userId)
        }

        return try {
            val apiKey = Base64Utils.decode(encodedApiKey)

            val response =
                tmdbService.getPopularMovies(apiKey)

            response.movies.forEach { apiMovie ->

                val movieEntity =
                    movieDao.getMovieByApiId(apiMovie.id)
                        ?: MovieEntity(
                            id = apiMovie.id,
                            title = apiMovie.title,
                            description = apiMovie.description,
                            posterUrl = apiMovie.posterPath?.let {
                                "https://image.tmdb.org/t/p/w500$it"
                            } ?: ""
                        )
                movieDao.insertMovie(movieEntity)


                val existingRating = ratingDao.getRating(movieEntity.id, userId)
                if (existingRating == null) {
                    ratingDao.insertRating(
                        RatingEntity(
                            movieId = movieEntity.id,
                            userId = userId
                        )
                    )
                }
            }

            fetchUserRatingsFromNetworkOrDb(userId)

        } catch (e: Exception) {
            Log.e("MovieRepository", "API failed, loading from dbs: ${e.message}")
            fetchUserRatingsFromNetworkOrDb(userId)
        }
    }


    //returns movies with ratings left joined data
    suspend fun getMovieById(movieId: Int): MovieWithRating? {
        val movie = movieDao.getMovieById(movieId) ?: return null
        val rating = ratingDao.getRating(movieId, userId)
        return rating?.let {
            MovieWithRating(
                id = it.id,
                movieId = movieId,
                userId = userId,
                rating = it.rating,
                voiceNotePath = it.voiceNotePath,
                comment = it.comment,
                title = movie.title,
                description = movie.description,
                posterUrl = movie.posterUrl
            )
        }
    }


    // updates rating and adds voice note / comment - rating; updates via Network (Loopback Server)
    suspend fun updateRating(
        movieId: Int,
        rating: Int,
        voiceNotePath: String?,
        comment: String? = null
    ) {
        val entity = RatingEntity(
            movieId = movieId,
            userId = userId,
            rating = rating,
            voiceNotePath = voiceNotePath,
            comment = comment
        )

        try {
            movieService.submitReview(entity)
        } catch (e: Exception) {
            Log.e("MovieRepo", "Network Error: ${e.message}")

            // DETECT PROXY / SSL ATTACK
            if (isSSLError(e)) {
                throw SecurityException("Security Alert: Untrusted Proxy Detected! Connection Refused.")
            }
            throw e // other errors
        }
    }


    // just update the text comment (used from ViewModel)
    suspend fun updateComment(movieId: Int, comment: String) {
        // Reuse the main update function to go through the network
        val existing = ratingDao.getRating(movieId, userId)
        val currentRating = existing?.rating ?: 0
        val currentVoice = existing?.voiceNotePath

        updateRating(movieId, currentRating, currentVoice, comment)
    }

    // function that updates/returns api stuff if new popular movies
    suspend fun fetchMovieDetailsFromApi(movieId: Int): MovieWithRating? {
        return try {
            // Keep local rating; fetch other details but don't overwrite user's rating
            val local = movieDao.getMovieById(movieId) ?: return null
            val apiId = local.id
            val apiKey = Base64Utils.decode(encodedApiKey)
            val response = movieService.getMovieDetails(apiId, apiKey) // Note: This uses the TMDB part of the service interface

            val updatedMovie = local.copy(
                title = response.title,
                description = response.description,
                posterUrl = response.posterPath?.let { "https://image.tmdb.org/t/p/w500$it" } ?: "",
            )
            movieDao.updateMovie(updatedMovie)

            val rating = ratingDao.getRating(movieId, userId)
            rating?.let {
                MovieWithRating(
                    id = it.id,
                    movieId = movieId,
                    userId = userId,
                    rating = it.rating,
                    voiceNotePath = it.voiceNotePath,
                    comment = it.comment,
                    title = updatedMovie.title,
                    description = updatedMovie.description,
                    posterUrl = updatedMovie.posterUrl
                )
            }

        } catch (e: Exception) {
            Log.e("MovieRepository", "API call failed: ${e.message}")
            // Fallback: return whatever we can from DB
            val movie = movieDao.getMovieById(movieId) ?: return null
            val rating = ratingDao.getRating(movieId, userId)
            rating?.let {
                MovieWithRating(
                    id = it.id,
                    movieId = movieId,
                    userId = userId,
                    rating = it.rating,
                    voiceNotePath = it.voiceNotePath,
                    comment = it.comment,
                    title = movie.title,
                    description = movie.description,
                    posterUrl = movie.posterUrl
                )
            }
        }
    }



    // Tries Network First (Vulnerable), Falls back to DB
    private suspend fun fetchUserRatingsFromNetworkOrDb(targetUserId: Int): List<MovieWithRating> {
        return try {
            // -> MockWebServer -> VulnerableDispatcher -> RatingDao
            // INTERCEPT HERE: Change targetUserId query param to see other users data
            movieService.getUserReviews(targetUserId) //GET /api/reviews?userId={targetUserId}

        } catch (e: Exception) {

            // DETECT PROXY / SSL ATTACK
            if (isSSLError(e)) {
                throw SecurityException("Security Alert: MITM Attack Detected! We have blocked the connection.")
            }

            Log.e("MovieRepo", "Network fetch failed: ${e.message}")

            throw e
        }
    }

    // Helper to identify SSL Pinning/Trust failures
    private fun isSSLError(e: Throwable): Boolean {
        return e is javax.net.ssl.SSLHandshakeException ||
                e is javax.net.ssl.SSLPeerUnverifiedException ||
                e is java.security.cert.CertPathValidatorException
    }
}