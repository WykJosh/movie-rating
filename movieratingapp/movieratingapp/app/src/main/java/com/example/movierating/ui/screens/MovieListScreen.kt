package com.example.movierating.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.movierating.viewmodel.MovieViewModel
import androidx.compose.foundation.Image
import androidx.compose.runtime.LaunchedEffect
import com.example.movierating.data.database.MovieWithRating
import android.util.Log
@Composable
fun MovieListScreen(
    viewModel: MovieViewModel,
    isRooted: Boolean,
    onMovieClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {



    val movies by viewModel.movies.collectAsState()


    LaunchedEffect(Unit) {
        Log.d("MovieListScreen", "TRIGGERING loadMovies()")
        viewModel.loadMovies()
    }

    Scaffold(
        topBar = { AppBar() }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                items(items = movies, key = { it.movieId }) { movie ->
                    MovieListItem(
                        movie = movie,
                        isRooted = isRooted,
                        onMovieClick = { onMovieClick(movie.movieId) }
                    )
                }
            }
        }
    }
}

@Composable
fun MovieListItem(
    movie: MovieWithRating,
    isRooted: Boolean,
    onMovieClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onMovieClick)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Image(
                painter = rememberAsyncImagePainter(movie.posterUrl),
                contentDescription = movie.title,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)
            ) {
                Text(
                    text = movie.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))

                if (isRooted) {
                    Text(
                        text = "⚠ Device is rooted – rating hidden",
                        fontSize = 14.sp,
                        color = Color.Red,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "Rating: ${movie.rating}/5",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = movie.description.take(50) + "...",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar() {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "Movie Rating App",
                fontWeight = FontWeight.Bold
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}