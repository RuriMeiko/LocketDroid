package com.locket.droid.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Local data layer: demo friends + demo feed, merged with the user's own photos
 * (in-memory additions and everything saved under Pictures/Locket in MediaStore).
 */
class LocketRepository(context: Context) {
    private val resolver = context.applicationContext.contentResolver

    val friends: StateFlow<List<Friend>> = MutableStateFlow(DEMO_FRIENDS).asStateFlow()

    private val demo = MutableStateFlow(buildDemoFeed())
    private val mine = MutableStateFlow<Map<String, Photo>>(emptyMap())

    init {
        // Asynchronously load any existing photos from Pictures/Locket MediaStore
        CoroutineScope(Dispatchers.IO).launch {
            refreshLocal()
        }
    }

    /** History feed, newest first. */
    val photos: Flow<List<Photo>> = combine(demo, mine) { d, m ->
        (d + m.values).sortedByDescending { it.timestamp }
    }

    /** Most recent photo of the user, for the "History" thumbnail. */
    val lastMine: Flow<Photo?> = mine.map { m -> m.values.maxByOrNull { it.timestamp } }

    fun addPhoto(photo: Photo) = mine.update { it + (photo.id to photo) }

    /** Registers a photo just captured (uri from [com.locket.droid.camera.CameraController.takePicture]). */
    fun addCapturedPhoto(uri: Uri, caption: String? = null): Photo {
        val photo = Photo(
            id = idFor(uri),
            uri = uri,
            senderName = ME,
            timestamp = System.currentTimeMillis(),
            isMine = true,
            caption = caption,
        )
        addPhoto(photo)
        return photo
    }

    /** Clears in-memory feed (demo + added). Files in MediaStore are not deleted. */
    fun clearAll() {
        demo.value = emptyList()
        mine.value = emptyMap()
    }

    /** Restores the demo feed (without touching the user's photos). */
    fun resetDemo() {
        demo.value = buildDemoFeed()
    }

    /** Re-reads Pictures/Locket from MediaStore. Needs no permission for app-owned files on API 29+. */
    suspend fun refreshLocal() = withContext(Dispatchers.IO) {
        val found = LinkedHashMap<String, Photo>()
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_ADDED,
        )
        val selection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            "${MediaStore.Images.Media.RELATIVE_PATH} LIKE ?"
        } else {
            "${MediaStore.Images.Media.DATA} LIKE ?"
        }
        val selectionArgs = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            arrayOf("Pictures/Locket%")
        } else {
            arrayOf("%/Pictures/Locket/%")
        }

        runCatching {
            resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                "${MediaStore.Images.Media.DATE_ADDED} DESC",
            )?.use { c ->
                val idCol = c.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val dateCol = c.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
                while (c.moveToNext()) {
                    val uri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, c.getLong(idCol),
                    )
                    found[idFor(uri)] = Photo(
                        id = idFor(uri),
                        uri = uri,
                        senderName = ME,
                        timestamp = c.getLong(dateCol) * 1000L,
                        isMine = true,
                    )
                }
            }
        }
        mine.update { found + it } // keep in-memory additions (captions) over disk entries
    }

    private fun idFor(uri: Uri): String =
        runCatching { "ms_${ContentUris.parseId(uri)}" }.getOrDefault(uri.toString())

    companion object {
        const val ME = "Me"

        // Names from the reference screenshots. Colors are avatar placeholders.
        val DEMO_FRIENDS = listOf(
            Friend("ava", "Ava", 0xFFE57373, true),
            Friend("jessica", "Jessica", 0xFFBA68C8, true),
            Friend("kyle", "Kyle", 0xFF64B5F6, false),
            Friend("alexandra", "Alexandra", 0xFF4DB6AC, true),
            Friend("dustin", "Dustin", 0xFFFFB74D, false),
            Friend("bobby", "Bobby", 0xFF8D6E63, true),
            Friend("sabrina", "Sabrina", 0xFFF06292, false),
            Friend("laurel", "Laurel", 0xFF81C784, false),
            Friend("greg", "Greg", 0xFF9575CD, false),
        )

        private const val MIN = 60_000L
        private const val HOUR = 60 * MIN

        private fun buildDemoFeed(): List<Photo> {
            val now = System.currentTimeMillis()
            fun p(i: Int, name: String, ago: Long, reactions: List<String>, caption: String? = null) =
                Photo("demo_$i", null, name, now - ago, false, reactions, caption, colorSeed = i)
            return listOf(
                p(0, "Jessica", 13 * MIN, listOf("🔥", "💖")),
                p(1, "Bobby", 47 * MIN, listOf("😍"), "Picnic day"),
                p(2, "Ava", 2 * HOUR, listOf("🔥")),
                p(3, "Kyle", 3 * HOUR, emptyList()),
                p(4, "Alexandra", 5 * HOUR, listOf("💖", "😍")),
                p(5, "Dustin", 8 * HOUR, emptyList(), "Gym"),
                p(6, "Sabrina", 20 * HOUR, listOf("🔥")),
                p(7, "Laurel", 26 * HOUR, listOf("💖")),
                p(8, "Greg", 30 * HOUR, emptyList()),
            )
        }
    }
}
