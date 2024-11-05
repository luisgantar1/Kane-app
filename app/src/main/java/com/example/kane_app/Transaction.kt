package com.example.kane_app

import android.app.DatePickerDialog
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import java.sql.Timestamp
import java.text.SimpleDateFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    navController: NavHostController,
    walletName: String // Pass walletName as a parameter
) {
    var transactionAmount by remember { mutableStateOf("") }
    var transactionType by remember { mutableStateOf("Pemasukan") }
    var transactionDate by remember { mutableStateOf("") }
    var transactionName by remember { mutableStateOf("") } // Field untuk nama transaksi
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var showTransactionTypeDialog by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    val firestore = FirebaseFirestore.getInstance()

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        // Icon Kembali
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(painter = painterResource(R.drawable.ic_back), contentDescription = "Back")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Add Transaction", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        // Input untuk nama transaksi
        TextField(
            value = transactionName,
            onValueChange = { transactionName = it },
            label = { Text("Transaction Name") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Input untuk jumlah transaksi
        TextField(
            value = transactionAmount,
            onValueChange = { transactionAmount = it },
            label = { Text("Transaction Amount") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Input untuk tanggal transaksi
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = transactionDate,
                onValueChange = {},
                label = { Text("Transaction Date") },
                readOnly = true,
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        showDatePickerDialog = true
                    }
            )

            IconButton(onClick = {
                showDatePickerDialog = true
            }) {
                Icon(painter = painterResource(R.drawable.ic_calendar), contentDescription = "Select Date")
            }
        }

        // Dialog pemilih tanggal
        if (showDatePickerDialog) {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(
                navController.context,
                { _, selectedYear, selectedMonth, selectedDay ->
                    transactionDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                    showDatePickerDialog = false
                },
                year,
                month,
                day
            ).show()
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Menampilkan pilihan jenis transaksi
        Text(text = "Select Transaction Type", style = MaterialTheme.typography.titleMedium)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showTransactionTypeDialog = true }
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = transactionType, style = MaterialTheme.typography.bodyLarge)
                Icon(painter = painterResource(R.drawable.ic_dropdown), contentDescription = "Select Transaction Type")
            }
        }

        // Dialog untuk memilih tipe transaksi
        if (showTransactionTypeDialog) {
            AlertDialog(
                onDismissRequest = { showTransactionTypeDialog = false },
                title = { Text("Select Transaction Type") },
                text = {
                    Column {
                        listOf("Pemasukan", "Pengeluaran").forEach { type ->
                            TextButton(onClick = {
                                transactionType = type
                                showTransactionTypeDialog = false
                            }) {
                                Text(type)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showTransactionTypeDialog = false }) {
                        Text("OK")
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (transactionAmount.isNotBlank() && transactionDate.isNotBlank() && transactionName.isNotBlank()) {
                    loading = true // Menunjukkan loading saat transaksi sedang diproses
                    val amount = transactionAmount.toDoubleOrNull() ?: 0.0

                    // Konversi string tanggal ke objek Date
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val date = dateFormat.parse(transactionDate) // Parse string tanggal

                    // Cek jika tanggal berhasil diparse
                    if (date != null) {
                        // Query wallet berdasarkan walletName
                        val walletQuery = firestore.collection("wallets")
                            .whereEqualTo("walletName", walletName) // Menggunakan field 'walletName'

                        walletQuery.get().addOnSuccessListener { querySnapshot ->
                            if (!querySnapshot.isEmpty) {
                                val walletSnapshot = querySnapshot.documents.first()
                                val currentBalance = walletSnapshot.getDouble("balance") ?: 0.0

                                // Tentukan jumlah balance baru
                                val newBalance = if (transactionType == "Pemasukan") {
                                    currentBalance + amount // Tambah jika pemasukan
                                } else {
                                    currentBalance - amount // Kurang jika pengeluaran
                                }

                                // Mulai transaksi untuk update balance
                                firestore.runTransaction { transaction ->
                                    // Update balance di Firestore
                                    transaction.update(walletSnapshot.reference, "balance", newBalance)

                                    // Simpan detail transaksi
                                    val transactionData = hashMapOf(
                                        "walletName" to walletName,
                                        "transactionName" to transactionName, // Menyimpan nama transaksi
                                        "amount" to amount,
                                        "date" to Timestamp(date.time), // Ubah di sini
                                        "type" to transactionType
                                    )
                                    firestore.collection("transactions").add(transactionData)
                                }.addOnSuccessListener {
                                    navController.navigate("walletDetails/$walletName")
                                    loading = false
                                }.addOnFailureListener { e ->
                                    loading = false
                                    Log.e("Firestore Error", "Error saving transaction", e)
                                }
                            } else {
                                loading = false
                                Toast.makeText(navController.context, "Wallet tidak ditemukan!", Toast.LENGTH_SHORT).show()
                            }
                        }.addOnFailureListener { e ->
                            loading = false
                            Log.e("Firestore Error", "Error retrieving wallet", e)
                        }
                    } else {
                        loading = false
                        Toast.makeText(navController.context, "Invalid date format!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(navController.context, "Please fill in all fields!", Toast.LENGTH_SHORT).show()
                }
            }
        ) {
            Text("Save Transaction")
        }

        // Loading Indicator
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}
