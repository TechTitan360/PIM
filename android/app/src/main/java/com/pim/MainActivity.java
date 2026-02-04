package com.pim;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

import java.util.Set;

/**
 * Main Activity - Simple UI to check permission status and navigate to settings.
 */
public class MainActivity extends AppCompatActivity {

    private TextView statusText;
    private Button permissionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.status_text);
        permissionButton = findViewById(R.id.permission_button);

        permissionButton.setOnClickListener(v -> openNotificationSettings());

        updateStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
    }

    private void updateStatus() {
        if (isNotificationServiceEnabled()) {
            statusText.setText("✅ PIM is ACTIVE\n\nListening for Instagram DMs...\n\n⚡ Your digital twin is ready.");
            permissionButton.setText("Settings");
        } else {
            statusText.setText("⚠️ Permission Required\n\nPIM needs notification access to intercept Instagram DMs.\n\nTap below to enable.");
            permissionButton.setText("Grant Permission");
        }
    }

    private boolean isNotificationServiceEnabled() {
        Set<String> enabledPackages = NotificationManagerCompat.getEnabledListenerPackages(this);
        return enabledPackages.contains(getPackageName());
    }

    private void openNotificationSettings() {
        Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
        startActivity(intent);
    }
}
