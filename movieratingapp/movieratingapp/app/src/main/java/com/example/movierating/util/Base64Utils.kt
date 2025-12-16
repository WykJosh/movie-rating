package com.example.movierating.util

import android.util.Base64

object Base64Utils {

    // Decode a Base64 string back to plain text
    fun decode(input: String): String {
        return String(Base64.decode(input, Base64.NO_WRAP), Charsets.UTF_8)
    }
}

