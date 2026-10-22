package com.locket.droid.ui

import androidx.camera.view.PreviewView
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.Dialpad
import androidx.compose.material.icons.rounded.FlashOff
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.FlipCameraIos
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.locket.droid.camera.FlashMode
import kotlinx.coroutines.flow.MutableStateFlow
import com.locket.droid.camera.CameraViewModel

/**
 * CameraHost composable: slot for CameraX preview with iOS Locket viewfinder styling
 */
@Composable
fun CameraHost(
    modifier: Modifier = Modifier,
    onCameraReady: ((PreviewView) -> Unit)? = null,
    onFlashToggle: (() -> Unit)? = null,
    onZoomClick: (() -> Unit)? = null,
    flashEnabled: Boolean = false,
    zoomText: String = "1×"
) {
    Box(
        modifier = modifier
            .clip(LocketShapes.Viewfinder)
            .background(Color(0xFF1B232A))
    ) {
        // AndroidView PreviewView placeholder for CameraX agent to plug into
        AndroidView(
            factory = { context ->
                PreviewView(context).apply {
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    onCameraReady?.invoke(this)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay: Flash icon top-left
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(14.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.35f))
                .bounceClick(scaleDown = 0.88f) {
                    onFlashToggle?.invoke()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (flashEnabled) Icons.Rounded.FlashOn else Icons.Rounded.FlashOff,
                contentDescription = "Flash Toggle",
                tint = if (flashEnabled) LocketColors.AccentYellow else Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        // Overlay: "1×" text top-right
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(14.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.35f))
                .bounceClick(scaleDown = 0.88f) {
                    onZoomClick?.invoke()
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = zoomText,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Main Camera Screen (shot_5 in ui-ref)
 */
@Composable
fun CameraScreen(
    modifier: Modifier = Modifier,
    viewModel: CameraViewModel? = null,
    onNavigateToFeed: () -> Unit = {},
    onNavigateToChat: () -> Unit = {},
    onOpenFriendsSheet: () -> Unit = {},
    onOpenOnboarding: () -> Unit = {},
    onGalleryClick: () -> Unit = {},
    onFlipCamera: () -> Unit = {},
    onShutterClick: () -> Unit = {},
    onCameraReady: ((PreviewView) -> Unit)? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val flashEnabled by (viewModel?.flashMode
        ?: MutableStateFlow(FlashMode.OFF)).collectAsStateWithLifecycle()
    val zoomRatio by (viewModel?.zoomRatio
        ?: MutableStateFlow(1f)).collectAsStateWithLifecycle()
    val zoomText = when {
        zoomRatio >= 1.75f -> "2×"
        zoomRatio <= 0.75f -> "0.5×"
        else -> "1×"
    }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    val lastPhotoUri by (viewModel?.lastPhotoUri
        ?: MutableStateFlow<android.net.Uri?>(null)).collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LocketColors.Background)
            .statusBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // TOP ROW: Megaphone circle | "12 Friends" pill | Avatar circle
        LocketTopBar(
            modifier = Modifier.fillMaxWidth(),
            leftAction = {
                CircleIconButton(
                    icon = Icons.Rounded.Campaign,
                    contentDescription = "Announcements",
                    size = 44.dp,
                    backgroundColor = LocketColors.SurfaceNavy,
                    onClick = onOpenOnboarding
                )
            },
            centerContent = {
                CapsulePill(
                    backgroundColor = LocketColors.SurfaceNavy,
                    onClick = onOpenFriendsSheet
                ) {
                    Icon(
                        imageVector = Icons.Rounded.People,
                        contentDescription = "Friends",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "12 Friends",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            rightAction = {
                AvatarCircle(
                    size = 44.dp,
                    name = "Me",
                    backgroundColor = LocketColors.SurfaceNavy,
                    onClick = onOpenFriendsSheet
                )
            }
        )

        // MIDDLE: Viewfinder placeholder Box with rounded 28dp
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
                .weight(1f, fill = true)
                .clip(LocketShapes.Viewfinder),
            contentAlignment = Alignment.Center
        ) {
            CameraHost(
                modifier = Modifier.fillMaxSize(),
                onCameraReady = { pv ->
                    previewView = pv
                    onCameraReady?.invoke(pv)
                    viewModel?.bindCamera(context, lifecycleOwner, pv)
                },
                onFlashToggle = {
                    viewModel?.toggleFlash() ?: run { }
                },
                onZoomClick = {
                    // Cycle zoom: 1× → 2× → 0.5× → 1×
                    val next = when {
                        zoomRatio >= 1.75f -> 0.5f
                        zoomRatio <= 0.75f -> 1f
                        else -> 2f
                    }
                    viewModel?.setZoom(next)
                },
                flashEnabled = flashEnabled == FlashMode.ON,
                zoomText = zoomText
            )
        }

        // CONTROL ROW: Gallery | Shutter 78dp viền vàng 6dp | Flip icon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Gallery icon
            CircleIconButton(
                icon = Icons.Rounded.PhotoLibrary,
                contentDescription = "Gallery",
                size = 52.dp,
                iconSize = 26.dp,
                backgroundColor = LocketColors.SurfaceNavy.copy(alpha = 0.7f),
                onClick = onGalleryClick
            )

            // Shutter Button (78dp, #F5B800 6dp border, pure white circle, spring scale 0.9)
            val shutterInteraction = remember { MutableInteractionSource() }
            val isShutterPressed by shutterInteraction.collectIsPressedAsState()
            val shutterScale by animateFloatAsState(
                targetValue = if (isShutterPressed) 0.9f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "shutterScale"
            )

            Box(
                modifier = Modifier
                    .size(78.dp)
                    .bounceClick(scaleDown = 0.9f) {
                        viewModel?.capture()
                        onShutterClick()
                    }
                    .border(6.dp, LocketColors.AccentYellow, CircleShape)
                    .padding(5.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )

            // Flip camera icon
            CircleIconButton(
                icon = Icons.Rounded.FlipCameraIos,
                contentDescription = "Flip Camera",
                size = 52.dp,
                iconSize = 26.dp,
                backgroundColor = LocketColors.SurfaceNavy.copy(alpha = 0.7f),
                onClick = {
                    viewModel?.flipCamera()
                    onFlipCamera()
                }
            )
        }

        // HISTORY ROW: thumbnail 48dp rounded 12 + "History" bold + chevron-down
        Row(
            modifier = Modifier
                .padding(top = 4.dp, bottom = 12.dp)
                .clip(CircleShape)
                .bounceClick(scaleDown = 0.96f) {
                    onNavigateToFeed()
                }
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Thumbnail 48dp rounded 12
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(LocketShapes.Thumbnail)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF38434B), Color(0xFF20282E))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (lastPhotoUri != null) {
                    coil.compose.AsyncImage(
                        model = lastPhotoUri,
                        contentDescription = "Last photo",
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = "📸",
                        fontSize = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = "History",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.width(4.dp))

            Icon(
                imageVector = Icons.Rounded.KeyboardArrowDown,
                contentDescription = "Open History",
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(24.dp)
            )
        }

        // BOTTOM TAB BAR: capsule #2A343B floating 3 icon — keypad | home active (#4A545C) | chat + badge "2"
        Box(
            modifier = Modifier
                .padding(bottom = 12.dp)
                .background(LocketColors.SurfaceNavy, CircleShape)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Keypad (left)
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .bounceClick(scaleDown = 0.9f) {
                            onNavigateToFeed()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Dialpad,
                        contentDescription = "Keypad",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Home (center, active pill #4A545C)
                Box(
                    modifier = Modifier
                        .height(44.dp)
                        .width(58.dp)
                        .clip(CircleShape)
                        .background(LocketColors.ActiveHighlight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Home,
                        contentDescription = "Home",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Chat (right) with yellow badge "2" at top-right
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .bounceClick(scaleDown = 0.9f) {
                            onNavigateToChat()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ChatBubble,
                        contentDescription = "Chat",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )

                    BadgeYellow(
                        count = "2",
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 2.dp, y = (-2).dp)
                    )
                }
            }
        }
    }
}
