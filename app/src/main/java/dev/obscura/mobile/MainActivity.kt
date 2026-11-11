package dev.obscura.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(Modifier.fillMaxSize()) {
                    ObscuraHome()
                }
            }
        }
    }
}

@Composable
private fun ObscuraHome() {
    var url by remember { mutableStateOf("https://example.com") }
    var script by remember { mutableStateOf("document.title") }
    var status by remember { mutableStateOf("Native engine not integrated") }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("OBSCURA", style = MaterialTheme.typography.headlineLarge)
        Text("Mobile · Android ARM64 · Developer Preview")
        HorizontalDivider()
        Text("Engine status", style = MaterialTheme.typography.titleMedium)
        Text(status, color = MaterialTheme.colorScheme.tertiary)
        OutlinedTextField(value = url, onValueChange = { url = it }, label = { Text("Target URL") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(value = script, onValueChange = { script = it }, label = { Text("JavaScript expression") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
        Button(onClick = { status = "Unavailable: Obscura Rust/V8 Android library has not been built or linked yet." }, modifier = Modifier.fillMaxWidth()) {
            Text("Run with Obscura")
        }
        HorizontalDivider()
        Text("Planned: native Rust/JNI bridge, CDP server, rendering, proxy, stealth.", style = MaterialTheme.typography.bodyMedium)
        Text("This preview does not execute JavaScript, fetch pages, or start a CDP server.")
    }
}
