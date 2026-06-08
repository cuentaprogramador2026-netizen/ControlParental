package com.controlparental.app.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.controlparental.app.domain.model.AppMode

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.registrationComplete) {
        if (uiState.registrationComplete) onComplete()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (uiState.step) {
            OnboardingStep.WELCOME -> WelcomeStep(viewModel)
            OnboardingStep.CHOOSE_MODE -> ChooseModeStep(viewModel)
            OnboardingStep.SETUP_PIN -> SetupPinStep(uiState, viewModel)
            OnboardingStep.SETUP_PHONE -> SetupPhoneStep(uiState, viewModel)
            OnboardingStep.LINK_PARENT -> LinkParentStep(uiState, viewModel)
            OnboardingStep.PERMISSIONS -> PermissionsStep(uiState, viewModel)
            OnboardingStep.COMPLETE -> CompleteStep(onComplete)
        }
    }
}

@Composable
private fun WelcomeStep(viewModel: OnboardingViewModel) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "ControlParental",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Gestiona el tiempo de uso de dispositivos",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = { viewModel.selectMode(AppMode.PARENT) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Comenzar", modifier = Modifier.padding(8.dp))
        }
    }
}

@Composable
private fun ChooseModeStep(viewModel: OnboardingViewModel) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "¿Quién eres?",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(32.dp))

        Card(
            onClick = { viewModel.selectMode(AppMode.PARENT) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(40.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Soy padre/madre", style = MaterialTheme.typography.titleLarge)
                    Text("Configurar el dispositivo del padre", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            onClick = { viewModel.selectMode(AppMode.CHILD) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.ChildCare, contentDescription = null, modifier = Modifier.size(40.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Soy hijo/a", style = MaterialTheme.typography.titleLarge)
                    Text("Configurar el dispositivo del hijo", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun SetupPinStep(uiState: OnboardingUiState, viewModel: OnboardingViewModel) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Configurar PIN maestro", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Este PIN te permitirá desbloquear el dispositivo", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = uiState.pin,
            onValueChange = { if (it.length <= 6) viewModel.updatePin(it) },
            label = { Text("PIN (4-6 dígitos)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Next),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = uiState.pinConfirm,
            onValueChange = { if (it.length <= 6) viewModel.updatePinConfirm(it) },
            label = { Text("Confirmar PIN") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            isError = uiState.pinError != null,
            supportingText = uiState.pinError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { viewModel.goToNextStep() },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Continuar")
        }
    }
}

@Composable
private fun SetupPhoneStep(uiState: OnboardingUiState, viewModel: OnboardingViewModel) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Número de teléfono", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Opcional, para recuperación de cuenta", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedTextField(
            value = uiState.phoneNumber,
            onValueChange = { if (it.length <= 15) viewModel.updatePhone(it) },
            label = { Text("Número de teléfono") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { viewModel.goToNextStep() },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Continuar")
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = { viewModel.skipPhone() },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Omitir")
        }
    }
}

@Composable
private fun LinkParentStep(uiState: OnboardingUiState, viewModel: OnboardingViewModel) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Vincular con padre", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Ingresa el ID del dispositivo de tu padre", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedTextField(
            value = uiState.parentDeviceId,
            onValueChange = { viewModel.updateParentDeviceId(it) },
            label = { Text("ID del dispositivo padre") },
            singleLine = true,
            isError = uiState.error != null,
            supportingText = uiState.error?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { viewModel.goToNextStep() },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Vincular")
        }
    }
}

@Composable
private fun PermissionsStep(uiState: OnboardingUiState, viewModel: OnboardingViewModel) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Permisos necesarios", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "La aplicación necesita los siguientes permisos para funcionar correctamente",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))

        listOf(
            "Acceso a uso de aplicaciones" to "Monitorear qué apps están en uso",
            "Superposición" to "Mostrar pantalla de bloqueo",
            "Notificaciones" to "Recibir alertas y solicitudes",
            "Optimización de batería" to "Evitar que el servicio se detenga"
        ).forEach { (title, desc) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(desc, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (uiState.isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = { viewModel.completeRegistration() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Configurar dispositivo")
            }
        }
    }
}

@Composable
private fun CompleteStep(onComplete: () -> Unit) {
    LaunchedEffect(Unit) { onComplete() }
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("¡Configuración completada!", style = MaterialTheme.typography.headlineMedium)
    }
}
