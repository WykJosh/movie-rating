package com.example.movierating.data.api

import com.example.movierating.data.database.MovieWithRating
import com.example.movierating.data.database.RatingEntity
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Path
import retrofit2.http.Query

//local demo server
interface MovieApiService {

    // THE VULNERABLE ENDPOINT
    // Attacker can swap 'userId' here to fetch anyone's data from the "Server"
    @GET("/api/reviews")
    suspend fun getUserReviews(
        @Query("userId") userId: Int
    ): List<MovieWithRating>

    // Update/Create Review -  SAFE
    @POST("/api/reviews")
    suspend fun submitReview(
        @Body rating: RatingEntity
    )


    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String
    ): MovieResponse

}


