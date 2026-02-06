package com.example.pim_main.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.pim_main.service.PimForegroundService
import com.example.pim_main.worker.BackendKeepAliveWorker

/**
 * Boot Receiver
 *
 * This receiver starts PIM services when the device boots up.
 * Ensures PIM keeps running even after a device restart.
 */
class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {

            Log.d(TAG, "📱 Device booted - Starting PIM services...")

            // Start the foreground service
            try {
                PimForegroundService.start(context)
                Log.d(TAG, "✅ Foreground service started")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to start foreground service: ${e.message}")
            }

            // Schedule the keep-alive worker as backup
            try {
                BackendKeepAliveWorker.schedule(context)
                Log.d(TAG, "✅ Keep-alive worker scheduled")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to schedule worker: ${e.message}")
            }
        }
    }
}
