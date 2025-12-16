package com.example.movierating.security

import android.content.Context
import java.io.File

class RootDetector(context: Context) {
    fun isDeviceRooted(): Boolean {
        return checkRootMethod1() || checkRootMethod2() || checkRootMethod3()
    }

    private fun checkRootMethod1(): Boolean {
        val buildTags = android.os.Build.TAGS
        return buildTags != null && buildTags.contains("test-keys")
    }
    // this means if build has tags that are test-keys then this is 1st indication that it is rooted

    private fun checkRootMethod2(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/system/xbin/su",
            "/system/bin/su",
            "/data/local/xbin/su"
        )
        for (path in paths) {
            if (File(path).exists()) return true
        }
        return false
    }
    //2nd indication that it is rooted is that if these files exist at all in the app folder

    private fun checkRootMethod3(): Boolean {
        return try {
            Runtime.getRuntime().exec("su").destroy()
            true
        } catch (e: Exception) {
            false
        }
    }
    //3rd indication that it is rooted is that if su command is executed and then is destroiable , therefore returns true, it is rooted

}