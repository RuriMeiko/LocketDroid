package com.locket.droid.data

import android.net.Uri
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.util.UUID

data class ChatMessage(
    val id: String,
    val type: Type,
    val text: String? = null,
    /** For PHOTO; null = demo photo (draw placeholder from [colorSeed]). */
    val photoUri: Uri? = null,
    val colorSeed: Int = 0,
    val isMine: Boolean,
    val timestamp: Long,
    val reaction: String? = null,
) {
    enum class Type { TEXT, PHOTO }
}

class ChatRepository(friends: List<Friend> = LocketRepository.DEMO_FRIENDS) {

    private val byId = friends.associateBy { it.id }
    private val _conversations = MutableStateFlow(
        buildDemo().mapNotNull { (id, m) -> byId[id]?.let { it to m } }.toMap(),
    )
    private val _unread = MutableStateFlow(mapOf("jessica" to 1, "kyle" to 1))

    /** Friend -> messages, oldest first. Friends without a conversation are absent. */
    val conversations: StateFlow<Map<Friend, List<ChatMessage>>> = _conversations.asStateFlow()

    /** Unread per friend id, and the total for the tab-bar badge. */
    val unread: StateFlow<Map<String, Int>> = _unread.asStateFlow()
    val unreadTotal: Flow<Int> = _unread.map { it.values.sum() }

    fun messagesFor(friendId: String): List<ChatMessage> =
        byId[friendId]?.let { _conversations.value[it] }.orEmpty()

    fun sendMessage(friendId: String, text: String) {
        if (text.isBlank()) return
        append(friendId, ChatMessage(newId(), ChatMessage.Type.TEXT, text = text.trim(), isMine = true, timestamp = now()))
    }

    fun sendMessage(friend: Friend, text: String) = sendMessage(friend.id, text)

    fun sendPhoto(friendId: String, uri: Uri) {
        append(friendId, ChatMessage(newId(), ChatMessage.Type.PHOTO, photoUri = uri, isMine = true, timestamp = now()))
    }

    fun sendPhoto(friend: Friend, uri: Uri) = sendPhoto(friend.id, uri)

    /** Set or clear (null) the reaction on a message. */
    fun react(friendId: String, messageId: String, emoji: String?) {
        val friend = byId[friendId] ?: return
        _conversations.update { all ->
            all + (friend to all[friend].orEmpty().map { if (it.id == messageId) it.copy(reaction = emoji) else it })
        }
    }

    fun markRead(friendId: String) = _unread.update { it - friendId }

    private fun append(friendId: String, msg: ChatMessage) {
        val friend = byId[friendId] ?: return
        _conversations.update { it + (friend to it[friend].orEmpty() + msg) }
    }

    private fun newId() = UUID.randomUUID().toString()
    private fun now() = System.currentTimeMillis()

    private fun buildDemo(): Map<String, List<ChatMessage>> {
        // Timeline of screenshot 3 ("May 2, 2025 at 10:09 AM"), relative to now.
        val t = now() - 60 * 60_000L
        fun text(i: Int, s: String, mine: Boolean, off: Long) =
            ChatMessage("bobby_$i", ChatMessage.Type.TEXT, text = s, isMine = mine, timestamp = t + off)
        val bobby = listOf(
            text(0, "Ya I think that's good", true, 0),
            text(1, "That's not for a bit right?", true, 20_000),
            ChatMessage("bobby_2", ChatMessage.Type.PHOTO, colorSeed = 5, isMine = false,
                timestamp = t + 2 * 60_000, reaction = "🧡"),
            text(3, "What a style icon 😍", true, 3 * 60_000),
            text(4, "Come hang ✨", false, 4 * 60_000),
            text(5, "I'll send you my location!", false, 4 * 60_000 + 15_000),
        )
        val jessica = listOf(
            ChatMessage("jess_0", ChatMessage.Type.PHOTO, colorSeed = 0, isMine = false, timestamp = now() - 13 * 60_000),
            ChatMessage("jess_1", ChatMessage.Type.TEXT, text = "Look where I am 🔥", isMine = false, timestamp = now() - 12 * 60_000),
        )
        val kyle = listOf(
            ChatMessage("kyle_0", ChatMessage.Type.TEXT, text = "Are you coming tonight?", isMine = false, timestamp = now() - 3 * 60 * 60_000L),
            ChatMessage("kyle_1", ChatMessage.Type.TEXT, text = "Yes! Save me a seat", isMine = true, timestamp = now() - 170 * 60_000L),
            ChatMessage("kyle_2", ChatMessage.Type.TEXT, text = "Done 💖", isMine = false, timestamp = now() - 160 * 60_000L),
        )
        return mapOf("bobby" to bobby, "jessica" to jessica, "kyle" to kyle)
    }
}
