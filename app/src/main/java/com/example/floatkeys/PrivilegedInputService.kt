package com.example.floatkeys

import android.content.Context
import android.os.SystemClock
import android.view.InputDevice
import android.view.InputEvent
import android.view.KeyEvent
import androidx.annotation.Keep
import java.io.BufferedReader
import java.io.InputStreamReader

@Keep
class PrivilegedInputService : IPrivilegedInput.Stub {
    constructor()
    @Keep constructor(context: Context)

    override fun sendCombination(keyCodes: IntArray): Boolean {
        if (keyCodes.isEmpty()) return false
        return try {
            val args = mutableListOf("/system/bin/input", "keycombination")
            keyCodes.forEach { args += it.toString() }
            val p = ProcessBuilder(args).redirectErrorStream(true).start()
            BufferedReader(InputStreamReader(p.inputStream)).use { r -> while (r.readLine() != null) {} }
            p.waitFor() == 0
        } catch (_: Throwable) { false }
    }

    override fun keyDown(keyCode: Int): Boolean = inject(keyCode, KeyEvent.ACTION_DOWN)
    override fun keyUp(keyCode: Int): Boolean = inject(keyCode, KeyEvent.ACTION_UP)

    private fun inject(keyCode: Int, action: Int): Boolean {
        return try {
            val now = SystemClock.uptimeMillis()
            val event = KeyEvent(now, now, action, keyCode, 0, 0, KeyEvent.KEYCODE_UNKNOWN, 0, 0, InputDevice.SOURCE_KEYBOARD)
            val cls = Class.forName("android.hardware.input.InputManager")
            val getInstance = cls.getDeclaredMethod("getInstance").apply { isAccessible = true }
            val manager = getInstance.invoke(null)
            val method = cls.getDeclaredMethod("injectInputEvent", InputEvent::class.java, Int::class.javaPrimitiveType).apply { isAccessible = true }
            (method.invoke(manager, event, 0) as? Boolean) == true
        } catch (_: Throwable) { false }
    }

    override fun destroy() { System.exit(0) }
}
