package com.example.myfilimapp.utility.media

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.os.Build
import androidx.core.app.ServiceCompat
import com.example.myfilimapp.model.request.MusicCommand
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MusicPlayerService : Service(){
    @Inject
    lateinit var musicController: MusicController
    private lateinit var notificationManager: MusicNotificationManager
    companion object{
        const val NOTIFICATION_ID = 101
        const val ACTION_PLAY     = "ACTION_PLAY"
        const val ACTION_PAUSE    = "ACTION_PAUSE"
        const val ACTION_NEXT     = "ACTION_NEXT"
        const val ACTION_PREV     = "ACTION_PREV"

        var currentTitle   : String  = ""
        var currentArtist  : String  = ""
        var currentArtwork : Bitmap? = null
        var isPlaying      : Boolean = false
        fun refresh(context: Context) {
            context.startService(
                Intent(context, MusicPlayerService::class.java)
            )
        }
    }
    override fun onCreate() {
        super.onCreate()
        notificationManager = MusicNotificationManager(this)
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Notification button tapped → emit to ViewModel via MusicController
        when (intent?.action) {
            ACTION_PLAY  -> musicController.command.tryEmit(MusicCommand.PLAY)
            ACTION_PAUSE -> musicController.command.tryEmit(MusicCommand.PAUSE)
            ACTION_NEXT  -> musicController.command.tryEmit(MusicCommand.NEXT)
            ACTION_PREV  -> musicController.command.tryEmit(MusicCommand.PREVIOUS)
        }

        val notification = notificationManager.buildNotification(
            title     = currentTitle,
            artist    = currentArtist,
            isPlaying = isPlaying,
            artwork   = currentArtwork
        )

        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            notification,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            else 0
        )

        return START_STICKY
    }


    override fun onBind(intent: Intent?) = null
    override fun onTaskRemoved(rootIntent: Intent?) {
        if (!isPlaying) stopSelf()
    }

    override fun onDestroy() {
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }
}