# iOS Camera Permission Handling

## Expected Behavior

When a user changes camera permissions in iOS Settings and returns to the app, **the app will restart**. This is **intentional iOS behavior**, not a bug.

### Why This Happens

According to Apple's privacy and security model:

1. **iOS terminates the app (SIGKILL)** when privacy-related permissions (camera, photos, contacts, etc.) are changed in Settings
2. This ensures the app cannot:
   - Continue running with outdated authorization states
   - Retain data it was previously authorized to access but no longer is
3. The app "launches from scratch" when the user returns from Settings

Source: [Stack Overflow - iOS app killed when returning from Settings](https://stackoverflow.com/questions/31962930)

## How We Handle This

Our app implements best practices to provide a smooth user experience:

### 1. Singleton ViewModel
- `ScannerViewModel.shared` persists state across potential app lifecycle changes
- Permission status is checked when app becomes active

### 2. Scene Phase Observer
```swift
.onChange(of: scenePhase) { oldValue, newValue in
    if newValue == .active {
        Task {
            await viewModel.checkCameraPermission()
        }
    }
}
```

### 3. Inline Permission Request
- No alerts that take users out of the app unnecessarily
- Clear, glass-styled overlay with "Open Settings" button
- Automatically updates when permission is granted

### 4. State Restoration (Future Enhancement)
For even smoother UX, we could implement:
- Saving current scan count and results before termination  
- Restoring user's position in the app on relaunch

## User Flow

1. User denies camera permission (or hasn't granted it)
2. App shows inline permission overlay
3. User taps "Open Settings"
4. **iOS terminates the app**
5. User grants permission in Settings
6. User returns to app (fresh launch)
7. App automatically checks permission and starts camera

This is the expected and secure behavior on iOS for privacy-sensitive permissions.
