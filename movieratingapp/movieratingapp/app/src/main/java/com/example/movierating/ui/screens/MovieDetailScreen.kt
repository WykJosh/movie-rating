package com.example.movierating.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.movierating.util.VoicePlayer
import com.example.movierating.util.VoiceRecorder
import com.example.movierating.viewmodel.MovieViewModel
import kotlinx.coroutines.delay
import androidx.core.content.ContextCompat
import java.util.Locale


// root detection happens here

@Composable
fun MovieDetailScreen(
    movieId: Int,
    viewModel: MovieViewModel,
    isRooted: Boolean, // this is the false/true changable and shown after defined in viewmodel as a function returning T F result from root folder/class
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val movieDetails by viewModel.currentMovieDetails.collectAsState()
    val scrollState = rememberScrollState()
    var showTrialDialog by remember { mutableStateOf(false) }
    var localStars by remember { mutableStateOf(0) }

    // comment state
    var commentText by remember { mutableStateOf("") }
    var isEditingComment by remember { mutableStateOf(true) }

    // --- Voice recording state ---
    val context = LocalContext.current
    val voiceRecorder = remember { VoiceRecorder(context) }
    var isRecording by remember { mutableStateOf(false) }
    var recordingSeconds by remember { mutableStateOf(0) }
    var currentRecordingPath by remember { mutableStateOf<String?>(null) }
    var movieIdForRecording by remember { mutableStateOf<Int?>(null) }

    val voicePlayer = remember { VoicePlayer(context) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentlyPlayingIndex by remember { mutableStateOf<Int?>(null) }

    // Timer while recording
    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingSeconds = 0
            while (isRecording) {
                delay(1000)
                recordingSeconds++
            }
        }
    }

    // Permission request
    val recordAudioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        val id = movieIdForRecording
        if (granted && id != null) {
            val path = voiceRecorder.startRecording(id)
            if (path != null) {
                currentRecordingPath = path
                isRecording = true
            }
        }
    }

    // Cleanup
    DisposableEffect(Unit) {
        onDispose {
            if (isRecording) voiceRecorder.cancel()
            if (isPlaying) voicePlayer.stop()
            currentlyPlayingIndex = null
        }
    }

    LaunchedEffect(movieId) {
        viewModel.loadMovieDetails(movieId)
    }

    LaunchedEffect(viewModel.ratingEvents) {
        viewModel.ratingEvents.collect { ev ->
            if (ev.success) {
                ev.rating?.let { r -> localStars = r.coerceIn(0, 5) }
            } else {
                showTrialDialog = true
            }
        }
    }

    BackHandler(onBack = onBackClick)

    Scaffold(
        topBar = { AppBar() }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            Button(
                onClick = onBackClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back to Movie List")
            }

            movieDetails?.let { movie ->
                // sync comment + editing mode whenever this movie changes
                LaunchedEffect(movie.movieId, movie.comment) {
                    commentText = movie.comment ?: ""
                    isEditingComment = movie.comment.isNullOrBlank()
                }

                Image(
                    painter = rememberAsyncImagePainter(model = movie.posterUrl),
                    contentDescription = movie.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = movie.title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                LaunchedEffect(movie.rating) {
                    localStars = movie.rating.coerceIn(0, 5)
                }

                // ------ STAR RATING / ROOT WARNING -------
                if (isRooted) {
                    RootWarningBox("!!! Device is rooted – ratings are hidden. !!!")
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Your Rating: ")
                        repeat(5) { index ->
                            val filled = index < localStars
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable {
                                        val canRateNow = viewModel.checkTrial()
                                        if (!canRateNow) {
                                            showTrialDialog = true
                                        } else {
                                            val newRating = (index + 1)
                                            localStars = index + 1
                                            viewModel.rateMovie(movie.movieId, newRating)
                                        }
                                    },
                                tint = if (filled) Color.Yellow else Color.Gray
                            )
                        }
                    }
                }

                // ------ TEXT COMMENT (directly after stars) -------
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Your Comment",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (isEditingComment) {
                    // Editable mode
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 64.dp),
                        placeholder = { Text("Write your thoughts about this movie...") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            viewModel.saveComment(movie.movieId, commentText)
                            isEditingComment = false // lock it in
                        },
                        modifier = Modifier.align(Alignment.End),
                        enabled = commentText.isNotBlank()
                    ) {
                        Text("Save Comment")
                    }
                } else {
                    // Locked / read-only mode
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFEFEFEF)
                    ) {
                        Text(
                            text = if (commentText.isBlank()) "No comment yet." else commentText,
                            modifier = Modifier.padding(12.dp),
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    TextButton(
                        onClick = { isEditingComment = true },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Edit Comment")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = movie.description,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(24.dp))

                // ------ VOICE REVIEW -------
                if (isRooted) {
                    RootWarningBox("⚠ Device is rooted – voice reviews are disabled.")
                } else {
                    Text(
                        text = "Voice Review",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (!isRecording) {
                        Button(
                            onClick = {
                                movieIdForRecording = movie.id
                                val permission = Manifest.permission.RECORD_AUDIO
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    permission
                                ) == PackageManager.PERMISSION_GRANTED
                                if (hasPermission) {
                                    val path = voiceRecorder.startRecording(movie.id)
                                    if (path != null) {
                                        currentRecordingPath = path
                                        isRecording = true
                                    }
                                } else {
                                    recordAudioLauncher.launch(permission)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Start Voice Review")
                        }
                    } else {
                        Column {
                            Text(
                                text = "Recording… ${formatSeconds(recordingSeconds)}",
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    val path = voiceRecorder.stopRecording()
                                        ?: currentRecordingPath
                                    Log.d("MovieDetail", "stopRecording returned: $path")
                                    Log.d(
                                        "MovieDetail",
                                        "Was using currentRecordingPath: $currentRecordingPath"
                                    )
                                    isRecording = false
                                    currentRecordingPath = null
                                    if (path != null) {
                                        viewModel.saveVoiceNotePath(movie.movieId, path)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Red,
                                    contentColor = Color.White
                                )
                            ) {
                                Text("Stop Recording")
                            }
                        }
                    }
                    // Show saved voice reviews
                    movie.voiceNotePath
                        ?.takeIf { it.isNotBlank() }
                        ?.let { pathsString ->
                            val paths = pathsString.split("|").filter { it.isNotBlank() }
                            if (paths.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Saved Voice Reviews (${paths.size})",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                paths.forEachIndexed { index, path ->
                                    Button(
                                        onClick = {
                                            if (!isPlaying || currentlyPlayingIndex != index) {
                                                isPlaying = true
                                                currentlyPlayingIndex = index
                                                voicePlayer.play(path) {
                                                    isPlaying = false
                                                    currentlyPlayingIndex = null
                                                }
                                            } else {
                                                voicePlayer.stop()
                                                isPlaying = false
                                                currentlyPlayingIndex = null
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 4.dp)
                                    ) {
                                        val label =
                                            if (isPlaying && currentlyPlayingIndex == index) {
                                                "Stop Review ${index + 1}"
                                            } else {
                                                "Play Review ${index + 1}"
                                            }
                                        Text(label)
                                    }
                                }
                            }
                        }
                }
            }
        }
    }

    if (showTrialDialog) {
        AlertDialog(
            onDismissRequest = { showTrialDialog = false },
            title = { Text("Trial limit reached") },
            text = { Text("Your trial allows rating up to 3 movies. Upgrade to Premium to rate more.") },
            confirmButton = {
                Button(onClick = { showTrialDialog = false }) { Text("OK") }
            }
        )
    }
}

// --- Helper Composables ---
@Composable
fun RootWarningBox(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .clip(RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Text(
            text = message,
            fontSize = 16.sp,
            color = Color.Red,
            fontWeight = FontWeight.Bold
        )
    }
}

// Format seconds into MM:SS
private fun formatSeconds(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}
