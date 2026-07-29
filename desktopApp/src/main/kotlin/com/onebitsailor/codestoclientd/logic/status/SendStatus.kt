package com.onebitsailor.codestoclientd.logic.status

sealed class SendStatus {
    object Idle : SendStatus()
    object Sending : SendStatus()
    object Success : SendStatus()
    data class Error(val message: String) : SendStatus()
}