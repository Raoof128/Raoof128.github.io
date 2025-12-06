// UI/Settings/SettingsView.swift
// QR-SHIELD Settings - iOS 26.2 Liquid Glass Edition
//
// UPDATED: December 2025 - iOS 26.2 RC
// - Liquid Glass customization options
// - Enhanced form styling
// - iOS 26 toggle animations
// - Privacy controls

import SwiftUI

struct SettingsView: View {
    @AppStorage("hapticEnabled") private var hapticEnabled = true
    @AppStorage("soundEnabled") private var soundEnabled = true
    @AppStorage("autoScan") private var autoScan = true
    @AppStorage("saveHistory") private var saveHistory = true
    @AppStorage("liquidGlassReduced") private var liquidGlassReduced = false
    @AppStorage("notificationsEnabled") private var notificationsEnabled = true
    
    @State private var showClearConfirmation = false
    
    var body: some View {
        List {
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
                SettingsToggle(
                    icon: "bell.badge",
                    title: "Security alerts",
                    subtitle: "Get notified about security threats",
                    isOn: $notificationsEnabled
                )
            } header: {
                sectionHeader("Notifications", icon: "bell")
            }
            .listRowBackground(Color.clear)
            
            // Appearance Section (iOS 26.2)
            Section {
                SettingsToggle(
                    icon: "sparkles",
                    title: "Reduce Liquid Glass",
                    subtitle: "Simplify visual effects for performance",
                    isOn: $liquidGlassReduced
                )
                
                // iOS 26.2: Link to system settings for Liquid Glass
                NavigationLink {
                    Text("System appearance settings")
                        .foregroundColor(.textSecondary)
                } label: {
                    HStack {
                        Image(systemName: "paintpalette")
                            .foregroundColor(.brandPrimary)
                            .frame(width: 28)
                        
                        Text("System Appearance")
                            .foregroundColor(.textPrimary)
                    }
                    .padding(.vertical, 4)
                }
            } header: {
                sectionHeader("Appearance", icon: "sparkles")
            } footer: {
                Text("iOS 26.2 introduces customizable Liquid Glass effects. Reduce effects if you experience performance issues on older devices.")
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
                aboutRow(icon: "info.circle", title: "Version", value: "1.0.0 (26)")
                aboutRow(icon: "hammer", title: "Build", value: "iOS 26.2 • Swift 6.1")
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
                    
                    // iOS 26.2 + Swift 6.1 badge
                    HStack(spacing: 12) {
                        HStack(spacing: 6) {
                            Image(systemName: "apple.logo")
                            Text("iOS 26.2")
                        }
                        .font(.caption2)
                        .foregroundColor(.brandPrimary)
                        .padding(.horizontal, 12)
                        .padding(.vertical, 6)
                        .background(.ultraThinMaterial, in: Capsule())
                        
                        HStack(spacing: 6) {
                            Image(systemName: "swift")
                            Text("Swift 6.1")
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
        // Clear history logic with haptic
        let generator = UINotificationFeedbackGenerator()
        generator.notificationOccurred(.warning)
        
        // In production: Clear from database
    }
}

// MARK: - Settings Toggle (iOS 26.2 Liquid Glass)

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
        .sensoryFeedback(.selection, trigger: isOn)
    }
}

#Preview {
    NavigationStack {
        SettingsView()
    }
}
