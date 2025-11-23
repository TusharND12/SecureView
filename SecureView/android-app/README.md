# SecureView Android Application

Android companion app for receiving intrusion alerts from SecureView desktop application.

## Building

1. Open project in Android Studio
2. Add `google-services.json` to `app/` directory
3. Build → Make Project

## Installation

1. Connect Android device or start emulator
2. Run → Run 'app'

## Features

- FCM token generation and display
- Intrusion alert notifications
- Intruder image display
- Remote action support (future)

## Configuration

1. Launch app to get FCM token
2. Copy token to desktop app configuration
3. Ensure Firebase project matches desktop app

## Project Structure

```
app/src/main/
├── java/com/secureview/android/
│   ├── MainActivity.java                    # Main activity
│   ├── IntrusionAlertActivity.java          # Alert display
│   ├── service/
│   │   └── SecureViewMessagingService.java  # FCM service
│   └── receiver/
│       └── BootReceiver.java                # Boot receiver
└── res/                                      # Resources
```

## Permissions

- INTERNET: For FCM communication
- POST_NOTIFICATIONS: For push notifications
- RECEIVE_BOOT_COMPLETED: For token refresh on boot

## Troubleshooting

See main README.md troubleshooting section.

