package com.example.kane_app

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HomeFragment : Fragment() {

    @Composable
    fun HomeScreen(navController: NavHostController) {
        Scaffold(
            topBar = { TopAppBarWithIcons() }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(100.dp))
                CreateWalletButton(navController)
                Spacer(modifier = Modifier.height(16.dp))


                WalletList(navController)
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TopAppBarWithIcons() {
        Column(
            modifier = Modifier.padding(top = 46.dp)
        ) {
            TopAppBar(
                title = {},
                navigationIcon = {
                    Card(
                        shape = CircleShape,
                        elevation = CardDefaults.cardElevation(4.dp),
                        modifier = Modifier
                            .padding(start = 40.dp, top = 8.dp, bottom = 8.dp)
                            .size(48.dp) // Ukuran total Card
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_default_profile),
                            contentDescription = "Profile Icon",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp)
                        )
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 40.dp, top = 8.dp, bottom = 8.dp)
                            .size(48.dp)
                            .border(1.dp, Color.Black, shape = MaterialTheme.shapes.small)
                            .padding(4.dp)
                    ) {
                        IconButton(onClick = {}) {
                            Icon(
                                painter = painterResource(R.drawable.ic_notification),
                                contentDescription = "Notification Icon",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun CreateWalletButton(navController: NavHostController) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(16.dp)
                .clickable { navController.navigate("create_wallet") }
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_add),
                contentDescription = "Create Wallet",
                modifier = Modifier
                    .size(32.dp)
                    .background(Color.Gray, CircleShape)
                    .padding(4.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Create Wallet", color = Color.Black, fontSize = 18.sp)
        }
    }

    @Composable
    fun WalletList(navController: NavHostController) {
        val db = FirebaseFirestore.getInstance()
        var wallets by remember { mutableStateOf<List<Wallet>>(emptyList()) }
        var loading by remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
            db.collection("wallets").get()
                .addOnSuccessListener { result ->
                    wallets = result.map { document ->
                        Wallet(
                            initialBalance = document.get("initialBalance")
                                .toString(), // Use get() to handle different types
                            balance = document.getDouble("balance") ?: 0.0,
                            eWalletType = document.getString("eWalletType") ?: "Unknown",
                            walletName = document.getString("walletName") ?: "Unnamed Wallet"
                        )
                    }
                    loading = false
                }
                .addOnFailureListener {
                    loading = false
                    // Handle error (optional)
                }
        }

        if (loading) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(wallets) { wallet ->
                    WalletCard(wallet = wallet) { selectedWallet ->
                        // Tentukan navigationPoint berdasarkan tipe wallet
                        val navigationPoint = if (selectedWallet.eWalletType == "Other") {
                            selectedWallet.walletName // Gunakan walletName untuk "Other"
                        } else {
                            selectedWallet.eWalletType // Gunakan eWalletType untuk tipe lainnya
                        }

                        // Navigasi menggunakan navigationPoint yang ditentukan
                        navController.navigate("walletDetails/$navigationPoint")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

    }


    @Composable
    fun WalletCard(wallet: Wallet, onNavigateToDetails: (Wallet) -> Unit) {
        val cardColor = when (wallet.eWalletType) {
            "ShopeePay" -> Color(0xFFFFA500) // Orange
            "GoPay" -> Color(0xFF00BFFF) // Blue
            "Ovo" -> Color(0xFF8A2BE2) // Purple
            "Other" -> Color(0xFF808080) // Gray for "Other" type
            else -> Color.Gray // Default color
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor)
        ) {
            // Use a Box to allow positioning
            Box(modifier = Modifier.padding(16.dp)) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = wallet.eWalletType,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 20.sp
                        )
                        Text(
                            text = wallet.walletName,
                            color = Color.White,
                            fontSize = 16.sp
                        )

                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Initial Balance: ${wallet.initialBalance}",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Balance: IDR ${wallet.balance}",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }

                // Navigation icon in the bottom right corner
                IconButton(
                    onClick = {
                        if (wallet.eWalletType.isNotEmpty()) {
                            onNavigateToDetails(wallet)
                        } else {
                            Log.e("WalletCard", "eWalletType is empty for wallet: ${wallet.walletName}")
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(top = 16.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_navigate),
                        contentDescription = "Navigate to Wallet Details",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

data class Wallet(
    var walletName: String = "",
    var eWalletType: String = "",
    var balance: Double = 0.0,
    var initialBalance: String = ""
)
