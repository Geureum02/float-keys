package com.example.floatkeys

import android.content.Context
import androidx.annotation.Keep
import java.io.BufferedReader
import java.io.InputStreamReader

@Keep
class PrivilegedInputService : IPrivilegedInput.Stub {

    constructor()

    @Keep
    constructor(context: Context)

    override fun sendCombination(keyCodes: IntArray): Boolean {
        if (keyCodes.isEmpty()) return false
        return try {
            val args = mutableListOf("/system/bin/input", "keycombination")
            keyCodes.forEach { args += it.toString() }
            val process = ProcessBuilder(args)
                .redirectErrorStream(true)
                .start()

            BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                while (reader.readLine() != null) { /* drain output */ }
            }
            process.waitFor() == 0
        } catch (_: Throwable) {
            false
        }
    }

    override fun destroy() {
        System.exit(0)
    }
}
