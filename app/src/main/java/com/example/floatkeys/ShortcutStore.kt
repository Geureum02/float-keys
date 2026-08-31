package com.example.floatkeys

import android.content.Context
import android.view.KeyEvent

enum class ActionType { TAP, HOLD }

data class Shortcut(
    val actionType: ActionType,
    val ctrl: Boolean,
    val shift: Boolean,
    val alt: Boolean,
    val keyCode: Int
) {
    fun label(): String {
        if (actionType == ActionType.HOLD) return "${KeyNames.displayName(keyCode)} (hold)"
        val p = mutableListOf<String>()
        if (ctrl) p += "Ctrl"
        if (shift) p += "Shift"
        if (alt) p += "Alt"
        p += KeyNames.displayName(keyCode)
        return p.joinToString(" + ")
    }
    fun keyCodes(): IntArray {
        val r = mutableListOf<Int>()
        if (ctrl) r += KeyEvent.KEYCODE_CTRL_LEFT
        if (shift) r += KeyEvent.KEYCODE_SHIFT_LEFT
        if (alt) r += KeyEvent.KEYCODE_ALT_LEFT
        r += keyCode
        return r.toIntArray()
    }
}

object ShortcutStore {
    private const val PREFS = "shortcut_prefs"
    private val defaults = listOf(
        Shortcut(ActionType.TAP, true, false, false, KeyEvent.KEYCODE_Z),
        Shortcut(ActionType.TAP, true, true, false, KeyEvent.KEYCODE_Z),
        Shortcut(ActionType.HOLD, false, false, false, KeyEvent.KEYCODE_ALT_LEFT),
        Shortcut(ActionType.TAP, false, false, false, KeyEvent.KEYCODE_I)
    )
    fun get(context: Context, slot: Int): Shortcut {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val d = defaults[slot]
        val type = runCatching { ActionType.valueOf(p.getString("${slot}_type", d.actionType.name) ?: d.actionType.name) }.getOrDefault(d.actionType)
        return Shortcut(type, p.getBoolean("${slot}_ctrl", d.ctrl), p.getBoolean("${slot}_shift", d.shift), p.getBoolean("${slot}_alt", d.alt), p.getInt("${slot}_key", d.keyCode))
    }
    fun set(context: Context, slot: Int, s: Shortcut) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString("${slot}_type", s.actionType.name)
            .putBoolean("${slot}_ctrl", s.ctrl)
            .putBoolean("${slot}_shift", s.shift)
            .putBoolean("${slot}_alt", s.alt)
            .putInt("${slot}_key", s.keyCode).apply()
    }
}

object KeyNames {
    val tapChoices: List<Pair<String, Int>> = buildList {
        ('A'..'Z').forEach { c -> add(c.toString() to KeyEvent.keyCodeFromString("KEYCODE_$c")) }
        ('0'..'9').forEach { c -> add(c.toString() to KeyEvent.keyCodeFromString("KEYCODE_$c")) }
        add("Enter" to KeyEvent.KEYCODE_ENTER); add("Esc" to KeyEvent.KEYCODE_ESCAPE); add("Tab" to KeyEvent.KEYCODE_TAB); add("Space" to KeyEvent.KEYCODE_SPACE)
        add("⌫ Backspace" to KeyEvent.KEYCODE_DEL); add("⌦ Delete" to KeyEvent.KEYCODE_FORWARD_DEL)
        add("←" to KeyEvent.KEYCODE_DPAD_LEFT); add("→" to KeyEvent.KEYCODE_DPAD_RIGHT); add("↑" to KeyEvent.KEYCODE_DPAD_UP); add("↓" to KeyEvent.KEYCODE_DPAD_DOWN)
        add("[" to KeyEvent.KEYCODE_LEFT_BRACKET); add("]" to KeyEvent.KEYCODE_RIGHT_BRACKET); add("-" to KeyEvent.KEYCODE_MINUS); add("=" to KeyEvent.KEYCODE_EQUALS)
        add("/" to KeyEvent.KEYCODE_SLASH); add("\" to KeyEvent.KEYCODE_BACKSLASH); add("," to KeyEvent.KEYCODE_COMMA); add("." to KeyEvent.KEYCODE_PERIOD)
        add(";" to KeyEvent.KEYCODE_SEMICOLON); add("'" to KeyEvent.KEYCODE_APOSTROPHE)
    }
    val holdChoices = listOf("Ctrl" to KeyEvent.KEYCODE_CTRL_LEFT, "Shift" to KeyEvent.KEYCODE_SHIFT_LEFT, "Alt" to KeyEvent.KEYCODE_ALT_LEFT)
    fun displayName(code: Int): String = (tapChoices + holdChoices).firstOrNull { it.second == code }?.first ?: KeyEvent.keyCodeToString(code).removePrefix("KEYCODE_")
}
