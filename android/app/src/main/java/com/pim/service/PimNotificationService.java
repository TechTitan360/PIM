package com.pim.service;

import android.app.Notification;
import android.app.RemoteInput;
import android.content.Intent;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.pim.api.PimApiClient;
import com.pim.model.ChatRequest;
import com.pim.model.ChatResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * The "Spy" - Intercepts Instagram notifications and triggers AI responses.
 *
 * Flow:
 * 1. Notification arrives from Instagram
 * 2. Extract sender + message
 * 3. Send to backend for AI processing
 * 4. Receive response and auto-reply via RemoteInput
 */
public class PimNotificationService extends NotificationListenerService {

    private static final String TAG = "PimNotificationService";
    private static final String INSTAGRAM_PACKAGE = "com.instagram.android";

    // Toggle this to enable/disable auto-reply (safety switch)
    private static final boolean AUTO_REPLY_ENABLED = true;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "🚀 PIM Service started. Listening for notifications...");
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        // Only care about Instagram
        if (!INSTAGRAM_PACKAGE.equals(sbn.getPackageName())) {
            return;
        }

        Notification notification = sbn.getNotification();
        Bundle extras = notification.extras;

        // Extract the juicy bits
        CharSequence senderRaw = extras.getCharSequence(Notification.EXTRA_TITLE);
        CharSequence messageRaw = extras.getCharSequence(Notification.EXTRA_TEXT);

        if (senderRaw == null || messageRaw == null) {
            Log.w(TAG, "Notification missing sender or message. Ignoring.");
            return;
        }

        String sender = senderRaw.toString();
        String message = messageRaw.toString();

        Log.d(TAG, "📩 New DM from: " + sender);
        Log.d(TAG, "💬 Message: " + message);

        // Find the reply action (the magic button)
        Notification.Action replyAction = findReplyAction(notification);

        if (replyAction == null) {
            Log.w(TAG, "No reply action found. This notification can't be replied to directly.");
            return;
        }

        // Send to backend for AI magic
        sendToBackend(sender, message, replyAction);
    }

    /**
     * Finds the "Reply" action in the notification that has a RemoteInput.
     * This is how we'll send the auto-reply.
     */
    private Notification.Action findReplyAction(Notification notification) {
        if (notification.actions == null) return null;

        for (Notification.Action action : notification.actions) {
            if (action.getRemoteInputs() != null && action.getRemoteInputs().length > 0) {
                Log.d(TAG, "✅ Found reply action: " + action.title);
                return action;
            }
        }
        return null;
    }

    /**
     * Ships the message off to our Bun backend for processing.
     */
    private void sendToBackend(String sender, String message, Notification.Action replyAction) {
        ChatRequest request = new ChatRequest(sender, message);

        PimApiClient.getInstance().getApi().chat(request).enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String aiReply = response.body().getReply();
                    Log.d(TAG, "🤖 AI Response: " + aiReply);

                    if (AUTO_REPLY_ENABLED) {
                        sendReply(replyAction, aiReply);
                    } else {
                        Log.d(TAG, "⚠️ Auto-reply disabled. Would have sent: " + aiReply);
                    }
                } else {
                    Log.e(TAG, "❌ Backend error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                Log.e(TAG, "❌ Network error: " + t.getMessage());
            }
        });
    }

    /**
     * The "Action" - Uses RemoteInput to send the reply directly from notification.
     * This is the spicy part that makes it feel like magic.
     */
    private void sendReply(Notification.Action action, String replyText) {
        try {
            RemoteInput[] remoteInputs = action.getRemoteInputs();
            if (remoteInputs == null || remoteInputs.length == 0) {
                Log.e(TAG, "No RemoteInput found in action");
                return;
            }

            // Build the reply intent
            Intent intent = new Intent();
            Bundle bundle = new Bundle();

            for (RemoteInput remoteInput : remoteInputs) {
                bundle.putCharSequence(remoteInput.getResultKey(), replyText);
            }

            RemoteInput.addResultsToIntent(remoteInputs, intent, bundle);

            // Fire it off!
            action.actionIntent.send(this, 0, intent);
            Log.d(TAG, "✅ Reply sent successfully: " + replyText);

        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to send reply: " + e.getMessage());
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        // Could track dismissed notifications here if needed
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        Log.d(TAG, "🔗 Notification listener connected!");
    }

    @Override
    public void onListenerDisconnected() {
        super.onListenerDisconnected();
        Log.d(TAG, "🔌 Notification listener disconnected!");
    }
}
