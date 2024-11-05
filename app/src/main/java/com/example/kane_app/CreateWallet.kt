import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.kane_app.R
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun CreateWalletScreen(
    navController: NavHostController,
    onConfirmClick: () -> Unit,
    onBackClick: () -> Unit
) {
    var initialBalance by remember { mutableStateOf("IDR") }
    var jumlahBalance by remember { mutableStateOf("") }
    var selectedEWallet by remember { mutableStateOf("") }
    var walletName by remember { mutableStateOf("") } // Field for wallet name
    var showWalletPreview by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }

    val db = FirebaseFirestore.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        HeaderSection(
            onBackClick = onBackClick,
            onConfirmClick = {
                loading = true
                val jumlahBalanceDouble = jumlahBalance.toDoubleOrNull() ?: 0.0
                // Check if wallet already exists before saving
                checkAndSaveWalletToFirestore(db, initialBalance, jumlahBalanceDouble, selectedEWallet, walletName) { success ->
                    loading = false // Stop loading
                    if (success) {
                        navController.navigate("home") // Navigate to home on success
                    } else {
                        // Handle error (e.g., show a snackbar or a toast)
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text("Create Wallet", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(24.dp))
        TextField(
            value = initialBalance,
            onValueChange = {},
            label = { Text("Initial Balance") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true // Make initial balance read-only
        )

        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = jumlahBalance,
            onValueChange = { jumlahBalance = it },
            label = { Text("Enter Jumlah Balance") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
        EWalletCard(
            selectedEWallet = selectedEWallet,
            onEWalletSelected = {
                selectedEWallet = it
                if (it == "Other") {
                    walletName = "" // Reset wallet name when "Other" is selected
                }
                showWalletPreview = true
            }
        )

        // Show wallet name field if "Other", "ShopeePay", "Ovo", or "GoPay" is selected
        if (selectedEWallet in listOf("Other", "ShopeePay", "Ovo", "GoPay")) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = walletName,
                onValueChange = { walletName = it },
                label = { Text("Wallet Name") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Show wallet preview if an e-wallet is selected
        if (showWalletPreview) {
            Spacer(modifier = Modifier.height(16.dp))
            WalletPreviewCard(initialBalance = initialBalance, jumlahBalance = jumlahBalance, eWalletType = selectedEWallet)
        }

        // Show loading indicator if saving
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        }
    }
}

@Composable
fun HeaderSection(
    onBackClick: () -> Unit,
    onConfirmClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(painter = painterResource(id = R.drawable.ic_back), contentDescription = "Back")
        }
        Text("Create Wallet", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        IconButton(onClick = onConfirmClick) {
            Icon(painter = painterResource(id = R.drawable.ic_confirm), contentDescription = "Confirm")
        }
    }
}

@Composable
fun EWalletCard(
    selectedEWallet: String,
    onEWalletSelected: (String) -> Unit
) {
    val eWalletOptions = listOf("GoPay", "Ovo", "ShopeePay", "Other") // Add "Other" option
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Selected E-Wallet: $selectedEWallet",
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_dropdown),
                    contentDescription = "Dropdown Icon",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { expanded = !expanded }
                )
            }
            if (expanded) {
                eWalletOptions.forEach { eWallet ->
                    Text(
                        text = eWallet,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .clickable {
                                onEWalletSelected(eWallet)
                                expanded = false
                            }
                    )
                }
            }
        }
    }
}

@Composable
fun WalletPreviewCard(initialBalance: String, jumlahBalance: String, eWalletType: String) {
    val eWalletColor = when (eWalletType) {
        "GoPay" -> Color(0xFF00BFFF)
        "Ovo" -> Color(0xFF8A2BE2)
        "ShopeePay" -> Color(0xFFFFA500)
        "Other" -> Color(0xFF808080)
        else -> Color.Gray
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(eWalletColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Text("Balance", fontWeight = FontWeight.Bold, fontSize = 24.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text("E-Wallet Type: $eWalletType", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Initial Balance: $initialBalance", fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Jumlah Balance: IDR $jumlahBalance", fontSize = 16.sp)
    }
}

// Update pada fungsi checkAndSaveWalletToFirestore
fun checkAndSaveWalletToFirestore(
    db: FirebaseFirestore,
    initialBalance: String,
    jumlahBalance: Double,
    eWalletType: String,
    walletName: String,
    onComplete: (Boolean) -> Unit
) {
    val walletData = hashMapOf(
        "initialBalance" to initialBalance,
        "balance" to jumlahBalance, // Ensure this is a Double
        "eWalletType" to eWalletType,
        "walletName" to walletName
    )

    // Check if the wallet already exists
    if (eWalletType == "Other") {
        // Query to check for existing wallet based on both eWalletType and walletName
        db.collection("wallets")
            .whereEqualTo("eWalletType", eWalletType)
            .whereEqualTo("walletName", walletName)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty()) {
                    // If no existing wallet, create a new one
                    db.collection("wallets")
                        .add(walletData)
                        .addOnSuccessListener { documentReference ->
                            println("Wallet added with ID: ${documentReference.id}")
                            onComplete(true)
                        }
                        .addOnFailureListener { e ->
                            println("Error adding wallet: $e")
                            onComplete(false)
                        }
                } else {
                    // If wallet exists, you can handle this case if needed
                    println("Wallet already exists for eWalletType: $eWalletType and walletName: $walletName")
                    onComplete(false) // Or implement any logic you want here
                }
            }
            .addOnFailureListener { e ->
                println("Error checking wallet: $e")
                onComplete(false)
            }
    } else {
        // For other eWallet types, check by eWalletType only
        db.collection("wallets")
            .whereEqualTo("eWalletType", eWalletType)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty()) {
                    // If no existing wallet, create a new one
                    db.collection("wallets")
                        .add(walletData)
                        .addOnSuccessListener { documentReference ->
                            println("Wallet added with ID: ${documentReference.id}")
                            onComplete(true)
                        }
                        .addOnFailureListener { e ->
                            println("Error adding wallet: $e")
                            onComplete(false)
                        }
                } else {
                    // If wallet exists, update the balance
                    val existingWalletId = documents.first().id
                    val newBalance = (documents.first().getDouble("balance") ?: 0.0) + jumlahBalance;

                    db.collection("wallets").document(existingWalletId)
                        .update("balance", newBalance)
                        .addOnSuccessListener {
                            println("Wallet updated with ID: $existingWalletId")
                            onComplete(true)
                        }
                        .addOnFailureListener { e ->
                            println("Error updating wallet: $e")
                            onComplete(false)
                        }
                }
            }
            .addOnFailureListener { e ->
                println("Error checking wallet: $e")
                onComplete(false)
            }
    }
}


