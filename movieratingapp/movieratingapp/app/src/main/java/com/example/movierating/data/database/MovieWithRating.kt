package com.example.movierating.data.database


data class MovieWithRating(
    val id: Int,
    val movieId: Int,
    val userId: Int,
    val rating: Int,
    val voiceNotePath: String?,
    val comment: String?,
    val title: String,
    val description: String,
    val posterUrl: String
)
