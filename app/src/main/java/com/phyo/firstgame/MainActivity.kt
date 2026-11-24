package com.phyo.firstgame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

private val Night = Color(0xFF090711)
private val Panel = Color(0xFF191329)
private val Violet = Color(0xFFB88CFF)
private val Cyan = Color(0xFF72D9E8)
private val Red = Color(0xFFFF7B91)
private val White = Color(0xFFF7F2FF)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(
                primary = Violet, background = Night, surface = Panel,
                onPrimary = Night, onBackground = White, onSurface = White
            )) { GameApp() }
        }
    }
}

private enum class Screen { MENU, LOBBY, GAME }

@Composable
private fun GameApp() {
    var screen by remember { mutableStateOf(Screen.MENU) }
    var mode by remember { mutableStateOf("Practice") }
    var nickname by remember { mutableStateOf("Player") }
    var hostIp by remember { mutableStateOf("") }
    var roomCode by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Ready") }
    var team by remember { mutableStateOf("Blue") }

    Box(Modifier.fillMaxSize().background(Night).padding(18.dp)) {
        when (screen) {
            Screen.MENU -> Column(
                Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("PHYO", color = Violet, fontSize = 15.sp, fontWeight = FontWeight.Black, letterSpacing = 5.sp)
                Text("FIRST GAME", color = White, fontSize = 34.sp, fontWeight = FontWeight.Black)
                Text("2D CARTOON ARENA • LOCAL PLAY", color = Cyan, fontSize = 11.sp)
                Spacer(Modifier.height(26.dp))
                Card(Modifier.fillMaxWidth().widthIn(max = 520.dp), colors = CardDefaults.cardColors(containerColor = Panel)) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("PLAYER NAME", color = Violet, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        SimpleField(nickname, { nickname = it }, "Player")
                        Text("CHOOSE MODE", color = Violet, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Button(onClick = { mode = "Practice"; screen = Screen.GAME }, modifier = Modifier.fillMaxWidth()) {
                            Text("OFFLINE PRACTICE")
                        }
                        OutlinedButton(onClick = { mode = "Host"; status = "Create a hotspot, then host a room."; screen = Screen.LOBBY }, modifier = Modifier.fillMaxWidth()) {
                            Text("HOST LOCAL ROOM")
                        }
                        OutlinedButton(onClick = { mode = "Join"; status = "Connect to the host hotspot, then enter host IP."; screen = Screen.LOBBY }, modifier = Modifier.fillMaxWidth()) {
                            Text("JOIN LOCAL ROOM")
                        }
                        Text("Up to 8 players • Wi-Fi hotspot / same local Wi-Fi", color = Color.LightGray, fontSize = 11.sp)
                    }
                }
            }
            Screen.LOBBY -> Column(
                Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(Modifier.fillMaxWidth().widthIn(max = 560.dp), colors = CardDefaults.cardColors(containerColor = Panel)) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(if (mode == "Host") "CREATE ROOM" else "JOIN ROOM", color = Violet, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text(status, color = Cyan, fontSize = 12.sp)
                        if (mode == "Host") {
                            Text("Host setup", color = White, fontWeight = FontWeight.Bold)
                            Text("1. Turn on your phone's Mobile Hotspot.\n2. Connect other phones to that hotspot.\n3. Find the host phone's local IP in Wi-Fi details.\n4. Share that IP with players.", color = Color.LightGray, fontSize = 12.sp)
                            Text("Room port: 45888", color = Violet)
                            Text("Room code: PHYO-45888", color = Violet, fontWeight = FontWeight.Bold)
                            Text("This starter build shows the lobby flow; live network synchronization is not implemented yet.", color = Red, fontSize = 11.sp)
                        } else {
                            Text("Host IP address", color = White, fontSize = 12.sp)
                            SimpleField(hostIp, { hostIp = it }, "e.g. 192.168.43.1")
                            Text("Room code", color = White, fontSize = 12.sp)
                            SimpleField(roomCode, { roomCode = it }, "PHYO-45888")
                            Text("Join UI is ready; real-time Wi-Fi networking will be added in the next build.", color = Red, fontSize = 11.sp)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { screen = Screen.MENU }, modifier = Modifier.weight(1f)) { Text("BACK") }
                            Button(onClick = { screen = Screen.GAME }, modifier = Modifier.weight(1f)) { Text("OPEN DEMO") }
                        }
                    }
                }
            }
            Screen.GAME -> DemoGame(
                onExit = { screen = if (mode == "Practice") Screen.MENU else Screen.LOBBY },
                team = team, onTeam = { team = it }
            )
        }
    }
}

@Composable
private fun SimpleField(value: String, onValue: (String) -> Unit, hint: String) {
    Box(Modifier.fillMaxWidth().background(Color(0xFF0E0B17), RoundedCornerShape(10.dp)).padding(12.dp)) {
        if (value.isEmpty()) Text(hint, color = Color.Gray, fontSize = 13.sp)
        BasicTextField(value, onValue, singleLine = true,
            textStyle = TextStyle(color = White, fontSize = 14.sp),
            modifier = Modifier.fillMaxWidth())
    }
}

private data class Dot(val x: Float, val y: Float, val team: Int, val name: String)

@Composable
private fun DemoGame(onExit: () -> Unit, team: String, onTeam: (String) -> Unit) {
    var tick by remember { mutableIntStateOf(0) }
    var x by remember { mutableFloatStateOf(0.22f) }
    var y by remember { mutableFloatStateOf(0.68f) }
    var shots by remember { mutableIntStateOf(0) }
    var message by remember { mutableStateOf("Practice arena — tap FIRE to launch a harmless energy orb") }
    var teamBlue by remember { mutableStateOf(team == "Blue") }
    LaunchedEffect(Unit) { while (true) { delay(60); tick++ } }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("PHYO FIRST GAME", color = Violet, fontWeight = FontWeight.Black)
            Text("PRACTICE • ${shots} ORBS", color = Cyan, fontSize = 11.sp)
            TextButton(onClick = onExit) { Text("EXIT") }
        }
        Box(Modifier.weight(1f).fillMaxWidth().background(Color(0xFF17152B), RoundedCornerShape(16.dp))) {
            Canvas(Modifier.fillMaxSize()) {
                val w = size.width; val h = size.height
                // Decorative hills and platforms
                drawCircle(Color(0xFF29234A), radius = h * .28f, center = Offset(w * .83f, h * .17f))
                drawRect(Color(0xFF282044), topLeft = Offset(0f, h * .78f), size = androidx.compose.ui.geometry.Size(w, h * .22f))
                for (i in 0..5) {
                    val px = w * (i * .18f + .04f)
                    drawRoundRect(Color(0xFF514276), Offset(px, h * (.53f + (i % 2) * .12f)), androidx.compose.ui.geometry.Size(w * .12f, h * .045f), androidx.compose.ui.geometry.CornerRadius(12f))
                }
                // Decorative stars
                for (i in 0..18) {
                    val sx = (i * 79 % 997) / 997f * w
                    val sy = (i * 43 % 431) / 431f * h * .55f
                    drawCircle(Color(0xFF8D82B4), 2.2f, Offset(sx, sy))
                }
                val px = x * w; val py = y * h
                val playerColor = if (teamBlue) Cyan else Red
                drawCircle(playerColor, h * .055f, Offset(px, py))
                drawCircle(White, h * .018f, Offset(px + h * .018f, py - h * .012f))
                drawRoundRect(playerColor, Offset(px - h * .035f, py + h * .035f), androidx.compose.ui.geometry.Size(h * .07f, h * .08f), androidx.compose.ui.geometry.CornerRadius(9f))
                // Harmless glowing practice targets
                for (i in 0..2) {
                    val tx = w * (.48f + i * .17f)
                    val ty = h * (.66f - (i % 2) * .14f)
                    drawCircle(if (i % 2 == 0) Red else Violet, h * .045f, Offset(tx, ty), style = Stroke(4f))
                    drawCircle(Color(0xFF302545), h * .025f, Offset(tx, ty))
                }
                // Animated energy dots
                for (i in 0 until min(shots, 12)) {
                    val orbX = (px + h * .10f + ((tick * 8 + i * 39) % (w * .55f).toInt())).coerceAtMost(w)
                    drawCircle(Violet, h * .012f, Offset(orbX, py - h * .025f))
                }
            }
            Text("MOVE: use the controls below", Modifier.align(Alignment.TopStart).padding(12.dp), color = Color.LightGray, fontSize = 10.sp)
        }
        Spacer(Modifier.height(10.dp))
        Text(message, color = Color.LightGray, fontSize = 11.sp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(onClick = { x = max(.06f, x - .045f) }, modifier = Modifier.weight(1f)) { Text("◀") }
                    Button(onClick = { x = min(.92f, x + .045f) }, modifier = Modifier.weight(1f)) { Text("▶") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(onClick = { y = max(.25f, y - .07f) }, modifier = Modifier.weight(1f)) { Text("JUMP") }
                    OutlinedButton(onClick = { y = min(.76f, y + .04f) }, modifier = Modifier.weight(1f)) { Text("DOWN") }
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(onClick = { shots++; message = "Energy orb launched! Targets are practice props." },
                    colors = ButtonDefaults.buttonColors(containerColor = Violet),
                    modifier = Modifier.height(64.dp).width(112.dp)) { Text("FIRE", fontWeight = FontWeight.Black) }
                TextButton(onClick = { teamBlue = !teamBlue; onTeam(if (teamBlue) "Blue" else "Red") }) {
                    Text(if (teamBlue) "BLUE TEAM" else "RED TEAM", color = if (teamBlue) Cyan else Red, fontSize = 10.sp)
                }
            }
        }
    }
}
