package com.example.movierating.data.server

import com.example.movierating.data.database.RatingDao
import com.example.movierating.data.database.RatingEntity
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest

class VulnerableDispatcher(
    private val ratingDao: RatingDao
) : Dispatcher() {

    private val gson = Gson()

    override fun dispatch(request: RecordedRequest): MockResponse {
        val path = request.path ?: return MockResponse().setResponseCode(404) // path of the link like /api/revires?userId=1
        val method = request.method // GET or POST

        val authHeader = request.getHeader("Authorization")

        return try {
            // -----------------------------------------------------------
            // ENDPOINT 1: GET REVIEWS (The IDOR Vulnerability)
            // -----------------------------------------------------------
            // Logic: The server blindly trusts the 'userId' query param
            if (path.startsWith("/api/reviews") && method == "GET") { // THIS IS IDOR ENDPOINT

                // Extract 'userId' from the URL (/api/reviews?userId=5)
                val userId = request.requestUrl?.queryParameter("userId")?.toIntOrNull() // such as "1" burp can change it

                if (userId != null) {
                    // SERVER-SIDE LOGIC: Query the DB for THAT user
                    val ratings = runBlocking {
                        ratingDao.getUserRatings(userId)
                    }

                    val responseBody = gson.toJson(ratings)
                    return MockResponse()
                        .setResponseCode(200)
                        .setBody(responseBody)
                        .addHeader("Content-Type", "application/json")
                }
            }

            // -----------------------------------------------------------
            // ENDPOINT 2: POST/UPDATE RATING
            // -----------------------------------------------------------
            if (path.startsWith("/api/reviews") && method == "POST") {

                // 1
                val bodyString = request.body.readUtf8()
                val ratingEntity = gson.fromJson(bodyString, RatingEntity::class.java)

                // 2
                val authenticatedUserId = getUserIdFromToken(authHeader)

                // 3
                if (authenticatedUserId == null) {
                    return MockResponse()
                        .setResponseCode(401)
                        .setBody("{\"error\": \"Unauthorized\"}")
                }

                if (authenticatedUserId != ratingEntity.userId) {
                    return MockResponse()
                        .setResponseCode(403)
                        .setBody("{\"error\": \"IDOR DETECTED: You cannot modify other users' data!\"}")
                }

                // 4
                runBlocking {
                    val existing = ratingDao.getRating(ratingEntity.movieId, authenticatedUserId!!)

                    if (existing != null) {
                        val updated = existing.copy(
                            rating = ratingEntity.rating,
                            comment = ratingEntity.comment,
                            voiceNotePath = ratingEntity.voiceNotePath
                        )
                        ratingDao.updateRating(updated)
                    } else {
                        val newEntry = ratingEntity.copy(userId = authenticatedUserId)
                        ratingDao.insertRating(newEntry)
                    }
                }

                return MockResponse().setResponseCode(200).setBody("{\"status\":\"success\"}") // sending back as json?
            }

            MockResponse().setResponseCode(404).setBody("Endpoint not found")

        } catch (e: Exception) {
            e.printStackTrace()
            MockResponse().setResponseCode(500).setBody("Server Error: ${e.message}")
        }
    }


    private fun getUserIdFromToken(header: String?): Int? {
        if (header == null || !header.startsWith("Bearer ")) return null

        val token = header.substringAfter("Bearer ").trim()

        return try {
            // Logic:
            // 1. The current token is header.user<ID>
            // 2. We split by "." to get the second part (user1)
            // 3. We remove user to get "1"

            if (token.contains(".")) {
                val payload = token.substringAfterLast(".")
                payload.replace("user", "").toIntOrNull()
            } else {
                token.toIntOrNull()
            }
        } catch (e: Exception) {
            null
        }
    }
}