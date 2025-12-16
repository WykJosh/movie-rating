package com.example.movierating

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.movierating.data.api.NetworkModule
import com.example.movierating.data.api.RequestInterceptor
import com.example.movierating.data.database.AppDatabase
import com.example.movierating.repository.MovieRepository
import com.example.movierating.security.RootDetector
import com.example.movierating.ui.screens.MovieDetailScreen
import com.example.movierating.ui.screens.MovieListScreen
import com.example.movierating.ui.theme.MovieratingTheme
import com.example.movierating.viewmodel.MovieViewModel

import com.example.movierating.ui.screens.LoginScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContent {


            MovieratingTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    AppWithLogin()
                }

            }
        }


    }
}


@Composable
fun AppWithLogin() {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }

    var isLoggedIn by rememberSaveable { mutableStateOf(false) }
    var currentUserId by rememberSaveable { mutableStateOf(0) }
    var currentUserToken by rememberSaveable { mutableStateOf("") }


    if (!isLoggedIn) {
        LoginScreen (
            onLoginSuccess= {userId, token ->
                currentUserId = userId
                currentUserToken = token
                isLoggedIn = true
            }
        )
    }else {
        val tmdbApiService = remember { NetworkModule.provideTmdbService() }
        val movieApiService = remember(currentUserToken) {
            NetworkModule.provideMovieApiService(
                ratingDao = db.ratingDao(),
                userToken = currentUserToken
            )
        }

        val repository = remember(currentUserId, currentUserToken) {
            MovieRepository(
                movieDao = db.movieDao(),
                ratingDao = db.ratingDao(),
                userId = currentUserId,
                tmdbService = tmdbApiService,
                movieService = movieApiService

            )
        }

        MovieAppNavigation(
            userId = currentUserId,
            userToken = currentUserToken, // gives to MovieAppNavigation
            repository = repository // based on movierepo basically
        )

    }

}

@Composable
fun MovieAppNavigation(userId:Int, userToken: String,    repository: MovieRepository,
                       modifier: Modifier = Modifier) {


    val context = LocalContext.current
    val rootDetector = remember { RootDetector(context) }

    val movieViewModel = remember { MovieViewModel(
        repository,
        context ) }
    val isRooted by movieViewModel.isRooted.collectAsState()


    var currentScreen by rememberSaveable { mutableStateOf("list") }
    var selectedMovieId by rememberSaveable { mutableStateOf(0) }

    // SECURITY ALERT DIALOG (SSL PINNING ALERT)
    val securityViolation = movieViewModel.securityViolation

    if (securityViolation != null) {
        AlertDialog(
            onDismissRequest = {
                // Do nothing, force user to click the button to dismiss/close the app
            },
            title = {
                Text(text = "⚠️ Security Alert")
            },
            text = {
                Text(text = securityViolation)
            },
            icon = {
                Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red)
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Crash/Close the app when confirmed
                        android.os.Process.killProcess(android.os.Process.myPid())
                    }
                ) {
                    Text("Close App")
                }
            }
        )
    }

    when (currentScreen) { // switching the screens
        "list" -> MovieListScreen(
            viewModel = movieViewModel,
            isRooted = isRooted,
            onMovieClick = { movieId ->
                selectedMovieId = movieId
                currentScreen = "detail"
            }
        )

        "detail" -> MovieDetailScreen(
            movieId = selectedMovieId,
            viewModel = movieViewModel,
            isRooted = isRooted,
            onBackClick = { currentScreen = "list" }
        )
    }



}