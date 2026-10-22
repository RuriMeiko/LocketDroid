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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

sealed class ChatMessage(val id: String, val isFromMe: Boolean) {
    data class TextMessage(
        val messageId: String,
        val text: String,
        val fromMe: Boolean
    ) : ChatMessage(messageId, fromMe)

    data class PhotoMessage(
        val messageId: String,
        val photoUrl: String? = null,
        val fromMe: Boolean
    ) : ChatMessage(messageId, fromMe)
}

/**
 * Chat Screen (shot_3 in ui-ref)
 */
@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    friendName: String = "Bobby",
    onBackClick: () -> Unit = {},
    onCameraClick: () -> Unit = {}
) {
    var messageInput by remember { mutableStateOf("") }

    val messages = remember {
        mutableStateListOf<ChatMessage>(
            ChatMessage.PhotoMessage(
                messageId = "1",
                fromMe = false
            ),
            ChatMessage.TextMessage(
                messageId = "2",
                text = "Hey! Look what I found today 👀",
                fromMe = false
            ),
            ChatMessage.TextMessage(
                messageId = "3",
                text = "Haha that looks amazing! Where is that?",
                fromMe = true
            ),
            ChatMessage.PhotoMessage(
                messageId = "4",
                fromMe = true
            ),
            ChatMessage.TextMessage(
                messageId = "5",
                text = "At the new coffee shop downtown!",
                fromMe = false
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LocketColors.Background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        // TOP BAR: back circle #2E332F chevron trái | avatar center + name pill "Bobby ›" #2A2F2B
        LocketTopBar(
            modifier = Modifier.fillMaxWidth(),
            leftAction = {
                CircleIconButton(
                    icon = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    size = 44.dp,
                    backgroundColor = LocketColors.SurfaceInput,
                    onClick = onBackClick
                )
            },
            centerContent = {
                CapsulePill(
                    backgroundColor = LocketColors.SurfaceChat,
                    contentPadding = PaddingValues(start = 6.dp, top = 4.dp, end = 12.dp, bottom = 4.dp),
                    onClick = {}
                ) {
                    AvatarCircle(
                        size = 32.dp,
                        name = friendName,
                        backgroundColor = Color(0xFF38434B)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = friendName,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = "Details",
                        tint = LocketColors.TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            },
            rightAction = {
                Spacer(modifier = Modifier.size(44.dp))
            }
        )

        // TIMESTAMP: "May 2, 2025 at 10:09 AM" #8A938C
        Text(
            text = "May 2, 2025 at 10:09 AM",
            color = LocketColors.TextMuted,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        )

        // BUBBLES LIST
        val listState = rememberLazyListState()
        LaunchedEffect(messages.size) {
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                when (msg) {
                    is ChatMessage.TextMessage -> {
                        TextMessageBubble(
                            text = msg.text,
                            isFromMe = msg.isFromMe
                        )
                    }
                    is ChatMessage.PhotoMessage -> {
                        PhotoMessageBubble(
                            isFromMe = msg.isFromMe
                        )
                    }
                }
            }
        }

        // INPUT BAR: shutter ring vàng nhỏ trái + pill field #2E332F "Message..." #8A938C + emoji 🔥💖😊+
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shutter ring vàng nhỏ trái -> returns to Camera
            val shutterInteraction = remember { MutableInteractionSource() }
            val isPressed by shutterInteraction.collectIsPressedAsState()
            val scale by animateFloatAsState(
                targetValue = if (isPressed) 0.88f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "chatShutterScale"
            )

            Box(
                modifier = Modifier
                    .size(38.dp)
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
                        onClick = onCameraClick
                    )
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Pill text field: #2E332F "Message..." #8A938C + emoji shortcuts + "+"
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(CircleShape)
                    .background(LocketColors.SurfaceInput)
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (messageInput.isEmpty()) {
                        Text(
                            text = "Message...",
                            color = LocketColors.TextMuted,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                    BasicTextField(
                        value = messageInput,
                        onValueChange = { messageInput = it },
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 15.sp,
                            fontFamily = NunitoFontFamily
                        ),
                        cursorBrush = SolidColor(LocketColors.AccentYellow),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Emoji shortcuts: 🔥 💖 😊
                listOf("🔥", "💖", "😊").forEach { emoji ->
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .bounceClick(scaleDown = 0.82f) {
                                messages.add(
                                    ChatMessage.TextMessage(
                                        messageId = System.currentTimeMillis().toString(),
                                        text = emoji,
                                        fromMe = true
                                    )
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // "+" icon button
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .bounceClick(scaleDown = 0.85f) {
                            if (messageInput.isNotBlank()) {
                                messages.add(
                                    ChatMessage.TextMessage(
                                        messageId = System.currentTimeMillis().toString(),
                                        text = messageInput.trim(),
                                        fromMe = true
                                    )
                                )
                                messageInput = ""
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Send or Add",
                        tint = if (messageInput.isNotBlank()) LocketColors.AccentYellow else LocketColors.TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Text bubble: sender (right) bg #E8E8E8 text #1A1A1A, receiver (left) bg #2A2F2B text white
 */
@Composable
private fun TextMessageBubble(
    text: String,
    isFromMe: Boolean
) {
    val bubbleShape = if (isFromMe) {
        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 4.dp, bottomEnd = 20.dp)
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isFromMe) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .clip(bubbleShape)
                .background(if (isFromMe) LocketColors.BubbleSender else LocketColors.BubbleReceiver)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(
                text = text,
                color = if (isFromMe) LocketColors.TextSender else LocketColors.TextReceiver,
                fontSize = 15.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Photo bubble: radius 40dp
 */
@Composable
private fun PhotoMessageBubble(
    isFromMe: Boolean
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isFromMe) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .width(220.dp)
                .aspectRatio(1f)
                .clip(LocketShapes.PhotoCard)
                .background(
                    Brush.verticalGradient(
                        if (isFromMe) {
                            listOf(Color(0xFF424A52), Color(0xFF282E34))
                        } else {
                            listOf(Color(0xFF383F45), Color(0xFF1E2429))
                        }
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isFromMe) "💛" else "📸",
                fontSize = 36.sp
            )
        }
    }
}
