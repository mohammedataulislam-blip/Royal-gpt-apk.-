package com.example.ui.navigation

import android.app.Application
import android.net.Uri
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.R
import com.example.data.firebase.AuthManager
import com.example.data.model.ConversationCategory
import com.example.data.repository.ConversationRepository
import com.example.data.repository.SettingsRepository
import com.example.data.repository.UserRepository
import com.example.ui.components.RoyalAtmosphereBackground
import com.example.ui.components.RoyalCrestLogo
import com.example.ui.screens.auth.AuthMode
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.chat.AiChatScreen
import com.example.ui.screens.chat.ChatViewModel
import com.example.ui.screens.home.HomeDashboardScreen
import com.example.ui.screens.home.HomeViewModel
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.profile.ProfileViewModel
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.settings.SettingsViewModel
import com.example.ui.screens.splash.SplashScreen
import com.example.ui.screens.tools.AiToolsScreen
import com.example.ui.screens.welcome.WelcomeScreen
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

private data class RoyalNavDestination(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

private val authenticatedDestinations = listOf(
    RoyalNavDestination(
        route = RoyalRoutes.HOME,
        label = "Home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        testTag = "nav_home"
    ),
    RoyalNavDestination(
        route = RoyalRoutes.CHAT,
        label = "Chat",
        selectedIcon = Icons.Filled.ChatBubble,
        unselectedIcon = Icons.Outlined.ChatBubbleOutline,
        testTag = "nav_chat"
    ),
    RoyalNavDestination(
        route = RoyalRoutes.TOOLS,
        label = "Tools",
        selectedIcon = Icons.Filled.AutoAwesome,
        unselectedIcon = Icons.Outlined.AutoAwesome,
        testTag = "nav_tools"
    ),
    RoyalNavDestination(
        route = RoyalRoutes.PROFILE,
        label = "Profile",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person,
        testTag = "nav_profile"
    ),
    RoyalNavDestination(
        route = RoyalRoutes.SETTINGS,
        label = "Settings",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings,
        testTag = "nav_settings"
    )
)

@Composable
fun RoyalAppRoot(
    currentUser: FirebaseUser?,
    settingsRepository: SettingsRepository
) {
    var splashCompleted by rememberSaveable { mutableStateOf(false) }

    if (!splashCompleted) {
        SplashScreen(
            onFinished = {
                splashCompleted = true
            }
        )
        return
    }

    if (currentUser == null) {
        UnauthenticatedNavGraph()
    } else {
        key(currentUser.uid) {
            AuthenticatedShell(
                currentUser = currentUser,
                settingsRepository = settingsRepository
            )
        }
    }
}

@Composable
private fun UnauthenticatedNavGraph() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = RoyalRoutes.WELCOME
    ) {
        composable(RoyalRoutes.WELCOME) {
            WelcomeScreen(
                onLoginClick = {
                    navController.navigate(RoyalRoutes.authRoute(AuthMode.LOGIN.name))
                },
                onCreateAccountClick = {
                    navController.navigate(RoyalRoutes.authRoute(AuthMode.CREATE_ACCOUNT.name))
                },
                onAuthenticated = {
                    // AuthStateListener in MainActivity transitions automatically to AuthenticatedShell
                }
            )
        }

        composable(
            route = RoyalRoutes.AUTH,
            arguments = listOf(navArgument("mode") { type = NavType.StringType })
        ) { backStackEntry ->
            val modeRaw = backStackEntry.arguments?.getString("mode")
            val initialMode = if (modeRaw == AuthMode.CREATE_ACCOUNT.name) {
                AuthMode.CREATE_ACCOUNT
            } else {
                AuthMode.LOGIN
            }
            AuthScreen(
                initialMode = initialMode,
                onBack = { navController.popBackStack() },
                onAuthenticated = {
                    // AuthStateListener transitions automatically to AuthenticatedShell
                }
            )
        }
    }
}

@Composable
private fun AuthenticatedShell(
    currentUser: FirebaseUser,
    settingsRepository: SettingsRepository
) {
    val uid = currentUser.uid
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val homeViewModel: HomeViewModel = viewModel(
        key = "home_$uid",
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) as Application
                val db = FirebaseFirestore.getInstance(app.getString(R.string.firestore_database_id))
                HomeViewModel(
                    userRepository = UserRepository(db),
                    conversationRepository = ConversationRepository(db),
                    currentUserId = uid
                )
            }
        }
    )

    val profileViewModel: ProfileViewModel = viewModel(
        key = "profile_$uid",
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) as Application
                val db = FirebaseFirestore.getInstance(app.getString(R.string.firestore_database_id))
                ProfileViewModel(
                    userRepository = UserRepository(db),
                    currentUserId = uid
                )
            }
        }
    )

    val settingsViewModel: SettingsViewModel = viewModel(
        key = "settings_$uid",
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) as Application
                val db = FirebaseFirestore.getInstance(app.getString(R.string.firestore_database_id))
                SettingsViewModel(
                    settingsRepository = settingsRepository,
                    userRepository = UserRepository(db),
                    conversationRepository = ConversationRepository(db),
                    currentUserId = uid
                )
            }
        }
    )

    val navigateToTab: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    val openChatRoute: (String?, ConversationCategory, String) -> Unit =
        { convId, category, prompt ->
            val encodedConv = Uri.encode(convId.orEmpty())
            val encodedCat = Uri.encode(category.name)
            val encodedPrompt = Uri.encode(prompt)
            navController.navigate(
                "${RoyalRoutes.CHAT}?convId=$encodedConv&category=$encodedCat&prompt=$encodedPrompt"
            ) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = false
                }
                launchSingleTop = true
            }
        }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 600.dp

        RoyalAtmosphereBackground {
            Scaffold(
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets.safeDrawing,
                bottomBar = {
                    if (!isWideScreen) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                            windowInsets = WindowInsets.navigationBars
                        ) {
                            authenticatedDestinations.forEach { dest ->
                                val selected = currentRoute?.startsWith(dest.route) == true
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = {
                                        if (dest.route == RoyalRoutes.CHAT) {
                                            openChatRoute(null, ConversationCategory.CHAT, "")
                                        } else {
                                            navigateToTab(dest.route)
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                            contentDescription = dest.label
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = dest.label,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.testTag(dest.testTag)
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (isWideScreen) {
                        NavigationRail(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            modifier = Modifier.fillMaxHeight(),
                            header = {
                                RoyalCrestLogo(
                                    size = 38.dp,
                                    animated = false,
                                    modifier = Modifier.padding(vertical = 12.dp)
                                )
                            }
                        ) {
                            Spacer(modifier = Modifier.weight(1f))
                            authenticatedDestinations.forEach { dest ->
                                val selected = currentRoute?.startsWith(dest.route) == true
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = {
                                        if (dest.route == RoyalRoutes.CHAT) {
                                            openChatRoute(null, ConversationCategory.CHAT, "")
                                        } else {
                                            navigateToTab(dest.route)
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                            contentDescription = dest.label
                                        )
                                    },
                                    label = { Text(dest.label) },
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                                    ),
                                    modifier = Modifier.testTag(dest.testTag)
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = RoyalRoutes.HOME,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                    ) {
                        composable(RoyalRoutes.HOME) {
                            HomeDashboardScreen(
                                viewModel = homeViewModel,
                                fallbackUserName = currentUser.displayName.orEmpty(),
                                isWideScreen = isWideScreen,
                                onOpenChat = { convId, category ->
                                    openChatRoute(convId, category, "")
                                },
                                onOpenTools = { navigateToTab(RoyalRoutes.TOOLS) },
                                onOpenProfile = { navigateToTab(RoyalRoutes.PROFILE) },
                                onOpenSettings = { navigateToTab(RoyalRoutes.SETTINGS) }
                            )
                        }

                        composable(
                            route = "${RoyalRoutes.CHAT}?convId={convId}&category={category}&prompt={prompt}",
                            arguments = listOf(
                                navArgument("convId") {
                                    type = NavType.StringType
                                    defaultValue = ""
                                },
                                navArgument("category") {
                                    type = NavType.StringType
                                    defaultValue = ConversationCategory.CHAT.name
                                },
                                navArgument("prompt") {
                                    type = NavType.StringType
                                    defaultValue = ""
                                }
                            )
                        ) { entry ->
                            val convId = entry.arguments?.getString("convId")?.takeIf { it.isNotBlank() }
                            val cat = ConversationCategory.fromRaw(entry.arguments?.getString("category"))
                            val prompt = entry.arguments?.getString("prompt").orEmpty()

                            val chatViewModel: ChatViewModel = viewModel(
                                key = "chat_${uid}_${convId ?: "new"}_${cat.name}_${prompt.hashCode()}",
                                factory = viewModelFactory {
                                    initializer {
                                        val app = checkNotNull(this[APPLICATION_KEY]) as Application
                                        val db = FirebaseFirestore.getInstance(
                                            app.getString(R.string.firestore_database_id)
                                        )
                                        ChatViewModel(
                                            repository = ConversationRepository(db),
                                            currentUserId = uid,
                                            initialConversationId = convId,
                                            initialCategory = cat,
                                            initialPrompt = prompt
                                        )
                                    }
                                }
                            )
                            AiChatScreen(viewModel = chatViewModel)
                        }

                        composable(RoyalRoutes.TOOLS) {
                            AiToolsScreen(
                                isWideScreen = isWideScreen,
                                onLaunchToolInChat = { category, prompt ->
                                    openChatRoute(null, category, prompt)
                                }
                            )
                        }

                        composable(RoyalRoutes.PROFILE) {
                            ProfileScreen(
                                viewModel = profileViewModel,
                                fallbackName = currentUser.displayName.orEmpty(),
                                fallbackEmail = currentUser.email.orEmpty(),
                                fallbackPhotoUrl = currentUser.photoUrl?.toString(),
                                fallbackVerified = currentUser.isEmailVerified,
                                onOpenSettings = { navigateToTab(RoyalRoutes.SETTINGS) },
                                onLogout = {
                                    AuthManager.signOut(
                                        credentialManager = credentialManager,
                                        scope = scope
                                    )
                                }
                            )
                        }

                        composable(RoyalRoutes.SETTINGS) {
                            SettingsScreen(viewModel = settingsViewModel)
                        }
                    }
                }
            }
        }
    }
}
