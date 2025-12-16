package com.example.movierating.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movies")
data class MovieEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Int, //  gotten from tmdb
    val title: String,
    val description: String,
    val posterUrl: String,
)

