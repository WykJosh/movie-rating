package com.example.movierating.data.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "ratings",
    foreignKeys = [
        ForeignKey(entity = MovieEntity::class, parentColumns = ["id"], childColumns = ["movieId"]),
        ForeignKey(entity = UserEntity::class, parentColumns = ["id"], childColumns = ["userId"]) // id in each entity = childcolumn name in this entity
    ]
)
data class RatingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val movieId: Int, // apimovieid btw
    val userId: Int,
    val rating: Int = 0,
    val voiceNotePath: String? = null,
    val comment: String? = null
)
