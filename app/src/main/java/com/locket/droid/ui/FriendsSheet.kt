package com.locket.droid.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class FriendItem(val id: String, val name: String)
data class SuggestionItem(val id: String, val name: String, val subtitle: String)

/**
 * FriendsSheet as a ModalBottomSheet (shot_6 in ui-ref)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = LocketColors.BackgroundPureDark,
        dragHandle = {
            // Custom grabber #48484A
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 12.dp)
                    .width(42.dp)
                    .height(5.dp)
                    .clip(CircleShape)
                    .background(LocketColors.Grabber)
            )
        },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = modifier
    ) {
        FriendsSheetContent(
            onDismiss = onDismissRequest,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Inner content for FriendsSheet
 */
@Composable
fun FriendsSheetContent(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {}
) {
    val friends = remember {
        listOf(
            FriendItem("1", "Bobby"),
            FriendItem("2", "Jessica"),
            FriendItem("3", "David"),
            FriendItem("4", "Chloe"),
            FriendItem("5", "James")
        )
    }

    val suggestions = remember {
        mutableStateListOf(
            SuggestionItem("s1", "Emma Watson", "In your contacts"),
            SuggestionItem("s2", "Lucas Miller", "From your school"),
            SuggestionItem("s3", "Olivia Bennett", "Mutual friends with Bobby")
        )
    }

    var showMoreFriends by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Title: "🔥 20 Friends" trắng bold lớn + Subtitle
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🔥 20 Friends",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                val subtitleText = buildAnnotatedString {
                    withStyle(SpanStyle(color = LocketColors.TextSecondary, fontWeight = FontWeight.Medium, fontSize = 16.sp)) {
                        append("Add your ")
                    }
                    withStyle(SpanStyle(color = LocketColors.AccentGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)) {
                        append("best friends")
                    }
                }
                Text(text = subtitleText, textAlign = TextAlign.Center)
            }
        }

        // Search pill: #2C2C2E kính lúp + "Add a new friend"
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(CircleShape)
                    .background(LocketColors.Surface)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Search",
                    tint = LocketColors.TextSecondary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Add a new friend",
                    color = LocketColors.TextSecondary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }

        // Section "Your friends" #98989F
        item {
            Text(
                text = "Your friends",
                color = LocketColors.TextTertiary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Grid 3 cột avatar circle ~110px + tên trắng; tile "Add": circle viền DASHED vàng + "+" vàng
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // First row of 3 items (Add tile + first 2 friends)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    // Tile "Add": circle viền DASHED vàng + "+" vàng + label "Add" vàng
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(96.dp)
                            .bounceClick(scaleDown = 0.92f) {}
                    ) {
                        Box(
                            modifier = Modifier
                                .size(78.dp)
                                .dashedCircleBorder(
                                    color = LocketColors.AccentGold,
                                    strokeWidth = 2.dp,
                                    dashLength = 16f,
                                    gapLength = 10f
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = "Add friend",
                                tint = LocketColors.AccentGold,
                                modifier = Modifier.size(34.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Add",
                            color = LocketColors.AccentGold,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Friends
                    friends.take(2).forEach { friend ->
                        FriendGridTile(friend = friend)
                    }
                }

                // Second row of remaining friends
                if (friends.size > 2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        friends.drop(2).take(3).forEach { friend ->
                            FriendGridTile(friend = friend)
                        }
                    }
                }
            }
        }

        // Pill "Show 15 more" #2C2C2E với squiggle lines hai bên
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                SquiggleLine(
                    modifier = Modifier
                        .weight(1f)
                        .height(14.dp)
                        .padding(horizontal = 8.dp),
                    color = Color(0xFF383E44)
                )

                CapsulePill(
                    backgroundColor = LocketColors.Surface,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    onClick = { showMoreFriends = !showMoreFriends }
                ) {
                    Text(
                        text = if (showMoreFriends) "Show less" else "Show 15 more",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                SquiggleLine(
                    modifier = Modifier
                        .weight(1f)
                        .height(14.dp)
                        .padding(horizontal = 8.dp),
                    color = Color(0xFF383E44)
                )
            }
        }

        // Section "Suggestions"
        item {
            Text(
                text = "Suggestions",
                color = LocketColors.TextTertiary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Suggestions rows: avatar 64 + tên trắng + X xám + nút Add capsule #FFC107 text ĐEN
        items(suggestions, key = { it.id }) { suggestion ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AvatarCircle(
                    size = 56.dp,
                    name = suggestion.name,
                    backgroundColor = LocketColors.Surface
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = suggestion.name,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = suggestion.subtitle,
                        color = LocketColors.TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal
                    )
                }

                // X gray icon
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .bounceClick(scaleDown = 0.85f) {
                            suggestions.remove(suggestion)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Dismiss",
                        tint = LocketColors.TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Add button capsule #FFC107 text ĐEN
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(LocketColors.AccentGold)
                        .bounceClick(scaleDown = 0.92f) {
                            suggestions.remove(suggestion)
                        }
                        .padding(horizontal = 18.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Add",
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
private fun FriendGridTile(friend: FriendItem) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(96.dp)
            .bounceClick(scaleDown = 0.92f) {}
    ) {
        AvatarCircle(
            size = 78.dp,
            name = friend.name,
            backgroundColor = LocketColors.SurfaceNavy,
            modifier = Modifier.width(78.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = friend.name,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
