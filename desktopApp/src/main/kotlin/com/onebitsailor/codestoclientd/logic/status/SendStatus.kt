package com.onebitsailor.codestoclientd.logic.status

sealed class SendStatus {
    data object Idle : SendStatus()
    data object Sending : SendStatus()
    data object Success : SendStatus()
    data class Error(val message: String) : SendStatus()
}

sealed class ConnectionStatus {
    data object Unknown : ConnectionStatus()
    data object Checking : ConnectionStatus()
    data object Connected : ConnectionStatus()
    data class Disconnected(val message: String) : ConnectionStatus()
}