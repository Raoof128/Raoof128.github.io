/*
 * Copyright 2025-2026 QR-SHIELD Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qrshield.android.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.qrshield.android.R
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.qrshield.sandbox.SandboxConfig
import com.qrshield.sandbox.SandboxEvent
import com.qrshield.sandbox.SandboxOverlay
import com.qrshield.sandbox.SandboxState

/**
 * Sandbox WebView Composable
 *
 * Provides an isolated, secure environment for previewing suspicious URLs.
 * All dangerous features are explicitly disabled to prevent security risks.
 *
 * ## Security Features
 * - JavaScript disabled (prevents XSS, drive-by downloads)
 * - Cookies blocked (prevents tracking, session hijacking)
 * - DOM storage disabled (prevents data persistence)
 * - Form data not saved (prevents credential theft)
 * - External intents blocked (prevents app launches)
 * - Redirects capped (prevents redirect loops)
 * - Persistent safety overlay (user awareness)
 *
 * @param url The URL to preview (will be validated and sanitized)
 * @param onClose Callback when user closes the sandbox
 * @param config Security configuration (defaults to maximum security)
 * @param modifier Compose modifier
 *
 * @author QR-SHIELD Security Team
 * @since 1.4.0
 */
@Composable
fun SandboxWebView(
    url: String,
    onClose: () -> Unit,
    config: SandboxConfig = SandboxConfig.MAXIMUM_SECURITY,
    modifier: Modifier = Modifier
) {
    var sandboxState by remember { mutableStateOf<SandboxState>(SandboxState.Idle) }
    var redirectCount by remember { mutableIntStateOf(0) }
    
    // Validate URL before proceeding
    val validationError = remember(url) { SandboxConfig.validateUrl(url) }
    
    if (validationError != null) {
        SandboxErrorView(
            error = validationError,
            url = url,
            onClose = onClose,
            modifier = modifier
        )
        return
    }
    
    val sanitizedUrl = remember(url) { SandboxConfig.sanitizeUrl(url) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Safety Overlay Banner (Non-dismissable)
            if (config.showSafetyOverlay) {
                SandboxSafetyBanner()
            }
            
            // Loading indicator
            when (val state = sandboxState) {
                is SandboxState.Loading -> {
                    LinearProgressIndicator(
                        progress = { state.progress / 100f },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                else -> {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
            
            // WebView
            Box(modifier = Modifier.weight(1f)) {
                when (val state = sandboxState) {
                    is SandboxState.Blocked -> {
                        BlockedRedirectView(
                            reason = state.reason,
                            url = state.url,
                            onClose = onClose
                        )
                    }
                    is SandboxState.Error -> {
                        SandboxErrorView(
                            error = state.message,
                            url = state.url,
                            onClose = onClose
                        )
                    }
                    else -> {
                        IsolatedWebView(
                            url = sanitizedUrl,
                            config = config,
                            redirectCount = redirectCount,
                            onEvent = { event ->
                                when (event) {
                                    is SandboxEvent.PageStarted -> {
                                        sandboxState = SandboxState.Loading(event.url, 0)
                                    }
                                    is SandboxEvent.PageFinished -> {
                                        sandboxState = SandboxState.Loaded(
                                            url = sanitizedUrl,
                                            finalUrl = event.url,
                                            redirectCount = redirectCount
                                        )
                                    }
                                    is SandboxEvent.ProgressChanged -> {
                                        val currentState = sandboxState
                                        if (currentState is SandboxState.Loading) {
                                            sandboxState = currentState.copy(progress = event.progress)
                                        }
                                    }
                                    is SandboxEvent.RedirectDetected -> {
                                        redirectCount = event.count
                                        if (event.count > config.maxRedirects) {
                                            sandboxState = SandboxState.Blocked(
                                                reason = "Too many redirects (${event.count}). Possible redirect attack.",
                                                url = event.toUrl
                                            )
                                        }
                                    }
                                    is SandboxEvent.ExternalLinkBlocked -> {
                                        // Log but don't change state
                                    }
                                    is SandboxEvent.Error -> {
                                        sandboxState = SandboxState.Error(
                                            message = event.message,
                                            url = sanitizedUrl
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
            }
            
            // Close button
            Button(
                onClick = onClose,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Close, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.sandbox_exit))
            }
        }
    }
}

/**
 * Safety banner that cannot be dismissed.
 */
@Composable
private fun SandboxSafetyBanner() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFFF9800), // Orange warning color
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = stringResource(R.string.cd_security),
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = SandboxOverlay.TITLE,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = SandboxOverlay.SUBTITLE,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black.copy(alpha = 0.8f)
                )
            }
            // Security indicators
            Column {
                SandboxOverlay.securityFeatures.take(3).forEach { feature ->
                    Text(
                        text = feature,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Black.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

/**
 * Isolated WebView with maximum security settings.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun IsolatedWebView(
    url: String,
    config: SandboxConfig,
    redirectCount: Int,
    onEvent: (SandboxEvent) -> Unit
) {
    val context = LocalContext.current
    var currentRedirectCount by remember { mutableIntStateOf(redirectCount) }
    var previousUrl by remember { mutableStateOf("") }

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                // === SECURITY SETTINGS ===
                settings.apply {
                    // CRITICAL: Disable JavaScript
                    javaScriptEnabled = config.javaScriptEnabled

                    // Disable storage and caching
                    domStorageEnabled = config.localStorageEnabled
                    // Note: databaseEnabled is deprecated since API 19, databases use DOM storage
                    cacheMode = WebSettings.LOAD_NO_CACHE

                    // Note: saveFormData is deprecated since API 26 and no longer functions
                    // Form data saving is disabled by default on modern WebViews

                    // Disable geolocation
                    setGeolocationEnabled(false)

                    // Set custom user agent
                    userAgentString = config.userAgent

                    // Disable file access
                    allowFileAccess = false
                    allowContentAccess = false

                    // Disable mixed content
                    mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW

                    // Enable zoom for usability
                    builtInZoomControls = config.zoomEnabled
                    displayZoomControls = false
                    setSupportZoom(config.zoomEnabled)
                    
                    // Block popups
                    javaScriptCanOpenWindowsAutomatically = false
                    setSupportMultipleWindows(false)
                }

                // Disable cookies
                val cookieManager = CookieManager.getInstance()
                cookieManager.setAcceptCookie(config.cookiesEnabled)
                cookieManager.setAcceptThirdPartyCookies(this, false)

                // Web view client with security controls
                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        url?.let { onEvent(SandboxEvent.PageStarted(it)) }
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        url?.let { onEvent(SandboxEvent.PageFinished(it)) }
                    }

                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        val requestUrl = request?.url?.toString() ?: return true
                        
                        // Block external links if configured
                        if (!config.allowExternalLinks) {
                            // Check if this is a navigation away from current host
                            if (requestUrl != url && previousUrl.isNotEmpty()) {
                                currentRedirectCount++
                                onEvent(SandboxEvent.RedirectDetected(
                                    fromUrl = previousUrl,
                                    toUrl = requestUrl,
                                    count = currentRedirectCount
                                ))
                                
                                // Block excessive redirects
                                if (currentRedirectCount > config.maxRedirects) {
                                    return true // Block
                                }
                            }
                        }
                        
                        // Block non-HTTP(S) URLs
                        if (!requestUrl.startsWith("http://") && !requestUrl.startsWith("https://")) {
                            onEvent(SandboxEvent.ExternalLinkBlocked(requestUrl))
                            return true // Block
                        }
                        
                        previousUrl = requestUrl
                        return false // Allow within sandbox
                    }

                    override fun onReceivedError(
                        view: WebView?,
                        errorCode: Int,
                        description: String?,
                        failingUrl: String?
                    ) {
                        onEvent(SandboxEvent.Error(
                            message = description ?: "Unknown error",
                            errorCode = errorCode
                        ))
                    }
                }

                // Chrome client for progress
                webChromeClient = object : WebChromeClient() {
                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        onEvent(SandboxEvent.ProgressChanged(newProgress))
                    }
                }
            }
        },
        update = { webView ->
            // Load URL if not already loaded
            if (webView.url != url) {
                webView.loadUrl(url)
            }
        },
        onRelease = { webView ->
            // Clean up WebView
            webView.stopLoading()
            webView.clearHistory()
            webView.clearCache(true)
            webView.clearFormData()
            webView.destroy()
        }
    )
}

/**
 * View shown when a redirect is blocked.
 */
@Composable
private fun BlockedRedirectView(
    reason: String,
    url: String,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Security,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Navigation Blocked",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = reason,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp)),
            color = MaterialTheme.colorScheme.errorContainer
        ) {
            Text(
                text = url,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

/**
 * View shown when there's an error.
 */
@Composable
private fun SandboxErrorView(
    error: String,
    url: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Security,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Cannot Open URL",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onClose) {
            Text(stringResource(R.string.cd_close))
        }
    }
}
