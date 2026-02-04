package com.example.pim_main.service

import android.app.Notification
import android.app.RemoteInput
import android.content.Intent
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.pim_main.api.PimApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * PIM Notification Listener Service
 *
 * This is the "Spy" - it listens for Instagram DM notifications,
 * sends them to our backend "Brain", and auto-replies using the
 * notification's direct reply action.
 */
class PimNotificationService : NotificationListenerService() {

    companion object {
        private const val TAG = "PimNotificationService"
        private const val INSTAGRAM_PACKAGE = "com.instagram.android"

        // Cooldown period per sender (prevent rapid-fire replies / feedback loops)
        private const val REPLY_COOLDOWN_MS = 10_000L // 10 seconds
    }

    // Coroutine scope for async operations
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Track processed messages to avoid duplicates and feedback loops
    private val processedMessages = mutableSetOf<String>()
    private val lastReplyTime = mutableMapOf<String, Long>()

    // Track our own replies so we don't reply to ourselves
    private val sentReplies = mutableSetOf<String>()

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "🚀 PIM Notification Service created")
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        Log.d(TAG, "💀 PIM Notification Service destroyed")
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "✅ Notification Listener connected - PIM is now watching!")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d(TAG, "❌ Notification Listener disconnected")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        Log.d(TAG, "🔔 Notification received from: ${sbn?.packageName ?: "null"}")

        sbn ?: return

        // Only process Instagram notifications
        if (sbn.packageName != INSTAGRAM_PACKAGE) {
            Log.d(TAG, "⏭️ Skipping non-Instagram notification: ${sbn.packageName}")
            return
        }

        Log.d(TAG, "📸 Instagram notification detected!")

        val notification = sbn.notification ?: return
        val extras = notification.extras ?: return

        // Extract sender and message
        val sender = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        val message = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()

        Log.d(TAG, "📝 Raw data - Sender: '$sender', Message: '$message'")

        // Skip if missing data or if it's a group summary
        if (sender.isNullOrBlank() || message.isNullOrBlank()) {
            Log.w(TAG, "⚠️ Missing sender or message, skipping")
            return
        }
        if (extras.getBoolean("android.isGroupSummary", false)) {
            Log.d(TAG, "⏭️ Skipping group summary notification")
            return
        }

        Log.d(TAG, "📩 Instagram DM from $sender: $message")

        // === ANTI-FEEDBACK LOOP CHECKS ===

        // 1. Check if this is our own reply (we sent it)
        val messageKey = "$sender:$message"
        if (sentReplies.contains(message.lowercase().trim())) {
            Log.d(TAG, "🔄 Skipping - this is our own reply")
            return
        }

        // 2. Check if we already processed this exact message
        if (processedMessages.contains(messageKey)) {
            Log.d(TAG, "🔄 Skipping - already processed this message")
            return
        }

        // 3. Check cooldown per sender (prevent rapid replies)
        val now = System.currentTimeMillis()
        val lastReply = lastReplyTime[sender] ?: 0
        if (now - lastReply < REPLY_COOLDOWN_MS) {
            Log.d(TAG, "⏳ Skipping - cooldown active for $sender (${(REPLY_COOLDOWN_MS - (now - lastReply)) / 1000}s left)")
            return
        }

        // Mark as processed
        processedMessages.add(messageKey)

        // Cleanup old entries (keep last 100)
        if (processedMessages.size > 100) {
            val toRemove = processedMessages.take(processedMessages.size - 100)
            processedMessages.removeAll(toRemove.toSet())
        }

        // Find the reply action
        val replyAction = findReplyAction(notification)
        if (replyAction == null) {
            Log.w(TAG, "⚠️ No reply action found for this notification")
            return
        }

        // Process in background
        serviceScope.launch {
            processMessage(sender, message, replyAction, sbn.key)
        }
    }

    /**
     * Find the notification action that has a RemoteInput (the reply button)
     */
    private fun findReplyAction(notification: Notification): Notification.Action? {
        notification.actions?.forEach { action ->
            action.remoteInputs?.forEach { remoteInput ->
                if (remoteInput.allowFreeFormInput) {
                    Log.d(TAG, "🔍 Found reply action: ${action.title}")
                    return action
                }
            }
        }
        return null
    }

    /**
     * Send message to backend and auto-reply
     */
    private suspend fun processMessage(
        sender: String,
        message: String,
        replyAction: Notification.Action,
        notificationKey: String
    ) {
        Log.d(TAG, "🧠 Sending to PIM backend...")

        // Get AI reply from backend
        val reply = PimApi.sendMessage(sender, message)

        if (reply.isNullOrBlank()) {
            Log.e(TAG, "❌ Failed to get reply from backend")
            return
        }

        Log.d(TAG, "🤖 AI Reply: $reply")

        // Track that we're sending this reply (to avoid replying to ourselves)
        sentReplies.add(reply.lowercase().trim())

        // Keep sentReplies small (last 50 replies)
        if (sentReplies.size > 50) {
            val toRemove = sentReplies.take(sentReplies.size - 50)
            sentReplies.removeAll(toRemove.toSet())
        }

        // Update cooldown timestamp for this sender
        lastReplyTime[sender] = System.currentTimeMillis()

        // Send the reply
        sendReply(replyAction, reply)

        // Dismiss the notification to prevent re-processing
        try {
            cancelNotification(notificationKey)
            Log.d(TAG, "🗑️ Notification dismissed")
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ Could not dismiss notification: ${e.message}")
        }
    }

    /**
     * Send reply using the notification's RemoteInput
     */
    private fun sendReply(action: Notification.Action, replyText: String) {
        try {
            val remoteInput = action.remoteInputs?.firstOrNull { it.allowFreeFormInput }
            if (remoteInput == null) {
                Log.e(TAG, "❌ No RemoteInput found")
                return
            }

            // Create the reply intent
            val replyIntent = Intent()
            val bundle = Bundle().apply {
                putCharSequence(remoteInput.resultKey, replyText)
            }
            RemoteInput.addResultsToIntent(action.remoteInputs, replyIntent, bundle)

            // Send it!
            action.actionIntent.send(applicationContext, 0, replyIntent)

            Log.d(TAG, "✅ Reply sent successfully: $replyText")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to send reply: ${e.message}", e)
        }
    }
}
