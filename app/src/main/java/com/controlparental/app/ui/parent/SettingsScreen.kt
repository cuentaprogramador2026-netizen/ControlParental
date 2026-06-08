package com.controlparental.app.ui.parent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: ParentViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = { Text("Configuración") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Límites de tiempo
            Text("Límites de tiempo", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    TimeLimitRow(
                        label = "Límite diario (minutos)",
                        value = uiState.timeLimits.dailyMinutes.toString(),
                        onValueChange = { value ->
                            value.toIntOrNull()?.let { minutes ->
                                viewModel.updateTimeLimits(uiState.timeLimits.copy(dailyMinutes = minutes))
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    TimeLimitRow(
                        label = "Límite semanal (minutos)",
                        value = uiState.timeLimits.weeklyMinutes.toString(),
                        onValueChange = { value ->
                            value.toIntOrNull()?.let { minutes ->
                                viewModel.updateTimeLimits(uiState.timeLimits.copy(weeklyMinutes = minutes))
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Aplicaciones restringidas
            Text("Aplicaciones restringidas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            RestrictedAppsSection(
                restrictedPackages = uiState.restrictedPackages,
                onToggle = { packageName ->
                    val updated = uiState.restrictedPackages.toMutableSet().apply {
                        if (contains(packageName)) remove(packageName) else add(packageName)
                    }
                    viewModel.updateRestrictedPackages(updated)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Información del dispositivo
            Text("Información del dispositivo", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Info, contentDescription = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Tu ID de dispositivo:", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "Comparte este ID con tus hijos para que se vinculen",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeLimitRow(label: String, value: String, onValueChange: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.width(100.dp)
        )
    }
}

@Composable
private fun RestrictedAppsSection(
    restrictedPackages: Set<String>,
    onToggle: (String) -> Unit
) {
    val commonApps = remember {
        listOf(
            "com.instagram.android" to "Instagram",
            "com.facebook.katana" to "Facebook",
            "com.tiktok.tiktok" to "TikTok",
            "com.google.android.youtube" to "YouTube",
            "com.snapchat.android" to "Snapchat",
            "com.roblox.client" to "Roblox",
            "com.tencent.ig" to "PUBG Mobile",
            "com.supercell.clashofclans" to "Clash of Clans",
            "com.netflix.mediaclient" to "Netflix",
            "com.spotify.music" to "Spotify"
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            commonApps.forEach { (packageName, appName) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = packageName in restrictedPackages,
                        onClick = { onToggle(packageName) },
                        label = { Text(appName) },
                        leadingIcon = if (packageName in restrictedPackages) {
                            { Icon(Icons.Filled.Block, contentDescription = null, modifier = Modifier.padding(12.dp)) }
                        } else null
                    )
                }
            }
        }
    }
}
