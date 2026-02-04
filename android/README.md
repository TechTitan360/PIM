# PIM Android App

The "Body" - Intercepts Instagram notifications and triggers auto-replies.

## Setup

1. Open this folder in Android Studio
2. Sync Gradle files
3. Update `BASE_URL` in `PimApiClient.java` to point to your backend

## Building

```bash
./gradlew assembleDebug
```

APK will be at `app/build/outputs/apk/debug/app-debug.apk`

## Permissions Required

After installing, you MUST grant notification access:
1. Open PIM app
2. Tap "Grant Permission"
3. Find "PIM" in the list and enable it

## Testing

1. Make sure backend is running
2. Install APK on your phone
3. Grant notification access
4. Have someone send you an Instagram DM
5. Check Logcat for output (filter by tag `PimNotificationService`)

## Safety Switch

In `PimNotificationService.java`, there's a toggle:
```java
private static final boolean AUTO_REPLY_ENABLED = true;
```

Set to `false` to disable auto-replies while still logging messages.

## Important Notes

- The app needs to remain in memory (don't force close it)
- Battery optimization should be disabled for PIM
- Some OEMs (Xiaomi, Huawei) aggressively kill background services - check device settings
