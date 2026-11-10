package omega.sunkey.sunkensonic.server

import android.app.Service
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import io.javalin.Javalin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import omega.sunkey.sunkensonic.server.endpoints.setBeforeHandler

class ServerService : Service() {
    private var server: Javalin? = null
    private val serverScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val channelId = "sunkensonic_server_channel"
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (server == null) start(intent?.getIntExtra("PORT", 4040) ?: 4040, intent?.getStringExtra("KEY") ?: "34b804c90e8b21d4765546bd0271ff98")
        val notification = notify()
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(1, notification)
        }

        return START_STICKY
    }
    private fun start(port: Int, lastFmKey: String) {
        server = Javalin.create { config ->
            config.showJavalinBanner = false
        }.apply {
            setBeforeHandler()
            setPaths(applicationContext, serverScope, lastFmKey)
        }.start("0.0.0.0", port)

        Log.i("ServerService", "started")
    }
    private fun notify(): Notification {
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Sunkensonic")
            .setContentText("Server running: 4040")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .build()
    }
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            channelId,
            "Sunkensonic Status",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        super.onDestroy()
        serverScope.cancel()
        server?.stop()
        server = null
        Log.i("ServerService", "stopped")
    }

    override fun onBind(p0: Intent?): IBinder? = null
}