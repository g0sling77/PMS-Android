package com.pms

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val PmsBlack = Color(0xFF101113)
private val PmsPanel = Color(0xFF1B1D20)
private val PmsText = Color(0xFFE8E8E8)
private val PmsMuted = Color(0xFF85888D)
private val PmsAccent = Color(0xFFB7A0E8)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                PmsHome()
            }
        }
    }
}

@Composable
private fun PmsHome() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = PmsBlack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 30.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "PMS",
                        color = PmsText,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "PERSONAL MUSIC STREAMING",
                        color = PmsMuted,
                        fontSize = 9.sp,
                        letterSpacing = 1.5.sp
                    )
                }

                Spacer(Modifier.height(48.dp))
                Text(
                    "YOUR MUSIC.",
                    color = PmsMuted,
                    fontSize = 12.sp,
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Without limits.",
                    color = PmsText,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(28.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PmsPanel, RoundedCornerShape(8.dp))
                        .padding(18.dp)
                ) {
                    Text(
                        "WELCOME TO PMS",
                        color = PmsAccent,
                        fontSize = 10.sp,
                        letterSpacing = 2.sp
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Your personal music space is taking shape.",
                        color = PmsText,
                        fontSize = 15.sp
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Server connection is coming soon.",
                        color = PmsMuted,
                        fontSize = 12.sp
                    )
                }
            }

            Column {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFF303236))
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    "OPEN SOURCE  •  NO SUBSCRIPTION",
                    color = PmsMuted,
                    fontSize = 9.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
