package com.locket.droid.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import android.widget.RemoteViews
import com.locket.droid.MainActivity
import com.locket.droid.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PhotoWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val uri = latestPhotoUri ?: getLatestMediaStorePhoto(context)
        updateWidgets(context, appWidgetManager, appWidgetIds, uri)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val uriString = intent.getStringExtra(EXTRA_PHOTO_URI)
        val uri = uriString?.let { Uri.parse(it) } ?: latestPhotoUri
        if (uri != null) {
            latestPhotoUri = uri
        }
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val ids = appWidgetManager.getAppWidgetIds(
            ComponentName(context, PhotoWidgetProvider::class.java)
        )
        if (ids.isNotEmpty()) {
            updateWidgets(context, appWidgetManager, ids, latestPhotoUri ?: getLatestMediaStorePhoto(context))
        }
    }

    companion object {
        const val EXTRA_PHOTO_URI = "extra_photo_uri"
        private var latestPhotoUri: Uri? = null

        fun requestUpdate(context: Context, photoUri: Uri? = null) {
            if (photoUri != null) {
                latestPhotoUri = photoUri
            }
            val intent = Intent(context, PhotoWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val ids = appWidgetManager.getAppWidgetIds(
                    ComponentName(context, PhotoWidgetProvider::class.java)
                )
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                photoUri?.let { putExtra(EXTRA_PHOTO_URI, it.toString()) }
            }
            context.sendBroadcast(intent)
        }

        private fun updateWidgets(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetIds: IntArray,
            photoUri: Uri?
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                val bitmap = photoUri?.let { loadScaledBitmap(context, it, 600) }
                for (widgetId in appWidgetIds) {
                    val views = RemoteViews(context.packageName, R.layout.widget_locket_photo)

                    val clickIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    val pendingIntent = PendingIntent.getActivity(
                        context,
                        0,
                        clickIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

                    if (bitmap != null) {
                        views.setImageViewBitmap(R.id.widget_image, bitmap)
                        views.setViewVisibility(R.id.widget_image, View.VISIBLE)
                        views.setViewVisibility(R.id.widget_placeholder, View.GONE)
                        views.setViewVisibility(R.id.widget_sender_badge, View.VISIBLE)
                        views.setTextViewText(R.id.widget_sender_name, "Locket")
                    } else {
                        views.setViewVisibility(R.id.widget_image, View.GONE)
                        views.setViewVisibility(R.id.widget_placeholder, View.VISIBLE)
                        views.setViewVisibility(R.id.widget_sender_badge, View.GONE)
                    }

                    appWidgetManager.updateAppWidget(widgetId, views)
                }
            }
        }

        private fun loadScaledBitmap(context: Context, uri: Uri, targetSize: Int): Bitmap? {
            return runCatching {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeStream(stream, null, options)
                    var sampleSize = 1
                    while (options.outWidth / (sampleSize * 2) >= targetSize &&
                        options.outHeight / (sampleSize * 2) >= targetSize
                    ) {
                        sampleSize *= 2
                    }
                    val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
                    context.contentResolver.openInputStream(uri)?.use { s2 ->
                        BitmapFactory.decodeStream(s2, null, decodeOpts)
                    }
                }
            }.getOrNull()
        }

        private fun getLatestMediaStorePhoto(context: Context): Uri? {
            return runCatching {
                val projection = arrayOf(MediaStore.Images.Media._ID)
                context.contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    "${MediaStore.Images.Media.RELATIVE_PATH} LIKE ?",
                    arrayOf("Pictures/Locket%"),
                    "${MediaStore.Images.Media.DATE_ADDED} DESC"
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                        ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                    } else null
                }
            }.getOrNull()
        }
    }
}
