package com.example.kane_app.ui

import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import com.example.kane_app.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class InputEmailFragment : Fragment() {

    @Composable
    fun InputEmailScreen(navController: NavController, firebaseAuth: FirebaseAuth, firestore: FirebaseFirestore) {
        var email by remember { mutableStateOf("") }
        var errorMessage by remember { mutableStateOf("") }
        val isButtonEnabled by derivedStateOf { email.isNotEmpty() }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp, start = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = "Back"
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Hello",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color.Black,
                modifier = Modifier.padding(start = 16.dp)
            )
            Spacer(modifier = Modifier.height(15.dp))

            Text(
                text = "Enter your email for your\n Kane account",
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 32.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Email Input Field
            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Enter your email") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Error message display
            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(0.9f)
                )
            }

            Spacer(modifier = Modifier.height(350.dp))

            Button(
                onClick = {
                    // Reset error message on button click
                    errorMessage = ""

                    if (email.isNotEmpty()) {
                        checkEmailExists(navController, email, firestore) { exists ->
                            if (exists) {
                                Log.d("InputEmailFragment", "Email exists, navigating to login_with_email")
                                // Pass email to the login screen
                                navController.navigate("login_with_email/$email")
                            } else {
                                Log.d("InputEmailFragment", "Email does not exist, navigating to register")
                                // Navigate to registration if the email does not exist
                                navController.navigate("register/$email")
                            }
                        }
                    } else {
                        errorMessage = "Please enter a valid email"
                    }
                },
                enabled = isButtonEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "Next", color = Color.White, fontSize = 16.sp)
            }
        }
    }

    private fun checkEmailExists(
        navController: NavController,
        email: String,
        firestore: FirebaseFirestore,
        onResult: (Boolean) -> Unit
    ) {
        val trimmedEmail = email.trim()
        Log.d("InputEmailFragment", "Checking email existence for: $trimmedEmail")

        firestore.collection("users")
            .whereEqualTo("email", trimmedEmail)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val exists = !task.result.isEmpty
                    onResult(exists)
                } else {
                    Log.e(TAG, "Error checking email existence: ${task.exception?.message}")
                    onResult(false)
                }
            }
    }
}