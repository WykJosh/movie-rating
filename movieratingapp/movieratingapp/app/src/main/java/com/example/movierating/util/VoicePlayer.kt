package com.example.movierating.util

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import java.io.File

class VoicePlayer(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null

        // Play the given audio file. Calls onCompletion when finished.
    fun play(path: String, onCompletion: (() -> Unit)? = null) {
        try {
            stop() // stop any previous playback

            val file = File(path)
            if (!file.exists()) {
                Log.e("VoicePlayer", "File does not exist: $path")
                onCompletion?.invoke()
                return
            }

            mediaPlayer = MediaPlayer().apply {
                Log.d("VoicePlayer", "Attempting to play: $path, exists=${File(path).exists()}")
                setDataSource(path)
                prepare()
                setOnCompletionListener {
                    stop()
                    onCompletion?.invoke()
                }
                start()
            }
        } catch (e: Exception) {
            Log.e("VoicePlayer", "Error playing file: ${e.message}")
            stop()
            onCompletion?.invoke()
        }
    }
// stop play and release to play
    fun stop() {
        try {
            mediaPlayer?.apply {
                stop()
                reset()
                release()
            }
        } catch (_: Exception) {
        } finally {
            mediaPlayer = null
        }
    }

    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying == true
    }
}


