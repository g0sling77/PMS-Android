package com.pms

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pms.data.NavidromeClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val PmsBlack = Color(0xFF101113)
private val PmsPanel = Color(0xFF1B1D20)
private val PmsText = Color(0xFFE8E8E8)
private val PmsMuted = Color(0xFF85888D)
private val PmsAccent = Color(0xFFB7A0E8)
private val PmsSuccess = Color(0xFF9BC7A5)
private val PmsError = Color(0xFFE28B8B)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = PmsAccent,
                    background = PmsBlack,
                    surface = PmsPanel,
                    onSurface = PmsText
                )
            ) {
                PmsConnectScreen()
            }
        }
    }
}

@Composable
private fun PmsConnectScreen() {
    var serverAddress by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isTesting by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var connectionSucceeded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = PmsBlack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 24.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Text(
                text = "PMS",
                color = PmsText,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp
            )
            Spacer(Modifier.height(5.dp))
            Text(
                text = "PERSONAL MUSIC STREAMING",
                color = PmsMuted,
                fontSize = 9.sp,
                letterSpacing = 1.7.sp
            )

            Spacer(Modifier.height(54.dp))
            Text(
                text = "CONNECT SERVER",
                color = PmsAccent,
                fontSize = 11.sp,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Your music. Your server.",
                color = PmsText,
                fontSize = 25.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(9.dp))
            Text(
                text = "Enter the address of your PMS music server.",
                color = PmsMuted,
                fontSize = 13.sp
            )

            Spacer(Modifier.height(30.dp))
            Text("SERVER ADDRESS", color = PmsMuted, fontSize = 10.sp, letterSpacing = 1.4.sp)
            Spacer(Modifier.height(7.dp))
            OutlinedTextField(
                value = serverAddress,
                onValueChange = {
                    serverAddress = it
                    statusMessage = null
                    connectionSucceeded = false
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("http://192.168.1.10:4533") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
            )

            Spacer(Modifier.height(17.dp))
            Text("USERNAME", color = PmsMuted, fontSize = 10.sp, letterSpacing = 1.4.sp)
            Spacer(Modifier.height(7.dp))
            OutlinedTextField(
                value = username,
                onValueChange = {
                    username = it
                    statusMessage = null
                    connectionSucceeded = false
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Navidrome username") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            Spacer(Modifier.height(17.dp))
            Text("PASSWORD", color = PmsMuted, fontSize = 10.sp, letterSpacing = 1.4.sp)
            Spacer(Modifier.height(7.dp))
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    statusMessage = null
                    connectionSucceeded = false
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Navidrome password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Spacer(Modifier.height(22.dp))
            Button(
                onClick = {
                    isTesting = true
                    statusMessage = null
                    connectionSucceeded = false
                    scope.launch {
                        try {
                            val result = withContext(Dispatchers.IO) {
                                NavidromeClient.testConnection(
                                    serverAddress = serverAddress,
                                    username = username,
                                    password = password
                                )
                            }
                            connectionSucceeded = true
                            statusMessage = "Connected · ${result.serverName} · API ${result.version}"
                        } catch (error: Exception) {
                            connectionSucceeded = false
                            statusMessage = error.message ?: "Connection failed. Check the server details."
                        } finally {
                            isTesting = false
                        }
                    }
                },
                enabled = !isTesting &&
                    serverAddress.isNotBlank() &&
                    username.isNotBlank() &&
                    password.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PmsAccent,
                    contentColor = PmsBlack,
                    disabledContainerColor = PmsPanel,
                    disabledContentColor = PmsMuted
                )
            ) {
                Text(
                    text = if (isTesting) "CONNECTING..." else "TEST CONNECTION",
                    fontSize = 11.sp,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (statusMessage != null) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = statusMessage!!,
                    color = if (connectionSucceeded) PmsSuccess else PmsError,
                    fontSize = 12.sp
                )
            }

            Spacer(Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PmsPanel, RoundedCornerShape(6.dp))
                    .padding(14.dp)
            ) {
                Text(
                    text = "LOCAL NETWORK · HTTP CONNECTIONS ARE NOT ENCRYPTED. " +
                        "Use this only on a trusted network. HTTPS or a VPN is recommended for remote access.",
                    color = PmsMuted,
                    fontSize = 10.sp,
                    lineHeight = 15.sp
                )
            }

            Spacer(Modifier.weight(1f))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFF303236))
            )
            Spacer(Modifier.height(14.dp))
            Text(
                text = "OPEN SOURCE  •  NO SUBSCRIPTION",
                color = PmsMuted,
                fontSize = 9.sp,
                letterSpacing = 1.sp
            )
        }
    }
}
