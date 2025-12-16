package com.example.movierating.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface RatingDao {
    @Query("SELECT * FROM ratings WHERE movieId = :movieId AND userId = :userId")
    suspend fun getRating(movieId: Int, userId: Int): RatingEntity?


    @Query(
        """
    SELECT 
        COALESCE(r.id, 0) AS id,
        m.id AS movieId,
        :userId AS userId,
        COALESCE(r.rating, 0) AS rating,
        r.voiceNotePath,
        r.comment,
        m.title,
        m.description,
        m.posterUrl
    FROM movies m 
    LEFT JOIN ratings r ON m.id = r.movieId AND r.userId = :userId 
    ORDER BY m.id DESC
"""
    ) // LEFT JOIN = show ALL movies from movies table, COALESCE(r.rating, 0) = use 0 if,  no rating exists, :userId AS userId = fill userId even for unrated movies
    suspend fun getUserRatings(userId: Int): List<MovieWithRating>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRating(rating: RatingEntity)

    @Update
    suspend fun updateRating(rating: RatingEntity)

}
