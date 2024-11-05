package com.example.kane_app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PinFragment : Fragment() {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun PinScreen(
        navController: NavController,
        email: String,
        password: String?,
        name: String?,
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore,
        isLogin: Boolean
    ) {
        var pin by remember { mutableStateOf("") }
        var confirmPin by remember { mutableStateOf("") }
        var errorMessage by remember { mutableStateOf("") }
        var isConfirmingPin by remember { mutableStateOf(false) }

        // Declare numbers for the numeric keypad layout
        val numbers = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("", "0", "DEL")
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Back Button and Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp, bottom = 40.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    if (isConfirmingPin) {
                        isConfirmingPin = false
                        confirmPin = ""
                    } else {
                        navController.popBackStack()
                    }
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = "Back"
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
            }

            Text(
                text = if (isLogin) "Enter Your PIN!" else if (isConfirmingPin) "Confirm your PIN!" else "Create PIN!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                color = Color.Black
            )

            // Dots for PIN input
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(4) {
                    Icon(
                        painter = painterResource(if ((isConfirmingPin && confirmPin.length > it) || (!isConfirmingPin && pin.length > it)) R.drawable.ic_filled_dot else R.drawable.ic_empty_dot),
                        contentDescription = null,
                        tint = if (errorMessage.isNotEmpty()) Color.Red else Color.Black,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))

            // Numeric keypad for entering PIN
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                for (row in numbers) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        for (number in row) {
                            if (number.isNotEmpty()) {
                                Button(
                                    onClick = {
                                        when (number) {
                                            "DEL" -> {
                                                if (isConfirmingPin) {
                                                    if (confirmPin.isNotEmpty()) confirmPin = confirmPin.dropLast(1)
                                                } else {
                                                    if (pin.isNotEmpty()) pin = pin.dropLast(1)
                                                }
                                            }
                                            else -> {
                                                if (isConfirmingPin) {
                                                    if (confirmPin.length < 4) confirmPin += number
                                                } else {
                                                    if (pin.length < 4) {
                                                        pin += number
                                                        if (pin.length == 4) isConfirmingPin = true
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier.size(80.dp),
                                    shape = CircleShape,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                                ) {
                                    if (number == "DEL") {
                                        Icon(
                                            painter = painterResource(id = R.drawable.delete),
                                            contentDescription = "Delete",
                                            tint = Color.Black,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    } else {
                                        Text(text = number, fontSize = 24.sp, color = Color.Black)
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.size(80.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Error message for mismatched PINs
            if (isConfirmingPin && pin != confirmPin && pin.isNotEmpty() && confirmPin.isNotEmpty()) {
                Text(
                    text = "Wrong PIN!",
                    color = Color.Red,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Button for Login/Register
            Button(
                onClick = {
                    if (isLogin) {
                        firestore.collection("users").whereEqualTo("email", email).get()
                            .addOnSuccessListener { documents ->
                                if (documents.isEmpty || documents.documents[0].getString("pin") != pin) {
                                    errorMessage = "Wrong PIN!"
                                } else {
                                    navController.navigate("home")
                                }
                            }
                            .addOnFailureListener {
                                errorMessage = "Error during login"
                            }
                    } else {
                        if (email.isNotEmpty() && password != null && password.isNotEmpty()) {
                            if (pin == confirmPin && pin.isNotEmpty()) {
                                // Register user in Firebase Auth
                                firebaseAuth.createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            // Add user data to Firestore
                                            val user = hashMapOf(
                                                "email" to email,
                                                "name" to name,
                                                "password" to password,
                                                "pin" to pin
                                            )
                                            firestore.collection("users").document(email)
                                                .set(user)
                                                .addOnSuccessListener {
                                                    // Optionally navigate or show a success message
                                                    navController.navigate("home")
                                                }
                                                .addOnFailureListener { e ->
                                                    errorMessage = "Failed to add user data: ${e.message}"
                                                }
                                        } else {
                                            errorMessage = "Registration failed: ${task.exception?.message}"
                                        }
                                    }
                            } else {
                                errorMessage = "PINs do not match"
                            }
                        } else {
                            errorMessage = "Email or password cannot be empty"
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                shape = CircleShape
            ) {
                Text(if (isLogin) "Login" else "Register", color = Color.White, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(10.dp))
            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}