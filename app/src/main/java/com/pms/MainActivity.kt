package com.pms

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.LaunchedEffect
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
import com.pms.data.MusicAlbum
import com.pms.data.MusicTrack
import com.pms.data.NavidromeClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val PmsBlack = Color(0xFF101113)
private val PmsPanel = Color(0xFF1B1D20)
private val PmsText = Color(0xFFE8E8E8)
private val PmsMuted = Color(0xFF85888D)
private val PmsAccent = Color(0xFFD34A43)
private val PmsSuccess = Color(0xFF9BC7A5)
private val PmsError = Color(0xFFE07B70)

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
    var showLibrary by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    if (showLibrary) {
        PmsLibraryScreen(
            serverAddress = serverAddress,
            username = username,
            password = password,
            onDisconnect = { showLibrary = false }
        )
        return
    }

    Surface(modifier = Modifier.fillMaxSize(), color = PmsBlack) {
        Column(
            modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 24.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Text("PMS", color = PmsText, fontSize = 30.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
            Spacer(Modifier.height(5.dp))
            Text("PERSONAL METAL STREAMING", color = PmsMuted, fontSize = 9.sp, letterSpacing = 1.7.sp)
            Spacer(Modifier.height(54.dp))
            Text("CONNECT SERVER", color = PmsAccent, fontSize = 11.sp, letterSpacing = 2.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(10.dp))
            Text("LOUDER. CLOSER. YOURS.", color = PmsText, fontSize = 25.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(9.dp))
            Text("Connect to your rock and metal library.", color = PmsMuted, fontSize = 13.sp)
            Spacer(Modifier.height(30.dp))
            FieldLabel("SERVER IP")
            OutlinedTextField(
                value = serverAddress,
                onValueChange = { serverAddress = it; statusMessage = null; connectionSucceeded = false },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                placeholder = { Text("192.168.1.10") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            Spacer(Modifier.height(17.dp))
            FieldLabel("USERNAME")
            OutlinedTextField(
                value = username,
                onValueChange = { username = it; statusMessage = null; connectionSucceeded = false },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                placeholder = { Text("Navidrome username") }
            )
            Spacer(Modifier.height(17.dp))
            FieldLabel("PASSWORD")
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; statusMessage = null; connectionSucceeded = false },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
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
                                NavidromeClient.testConnection(serverAddress, username, password)
                            }
                            connectionSucceeded = true
                            statusMessage = "Connected · ${result.serverName} · API ${result.version}"
                        } catch (error: Exception) {
                            statusMessage = error.message ?: "Connection failed. Check the server details."
                        } finally {
                            isTesting = false
                        }
                    }
                },
                enabled = !isTesting && serverAddress.isNotBlank() && username.isNotBlank() && password.isNotEmpty(),
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PmsAccent, contentColor = PmsBlack,
                    disabledContainerColor = PmsPanel, disabledContentColor = PmsMuted
                )
            ) {
                Text(if (isTesting) "CONNECTING..." else "TEST CONNECTION", fontSize = 11.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold)
            }
            if (statusMessage != null) {
                Spacer(Modifier.height(16.dp))
                Text(statusMessage!!, color = if (connectionSucceeded) PmsSuccess else PmsError, fontSize = 12.sp)
            }
            if (connectionSucceeded) {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { showLibrary = true },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PmsPanel, contentColor = PmsAccent)
                ) { Text("OPEN MUSIC LIBRARY", fontSize = 11.sp, letterSpacing = 1.4.sp) }
            }
            Spacer(Modifier.height(20.dp))
            Box(Modifier.fillMaxWidth().background(PmsPanel, RoundedCornerShape(6.dp)).padding(14.dp)) {
                Text(
                    "LOCAL NETWORK · HTTP CONNECTIONS ARE NOT ENCRYPTED. Use this only on a trusted network. HTTPS or a VPN is recommended for remote access.",
                    color = PmsMuted, fontSize = 10.sp, lineHeight = 15.sp
                )
            }
            Spacer(Modifier.weight(1f))
            Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF303236)))
            Spacer(Modifier.height(14.dp))
            Text("ROCK & METAL  •  NO SUBSCRIPTION", color = PmsMuted, fontSize = 9.sp, letterSpacing = 1.sp)
        }
    }
}

@Composable
private fun PmsLibraryScreen(
    serverAddress: String,
    username: String,
    password: String,
    onDisconnect: () -> Unit
) {
    var albums by remember { mutableStateOf<List<MusicAlbum>>(emptyList()) }
    var selectedAlbum by remember { mutableStateOf<MusicAlbum?>(null) }
    var tracks by remember { mutableStateOf<List<MusicTrack>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun loadAlbums() {
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                albums = withContext(Dispatchers.IO) {
                    NavidromeClient.getAlbums(serverAddress, username, password)
                }
            } catch (error: Exception) {
                errorMessage = error.message ?: "Не удалось загрузить библиотеку"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(serverAddress, username) { loadAlbums() }

    Surface(modifier = Modifier.fillMaxSize(), color = PmsBlack) {
        Column(
            modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 22.dp, vertical = 24.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("PMS", color = PmsText, fontSize = 27.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
                    Text("ROCK · METAL · ALTERNATIVE", color = PmsMuted, fontSize = 9.sp, letterSpacing = 1.6.sp)
                }
                Text(
                    "DISCONNECT", color = PmsMuted, fontSize = 9.sp, letterSpacing = 1.sp,
                    modifier = Modifier.clickable(onClick = onDisconnect).padding(8.dp)
                )
            }
            Spacer(Modifier.height(28.dp))
            if (selectedAlbum == null) {
                Text("ALBUMS", color = PmsAccent, fontSize = 11.sp, letterSpacing = 2.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(12.dp))
                when {
                    isLoading -> Text("Загружаем библиотеку…", color = PmsMuted, fontSize = 13.sp)
                    errorMessage != null -> {
                        Text(errorMessage!!, color = PmsError, fontSize = 13.sp)
                        Spacer(Modifier.height(12.dp))
                        Text("RETRY", color = PmsAccent, fontSize = 11.sp, modifier = Modifier.clickable { loadAlbums() }.padding(8.dp))
                    }
                    albums.isEmpty() -> Text("В библиотеке пока нет альбомов.", color = PmsMuted, fontSize = 13.sp)
                    else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                        items(albums, key = { it.id }) { album ->
                            Column(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    selectedAlbum = album
                                    isLoading = true
                                    errorMessage = null
                                    scope.launch {
                                        try {
                                            tracks = withContext(Dispatchers.IO) {
                                                NavidromeClient.getAlbum(serverAddress, username, password, album.id)
                                            }
                                        } catch (error: Exception) {
                                            errorMessage = error.message ?: "Не удалось открыть альбом"
                                        } finally {
                                            isLoading = false
                                        }
                                    }
                                }.padding(vertical = 15.dp)
                            ) {
                                Text(album.name, color = PmsText, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "${album.artist}  ·  ${album.songCount} треков" + (album.year?.let { "  ·  $it" } ?: ""),
                                    color = PmsMuted, fontSize = 11.sp
                                )
                            }
                            Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF292B2F)))
                        }
                    }
                }
            } else {
                Text(
                    "‹  ALL ALBUMS", color = PmsAccent, fontSize = 10.sp, letterSpacing = 1.2.sp,
                    modifier = Modifier.clickable { selectedAlbum = null; errorMessage = null }.padding(vertical = 8.dp)
                )
                Spacer(Modifier.height(14.dp))
                Text(selectedAlbum!!.name, color = PmsText, fontSize = 22.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(5.dp))
                Text(selectedAlbum!!.artist, color = PmsMuted, fontSize = 12.sp)
                Spacer(Modifier.height(22.dp))
                when {
                    isLoading -> Text("Загружаем треки…", color = PmsMuted, fontSize = 13.sp)
                    errorMessage != null -> Text(errorMessage!!, color = PmsError, fontSize = 13.sp)
                    tracks.isEmpty() -> Text("В этом альбоме нет треков.", color = PmsMuted, fontSize = 13.sp)
                    else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                        items(tracks, key = { it.id }) { track ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(track.title, color = PmsText, fontSize = 14.sp)
                                    Text(track.artist, color = PmsMuted, fontSize = 11.sp)
                                }
                                Text(formatDuration(track.durationSeconds), color = PmsMuted, fontSize = 11.sp)
                            }
                            Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF292B2F)))
                        }
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            Text("PERSONAL METAL STREAMING", color = PmsMuted, fontSize = 9.sp, letterSpacing = 1.2.sp)
        }
    }
}

@Composable
private fun FieldLabel(value: String) {
    Text(value, color = PmsMuted, fontSize = 10.sp, letterSpacing = 1.4.sp)
    Spacer(Modifier.height(7.dp))
}

private fun formatDuration(seconds: Int): String {
    if (seconds <= 0) return "--:--"
    return "${seconds / 60}:${(seconds % 60).toString().padStart(2, '0')}"
}
