package com.onebitsailor.codestoclientd.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.onebitsailor.codestoclientd.logic.models.MessageRequest
import com.onebitsailor.codestoclientd.logic.models.client
import com.onebitsailor.codestoclientd.logic.status.SendStatus
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.launch

@Composable
fun ClientScreen() {
    var address by remember { mutableStateOf("192.168.1.42:8080") }
    var message by remember { mutableStateOf("") }
    var status by remember { mutableStateOf<SendStatus>(SendStatus.Idle) }
    val scope = rememberCoroutineScope()

    fun sendMessage() {
        val textToSend = message
        if (textToSend.isBlank()) return
        scope.launch {
            status = SendStatus.Sending
            try {
                val response = client.post("http://$address/message") {
                    headers { append(HttpHeaders.ContentType, "application/json") }
                    setBody(MessageRequest(textToSend))
                }
                status = if (response.status == HttpStatusCode.OK) {
                    message = ""
                    SendStatus.Success
                } else {
                    SendStatus.Error("Server responded: ${response.status}")
                }
            } catch (e: Exception) {
                status = SendStatus.Error(e.message ?: "Could not reach the phone")
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Send to Phone", style = MaterialTheme.typography.h3)

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Phone address (ip:port)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Message") },
            modifier = Modifier.fillMaxWidth().weight(1f)
        )

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { sendMessage() }, enabled = status !is SendStatus.Sending) {
                Text("Submit")
            }

            when (val s = status) {
                is SendStatus.Sending -> Text("Sending…")
                is SendStatus.Success -> Text("Sent ✓")
                is SendStatus.Error -> Text("Error: ${s.message}")
                is SendStatus.Idle -> {}
            }
        }

        Text(
            "Make sure the phone and this computer are on the same Wi-Fi network, " +
                    "and that the Messenger Server app is open on the phone.",
            style = MaterialTheme.typography.h5
        )
    }
}
