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
        if (actionType == ActionType.HOLD) {
            return "${KeyNames.displayName(keyCode)} (hold)"
        }

        val parts = mutableListOf<String>()
        if (ctrl) parts += "Ctrl"
        if (shift) parts += "Shift"
        if (alt) parts += "Alt"
        parts += KeyNames.displayName(keyCode)
        return parts.joinToString(" + ")
    }

    fun keyCodes(): IntArray {
        val result = mutableListOf<Int>()
        if (ctrl) result += KeyEvent.KEYCODE_CTRL_LEFT
        if (shift) result += KeyEvent.KEYCODE_SHIFT_LEFT
        if (alt) result += KeyEvent.KEYCODE_ALT_LEFT
        result += keyCode
        return result.toIntArray()
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
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val default = defaults[slot]
        val savedType = prefs.getString("${slot}_type", default.actionType.name)
            ?: default.actionType.name
        val type = runCatching { ActionType.valueOf(savedType) }
            .getOrDefault(default.actionType)

        return Shortcut(
            actionType = type,
            ctrl = prefs.getBoolean("${slot}_ctrl", default.ctrl),
            shift = prefs.getBoolean("${slot}_shift", default.shift),
            alt = prefs.getBoolean("${slot}_alt", default.alt),
            keyCode = prefs.getInt("${slot}_key", default.keyCode)
        )
    }

    fun set(context: Context, slot: Int, shortcut: Shortcut) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString("${slot}_type", shortcut.actionType.name)
            .putBoolean("${slot}_ctrl", shortcut.ctrl)
            .putBoolean("${slot}_shift", shortcut.shift)
            .putBoolean("${slot}_alt", shortcut.alt)
            .putInt("${slot}_key", shortcut.keyCode)
            .apply()
    }
}

object KeyNames {
    val tapChoices: List<Pair<String, Int>> = buildList {
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
        add("⌫ Backspace" to KeyEvent.KEYCODE_DEL)
        add("⌦ Delete" to KeyEvent.KEYCODE_FORWARD_DEL)
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

    val holdChoices: List<Pair<String, Int>> = listOf(
        "Ctrl" to KeyEvent.KEYCODE_CTRL_LEFT,
        "Shift" to KeyEvent.KEYCODE_SHIFT_LEFT,
        "Alt" to KeyEvent.KEYCODE_ALT_LEFT
    )

    fun displayName(code: Int): String =
        (tapChoices + holdChoices).firstOrNull { it.second == code }?.first
            ?: KeyEvent.keyCodeToString(code).removePrefix("KEYCODE_")
}
