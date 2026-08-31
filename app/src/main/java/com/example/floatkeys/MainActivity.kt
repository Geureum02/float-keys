package com.example.floatkeys

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import rikka.shizuku.Shizuku

class MainActivity : Activity() {

    private lateinit var content: LinearLayout
    private lateinit var shizukuStatus: TextView
    private lateinit var overlayStatus: TextView
    private val shortcutButtons = mutableListOf<TextView>()

    private val permissionListener =
        Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
            if (requestCode == 4102 && grantResult == PackageManager.PERMISSION_GRANTED) {
                ShizukuBridge.bind(this)
                refresh()
            } else {
                refresh()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Shizuku.addRequestPermissionResultListener(permissionListener)
        buildUi()
    }

    override fun onResume() {
        super.onResume()
        ShizukuBridge.bind(this)
        refresh()
    }

    override fun onDestroy() {
        Shizuku.removeRequestPermissionResultListener(permissionListener)
        super.onDestroy()
    }

    private fun buildUi() {
        val scroll = ScrollView(this)
        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(Ui.dp(this@MainActivity, 28), Ui.dp(this@MainActivity, 42),
                Ui.dp(this@MainActivity, 28), Ui.dp(this@MainActivity, 42))
            setBackgroundColor(Color.rgb(247, 247, 248))
        }
        scroll.addView(content)
        setContentView(scroll)

        content.addView(Ui.title(this, "Float Keys"))
        content.addView(Ui.label(this, "Four movable desktop-style shortcuts for Android.", 16f).apply {
            setPadding(0, Ui.dp(this@MainActivity, 8), 0, Ui.dp(this@MainActivity, 28))
        })

        content.addView(sectionTitle("Setup"))
        overlayStatus = statusCard("Overlay permission")
        shizukuStatus = statusCard("Shizuku")
        content.addView(overlayStatus)
        gap(10)
        content.addView(shizukuStatus)
        gap(14)

        content.addView(Ui.actionButton(this, "Allow display over other apps").apply {
            setOnClickListener {
                startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                )
            }
        })
        gap(10)
        content.addView(Ui.actionButton(this, "Connect Shizuku").apply {
            setOnClickListener { connectShizuku() }
        })

        content.addView(sectionTitle("Shortcut keys").apply {
            setPadding(0, Ui.dp(this@MainActivity, 34), 0, Ui.dp(this@MainActivity, 12))
        })

        repeat(4) { slot ->
            val button = Ui.actionButton(this, "")
            button.setOnClickListener { editShortcut(slot) }
            shortcutButtons += button
            content.addView(button)
            gap(10)
        }

        content.addView(sectionTitle("Floating controls").apply {
            setPadding(0, Ui.dp(this@MainActivity, 34), 0, Ui.dp(this@MainActivity, 12))
        })

        content.addView(Ui.actionButton(this, "Start floating keys").apply {
            setOnClickListener { startOverlay() }
        })
        gap(10)
        content.addView(Ui.actionButton(this, "Stop floating keys").apply {
            setOnClickListener { stopService(Intent(this@MainActivity, OverlayService::class.java)) }
        })

        content.addView(Ui.label(
            this,
            "Tip: drag the centre circle anywhere. Tap it to open/close the arc. The × button closes the overlay.",
            14f
        ).apply {
            setPadding(0, Ui.dp(this@MainActivity, 22), 0, 0)
        })
    }

    private fun statusCard(label: String): TextView =
        Ui.actionButton(this, label).apply {
            isClickable = false
            isFocusable = false
        }

    private fun sectionTitle(text: String): TextView =
        TextView(this).apply {
            this.text = text
            textSize = 15f
            setTextColor(Color.rgb(60, 60, 66))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, Ui.dp(this@MainActivity, 12))
        }

    private fun gap(dp: Int) {
        content.addView(View(this), LinearLayout.LayoutParams(1, Ui.dp(this, dp)))
    }

    private fun refresh() {
        val overlay = Settings.canDrawOverlays(this)
        overlayStatus.text = if (overlay) "Overlay permission   ✓" else "Overlay permission   Needed"

        val binder = ShizukuBridge.isBinderAlive()
        val permission = ShizukuBridge.hasPermission()
        shizukuStatus.text = when {
            !binder -> "Shizuku   Start Shizuku first"
            !permission -> "Shizuku   Permission needed"
            ShizukuBridge.isReady() -> "Shizuku   Connected ✓"
            else -> "Shizuku   Connecting…"
        }

        shortcutButtons.forEachIndexed { i, button ->
            button.text = "Key ${i + 1}     ${ShortcutStore.get(this, i).label()}"
        }
    }

    private fun connectShizuku() {
        if (!ShizukuBridge.isBinderAlive()) {
            Toast.makeText(this, "Open Shizuku and start its service first.", Toast.LENGTH_LONG).show()
            return
        }

        if (!ShizukuBridge.hasPermission()) {
            try {
                Shizuku.requestPermission(4102)
            } catch (_: Throwable) {
                Toast.makeText(this, "Could not request Shizuku permission.", Toast.LENGTH_LONG).show()
            }
        } else {
            ShizukuBridge.bind(this)
            Toast.makeText(this, "Connecting…", Toast.LENGTH_SHORT).show()
        }
        refresh()
    }

    private fun startOverlay() {
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Grant overlay permission first.", Toast.LENGTH_LONG).show()
            return
        }
        if (!ShizukuBridge.isReady()) {
            Toast.makeText(this, "Connect Shizuku first.", Toast.LENGTH_LONG).show()
            return
        }

        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 9001)
        }

        ContextCompat.startForegroundService(this, Intent(this, OverlayService::class.java))
        moveTaskToBack(true)
    }

    private fun editShortcut(slot: Int) {
        val existing = ShortcutStore.get(this, slot)

        val wrapper = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(Ui.dp(this@MainActivity, 24), Ui.dp(this@MainActivity, 6),
                Ui.dp(this@MainActivity, 24), 0)
        }

        val modifiers = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        fun check(text: String, checked: Boolean): CheckBox =
            CheckBox(this).apply {
                this.text = text
                isChecked = checked
            }

        val ctrl = check("Ctrl", existing.ctrl)
        val shift = check("Shift", existing.shift)
        val alt = check("Alt", existing.alt)
        val meta = check("Meta", existing.meta)

        modifiers.addView(ctrl)
        modifiers.addView(shift)
        modifiers.addView(alt)
        modifiers.addView(meta)
        wrapper.addView(modifiers)

        val spinner = Spinner(this)
        val names = KeyNames.choices.map { it.first }
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, names)
        val selected = KeyNames.choices.indexOfFirst { it.second == existing.keyCode }.coerceAtLeast(0)
        spinner.setSelection(selected)
        wrapper.addView(spinner)

        AlertDialog.Builder(this)
            .setTitle("Assign Key ${slot + 1}")
            .setMessage("Choose modifiers, then choose the key.")
            .setView(wrapper)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save") { _, _ ->
                val pair = KeyNames.choices[spinner.selectedItemPosition]
                ShortcutStore.set(
                    this,
                    slot,
                    Shortcut(
                        ctrl = ctrl.isChecked,
                        shift = shift.isChecked,
                        alt = alt.isChecked,
                        meta = meta.isChecked,
                        keyCode = pair.second
                    )
                )
                refresh()
            }
            .show()
    }
}
