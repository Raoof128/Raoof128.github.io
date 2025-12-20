//
// Copyright 2025-2026 QR-SHIELD Contributors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

// UI/Settings/SettingsView.swift
// QR-SHIELD Settings - iOS 17+ Liquid Glass Edition
//
// UPDATED: December 2025 - iOS 17+ Compatible
// - Liquid Glass customization options
// - Enhanced form styling
// - iOS 17+ toggle animations
// - Privacy controls

import SwiftUI
#if os(iOS)

struct SettingsView: View {
    @AppStorage("hapticEnabled") private var hapticEnabled = true
    @AppStorage("soundEnabled") private var soundEnabled = true
    @AppStorage("autoScan") private var autoScan = true
    @AppStorage("saveHistory") private var saveHistory = true
    @AppStorage("liquidGlassReduced") private var liquidGlassReduced = false
    @AppStorage("notificationsEnabled") private var notificationsEnabled = true
    @AppStorage("useDarkMode") private var useDarkMode = true
    
    @State private var showClearConfirmation = false
    @State private var showNotificationDeniedAlert = false
    @State private var showTrustCentre = false
    @State private var showExport = false
    @State private var showThreatHistory = false
    
    var body: some View {
        List {
            // Quick Actions Section
            Section {
                NavigationLink {
                    ThreatHistoryView()
                } label: {
                    HStack(spacing: 12) {
                        ZStack {
                            RoundedRectangle(cornerRadius: 8)
                                .fill(Color.verdictDanger.opacity(0.15))
                                .frame(width: 32, height: 32)
                            Image(systemName: "shield.slash.fill")
                                .font(.system(size: 14))
                                .foregroundColor(.verdictDanger)
                        }
                        
                        VStack(alignment: .leading, spacing: 2) {
                            Text("Threat Monitor")
                                .foregroundColor(.textPrimary)
                            Text("View live threats and run security audit")
                                .font(.caption)
                                .foregroundColor(.textMuted)
                        }
                    }
                    .padding(.vertical, 4)
                }
                
                Button {
                    showTrustCentre = true
                } label: {
                    HStack(spacing: 12) {
                        ZStack {
                            RoundedRectangle(cornerRadius: 8)
                                .fill(Color.verdictSafe.opacity(0.15))
                                .frame(width: 32, height: 32)
                            Image(systemName: "lock.shield.fill")
                                .font(.system(size: 14))
                                .foregroundColor(.verdictSafe)
                        }
                        
                        VStack(alignment: .leading, spacing: 2) {
                            Text("Trust Centre")
                                .foregroundColor(.textPrimary)
                            Text("Privacy settings and threat sensitivity")
                                .font(.caption)
                                .foregroundColor(.textMuted)
                        }
                        
                        Spacer()
                        
                        Image(systemName: "chevron.right")
                            .font(.caption)
                            .foregroundColor(.textMuted)
                    }
                    .padding(.vertical, 4)
                }
                
                Button {
                    showExport = true
                } label: {
                    HStack(spacing: 12) {
                        ZStack {
                            RoundedRectangle(cornerRadius: 8)
                                .fill(Color.brandPrimary.opacity(0.15))
                                .frame(width: 32, height: 32)
                            Image(systemName: "doc.text.fill")
                                .font(.system(size: 14))
                                .foregroundColor(.brandPrimary)
                        }
                        
                        VStack(alignment: .leading, spacing: 2) {
                            Text("Export Report")
                                .foregroundColor(.textPrimary)
                            Text("Generate PDF or JSON security report")
                                .font(.caption)
                                .foregroundColor(.textMuted)
                        }
                        
                        Spacer()
                        
                        Image(systemName: "chevron.right")
                            .font(.caption)
                            .foregroundColor(.textMuted)
                    }
                    .padding(.vertical, 4)
                }
            } header: {
                sectionHeader("Quick Actions", icon: "bolt.fill")
            }
            .listRowBackground(Color.clear)
            
            // Scanning Section
            Section {
                SettingsToggle(
                    icon: "qrcode.viewfinder",
                    title: "Auto-scan on launch",
                    subtitle: "Start scanning immediately when app opens",
                    isOn: $autoScan
                )
                
                SettingsToggle(
                    icon: "waveform",
                    title: "Haptic feedback",
                    subtitle: "Vibrate on scan results",
                    isOn: $hapticEnabled
                )
                
                SettingsToggle(
                    icon: "speaker.wave.2",
                    title: "Sound effects",
                    subtitle: "Play sounds for alerts",
                    isOn: $soundEnabled
                )
            } header: {
                sectionHeader("Scanning", icon: "viewfinder")
            }
            .listRowBackground(Color.clear)
            
            // Notifications Section
            Section {
                Toggle(isOn: $notificationsEnabled) {
                    HStack(spacing: 12) {
                        Image(systemName: "bell.badge")
                            .foregroundColor(.brandPrimary)
                            .frame(width: 28)
                            .symbolEffect(.bounce, value: notificationsEnabled)
                        
                        VStack(alignment: .leading, spacing: 2) {
                            Text("Security alerts")
                                .foregroundColor(.textPrimary)
                            
                            Text("Get notified about security threats")
                                .font(.caption)
                                .foregroundColor(.textMuted)
                        }
                    }
                }
                .tint(.brandPrimary)
                .padding(.vertical, 4)
                .onChange(of: notificationsEnabled) { _, newValue in
                    if newValue {
                        // Request notification permission when enabled
                        Task {
                            let granted = await SettingsManager.shared.requestNotificationPermission()
                            if !granted {
                                // If permission denied, turn off the toggle and show alert
                                await MainActor.run {
                                    notificationsEnabled = false
                                    showNotificationDeniedAlert = true
                                }
                            }
                        }
                    }
                    SettingsManager.shared.triggerHaptic(.selection)
                }
            } header: {
                sectionHeader("Notifications", icon: "bell")
            }
            .listRowBackground(Color.clear)
            .alert("Notifications Disabled", isPresented: $showNotificationDeniedAlert) {
                Button("Open Settings") {
                    if let url = URL(string: UIApplication.openSettingsURLString) {
                        UIApplication.shared.open(url)
                    }
                }
                Button("Cancel", role: .cancel) {}
            } message: {
                Text("To receive security alerts, please enable notifications for QR-SHIELD in Settings.")
            }
            
            // Appearance Section (iOS 17+)
            Section {
                SettingsToggle(
                    icon: "moon.fill",
                    title: "Dark Mode",
                    subtitle: "Enable dark color scheme",
                    isOn: $useDarkMode
                )
                
                SettingsToggle(
                    icon: "sparkles",
                    title: "Reduce Liquid Glass",
                    subtitle: "Simplify visual effects for performance",
                    isOn: $liquidGlassReduced
                )
                
                // iOS 17+: Link to system settings for appearance
                Button {
                    if let url = URL(string: UIApplication.openSettingsURLString) {
                        UIApplication.shared.open(url)
                    }
                } label: {
                    HStack {
                        Image(systemName: "paintpalette")
                            .foregroundColor(.brandPrimary)
                            .frame(width: 28)
                        
                        Text("System Appearance")
                            .foregroundColor(.textPrimary)
                        
                        Spacer()
                        
                        Image(systemName: "arrow.up.right.square")
                            .font(.caption)
                            .foregroundColor(.brandPrimary)
                    }
                    .padding(.vertical, 4)
                }
            } header: {
                sectionHeader("Appearance", icon: "sparkles")
            } footer: {
                Text("iOS 17+ introduces beautiful Liquid Glass effects. Reduce effects if you experience performance issues.")
                    .font(.caption2)
                    .foregroundColor(.textMuted)
            }
            .listRowBackground(Color.clear)
            
            // Privacy Section
            Section {
                SettingsToggle(
                    icon: "clock.arrow.circlepath",
                    title: "Save scan history",
                    subtitle: "Keep a record of scanned URLs",
                    isOn: $saveHistory
                )
                
                Button {
                    showClearConfirmation = true
                } label: {
                    HStack {
                        Image(systemName: "trash")
                            .foregroundColor(.verdictDanger)
                            .frame(width: 28)
                        
                        Text("Clear History")
                            .foregroundColor(.verdictDanger)
                        
                        Spacer()
                    }
                    .padding(.vertical, 4)
                }
                
                // Privacy Policy Link
                Link(destination: URL(string: "https://github.com/Raoof128/QDKMP-KotlinConf-2026-")!) {
                    HStack {
                        Image(systemName: "hand.raised")
                            .foregroundColor(.brandPrimary)
                            .frame(width: 28)
                        
                        Text("Privacy Policy")
                            .foregroundColor(.textPrimary)
                        
                        Spacer()
                        
                        Image(systemName: "arrow.up.right.square")
                            .font(.caption)
                            .foregroundColor(.brandPrimary)
                    }
                    .padding(.vertical, 4)
                }
            } header: {
                sectionHeader("Privacy", icon: "hand.raised")
            }
            .listRowBackground(Color.clear)
            
            // About Section
            Section {
                aboutRow(icon: "info.circle", title: "Version", value: "1.0.0 (1)")
                aboutRow(icon: "hammer", title: "Build", value: "iOS 17+ • Swift 6")
                aboutRow(icon: "cpu", title: "Engine", value: "KMP PhishingEngine")
                
                Link(destination: URL(string: "https://github.com/Raoof128/QDKMP-KotlinConf-2026-")!) {
                    HStack {
                        Image(systemName: "chevron.left.forwardslash.chevron.right")
                            .foregroundColor(.brandPrimary)
                            .frame(width: 28)
                        
                        Text("Source Code")
                            .foregroundColor(.textPrimary)
                        
                        Spacer()
                        
                        Image(systemName: "arrow.up.right.square")
                            .font(.caption)
                            .foregroundColor(.brandPrimary)
                    }
                    .padding(.vertical, 4)
                }
            } header: {
                sectionHeader("About", icon: "info.circle")
            }
            .listRowBackground(Color.clear)
            
            // Credits Section
            Section {
                VStack(spacing: 16) {
                    // Animated Logo with custom asset
                    ZStack {
                        Circle()
                            .fill(.ultraThinMaterial)
                            .frame(width: 80, height: 80)
                        
                        // Use branding logo if available
                        Image.sfSymbolForVerdict(.safe)
                            .font(.system(size: 40))
                            .foregroundStyle(LinearGradient.brandGradient)
                            .symbolEffect(.pulse)
                    }
                    
                    VStack(spacing: 4) {
                        Text("QR-SHIELD")
                            .font(.headline)
                            .foregroundColor(.textPrimary)
                        
                        Text("Kotlin Multiplatform QRishing Detector")
                            .font(.caption)
                            .foregroundColor(.textSecondary)
                    }
                    
                    Text("Made with ❤️ for KotlinConf 2026")
                        .font(.caption2)
                        .foregroundColor(.textMuted)
                    
                    // iOS 17+ + Swift 6 badge
                    HStack(spacing: 12) {
                        HStack(spacing: 6) {
                            Image(systemName: "apple.logo")
                            Text("iOS 17+")
                        }
                        .font(.caption2)
                        .foregroundColor(.brandPrimary)
                        .padding(.horizontal, 12)
                        .padding(.vertical, 6)
                        .background(.ultraThinMaterial, in: Capsule())
                        
                        HStack(spacing: 6) {
                            Image(systemName: "swift")
                            Text("Swift 6")
                        }
                        .font(.caption2)
                        .foregroundColor(.brandSecondary)
                        .padding(.horizontal, 12)
                        .padding(.vertical, 6)
                        .background(.ultraThinMaterial, in: Capsule())
                    }
                }
                .frame(maxWidth: .infinity)
                .padding(.vertical, 24)
            }
            .listRowBackground(Color.clear)
        }
        .listStyle(.insetGrouped)
        .scrollContentBackground(.hidden)
        .background {
            LiquidGlassBackground()
                .ignoresSafeArea()
        }
        .navigationTitle("Settings")
        .confirmationDialog(
            "Clear All History?",
            isPresented: $showClearConfirmation,
            titleVisibility: .visible
        ) {
            Button("Clear All", role: .destructive) {
                clearHistory()
            }
            Button("Cancel", role: .cancel) {}
        } message: {
            Text("This action cannot be undone. All scan history will be permanently deleted.")
        }
        .sheet(isPresented: $showTrustCentre) {
            TrustCentreView()
        }
        .sheet(isPresented: $showExport) {
            ReportExportView()
        }
    }
    
    // MARK: - Components
    
    private func sectionHeader(_ title: String, icon: String) -> some View {
        HStack(spacing: 6) {
            Image(systemName: icon)
                .font(.caption)
            Text(title)
        }
        .font(.subheadline.weight(.semibold))
        .foregroundColor(.brandPrimary)
        .textCase(nil)
    }
    
    private func aboutRow(icon: String, title: String, value: String) -> some View {
        HStack {
            Image(systemName: icon)
                .foregroundColor(.brandPrimary)
                .frame(width: 28)
            
            Text(title)
                .foregroundColor(.textPrimary)
            
            Spacer()
            
            Text(value)
                .foregroundColor(.textSecondary)
                .font(.subheadline)
        }
        .padding(.vertical, 4)
    }
    
    private func clearHistory() {
        // Clear actual history from store
        HistoryStore.shared.clearAll()
        
        // Use SettingsManager for haptic since it respects settings
        SettingsManager.shared.triggerHaptic(.warning)
        SettingsManager.shared.playSound(.warning)
        
        #if DEBUG
        print("🗑️ History cleared from Settings")
        #endif
    }
}
// MARK: - Settings Toggle (iOS 17+ Liquid Glass)

struct SettingsToggle: View {
    let icon: String
    let title: String
    var subtitle: String? = nil
    @Binding var isOn: Bool
    
    var body: some View {
        Toggle(isOn: $isOn) {
            HStack(spacing: 12) {
                Image(systemName: icon)
                    .foregroundColor(.brandPrimary)
                    .frame(width: 28)
                    .symbolEffect(.bounce, value: isOn)
                
                VStack(alignment: .leading, spacing: 2) {
                    Text(title)
                        .foregroundColor(.textPrimary)
                    
                    if let subtitle {
                        Text(subtitle)
                            .font(.caption)
                            .foregroundColor(.textMuted)
                    }
                }
            }
        }
        .tint(.brandPrimary)
        .padding(.vertical, 4)
        .onChange(of: isOn) { _, _ in
            // Use SettingsManager so it respects haptic settings
            SettingsManager.shared.triggerHaptic(.selection)
        }
        .accessibilityElement(children: .combine)
        .accessibilityLabel(title)
        .accessibilityValue(isOn ? "Enabled" : "Disabled")
        .accessibilityHint(subtitle ?? "")
    }
}

#endif
