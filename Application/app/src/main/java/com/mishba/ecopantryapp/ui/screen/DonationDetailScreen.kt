package com.mishba.ecopantryapp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mishba.ecopantryapp.model.DonationStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonationDetailScreen(
    donationId: String,
    navigateBack: () -> Unit
) {
    val context = LocalContext.current
    val vm: DonationDetailScreenViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return DonationDetailScreenViewModel(context, donationId) as T
            }
        }
    )
    val state by vm.uiState.collectAsState()
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Donation Details") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { padding ->
        val donation = state.donation
        if (state.isLoading || donation == null) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                if (state.isLoading) CircularProgressIndicator() else Text("Donation not found")
            }
            return@Scaffold
        }

        val isOwnDonation = donation.donorId == state.currentUserId
        val isClaimed = donation.status == DonationStatus.CLAIMED.name

        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(donation.itemName, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))

            AssistChip(onClick = {}, label = { Text(if (isClaimed) "Claimed" else "Available") })

            Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    DetailLine("Quantity", donation.quantity)
                    DetailLine("Category", donation.category)
                    donation.expiryDate?.let { DetailLine("Best Before", dateFormat.format(Date(it))) }
                    DetailLine("Pickup Address", donation.pickupAddress)
                    DetailLine("City", donation.pickupCity)
                    if (donation.availability.isNotBlank()) DetailLine("Availability", donation.availability)
                    DetailLine("Donor", donation.donorName)
                    if (donation.remarks.isNotBlank()) DetailLine("Remarks", donation.remarks)
                }
            }

            if (state.errorMessage.isNotBlank()) {
                Text(state.errorMessage, color = MaterialTheme.colorScheme.error)
            }

            if (state.claimSuccess) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("Claimed! The donor has been notified.", color = MaterialTheme.colorScheme.primary)
                }
            } else if (!isOwnDonation && !isClaimed) {
                Button(
                    onClick = vm::claim,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !state.isClaiming
                ) {
                    if (state.isClaiming) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Claim this Donation", fontWeight = FontWeight.Bold)
                    }
                }
            } else if (isOwnDonation) {
                Text(
                    "This is one of your own donation listings.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Medium)
    }
}

