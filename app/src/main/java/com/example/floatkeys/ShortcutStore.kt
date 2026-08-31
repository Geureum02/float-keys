package com.example.floatkeys

import android.content.Context
import android.view.KeyEvent

data class Shortcut(
    val ctrl: Boolean,
    val shift: Boolean,
    val alt: Boolean,
    val meta: Boolean,
    val keyCode: Int
) {
    fun label(): String {
        val pieces = mutableListOf<String>()
        if (ctrl) pieces += "Ctrl"
        if (shift) pieces += "Shift"
        if (alt) pieces += "Alt"
        if (meta) pieces += "Meta"
        pieces += KeyNames.displayName(keyCode)
        return pieces.joinToString(" + ")
    }

    fun keyCodes(): IntArray {
        val result = mutableListOf<Int>()
        if (ctrl) result += KeyEvent.KEYCODE_CTRL_LEFT
        if (shift) result += KeyEvent.KEYCODE_SHIFT_LEFT
        if (alt) result += KeyEvent.KEYCODE_ALT_LEFT
        if (meta) result += KeyEvent.KEYCODE_META_LEFT
        result += keyCode
        return result.toIntArray()
    }
}

object ShortcutStore {
    private const val PREFS = "shortcut_prefs"

    private val defaults = listOf(
        Shortcut(ctrl = true, shift = false, alt = false, meta = false, keyCode = KeyEvent.KEYCODE_Z),
        Shortcut(ctrl = true, shift = true, alt = false, meta = false, keyCode = KeyEvent.KEYCODE_Z),
        Shortcut(ctrl = false, shift = false, alt = false, meta = false, keyCode = KeyEvent.KEYCODE_V),
        Shortcut(ctrl = false, shift = false, alt = false, meta = false, keyCode = KeyEvent.KEYCODE_I)
    )

    fun get(context: Context, slot: Int): Shortcut {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val d = defaults[slot]
        return Shortcut(
            ctrl = p.getBoolean("${slot}_ctrl", d.ctrl),
            shift = p.getBoolean("${slot}_shift", d.shift),
            alt = p.getBoolean("${slot}_alt", d.alt),
            meta = p.getBoolean("${slot}_meta", d.meta),
            keyCode = p.getInt("${slot}_key", d.keyCode)
        )
    }

    fun set(context: Context, slot: Int, shortcut: Shortcut) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean("${slot}_ctrl", shortcut.ctrl)
            .putBoolean("${slot}_shift", shortcut.shift)
            .putBoolean("${slot}_alt", shortcut.alt)
            .putBoolean("${slot}_meta", shortcut.meta)
            .putInt("${slot}_key", shortcut.keyCode)
            .apply()
    }
}

object KeyNames {
    val choices: List<Pair<String, Int>> = buildList {
        ('A'..'Z').forEach { c ->
            add(c.toString() to KeyEvent.keyCodeFromString("KEYCODE_$c"))
        }
        ('0'..'9').forEach { c ->
            add(c.toString() to KeyEvent.keyCodeFromString("KEYCODE_$c"))
        }
        add("Enter" to KeyEvent.KEYCODE_ENTER)
        add("Esc" to KeyEvent.KEYCODE_ESCAPE)
        add("Tab" to KeyEvent.KEYCODE_TAB)
        add("Space" to KeyEvent.KEYCODE_SPACE)
        add("Backspace" to KeyEvent.KEYCODE_DEL)
        add("Delete" to KeyEvent.KEYCODE_FORWARD_DEL)
        add("←" to KeyEvent.KEYCODE_DPAD_LEFT)
        add("→" to KeyEvent.KEYCODE_DPAD_RIGHT)
        add("↑" to KeyEvent.KEYCODE_DPAD_UP)
        add("↓" to KeyEvent.KEYCODE_DPAD_DOWN)
        add("[" to KeyEvent.KEYCODE_LEFT_BRACKET)
        add("]" to KeyEvent.KEYCODE_RIGHT_BRACKET)
        add("-" to KeyEvent.KEYCODE_MINUS)
        add("=" to KeyEvent.KEYCODE_EQUALS)
        add("/" to KeyEvent.KEYCODE_SLASH)
        add("\\" to KeyEvent.KEYCODE_BACKSLASH)
        add("," to KeyEvent.KEYCODE_COMMA)
        add("." to KeyEvent.KEYCODE_PERIOD)
        add(";" to KeyEvent.KEYCODE_SEMICOLON)
        add("'" to KeyEvent.KEYCODE_APOSTROPHE)
    }

    fun displayName(code: Int): String =
        choices.firstOrNull { it.second == code }?.first
            ?: KeyEvent.keyCodeToString(code).removePrefix("KEYCODE_")
}
