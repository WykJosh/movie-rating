package com.example.movierating.data.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

//uses api key to get back the movies from the api
interface TmdbApiService {
    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String
    ): MovieListResponse

}