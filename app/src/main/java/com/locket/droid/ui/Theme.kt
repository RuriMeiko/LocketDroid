package com.locket.droid.ui

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Design tokens for LocketDroid (matching Locket Widget iOS v2.69.0)
 */
object LocketColors {
    // Backgrounds
    val Background = Color(0xFF141D24)
    val BackgroundDark = Color(0xFF0F171D)
    val BackgroundPureDark = Color(0xFF1C1C1E)
    val OnboardingBackground = Color(0xFF1E1A08)

    // Surfaces & Pills
    val Surface = Color(0xFF2C2C2E)
    val SurfaceNavy = Color(0xFF2A343B)
    val SurfaceChat = Color(0xFF2A2F2B)
    val SurfaceInput = Color(0xFF2E332F)
    val ActiveHighlight = Color(0xFF4A545C)
    val Grabber = Color(0xFF48484A)

    // Accents & Brand Yellows
    val AccentYellow = Color(0xFFF5B800)
    val AccentGold = Color(0xFFFFC107)
    val AccentBadge = Color(0xFFF5C518)
    val HeartBrown = Color(0xFFC8880A)

    // Texts
    val TextPrimary = Color(0xFFFFFFFF)
    val TextPrimaryAlt = Color(0xFFF2F2F7)
    val TextOnboarding = Color(0xFFF2F0E4)
    val TextSecondary = Color(0xFF8E8E93)
    val TextTertiary = Color(0xFF98989F)
    val TextMuted = Color(0xFF8A938C)

    // Chat Bubbles
    val BubbleSender = Color(0xFFE8E8E8)
    val TextSender = Color(0xFF1A1A1A)
    val BubbleReceiver = Color(0xFF2A2F2B)
    val TextReceiver = Color(0xFFFFFFFF)

    // Timestamp Gradient
    val TimestampGradientStart = Color(0xFFA78BFA)
    val TimestampGradientEnd = Color(0xFFF0ABFC)
}

/**
 * Shape tokens matching iOS rounded corners
 */
object LocketShapes {
    val PhotoCard = RoundedCornerShape(40.dp)
    val Viewfinder = RoundedCornerShape(28.dp)
    val Thumbnail = RoundedCornerShape(12.dp)
    val Bubble = RoundedCornerShape(20.dp)
    val Capsule = CircleShape
}

// Nunito fallback font family
val NunitoFontFamily = FontFamily.SansSerif

val LocketTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = NunitoFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 38.sp,
        lineHeight = 44.sp,
        letterSpacing = (-0.5).sp
    ),
    displayMedium = TextStyle(
        fontFamily = NunitoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 38.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = NunitoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 32.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = NunitoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleLarge = TextStyle(
        fontFamily = NunitoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    titleMedium = TextStyle(
        fontFamily = NunitoFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = NunitoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = NunitoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelLarge = TextStyle(
        fontFamily = NunitoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 18.sp
    ),
    labelMedium = TextStyle(
        fontFamily = NunitoFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = NunitoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp
    )
)

private val LocketColorScheme = darkColorScheme(
    primary = LocketColors.AccentYellow,
    onPrimary = Color.Black,
    secondary = LocketColors.AccentGold,
    onSecondary = Color.Black,
    background = LocketColors.Background,
    onBackground = LocketColors.TextPrimary,
    surface = LocketColors.Surface,
    onSurface = LocketColors.TextPrimary,
    surfaceVariant = LocketColors.SurfaceNavy,
    onSurfaceVariant = LocketColors.TextSecondary
)

private val MaterialShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = LocketShapes.PhotoCard
)

@Composable
fun LocketTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LocketColorScheme,
        typography = LocketTypography,
        shapes = MaterialShapes,
        content = content
    )
}
