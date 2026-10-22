package com.locket.droid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.locket.droid.ui.CameraScreen
import com.locket.droid.ui.ChatScreen
import com.locket.droid.ui.FeedScreen
import com.locket.droid.ui.FriendsSheet
import com.locket.droid.ui.LocketColors
import com.locket.droid.ui.LocketTheme
import com.locket.droid.ui.OnboardingScreen
import com.locket.droid.camera.CameraViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

enum class LocketScreen {
    Camera,
    Feed,
    Chat
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LocketTheme {
                LocketMainFlow()
            }
        }
    }
}

/**
 * Main 3-tab navigation flow + FriendsSheet modal bottom sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocketMainFlow() {
    var currentScreen by remember { mutableStateOf(LocketScreen.Camera) }
    var showFriendsSheet by remember { mutableStateOf(false) }
    var showOnboarding by remember { mutableStateOf(false) }

    // System back button handling
    BackHandler(enabled = showOnboarding || showFriendsSheet || currentScreen != LocketScreen.Camera) {
        when {
            showOnboarding -> showOnboarding = false
            showFriendsSheet -> showFriendsSheet = false
            currentScreen != LocketScreen.Camera -> currentScreen = LocketScreen.Camera
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LocketColors.Background)
    ) {
        // 3-tab animated screen transitions
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                when {
                    // Camera <-> Feed: vertical slide
                    initialState == LocketScreen.Camera && targetState == LocketScreen.Feed -> {
                        (slideInVertically { it } + fadeIn()).togetherWith(
                            slideOutVertically { -it / 3 } + fadeOut()
                        )
                    }
                    initialState == LocketScreen.Feed && targetState == LocketScreen.Camera -> {
                        (slideInVertically { -it } + fadeIn()).togetherWith(
                            slideOutVertically { it / 3 } + fadeOut()
                        )
                    }
                    // Camera <-> Chat: horizontal slide
                    initialState == LocketScreen.Camera && targetState == LocketScreen.Chat -> {
                        (slideInHorizontally { it } + fadeIn()).togetherWith(
                            slideOutHorizontally { -it / 3 } + fadeOut()
                        )
                    }
                    initialState == LocketScreen.Chat && targetState == LocketScreen.Camera -> {
                        (slideInHorizontally { -it } + fadeIn()).togetherWith(
                            slideOutHorizontally { it / 3 } + fadeOut()
                        )
                    }
                    // Feed <-> Chat
                    else -> {
                        (fadeIn()).togetherWith(fadeOut())
                    }
                }
            },
            label = "screenTransition"
        ) { screen ->
            when (screen) {
                LocketScreen.Camera -> {
                    val cameraVm: CameraViewModel = viewModel()
                    CameraScreen(
                        viewModel = cameraVm,
                        onNavigateToFeed = { currentScreen = LocketScreen.Feed },
                        onNavigateToChat = { currentScreen = LocketScreen.Chat },
                        onOpenFriendsSheet = { showFriendsSheet = true },
                        onOpenOnboarding = { showOnboarding = true }
                    )
                }
                LocketScreen.Feed -> {
                    FeedScreen(
                        onNavigateToCamera = { currentScreen = LocketScreen.Camera },
                        onNavigateToChat = { currentScreen = LocketScreen.Chat },
                        onOpenFriendsSheet = { showFriendsSheet = true },
                        onAnnouncementsClick = { showOnboarding = true }
                    )
                }
                LocketScreen.Chat -> {
                    ChatScreen(
                        onBackClick = { currentScreen = LocketScreen.Camera },
                        onCameraClick = { currentScreen = LocketScreen.Camera }
                    )
                }
            }
        }

        // Modal Bottom Sheet: FriendsSheet
        if (showFriendsSheet) {
            FriendsSheet(
                onDismissRequest = { showFriendsSheet = false }
            )
        }

        // Onboarding overlay (shot_0)
        AnimatedVisibility(
            visible = showOnboarding,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            OnboardingScreen(
                onGetStarted = { showOnboarding = false },
                onSignInClick = { showOnboarding = false }
            )
        }
    }
}
