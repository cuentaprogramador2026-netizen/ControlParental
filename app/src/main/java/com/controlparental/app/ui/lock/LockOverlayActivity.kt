package com.controlparental.app.ui.lock

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.FilledTonalButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.controlparental.app.ui.theme.LockAccent
import com.controlparental.app.ui.theme.LockBackground
import com.controlparental.app.ui.theme.LockCard
import com.controlparental.app.ui.theme.LockError
import com.controlparental.app.ui.theme.LockSuccess
import com.controlparental.app.ui.theme.LockText
import com.controlparental.app.util.Constants

class LockOverlayActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        makeActivityFullscreen()
        setContentView(
            androidx.compose.ui.platform.ComposeView(this).apply {
                setContent {
                    LockScreenContent(
                        packageName = intent.getStringExtra(Constants.EXTRA_PACKAGE_NAME) ?: "Desconocida",
                        onUnlock = { finish() }
                    )
                }
            }
        )
    }

    override fun onBackPressed() {
        // Bloquear botón de retroceso
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        // Relanzar si intentan salir
        val intent = intent.apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        startActivity(intent)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }

    private fun makeActivityFullscreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
        }
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        window.setFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED,
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
        )
        window.setFlags(
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )
        hideSystemUI()
    }

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        )
        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                hideSystemUI()
            }
        }
    }
}

@Composable
fun LockScreenContent(
    packageName: String,
    viewModel: LockViewModel = hiltViewModel(),
    onUnlock: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showPinField by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.unlocked) {
        if (uiState.unlocked) onUnlock()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LockBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = LockAccent
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Aplicación Bloqueada",
                style = MaterialTheme.typography.headlineMedium,
                color = LockText,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = LockCard)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = packageName,
                        style = MaterialTheme.typography.titleMedium,
                        color = LockText
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Timer,
                            contentDescription = null,
                            tint = if (uiState.hasExtraTime) LockSuccess else LockError,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (uiState.hasExtraTime)
                                "Tiempo extra: ${uiState.extraTimeRemaining} min"
                            else
                                "Tiempo agotado",
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (uiState.hasExtraTime) LockSuccess else LockError,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Minutos predefinidos para solicitar
            Text(
                text = "Solicitar más tiempo",
                style = MaterialTheme.typography.titleLarge,
                color = LockText
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf(15, 30, 60).forEach { minutes ->
                    FilledTonalButton(
                        onClick = {
                            viewModel.requestExtraTime(minutes, packageName)
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = LockAccent.copy(alpha = 0.2f),
                            contentColor = LockText
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("+${minutes}min", fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf(120, 240).forEach { minutes ->
                    FilledTonalButton(
                        onClick = {
                            viewModel.requestExtraTime(minutes, packageName)
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = LockAccent.copy(alpha = 0.2f),
                            contentColor = LockText
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("+${minutes}min", fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.requestSent) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = LockSuccess.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Solicitud enviada. Esperando aprobación...",
                        modifier = Modifier.padding(16.dp),
                        color = LockSuccess,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Boton para desbloquear con PIN
            if (!showPinField) {
                Button(
                    onClick = { showPinField = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LockAccent,
                        contentColor = LockText
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Desbloquear con PIN maestro", modifier = Modifier.padding(8.dp))
                }
            } else {
                OutlinedTextField(
                    value = pinInput,
                    onValueChange = {
                        if (it.length <= 6) {
                            pinInput = it
                            pinError = false
                        }
                    },
                    label = { Text("PIN maestro") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            viewModel.verifyPin(pinInput) { valid ->
                                if (valid) onUnlock() else pinError = true
                            }
                        }
                    ),
                    isError = pinError,
                    supportingText = if (pinError) {
                        { Text("PIN incorrecto", color = LockError) }
                    } else null,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = LockText,
                        unfocusedTextColor = LockText,
                        cursorColor = LockAccent,
                        focusedBorderColor = LockAccent,
                        unfocusedBorderColor = LockText.copy(alpha = 0.5f),
                        focusedLabelColor = LockAccent,
                        unfocusedLabelColor = LockText.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        viewModel.verifyPin(pinInput) { valid ->
                            if (valid) onUnlock() else pinError = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LockAccent,
                        contentColor = LockText
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Desbloquear", modifier = Modifier.padding(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Comunicate con tu padre/madre para obtener más tiempo",
                style = MaterialTheme.typography.bodyMedium,
                color = LockText.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
        }
    }
}
