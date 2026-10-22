package com.locket.droid.ui

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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material.icons.rounded.WbSunny
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

data class FeedItem(
    val id: String,
    val senderName: String,
    val timeAgo: String,
    val timestampFormatted: String,
    val emojiReactions: List<String> = listOf("🔥", "💖", "😊")
)

/**
 * Feed / History Screen (shot_7 in ui-ref)
 */
@Composable
fun FeedScreen(
    modifier: Modifier = Modifier,
    onNavigateToCamera: () -> Unit = {},
    onNavigateToChat: () -> Unit = {},
    onOpenFriendsSheet: () -> Unit = {},
    onAnnouncementsClick: () -> Unit = {}
) {
    val sampleFeedItems = remember {
        listOf(
            FeedItem("1", "Jessica", "13m", "9:41 AM"),
            FeedItem("2", "Bobby", "1h", "8:25 AM"),
            FeedItem("3", "Sarah", "4h", "5:10 AM"),
            FeedItem("4", "Alex", "Yesterday", "11:42 PM")
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LocketColors.Background)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // TOP BAR: Megaphone circle | "Everyone ▾" pill (#2C2C2E) | Avatar circle
            LocketTopBar(
                modifier = Modifier.fillMaxWidth(),
                leftAction = {
                    CircleIconButton(
                        icon = Icons.Rounded.Campaign,
                        contentDescription = "Announcements",
                        size = 44.dp,
                        backgroundColor = LocketColors.Surface,
                        onClick = onAnnouncementsClick
                    )
                },
                centerContent = {
                    CapsulePill(
                        backgroundColor = LocketColors.Surface,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        onClick = onOpenFriendsSheet
                    ) {
                        Text(
                            text = "Everyone",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Rounded.ArrowDropDown,
                            contentDescription = "Filter",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                rightAction = {
                    AvatarCircle(
                        size = 44.dp,
                        name = "Me",
                        backgroundColor = LocketColors.Surface,
                        onClick = onOpenFriendsSheet
                    )
                }
            )

            // LazyColumn photo cards
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                items(sampleFeedItems, key = { it.id }) { item ->
                    FeedCardItem(
                        item = item,
                        onReplyClick = onNavigateToChat
                    )
                }
            }
        }

        // BOTTOM TOOLBAR: Grid circle | Capsule (Keyboard + Small Yellow Shutter + Chat) | Share circle
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Grid circle button (#2C2C2E)
                CircleIconButton(
                    icon = Icons.Rounded.GridView,
                    contentDescription = "Grid View",
                    size = 48.dp,
                    backgroundColor = LocketColors.Surface,
                    onClick = {}
                )

                // Center: Capsule with keyboard + shutter nhỏ (ring vàng) + chat
                Box(
                    modifier = Modifier
                        .height(52.dp)
                        .clip(CircleShape)
                        .background(LocketColors.Surface)
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        // Keyboard icon
                        Icon(
                            imageVector = Icons.Rounded.Keyboard,
                            contentDescription = "Keyboard",
                            tint = Color.White,
                            modifier = Modifier
                                .size(24.dp)
                                .bounceClick(scaleDown = 0.88f) {}
                        )

                        // Shutter nhỏ ring vàng #F5B800 -> click returns to camera
                        val shutterInteraction = remember { MutableInteractionSource() }
                        val isPressed by shutterInteraction.collectIsPressedAsState()
                        val scale by animateFloatAsState(
                            targetValue = if (isPressed) 0.88f else 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            ),
                            label = "feedShutterScale"
                        )

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                }
                                .border(3.5.dp, LocketColors.AccentYellow, CircleShape)
                                .padding(3.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .clickable(
                                    interactionSource = shutterInteraction,
                                    indication = null,
                                    onClick = onNavigateToCamera
                                )
                        )

                        // Chat icon -> navigates to ChatScreen
                        Icon(
                            imageVector = Icons.Rounded.ChatBubble,
                            contentDescription = "Chat",
                            tint = Color.White,
                            modifier = Modifier
                                .size(24.dp)
                                .bounceClick(scaleDown = 0.88f) {
                                    onNavigateToChat()
                                }
                        )
                    }
                }

                // Right: Share circle button (#2C2C2E)
                CircleIconButton(
                    icon = Icons.Rounded.IosShare,
                    contentDescription = "Share",
                    size = 48.dp,
                    backgroundColor = LocketColors.Surface,
                    onClick = {}
                )
            }
        }
    }
}

/**
 * Individual Photo Card with gradient placeholder, timestamp pill, sender row, and reply bar
 */
@Composable
private fun FeedCardItem(
    item: FeedItem,
    onReplyClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Photo Card placeholder (radius 40dp, aspect ratio 1:1, gradient xám)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(LocketShapes.PhotoCard)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF383D43), Color(0xFF1E2125))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // Placeholder subtle photo icon / pattern
            Text(
                text = "✨",
                fontSize = 32.sp,
                modifier = Modifier.align(Alignment.Center)
            )

            // Timestamp pill overlay bottom-center (gradient #A78BFA→#F0ABFC, icon mặt trời + text giờ trắng bold)
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                LocketColors.TimestampGradientStart,
                                LocketColors.TimestampGradientEnd
                            )
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.WbSunny,
                    contentDescription = "Sun",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = item.timestampFormatted,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Sender row dưới card: avatar 44dp + tên trắng bold + timeAgo #8E8E93
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarCircle(
                size = 44.dp,
                name = item.senderName,
                backgroundColor = LocketColors.Surface
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = item.senderName,
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = item.timeAgo,
                color = LocketColors.TextSecondary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Reply bar capsule #2C2C2E: "Reply to Jessica..." + 3 emoji 🔥💖😊 bên phải
        var replyText by remember { mutableStateOf("") }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(CircleShape)
                .background(LocketColors.Surface)
                .bounceClick(scaleDown = 0.98f) {
                    onReplyClick()
                }
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Reply to ${item.senderName}...",
                color = LocketColors.TextSecondary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal
            )

            ReactionRow(
                reactions = item.emojiReactions,
                onReactionClick = { emoji ->
                    // Emoji feedback
                }
            )
        }
    }
}
