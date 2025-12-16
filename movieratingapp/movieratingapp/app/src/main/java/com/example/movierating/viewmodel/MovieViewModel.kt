package com.example.movierating.viewmodel

import android.util.Log
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movierating.repository.MovieRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.movierating.data.database.MovieWithRating
import com.example.movierating.security.RootDetector

data class RatingEvent(val success: Boolean, val rating: Int?)

class MovieViewModel(
    private val repository: MovieRepository,
    private val context: Context

) : ViewModel() {

    // ! private = can change value
    // public = ui can read

    // ----------------------------------------------------
    // SECURITY ALERT STATE
    // ----------------------------------------------------
    var securityViolation by mutableStateOf<String?>(null)
        private set

    // Call this from UI when user clicks "OK" or "Close" on the alert
    fun dismissSecurityAlert() {
        securityViolation = null
    } // not used


    private val _movies = MutableStateFlow<List<MovieWithRating>>(emptyList())
    val movies: StateFlow<List<MovieWithRating>> = _movies.asStateFlow()


    private val _currentMovieDetails =
        MutableStateFlow<MovieWithRating?>(null)
    val currentMovieDetails: StateFlow<MovieWithRating?> = _currentMovieDetails.asStateFlow()

    // 3
    private val _ratingEvents = MutableSharedFlow<RatingEvent>(replay = 0)
    val ratingEvents = _ratingEvents.asSharedFlow()

    // 4
    private var currentDetailsJob: Job? = null
    private var lastRequestedMovieId: Int? = null

    // 5
    private val TRIAL_LIMIT = 3 // max to rate
    private var RATED_MOVIE_COUNT = 0

    //6 - root detector
    private val _isRooted = MutableStateFlow(false)
    val isRooted: StateFlow<Boolean> = _isRooted.asStateFlow()

    // setting up the trial limit
    private fun getMovieCount(): Int = RATED_MOVIE_COUNT
    private fun increaseRatedMovieCount() {
        RATED_MOVIE_COUNT++
    }

    fun checkTrial(): Boolean {
        return if (getMovieCount() < TRIAL_LIMIT) {
            increaseRatedMovieCount()
            true
        } else {
            false
        }
    }


    fun loadMovies() {

        viewModelScope.launch {
            try {
                val moviesFromRepo = repository.getAllMovies() // here gets
                    .distinctBy { it.movieId }
                _movies.value = moviesFromRepo // sets empty list value to that gotten movies
            } catch (e: SecurityException) {
                // NEW: Catch SSL/Security errors from Repository
                Log.e("MovieViewModel", "Security Violation: ${e.message}")
                securityViolation = e.message
            } catch (e: Exception) {
                Log.e("MovieViewModel", "Error loading movies: ${e.message}")
            }
        }
    }

    fun loadMovieDetails(movieId: Int) { // for details page
        currentDetailsJob?.cancel()
        currentDetailsJob = viewModelScope.launch {
            try {
                lastRequestedMovieId = movieId

                // 1: load current details from DB (Still safe as it is local)
                val fromDb = repository.getMovieById(movieId)
                _currentMovieDetails.value = fromDb

                // 2:  refresh from api - Network Call -> Vulnerable to SSL PINNING  check
                val updated = repository.fetchMovieDetailsFromApi(movieId)
                if (updated != null && lastRequestedMovieId == movieId) {
                    _currentMovieDetails.value = updated
                }
            } catch (e: SecurityException) {
                // Catch SSL/Security errors
                Log.e("MovieViewModel", "Security Violation in Details: ${e.message}")
                securityViolation = e.message
            } catch (e: Exception) {
                Log.e("MovieViewModel", "Error loading details: ${e.message}")
            }
        }
    }

    fun rateMovie(movieId: Int, newRating: Int) {
        viewModelScope.launch {
            try {
                //refersh dets and list - Network Call -> Vulnerable
                repository.updateRating(movieId, newRating, voiceNotePath = null)

                val updated = repository.getMovieById(movieId)
                _currentMovieDetails.value = updated
                _movies.value = repository.getAllMovies()

                _ratingEvents.emit(
                    RatingEvent(
                        true,
                        newRating
                    )
                )
            } catch (e: SecurityException) {
                Log.e("MovieViewModel", "Security Violation in Rating: ${e.message}")
                securityViolation = e.message
                _ratingEvents.emit(RatingEvent(false, null))
            } catch (e: Exception) {
                Log.e("MovieViewModel", "Error rating movie: ${e.message}")
                _ratingEvents.emit(RatingEvent(false, null))
            }
        }
    }


    // ----------------------voice------------------
    fun saveVoiceNotePath(movieId: Int, path: String) {
        viewModelScope.launch {
            try {

                val movie = repository.getMovieById(movieId) ?: return@launch

                val newPathString = if (movie.voiceNotePath.isNullOrBlank()) {
                    path // this means no previous path, set new
                } else {
                    movie.voiceNotePath + "|" + path
                }

                //  update rating in DB, passing that new voice note path string (Network Call)
                repository.updateRating(movieId, movie.rating, newPathString)

                // Refresh detail and list data to re-show(like refreshing page) UI with updated voice notes
                _currentMovieDetails.value = repository.getMovieById(movieId)
                _movies.value =
                    repository.getAllMovies()
            } catch (e: SecurityException) {
                securityViolation = e.message
            } catch (e: Exception) {
                Log.e("MovieViewModel", "Error saving voice note: ${e.message}")
            }
        }
    }


    // ------------ COMMENTS -------------
    fun saveComment(movieId: Int, comment: String) {
        viewModelScope.launch {
            try {
                repository.updateComment(movieId, comment)
                _currentMovieDetails.value = repository.getMovieById(movieId)
                _movies.value = repository.getAllMovies()
            } catch (e: SecurityException) {
                securityViolation = e.message
            } catch (e: Exception) {
                Log.e("MovieViewModel", "Error saving comment: ${e.message}")
            }
        }
    }

    // ------------rootdetect-------------
    init {
        _isRooted.value = RootDetector(context).isDeviceRooted()
        Log.d("MovieViewModel", "Root check: ${_isRooted.value}")
    }


}