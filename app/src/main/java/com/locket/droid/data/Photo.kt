package com.locket.droid.data

import android.net.Uri

/**
 * A photo in the History / Rollcall feed.
 *
 * [uri] is null for demo photos that have no real image: the UI should draw a placeholder
 * gradient derived from [colorSeed].
 */
data class Photo(
    val id: String,
    val uri: Uri?,
    val senderName: String,
    val timestamp: Long,
    val isMine: Boolean,
    val reactions: List<String> = emptyList(),
    val caption: String? = null,
    val colorSeed: Int = 0,
) {
    val timeAgoFormatted: String
        get() {
            val diff = (System.currentTimeMillis() - timestamp).coerceAtLeast(0)
            val mins = diff / 60_000L
            if (mins < 1) return "Just now"
            if (mins < 60) return "${mins}m"
            val hours = mins / 60L
            if (hours < 24) return "${hours}h"
            val days = hours / 24L
            return "${days}d"
        }
}

/** [avatarColor] is ARGB (use `Color(friend.avatarColor)` in Compose). */
data class Friend(
    val id: String,
    val name: String,
    val avatarColor: Long,
    val isBestFriend: Boolean = false,
)
