package com.pms

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.sin
import com.pms.data.MusicAlbum
import com.pms.data.MusicTrack
import com.pms.data.NavidromeClient
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
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
    var previewMode by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    if (showLibrary) {
        PmsLibraryScreen(
            serverAddress = serverAddress,
            username = username,
            password = password,
            previewMode = previewMode,
            onDisconnect = { showLibrary = false; previewMode = false }
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
                    onClick = { previewMode = false; showLibrary = true },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PmsPanel, contentColor = PmsAccent)
                ) { Text("OPEN MUSIC LIBRARY", fontSize = 11.sp, letterSpacing = 1.4.sp) }
            }
            Spacer(Modifier.height(14.dp))
            Button(
                onClick = { previewMode = true; showLibrary = true },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PmsPanel,
                    contentColor = PmsText
                )
            ) {
                Text("ADMIN  /  DESIGN PREVIEW", fontSize = 10.sp, letterSpacing = 1.4.sp)
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
    serverAddress: String, username: String, password: String, previewMode: Boolean, onDisconnect: () -> Unit
) {
    val demoAlbums = remember {
        listOf(
            MusicAlbum("demo-1", "Ashes & Iron", "PMS Demo", 2024, 9),
            MusicAlbum("demo-2", "Black Horizon", "The Hollow Signal", 2022, 11),
            MusicAlbum("demo-3", "Static Ritual", "Northern Collapse", 2025, 8),
            MusicAlbum("demo-4", "No Light Remains", "PMS Demo", 2021, 10)
        )
    }
    var albums by remember { mutableStateOf(if (previewMode) demoAlbums else emptyList()) }
    var selectedAlbum by remember { mutableStateOf<MusicAlbum?>(null) }
    var tracks by remember { mutableStateOf<List<MusicTrack>>(emptyList()) }
    var isLoading by remember { mutableStateOf(!previewMode) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var currentTrack by remember { mutableStateOf<MusicTrack?>(null) }
    var currentTrackIndex by remember { mutableStateOf(-1) }
    var isPlaying by remember { mutableStateOf(false) }
    var mainTab by remember { mutableStateOf("RADIO") }
    var librarySection by remember { mutableStateOf("MENU") }
    var searchQuery by remember { mutableStateOf("") }
    var showPlayerDetails by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val player = remember { ExoPlayer.Builder(context).build() }

    BackHandler {
        when {
            showPlayerDetails -> showPlayerDetails = false
            selectedAlbum != null -> selectedAlbum = null
            mainTab != "RADIO" -> mainTab = "RADIO"
            else -> onDisconnect()
        }
    }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) { isPlaying = playing }
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                errorMessage = error.message ?: "Не удалось воспроизвести трек"
                isPlaying = false
            }
        }
        player.addListener(listener)
        onDispose { player.removeListener(listener); player.release() }
    }

    fun startTrack(index: Int) {
        if (previewMode) return
        val track = tracks.getOrNull(index) ?: return
        try {
            val url = NavidromeClient.getStreamUrl(serverAddress, username, password, track.id)
            currentTrackIndex = index
            currentTrack = track
            player.setMediaItem(MediaItem.fromUri(url))
            player.prepare()
            player.play()
            errorMessage = null
        } catch (error: Exception) {
            errorMessage = error.message ?: "Не удалось запустить воспроизведение"
        }
    }

    fun loadAlbums() {
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                albums = withContext(Dispatchers.IO) { NavidromeClient.getAlbums(serverAddress, username, password) }
            } catch (error: Exception) {
                errorMessage = error.message ?: "Не удалось загрузить библиотеку"
            } finally { isLoading = false }
        }
    }

    fun openAlbum(album: MusicAlbum) {
        selectedAlbum = album
        errorMessage = null
        if (previewMode) {
            tracks = listOf(
                MusicTrack("demo-" + album.id + "-1", "Opening Signal", album.artist, 238),
                MusicTrack("demo-" + album.id + "-2", "Concrete Sky", album.artist, 214),
                MusicTrack("demo-" + album.id + "-3", "Burn the Silence", album.artist, 267),
                MusicTrack("demo-" + album.id + "-4", "After the Fallout", album.artist, 301)
            ).take(if (album.songCount < 5) album.songCount else 4)
            isLoading = false
        } else {
            scope.launch {
                isLoading = true
                try {
                    tracks = withContext(Dispatchers.IO) { NavidromeClient.getAlbum(serverAddress, username, password, album.id) }
                } catch (error: Exception) {
                    errorMessage = error.message ?: "Не удалось открыть альбом"
                } finally { isLoading = false }
            }
        }
    }

    LaunchedEffect(serverAddress, username, previewMode) {
        if (previewMode) { albums = demoAlbums; isLoading = false } else loadAlbums()
    }

    val filteredAlbums = albums.filter { it.name.contains(searchQuery, true) || it.artist.contains(searchQuery, true) }
    val artists = albums.map { it.artist }.distinct().sorted()

    Surface(Modifier.fillMaxSize(), color = PmsBlack) {
        Column(
            Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Column {
                    Text("PMS", color = PmsText, fontSize = 29.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                    Text("PERSONAL METAL STREAMING", color = PmsMuted, fontSize = 8.sp, letterSpacing = 1.8.sp)
                }
                Text("SETTINGS", color = PmsMuted, fontSize = 9.sp, letterSpacing = 1.sp,
                    modifier = Modifier.clickable(onClick = onDisconnect).padding(8.dp))
            }
            Spacer(Modifier.height(22.dp))

            when {
                showPlayerDetails -> {
                    ScreenHeading("NOW PLAYING", "RADIO PMS / SHUFFLE")
                    Spacer(Modifier.height(25.dp))
                    Text(currentTrack?.title ?: "RADIO PMS", color = PmsText, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text(currentTrack?.artist ?: "READY FOR TRANSMISSION", color = PmsMuted, fontSize = 11.sp)
                    Spacer(Modifier.height(25.dp))
                    SpectrumBars(isPlaying, Modifier.fillMaxWidth().height(105.dp))
                    Spacer(Modifier.height(24.dp))
                    PlayerControls(isPlaying, { startTrack(currentTrackIndex - 1) }, { if (player.isPlaying) player.pause() else player.play() }, { startTrack(currentTrackIndex + 1) })
                }
                selectedAlbum != null -> {
                    Text("‹  BACK TO " + librarySection, color = PmsMuted, fontSize = 10.sp,
                        modifier = Modifier.clickable { selectedAlbum = null }.padding(vertical = 8.dp))
                    Spacer(Modifier.height(12.dp))
                    Text(selectedAlbum!!.name, color = PmsText, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                    Text(selectedAlbum!!.artist, color = PmsMuted, fontSize = 12.sp)
                    Spacer(Modifier.height(18.dp))
                    when {
                        isLoading -> Text("LOADING TRACKS...", color = PmsMuted, fontSize = 11.sp)
                        errorMessage != null -> Text(errorMessage!!, color = PmsError, fontSize = 12.sp)
                        tracks.isEmpty() -> Text("NO TRACKS IN THIS ALBUM", color = PmsMuted, fontSize = 11.sp)
                        else -> LazyColumn {
                            items(tracks, key = { it.id }) { track ->
                                Row(Modifier.fillMaxWidth().clickable { startTrack(tracks.indexOf(track)) }.padding(vertical = 13.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text(track.title, color = if (currentTrack?.id == track.id) PmsText else PmsMuted, fontSize = 14.sp)
                                        Text(track.artist, color = PmsMuted, fontSize = 10.sp)
                                    }
                                    Text(formatDuration(track.durationSeconds), color = PmsMuted, fontSize = 10.sp)
                                }
                                PmsDivider()
                            }
                        }
                    }
                }
                else -> when (mainTab) {
                    "RADIO" -> {
                        ScreenHeading("RADIO PMS", "24/7 SHUFFLE STREAM")
                        Spacer(Modifier.height(28.dp))
                        SpectrumBars(isPlaying, Modifier.fillMaxWidth().height(105.dp))
                        Spacer(Modifier.height(26.dp))
                        Column(Modifier.fillMaxWidth().border(1.dp, Color(0xFF34383A), RoundedCornerShape(5.dp)).padding(17.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("NOW PLAYING", color = PmsMuted, fontSize = 9.sp, letterSpacing = 1.5.sp)
                                Text("SHUFFLE", color = PmsMuted, fontSize = 9.sp, letterSpacing = 1.sp)
                            }
                            Spacer(Modifier.height(17.dp))
                            Text(currentTrack?.title ?: "PMS RADIO", color = PmsText, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Text(currentTrack?.artist ?: "READY FOR TRANSMISSION", color = PmsMuted, fontSize = 10.sp, letterSpacing = 1.sp)
                            Spacer(Modifier.height(20.dp))
                            PlayerControls(isPlaying, { startTrack(currentTrackIndex - 1) }, { if (player.isPlaying) player.pause() else player.play() }, { startTrack(currentTrackIndex + 1) })
                            if (previewMode) {
                                Spacer(Modifier.height(10.dp))
                                Text("DESIGN PREVIEW · PLAYBACK DISABLED", color = PmsMuted, fontSize = 8.sp, letterSpacing = 1.sp)
                            }
                        }
                    }
                    "LIBRARY" -> {
                        if (librarySection == "MENU") {
                            ScreenHeading("LIBRARY", "YOUR MUSIC COLLECTION")
                            Spacer(Modifier.height(18.dp))
                            listOf(
                                Triple("ALBUMS", albums.size.toString(), "DISC"),
                                Triple("ARTISTS", artists.size.toString(), "USER"),
                                Triple("GENRES", "—", "WAVE"),
                                Triple("TRACKS", "—", "NOTE")
                            ).forEach { section ->
                                Row(Modifier.fillMaxWidth().clickable { librarySection = section.first }.padding(vertical = 17.dp),
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                    Text(section.third, color = PmsMuted, fontSize = 9.sp, modifier = Modifier.width(54.dp))
                                    Text(section.first, color = PmsText, fontSize = 14.sp, modifier = Modifier.weight(1f))
                                    Text(section.second + "  ›", color = PmsMuted, fontSize = 10.sp)
                                }
                                PmsDivider()
                            }
                        } else {
                            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                Text("‹ MENU", color = PmsMuted, fontSize = 9.sp,
                                    modifier = Modifier.clickable { librarySection = "MENU" }.padding(end = 14.dp, vertical = 8.dp))
                                Text(librarySection, color = PmsText, fontSize = 17.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            }
                            Spacer(Modifier.height(14.dp))
                            when (librarySection) {
                                "ALBUMS" -> when {
                                    isLoading -> Text("LOADING LIBRARY...", color = PmsMuted, fontSize = 11.sp)
                                    errorMessage != null -> Column {
                                        Text(errorMessage!!, color = PmsError, fontSize = 12.sp)
                                        Text("RETRY", color = PmsText, fontSize = 10.sp, modifier = Modifier.clickable { loadAlbums() }.padding(8.dp))
                                    }
                                    albums.isEmpty() -> Text("NO ALBUMS FOUND", color = PmsMuted, fontSize = 11.sp)
                                    else -> LazyColumn {
                                        items(albums, key = { it.id }) { album ->
                                            Row(Modifier.fillMaxWidth().clickable { openAlbum(album) }.padding(vertical = 14.dp),
                                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                                Column(Modifier.weight(1f)) {
                                                    Text(album.name, color = PmsText, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                                    Text(album.artist + "  ·  " + album.songCount + " TRACKS", color = PmsMuted, fontSize = 10.sp)
                                                }
                                                Text(album.year?.toString() ?: "›", color = PmsMuted, fontSize = 10.sp)
                                            }
                                            PmsDivider()
                                        }
                                    }
                                }
                                "ARTISTS" -> LazyColumn {
                                    items(artists) { artist ->
                                        Column(Modifier.fillMaxWidth().clickable { searchQuery = artist; mainTab = "SEARCH" }.padding(vertical = 15.dp)) {
                                            Text(artist, color = PmsText, fontSize = 14.sp)
                                            Text(albums.count { it.artist == artist }.toString() + " ALBUMS", color = PmsMuted, fontSize = 9.sp)
                                        }
                                        PmsDivider()
                                    }
                                }
                                else -> Text("THIS SECTION WILL BE CONNECTED IN A FUTURE UPDATE", color = PmsMuted, fontSize = 10.sp, lineHeight = 16.sp)
                            }
                        }
                    }
                    else -> {
                        ScreenHeading("SEARCH", "FIND MUSIC")
                        Spacer(Modifier.height(18.dp))
                        OutlinedTextField(value = searchQuery, onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth(), singleLine = true,
                            placeholder = { Text("Artist, album...", color = PmsMuted, fontSize = 13.sp) },
                            shape = RoundedCornerShape(5.dp))
                        Spacer(Modifier.height(14.dp))
                        if (searchQuery.isBlank()) {
                            Text("SEARCH THE PMS LIBRARY", color = PmsMuted, fontSize = 9.sp, letterSpacing = 1.2.sp)
                        } else {
                            Text("RESULTS  ·  " + filteredAlbums.size, color = PmsMuted, fontSize = 9.sp, letterSpacing = 1.2.sp)
                            LazyColumn {
                                items(filteredAlbums, key = { it.id }) { album ->
                                    Column(Modifier.fillMaxWidth().clickable {
                                        mainTab = "LIBRARY"; librarySection = "ALBUMS"; openAlbum(album)
                                    }.padding(vertical = 14.dp)) {
                                        Text(album.name, color = PmsText, fontSize = 14.sp)
                                        Text(album.artist, color = PmsMuted, fontSize = 10.sp)
                                    }
                                    PmsDivider()
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f, fill = false))
            if (currentTrack != null && !previewMode && !showPlayerDetails) {
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth().border(1.dp, Color(0xFF34383A), RoundedCornerShape(5.dp))
                    .clickable { showPlayerDetails = true }.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Text("▥", color = PmsText, fontSize = 19.sp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(currentTrack!!.title, color = PmsText, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                        Text(currentTrack!!.artist, color = PmsMuted, fontSize = 9.sp, maxLines = 1)
                    }
                    Text(if (isPlaying) "Ⅱ" else "▶", color = PmsText, fontSize = 19.sp,
                        modifier = Modifier.clickable { if (player.isPlaying) player.pause() else player.play() }.padding(horizontal = 8.dp))
                }
            }
            Spacer(Modifier.height(12.dp))
            PmsDivider()
            Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.SpaceAround) {
                listOf("RADIO", "LIBRARY", "SEARCH").forEach { tab ->
                    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            mainTab = tab; selectedAlbum = null; showPlayerDetails = false
                            if (tab == "LIBRARY") librarySection = "MENU"
                        }.padding(horizontal = 13.dp, vertical = 4.dp)) {
                        Text(when (tab) { "RADIO" -> "◉"; "LIBRARY" -> "▱"; else -> "⌕" },
                            color = if (mainTab == tab) PmsText else PmsMuted, fontSize = 20.sp)
                        Text(tab, color = if (mainTab == tab) PmsText else PmsMuted, fontSize = 8.sp, letterSpacing = 1.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ScreenHeading(title: String, subtitle: String) {
    Column {
        Text(title, color = PmsText, fontSize = 20.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        Spacer(Modifier.height(4.dp))
        Text(subtitle, color = PmsMuted, fontSize = 9.sp, letterSpacing = 1.5.sp)
    }
}

@Composable
private fun PmsDivider() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF2C3032)))
}

@Composable
private fun PlayerControls(isPlaying: Boolean, onPrevious: () -> Unit, onToggle: () -> Unit, onNext: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        Text("PREV", color = PmsMuted, fontSize = 9.sp, modifier = Modifier.clickable(onClick = onPrevious).padding(12.dp))
        Text(if (isPlaying) "Ⅱ" else "▶", color = PmsText, fontSize = 27.sp,
            modifier = Modifier.clickable(onClick = onToggle).padding(horizontal = 24.dp, vertical = 4.dp))
        Text("NEXT", color = PmsMuted, fontSize = 9.sp, modifier = Modifier.clickable(onClick = onNext).padding(12.dp))
    }
}

@Composable
private fun SpectrumBars(isPlaying: Boolean, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "pms-spectrum")
    val phase by transition.animateFloat(
        initialValue = 0f, targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "spectrum-phase"
    )
    Row(modifier, horizontalArrangement = Arrangement.Center, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        repeat(30) { index ->
            val wave = abs(sin(phase + index * 0.58f))
            val envelope = 0.25f + abs(sin(index * 0.31f)) * 0.75f
            val barHeight = if (isPlaying) 9f + wave * envelope * 76f else 8f + envelope * 12f
            Box(Modifier.padding(horizontal = 2.dp).width(4.dp).height(barHeight.dp)
                .background(Color(0xFF9EA4A7), RoundedCornerShape(1.dp)))
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
