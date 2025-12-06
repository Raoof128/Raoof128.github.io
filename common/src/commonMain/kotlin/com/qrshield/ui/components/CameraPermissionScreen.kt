package com.qrshield.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Camera permission state for UI handling.
 */
enum class CameraPermissionState {
    /** Permission not yet requested */
    NOT_REQUESTED,
    /** Waiting for user decision */
    REQUESTING,
    /** Permission granted */
    GRANTED,
    /** Permission denied (can request again) */
    DENIED,
    /** Permission permanently denied (must go to settings) */
    PERMANENTLY_DENIED,
    /** Camera not available on device */
    CAMERA_NOT_AVAILABLE
}

/**
 * Camera Permission Screen Component
 * 
 * Handles all camera permission states with appropriate UI and actions.
 * Provides a robust UX flow for camera permission on all platforms.
 * 
 * @param permissionState Current permission state
 * @param onRequestPermission Callback to request camera permission
 * @param onOpenSettings Callback to open device settings
 * @param onDismiss Callback when user dismisses without granting
 * @param modifier Optional modifier
 */
@Composable
fun CameraPermissionScreen(
    permissionState: CameraPermissionState,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (permissionState) {
                CameraPermissionState.NOT_REQUESTED -> {
                    PermissionRequestContent(
                        icon = "📷",
                        title = "Camera Access Required",
                        description = "QR-SHIELD needs camera access to scan QR codes and protect you from phishing attacks.",
                        primaryButtonText = "Grant Camera Access",
                        onPrimaryClick = onRequestPermission,
                        secondaryButtonText = "Not Now",
                        onSecondaryClick = onDismiss
                    )
                }
                
                CameraPermissionState.REQUESTING -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = "Waiting for permission...",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                
                CameraPermissionState.GRANTED -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "✅",
                            style = MaterialTheme.typography.displayLarge
                        )
                        Text(
                            text = "Camera Access Granted",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                CameraPermissionState.DENIED -> {
                    PermissionRequestContent(
                        icon = "🚫",
                        title = "Camera Permission Denied",
                        description = "Without camera access, QR-SHIELD cannot scan QR codes. Please grant permission to continue.",
                        primaryButtonText = "Try Again",
                        onPrimaryClick = onRequestPermission,
                        secondaryButtonText = "Skip for Now",
                        onSecondaryClick = onDismiss
                    )
                }
                
                CameraPermissionState.PERMANENTLY_DENIED -> {
                    PermissionRequestContent(
                        icon = "⚙️",
                        title = "Permission Required",
                        description = "Camera permission was denied. Please enable it in your device settings to use QR scanning.",
                        primaryButtonText = "Open Settings",
                        onPrimaryClick = onOpenSettings,
                        secondaryButtonText = "Maybe Later",
                        onSecondaryClick = onDismiss
                    )
                }
                
                CameraPermissionState.CAMERA_NOT_AVAILABLE -> {
                    PermissionRequestContent(
                        icon = "📵",
                        title = "Camera Not Available",
                        description = "This device doesn't have a camera available. You can still analyze URLs by entering them manually or importing QR code images.",
                        primaryButtonText = "Continue Without Camera",
                        onPrimaryClick = onDismiss,
                        secondaryButtonText = null,
                        onSecondaryClick = null
                    )
                }
            }
        }
    }
}

/**
 * Internal composable for permission request content.
 */
@Composable
private fun PermissionRequestContent(
    icon: String,
    title: String,
    description: String,
    primaryButtonText: String,
    onPrimaryClick: () -> Unit,
    secondaryButtonText: String?,
    onSecondaryClick: (() -> Unit)?
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Icon
        Text(
            text = icon,
            style = MaterialTheme.typography.displayLarge
        )
        
        // Title
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        
        // Description
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Primary Button
        Button(
            onClick = onPrimaryClick,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text(primaryButtonText)
        }
        
        // Secondary Button (optional)
        if (secondaryButtonText != null && onSecondaryClick != null) {
            TextButton(
                onClick = onSecondaryClick
            ) {
                Text(secondaryButtonText)
            }
        }
    }
}

/**
 * Privacy explanation card for camera permission.
 */
@Composable
fun CameraPrivacyCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "🔒 Your Privacy Matters",
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = "• Camera images are processed on-device only\n" +
                      "• No images are stored or transmitted\n" +
                      "• Camera is only active during scanning\n" +
                      "• You can revoke permission anytime",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
