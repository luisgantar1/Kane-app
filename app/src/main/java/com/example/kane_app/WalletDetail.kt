package com.example.kane_app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

@Composable
fun WalletDetailScreen(navController: NavHostController, wallet: Wallet) {
    val transactions = remember { mutableStateListOf<Transaction>() } // List to hold transactions
    val isLoading = remember { mutableStateOf(true) } // Loading state for transactions

    LaunchedEffect(wallet.walletName) {
        val db = FirebaseFirestore.getInstance()

        // Fetch transactions related to the wallet
        db.collection("transactions")
            .whereEqualTo("walletName", wallet.walletName) // Assuming transactions have walletName field
            .get()
            .addOnSuccessListener { querySnapshot ->
                transactions.clear() // Clear existing transactions
                for (document in querySnapshot.documents) {
                    val transaction = document.toObject(Transaction::class.java)
                    transaction?.let {
                        // Get the Timestamp and convert it to Date
                        val timestamp = document.getTimestamp("date")
                        it.date = timestamp?.toDate() ?: Date() // Convert Timestamp to Date
                        transactions.add(it) // Add each transaction to the list
                    }
                }
                isLoading.value = false // Set loading to false once data is fetched
            }
            .addOnFailureListener {
                isLoading.value = false // Set loading to false on failure
                // Handle the error if necessary
            }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Back Button to Home
        IconButton(onClick = { navController.navigate("home") }) { // Navigasi ke layar home
            Icon(painter = painterResource(R.drawable.ic_back), contentDescription = "Back to Home")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Balance Display
        Text(
            text = "Balance",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "IDR ${wallet.balance}",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Add Transaction Button
        Button(
            onClick = {
                val db = FirebaseFirestore.getInstance()
                // Pastikan walletName yang digunakan adalah field di dalam dokumen dan bukan nama dokumen
                db.collection("wallets")
                    .whereEqualTo("walletName", wallet.walletName)
                    .whereEqualTo("eWalletType", wallet.eWalletType)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        if (!querySnapshot.isEmpty) {
                            val walletDocument = querySnapshot.documents.firstOrNull()
                            val walletData = walletDocument?.toObject(Wallet::class.java)
                            if (walletData != null) {
                                navController.navigate("add_transaction/${walletData.walletName}/${walletData.eWalletType}") {
                                    launchSingleTop = true
                                }
                            }
                        }
                    }
                    .addOnFailureListener {
                        // Tangani kegagalan pengambilan data jika perlu
                    }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Text("Add Transaction")
        }
        Spacer(modifier = Modifier.height(24.dp))

        // Transaction Details
        Text(text = "Transaction Details", fontSize = 20.sp, fontWeight = FontWeight.Bold)

        if (isLoading.value) {
            Text("Loading transactions...") // Display loading message
        } else if (transactions.isEmpty()) {
            Text("No transactions added.")
        } else {
            LazyColumn {
                items(transactions) { transaction ->
                    TransactionCard(transaction)
                }
            }
        }
    }
}

@Composable
fun TransactionCard(transaction: Transaction) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Removed date display
            Text("Name: ${transaction.name}")
            Text("Type: ${transaction.type}")
            Text("Amount: IDR ${transaction.amount}")
        }
    }
}

// Transaction data class
data class Transaction(
    var date: Date = Date(),  // Ganti menjadi var agar bisa dimodifikasi
    val name: String = "",
    val type: String = "",
    val amount: Double = 0.0
)
