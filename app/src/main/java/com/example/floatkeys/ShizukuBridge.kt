package com.example.floatkeys

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import rikka.shizuku.Shizuku

object ShizukuBridge {
    @Volatile private var service: IPrivilegedInput? = null
    @Volatile private var binding = false
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) { service = IPrivilegedInput.Stub.asInterface(binder); binding = false }
        override fun onServiceDisconnected(name: ComponentName?) { service = null; binding = false }
    }
    fun isBinderAlive() = try { Shizuku.pingBinder() } catch (_: Throwable) { false }
    fun hasPermission() = try { Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED } catch (_: Throwable) { false }
    fun isReady() = isBinderAlive() && hasPermission() && service != null
    fun bind(context: Context) {
        if (binding || service != null || !isBinderAlive() || !hasPermission()) return
        binding = true
        try {
            val args = Shizuku.UserServiceArgs(ComponentName(context, PrivilegedInputService::class.java)).processNameSuffix("input").debuggable(false).version(2)
            Shizuku.bindUserService(args, connection)
        } catch (_: Throwable) { binding = false }
    }
    fun send(s: Shortcut) = try { service?.sendCombination(s.keyCodes()) == true } catch (_: Throwable) { false }
    fun keyDown(code: Int) = try { service?.keyDown(code) == true } catch (_: Throwable) { false }
    fun keyUp(code: Int) = try { service?.keyUp(code) == true } catch (_: Throwable) { false }
}
