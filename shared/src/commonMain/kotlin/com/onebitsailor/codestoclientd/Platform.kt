package com.onebitsailor.codestoclientd

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform