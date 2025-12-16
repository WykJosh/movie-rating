package com.example.movierating.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

import androidx.compose.foundation.layout.*

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.PasswordVisualTransformation

import androidx.compose.ui.unit.dp
import com.example.movierating.data.database.AppDatabase
import com.example.movierating.data.database.UserEntity
import kotlinx.coroutines.launch


@Composable
fun LoginScreen(
    onLoginSuccess: (Int, String) -> Unit, // we call this inside here, userId, token returns whoever calls LoginScreen must give me a function that takes (userId: Int, token: String) and returns Unit
    modifier: Modifier = Modifier
) {

    val context = androidx.compose.ui.platform.LocalContext.current // access point - gives current context inside activity, neeeded to call dbs rooms to know where to store db file
    val db =
        remember { AppDatabase.getDatabase(context) }// this initializes dbs with the context from here?
    val scope = rememberCoroutineScope() // this launches backg jobs used to run the "suspend" fucntions like insertuser here so scope lunch execs that


    //when the screen first appears, prefill the text fields with these values
    var username by remember { mutableStateOf("user1") }
    var password by remember { mutableStateOf("password") }


    var error by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }


    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Login", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = username, // this is where username is changed
            onValueChange = { username = it },
            label = { Text("Username") },
            enabled = !isLoading// enabled logging is true
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") }, // entering the password
            visualTransformation = PasswordVisualTransformation(),
                    enabled = !isLoading // enabled logging is true
        )
        Spacer(Modifier.height(16.dp))


        // this checks password and gives token
        Button(
            onClick = {
                isLoading = true // login processing
                scope.launch {
                    // Checks users w passwords(hardcoded)
                    val userId = when {
                        username == "user1" && password == "password" -> 1 // returns 1
                        username == "user2" && password == "password" -> 2
                        else -> null
                    }// returns 1 or 2 as id

                    if (userId != null) { // actual login confirm
                        // Verify/save user in DB
//                        val user = UserEntity(id = userId, username=username, token = "user$userId")
//                        db.userDao().insertUser(user) // inserts user
                        //instead of insert and conflicts(which insertuser has conflict safety but still) we use just read
                        val user = db.userDao().getUserById(userId)  // read existing row
                        val token = user?.token ?: "fallbackToken$userId" // save token in seperate line before we just passed onloginsuccess user.token cuz it also returned token from userentity
                        onLoginSuccess(userId, token)
                    } else {
                        error = "Wrong username/password"
                    }
                    isLoading = false // login processing ends
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text(if (isLoading) "Logging in..." else "Login")
        }

        if (error.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(error, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(16.dp))
        Text("Demo: user1/password or user2/password", style = MaterialTheme.typography.bodySmall) // this goes under button telling tester(mr koreman and ms an which users to use)
    }
}

