package com.example.myfilimapp.utility.media

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.myfilimapp.R
import com.example.myfilimapp.ui.screens.base_screens.MainActivity

class MusicNotificationManager(private val context: Context) {

    companion object {
        const val NOTIFICATION_ID = 101
        const val CHANNEL_ID      = "music_playback_channel"
    }

    private val nm = context.getSystemService(Context.NOTIFICATION_SERVICE)
            as NotificationManager

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Music Playback",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Shows current playing song controls"
                    setShowBadge(false)
                }
            )
        }
    }

    fun buildNotification2(
        title     : String,
        artist    : String,
        isPlaying : Boolean,
        artwork   : Bitmap? = null
    ): Notification {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.music)
            .setContentTitle(title)
            .setContentText(artist)
            .setLargeIcon(artwork)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(isPlaying)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .addAction(
                R.drawable.previous,
                "Previous",
                actionIntent(MusicPlayerService.ACTION_PREV, 1)
            )
            .addAction(
                if (isPlaying) R.drawable.pause_button else R.drawable.play_button,
                if (isPlaying) "Pause" else "Play",
                actionIntent(
                    if (isPlaying) MusicPlayerService.ACTION_PAUSE
                    else           MusicPlayerService.ACTION_PLAY, 2
                )
            )
            .addAction(
                R.drawable.next_button,
                "Next",
                actionIntent(MusicPlayerService.ACTION_NEXT, 3)
            )
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .build()
    }


    fun buildNotification(
        title     : String,
        artist    : String,
        isPlaying : Boolean,
        artwork   : Bitmap? = null
    ): Notification {

        // ⚠️  IMPORTANT – notification action icons MUST be white/monochrome.
        //    If your custom drawables are full-colour they will render as blank.
        //    Replace the android.R.drawable.* references below with your own
        //    monochrome vector drawables once you have confirmed they are correct.
        val prevIcon  = android.R.drawable.ic_media_previous
        val pauseIcon = android.R.drawable.ic_media_pause
        val playIcon  = android.R.drawable.ic_media_play
        val nextIcon  = android.R.drawable.ic_media_next

        val playPauseIcon  = if (isPlaying) pauseIcon  else playIcon
        val playPauseLabel = if (isPlaying) "Pause"    else "Play"
        val playPauseAction = if (isPlaying) MusicPlayerService.ACTION_PAUSE
        else           MusicPlayerService.ACTION_PLAY

        return NotificationCompat.Builder(context, CHANNEL_ID)
            // smallIcon must also be a monochrome icon (white on transparent)
            .setSmallIcon(R.drawable.music)
            .setContentTitle(title)
            .setContentText(artist)
            .setLargeIcon(artwork)
            // Tapping the notification opens the app
            .setContentIntent(openAppIntent())
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            // Keep notification alive while playing; dismiss when stopped
            .setOngoing(isPlaying)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            // action index 0 – Previous
            .addAction(
                NotificationCompat.Action.Builder(
                    prevIcon,
                    "Previous",
                    actionIntent(MusicPlayerService.ACTION_PREV, 1)
                ).build()
            )
            // action index 1 – Play / Pause
            .addAction(
                NotificationCompat.Action.Builder(
                    playPauseIcon,
                    playPauseLabel,
                    actionIntent(playPauseAction, 2)
                ).build()
            )
            // action index 2 – Next
            .addAction(
                NotificationCompat.Action.Builder(
                    nextIcon,
                    "Next",
                    actionIntent(MusicPlayerService.ACTION_NEXT, 3)
                ).build()
            )
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    // show all 3 buttons in the collapsed (compact) view
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .build()
    }


    fun update(
        title     : String,
        artist    : String,
        isPlaying : Boolean,
        artwork   : Bitmap?
    ) {
        nm.notify(NOTIFICATION_ID, buildNotification(title, artist, isPlaying, artwork))
    }

    private fun openAppIntent() = PendingIntent.getActivity(
        context, 0,
        Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        },
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    private fun actionIntent(action: String, requestCode: Int) =
        PendingIntent.getService(
            context, requestCode,
            Intent(context, MusicPlayerService::class.java).apply {
                this.action = action
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
}