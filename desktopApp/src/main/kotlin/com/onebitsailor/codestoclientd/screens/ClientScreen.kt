package com.onebitsailor.codestoclientd.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onebitsailor.codestoclientd.logic.format.CodeFormatter
import com.onebitsailor.codestoclientd.logic.models.ApiResponse
import com.onebitsailor.codestoclientd.logic.models.CodeSubmission
import com.onebitsailor.codestoclientd.logic.models.client
import com.onebitsailor.codestoclientd.logic.status.ConnectionStatus
import com.onebitsailor.codestoclientd.logic.status.SendStatus
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ClientScreen() {
    var address by remember { mutableStateOf("192.168.1.42:8080") }
    var fileName by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }

    var fontSize by remember { mutableStateOf(14) }
    var isBold by remember { mutableStateOf(false) }

    var sendStatus by remember { mutableStateOf<SendStatus>(SendStatus.Idle) }
    var connectionStatus by remember { mutableStateOf<ConnectionStatus>(ConnectionStatus.Unknown) }

    val scope = rememberCoroutineScope()

    // Debounced auto-check: re-pings ~1s after the user stops editing the address.
    LaunchedEffect(address) {
        connectionStatus = ConnectionStatus.Unknown
        delay(800)
        connectionStatus = ConnectionStatus.Checking
        try {
            val response = client.get("http://$address/ping")
            connectionStatus = if (response.status == HttpStatusCode.OK) {
                ConnectionStatus.Connected
            } else {
                ConnectionStatus.Disconnected("Server responded: ${response.status}")
            }
        } catch (e: Exception) {
            connectionStatus = ConnectionStatus.Disconnected(e.message ?: "Unreachable")
        }
    }

    fun sendCode() {
        if (fileName.isBlank() || code.isBlank()) {
            sendStatus = SendStatus.Error("File name and code are required")
            return
        }
        scope.launch {
            sendStatus = SendStatus.Sending
            try {
                val response = client.post("http://$address/receivetext") {
                    headers { append(HttpHeaders.ContentType, "application/json") }
                    setBody(CodeSubmission(fileName.trim(), code))
                }
                sendStatus = if (response.status == HttpStatusCode.OK) {
                    val body = try { response.body<ApiResponse>() } catch (e: Exception) { null }
                    fileName = ""
                    code = ""
                    SendStatus.Success
                } else {
                    SendStatus.Error("Server responded: ${response.status}")
                }
            } catch (e: Exception) {
                sendStatus = SendStatus.Error(e.message ?: "Could not reach the phone")
            }
            delay(1800)
            if (sendStatus is SendStatus.Success || sendStatus is SendStatus.Error) {
                sendStatus = SendStatus.Idle
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("Send to Phone", style = MaterialTheme.typography.h4, fontWeight = FontWeight.Bold)
            ConnectionBadge(connectionStatus)
        }

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Phone address (ip:port)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
        )

        OutlinedTextField(
            value = fileName,
            onValueChange = { fileName = it },
            label = { Text("File name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Toolbar above the code field: font size, bold, format
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Snippet", style = MaterialTheme.typography.subtitle1, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))

            IconButton(onClick = { fontSize = (fontSize - 1).coerceAtLeast(10) }) {
                Icon(Icons.Default.Delete, contentDescription = "Decrease font size")
            }
            Text("${fontSize}sp", style = MaterialTheme.typography.caption)
            IconButton(onClick = { fontSize = (fontSize + 1).coerceAtMost(28) }) {
                Icon(Icons.Default.Add, contentDescription = "Increase font size")
            }

            IconToggleButton(checked = isBold, onCheckedChange = { isBold = it }) {
                Icon(
                    Icons.Default.Clear,
                    contentDescription = "Bold",
                    tint = if (isBold) MaterialTheme.colors.primary else LocalContentColor.current
                )
            }

            OutlinedButton(onClick = { code = CodeFormatter.format(code) }, enabled = code.isNotBlank()) {
                Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Format")
            }
        }

        OutlinedTextField(
            value = code,
            onValueChange = { code = it },
            modifier = Modifier.fillMaxWidth().weight(1f),
            textStyle = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = fontSize.sp,
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
            ),
            placeholder = { Text("Paste or type your code here") }
        )

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SendButton(
                status = sendStatus,
                enabled = connectionStatus is ConnectionStatus.Connected,
                onClick = { sendCode() }
            )

            AnimatedContent(targetState = sendStatus, label = "send_status_text") { s ->
                when (s) {
                    is SendStatus.Error -> Text("Error: ${s.message}", color = MaterialTheme.colors.error)
                    is SendStatus.Sending -> Text("Sending…")
                    is SendStatus.Success -> Text("Sent ✓", color = Color(0xFF4CAF50))
                    is SendStatus.Idle -> {}
                }
            }
        }

        Text(
            "Make sure this computer and the phone are on the same Wi-Fi network, " +
                    "and the server is running in the CodeSto app.",
            style = MaterialTheme.typography.caption,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun ConnectionBadge(status: ConnectionStatus) {
    val (color, label) = when (status) {
        is ConnectionStatus.Unknown -> MaterialTheme.colors.onSurface.copy(alpha = 0.3f) to "Idle"
        is ConnectionStatus.Checking -> Color(0xFFFFA000) to "Checking…"
        is ConnectionStatus.Connected -> Color(0xFF4CAF50) to "Connected"
        is ConnectionStatus.Disconnected -> MaterialTheme.colors.error to "Not reachable"
    }

    val pulse = rememberInfiniteTransition(label = "pulse")
    val alpha by pulse.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "pulse_alpha"
    )

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = if (status is ConnectionStatus.Checking) alpha else 1f))
        )
        Text(label, style = MaterialTheme.typography.body2, color = color)
    }
}

@Composable
private fun SendButton(status: SendStatus, enabled: Boolean, onClick: () -> Unit) {
    val scale by animateFloatAsState(
        targetValue = if (status is SendStatus.Sending) 0.95f else 1f,
        animationSpec = tween(150),
        label = "send_button_scale"
    )

    Button(
        onClick = onClick,
        enabled = enabled && status !is SendStatus.Sending,
        modifier = Modifier
            .scale(scale)
            .height(44.dp)
    ) {
        AnimatedContent(targetState = status, label = "send_button_content") { s ->
            when (s) {
                is SendStatus.Sending -> Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colors.onPrimary)
                    Spacer(Modifier.width(8.dp))
                    Text("Sending")
                }
                is SendStatus.Success -> Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Sent")
                }
                else -> Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Send to phone")
                }
            }
        }
    }
}