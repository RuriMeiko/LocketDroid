package com.locket.droid.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Spring bounce animation on click for a smooth iOS-like tactile interaction
 */
fun Modifier.bounceClick(
    scaleDown: Float = 0.9f,
    onClick: () -> Unit = {}
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "bounceScale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
}

/**
 * Top bar matching Locket iOS navigation bar layout
 */
@Composable
fun LocketTopBar(
    modifier: Modifier = Modifier,
    leftAction: @Composable (() -> Unit)? = null,
    centerContent: @Composable (() -> Unit)? = null,
    rightAction: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.defaultMinSize(minWidth = 44.dp, minHeight = 44.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            leftAction?.invoke()
        }

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            centerContent?.invoke()
        }

        Box(
            modifier = Modifier.defaultMinSize(minWidth = 44.dp, minHeight = 44.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            rightAction?.invoke()
        }
    }
}

/**
 * Small circular yellow badge (e.g., unread count "2")
 */
@Composable
fun BadgeYellow(
    count: String = "2",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(LocketColors.AccentYellow, CircleShape)
            .padding(horizontal = 5.dp, vertical = 1.dp)
            .defaultMinSize(minWidth = 18.dp, minHeight = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = count,
            color = Color.Black,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            lineHeight = 12.sp
        )
    }
}

/**
 * Rounded capsule pill container used for status, counts, and interactive chips
 */
@Composable
fun CapsulePill(
    modifier: Modifier = Modifier,
    backgroundColor: Color = LocketColors.SurfaceNavy,
    contentPadding: PaddingValues = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit
) {
    val clickableModifier = if (onClick != null) {
        Modifier.bounceClick(scaleDown = 0.95f, onClick = onClick)
    } else {
        Modifier
    }

    Row(
        modifier = modifier
            .then(clickableModifier)
            .background(backgroundColor, CircleShape)
            .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        content = content
    )
}

/**
 * Reusable avatar circle placeholder with initials or person icon
 */
@Composable
fun AvatarCircle(
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    name: String = "",
    backgroundColor: Color = Color(0xFF38444E),
    borderColor: Color? = null,
    borderWidth: Dp = 0.dp,
    onClick: (() -> Unit)? = null
) {
    val clickableModifier = if (onClick != null) {
        Modifier.bounceClick(scaleDown = 0.92f, onClick = onClick)
    } else {
        Modifier
    }

    val borderModifier = if (borderColor != null && borderWidth > 0.dp) {
        Modifier.border(borderWidth, borderColor, CircleShape)
    } else {
        Modifier
    }

    val initials = name.split(" ")
        .filter { it.isNotBlank() }
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2)
        .joinToString("")
        .uppercase()

    Box(
        modifier = modifier
            .size(size)
            .then(borderModifier)
            .clip(CircleShape)
            .background(backgroundColor)
            .then(clickableModifier),
        contentAlignment = Alignment.Center
    ) {
        if (initials.isNotEmpty()) {
            Text(
                text = initials,
                color = Color.White,
                fontSize = (size.value * 0.38f).sp,
                fontWeight = FontWeight.Bold
            )
        } else {
            Icon(
                imageVector = Icons.Rounded.Person,
                contentDescription = "Avatar",
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size((size.value * 0.55f).dp)
            )
        }
    }
}

/**
 * Row of quick emoji reactions (😍, 🔥, 💖, etc.)
 */
@Composable
fun ReactionRow(
    modifier: Modifier = Modifier,
    reactions: List<String> = listOf("😍", "🔥", "💖"),
    onReactionClick: ((String) -> Unit)? = null
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        reactions.forEach { emoji ->
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .bounceClick(scaleDown = 0.85f) {
                        onReactionClick?.invoke(emoji)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emoji,
                    fontSize = 18.sp
                )
            }
        }
    }
}

/**
 * Reusable circular icon button with spring animation
 */
@Composable
fun CircleIconButton(
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    iconSize: Dp = 22.dp,
    backgroundColor: Color = LocketColors.SurfaceNavy,
    tint: Color = Color.White,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor)
            .bounceClick(scaleDown = 0.9f, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
    }
}

/**
 * Squiggle decorative line used beside "Show more" pills
 */
@Composable
fun SquiggleLine(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF3E4348),
    waves: Int = 3
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val midY = height / 2f
        val step = width / (waves * 2)
        val path = Path().apply {
            moveTo(0f, midY)
            for (i in 0 until waves) {
                val cx1 = (i * 2 + 0.5f) * step
                val cy1 = 0f
                val ex1 = (i * 2 + 1f) * step
                quadraticTo(cx1, cy1, ex1, midY)

                val cx2 = (i * 2 + 1.5f) * step
                val cy2 = height
                val ex2 = (i * 2 + 2f) * step
                quadraticTo(cx2, cy2, ex2, midY)
            }
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = 2.dp.toPx(),
                pathEffect = PathEffect.cornerPathEffect(4f)
            )
        )
    }
}

/**
 * Modifier drawing a dashed circular border (for the "Add" tile in FriendsSheet)
 */
fun Modifier.dashedCircleBorder(
    color: Color = LocketColors.AccentGold,
    strokeWidth: Dp = 2.dp,
    dashLength: Float = 16f,
    gapLength: Float = 10f
): Modifier = this.drawBehind {
    val radius = (size.minDimension - strokeWidth.toPx()) / 2f
    drawCircle(
        color = color,
        radius = radius,
        style = Stroke(
            width = strokeWidth.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashLength, gapLength), 0f)
        )
    )
}
