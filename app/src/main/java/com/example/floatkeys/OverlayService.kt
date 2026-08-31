package com.example.floatkeys

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.TextView
import android.widget.Toast
import kotlin.math.abs

class OverlayService : Service() {

    private lateinit var wm: WindowManager
    private lateinit var hub: TextView
    private lateinit var hubParams: WindowManager.LayoutParams
    private val keys = mutableListOf<Pair<TextView, WindowManager.LayoutParams>>()
    private var closeButton: Pair<TextView, WindowManager.LayoutParams>? = null
    private var expanded = true

    private var hubX = 0
    private var hubY = 0

    override fun onCreate() {
        super.onCreate()
        createNotification()
        wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val screen = resources.displayMetrics
        hubX = screen.widthPixels - Ui.dp(this, 92)
        hubY = screen.heightPixels - Ui.dp(this, 180)

        addHub()
        setExpanded(true)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        removeAll()
        super.onDestroy()
    }

    private fun createNotification() {
        val channelId = "float_keys"
        if (Build.VERSION.SDK_INT >= 26) {
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(
                NotificationChannel(channelId, "Float Keys", NotificationManager.IMPORTANCE_LOW)
            )
        }

        val stopIntent = Intent(this, OverlayService::class.java).apply {
            action = "STOP"
        }
        val pendingStop = PendingIntent.getService(
            this, 11, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = Notification.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_manage)
            .setContentTitle("Float Keys is active")
            .setContentText("Floating shortcut controls are on.")
            .addAction(Notification.Action.Builder(null, "Stop", pendingStop).build())
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(
                73,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(73, notification)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP") {
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    private fun baseParams(sizeDp: Int): WindowManager.LayoutParams {
        val size = Ui.dp(this, sizeDp)
        return WindowManager.LayoutParams(
            size,
            size,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }
    }

    private fun roundButton(text: String, sizeDp: Int, color: Int): TextView =
        TextView(this).apply {
            this.text = text
            textSize = if (text.length <= 2) 17f else 12f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            background = Ui.circle(color)
            elevation = Ui.dp(this@OverlayService, 6).toFloat()
        }

    private fun addHub() {
        hub = roundButton("⋮", 64, Color.rgb(32, 33, 37))
        hubParams = baseParams(64).apply {
            x = hubX
            y = hubY
        }
        wm.addView(hub, hubParams)

        var downRawX = 0f
        var downRawY = 0f
        var startX = 0
        var startY = 0
        var moved = false

        hub.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downRawX = event.rawX
                    downRawY = event.rawY
                    startX = hubParams.x
                    startY = hubParams.y
                    moved = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - downRawX).toInt()
                    val dy = (event.rawY - downRawY).toInt()
                    if (abs(dx) > 5 || abs(dy) > 5) moved = true

                    hubParams.x = startX + dx
                    hubParams.y = startY + dy
                    hubX = hubParams.x
                    hubY = hubParams.y
                    wm.updateViewLayout(hub, hubParams)
                    repositionArc()
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!moved) setExpanded(!expanded)
                    true
                }
                else -> false
            }
        }
    }

    private fun setExpanded(show: Boolean) {
        expanded = show
        if (show) {
            if (keys.isEmpty()) {
                repeat(4) { slot ->
                    val shortcut = ShortcutStore.get(this, slot)
                    val view = roundButton(shortLabel(shortcut), 58, Color.rgb(62, 106, 225))
                    val params = baseParams(58)
                    view.setOnClickListener {
                        val current = ShortcutStore.get(this, slot)
                        if (!ShizukuBridge.send(current)) {
                            Toast.makeText(this, "Shortcut failed. Reconnect Shizuku.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    wm.addView(view, params)
                    keys += view to params
                }

                val close = roundButton("×", 42, Color.rgb(90, 90, 96))
                val closeParams = baseParams(42)
                close.setOnClickListener { stopSelf() }
                wm.addView(close, closeParams)
                closeButton = close to closeParams
            } else {
                keys.forEach { it.first.visibility = View.VISIBLE }
                closeButton?.first?.visibility = View.VISIBLE
            }
            repositionArc()
        } else {
            keys.forEach { it.first.visibility = View.GONE }
            closeButton?.first?.visibility = View.GONE
        }
    }

    private fun shortLabel(s: Shortcut): String {
        val mods = buildString {
            if (s.ctrl) append("⌃")
            if (s.shift) append("⇧")
            if (s.alt) append("⌥")
            if (s.meta) append("◆")
        }
        return mods + KeyNames.displayName(s.keyCode)
    }

    private fun repositionArc() {
        if (keys.isEmpty()) return

        val offsets = listOf(
            -82 to 10,
            -72 to -62,
            -18 to -102,
            52 to -72
        )

        keys.forEachIndexed { index, (_, params) ->
            params.x = hubX + Ui.dp(this, offsets[index].first)
            params.y = hubY + Ui.dp(this, offsets[index].second)
            try { wm.updateViewLayout(keys[index].first, params) } catch (_: Throwable) {}
        }

        closeButton?.let { (view, params) ->
            params.x = hubX + Ui.dp(this, 68)
            params.y = hubY + Ui.dp(this, 8)
            try { wm.updateViewLayout(view, params) } catch (_: Throwable) {}
        }
    }

    private fun removeAll() {
        try { wm.removeView(hub) } catch (_: Throwable) {}
        keys.forEach { (v, _) -> try { wm.removeView(v) } catch (_: Throwable) {} }
        closeButton?.let { (v, _) -> try { wm.removeView(v) } catch (_: Throwable) {} }
        keys.clear()
        closeButton = null
    }
}
