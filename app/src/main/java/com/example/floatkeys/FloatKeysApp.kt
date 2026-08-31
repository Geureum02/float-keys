package com.example.floatkeys

import android.app.Application
import rikka.shizuku.Shizuku

class FloatKeysApp : Application() {
    private val binderReceived = Shizuku.OnBinderReceivedListener {
        ShizukuBridge.bind(this)
    }

    override fun onCreate() {
        super.onCreate()
        Shizuku.addBinderReceivedListenerSticky(binderReceived)
    }

    override fun onTerminate() {
        Shizuku.removeBinderReceivedListener(binderReceived)
        super.onTerminate()
    }
}
