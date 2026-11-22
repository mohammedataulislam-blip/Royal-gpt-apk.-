package com.example.ui.navigation

object RoyalRoutes {
    const val SPLASH = "splash"
    const val WELCOME = "welcome"
    const val AUTH = "auth/{mode}"
    const val HOME = "home"
    const val CHAT = "chat"
    const val TOOLS = "tools"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"

    fun authRoute(mode: String): String = "auth/$mode"
}
