package com.example.movierating.data.api

import com.google.gson.annotations.SerializedName
data class MovieResponse( //
    val id: Int,
    val title: String,
    @SerializedName("overview") val description: String, // Matches 'overview' in JSON/TMDB
    @SerializedName("poster_path") val posterPath: String,
    @SerializedName("vote_average") val rating: Float
)

data class MovieListResponse(
    val page: Int,
    @SerializedName("results") val movies: List<MovieResponse>,
    @SerializedName("total_results") val totalResults: Int,
    @SerializedName("total_pages") val totalPages: Int
)
