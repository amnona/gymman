package com.example.gymtracker.ui.timer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.Manifest
import android.content.pm.PackageManager
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*

class TimerForegroundService : Service() {
    companion object {
        const val ACTION_START_TIMER = "com.example.gymtracker.action.START_TIMER"
        const val ACTION_STOP_TIMER = "com.example.gymtracker.action.STOP_TIMER"
        const val EXTRA_END_TIME = "extra_end_time_ms"

        const val CHANNEL_ID = "timer_channel"
        const val CHANNEL_NAME = "Timer"
        const val NOTIF_ID = 1001
    }

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var job: Job? = null
    private var endTimeMillis: Long? = null
    private var hasBeeped = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        // Always ensure we have a foreground notification (required for foreground service)
        createNotificationChannelIfNeeded()

        when (action) {
            ACTION_START_TIMER -> {
                endTimeMillis = intent.getLongExtra(EXTRA_END_TIME, -1L).takeIf { it > 0L }
                hasBeeped = false
                startForeground(NOTIF_ID, buildNotification("Starting..."))
                startLoop()
            }
            ACTION_STOP_TIMER -> {
                stopSelf()
            }
            else -> {
                // Default: show notification and continue
                try {
                    startForeground(NOTIF_ID, buildNotification("Timer service running..."))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        return START_STICKY
    }

    private fun startLoop() {
        job?.cancel()
        job = serviceScope.launch {
            try {
                while (isActive) {
                    val now = SystemClock.elapsedRealtime()
                    val remainingMs = (endTimeMillis ?: now) - now
                    val remainingSec = kotlin.math.floor(remainingMs / 1000.0).toLong()

                    // update notification
                    val notif = buildNotification("Remaining: ${remainingSec}s")
                    val nm = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (nm != null && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                            try {
                                nm.notify(NOTIF_ID, notif)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    } else {
                        if (nm != null) {
                            try {
                                nm.notify(NOTIF_ID, notif)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }

                    if (remainingSec <= 0L && !hasBeeped) {
                        hasBeeped = true
                        playBeeps()
                    }

                    if (remainingSec < -60L) {
                        stopSelf()
                        break
                    }

                    delay(500L)
                }
            } catch (_: CancellationException) {
                // cancelled
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

     private fun buildNotification(content: String) = NotificationCompat.Builder(this, CHANNEL_ID)
         .setSmallIcon(com.example.gymtracker.R.mipmap.ic_launcher)
         .setContentTitle("Countdown Timer")
         .setContentText(content)
         .setOngoing(true)
         .setOnlyAlertOnce(true)
         .build()

    private fun createNotificationChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val nm = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                if (nm != null) {
                    val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW)
                    nm.createNotificationChannel(channel)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun playBeeps() {
        // run short beep sequence
        try {
            val toneGenerator = android.media.ToneGenerator(android.media.AudioManager.STREAM_ALARM, 100)
            repeat(3) {
                toneGenerator.startTone(android.media.ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 150)
                Thread.sleep(200)
            }
            toneGenerator.release()
        } catch (_: Exception) {
            // ignore
        }
    }

    override fun onDestroy() {
        job?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }
}

