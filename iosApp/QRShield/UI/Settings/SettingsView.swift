// UI/Settings/SettingsView.swift
// QR-SHIELD Settings - iOS 26 Liquid Glass Edition
//
// UPDATED: December 2025
// - Liquid Glass design throughout
// - Enhanced form styling
// - iOS 26 toggle animations

import SwiftUI

struct SettingsView: View {
    @AppStorage("hapticEnabled") private var hapticEnabled = true
    @AppStorage("soundEnabled") private var soundEnabled = true
    @AppStorage("autoScan") private var autoScan = true
    @AppStorage("saveHistory") private var saveHistory = true
    @AppStorage("liquidGlassReduced") private var liquidGlassReduced = false
    
    var body: some View {
        List {
            // Scanning Section
            Section {
                SettingsToggle(
                    icon: "qrcode.viewfinder",
                    title: "Auto-scan on launch",
                    isOn: $autoScan
                )
                
                SettingsToggle(
                    icon: "waveform",
                    title: "Haptic feedback",
                    isOn: $hapticEnabled
                )
                
                SettingsToggle(
                    icon: "speaker.wave.2",
                    title: "Sound effects",
                    isOn: $soundEnabled
                )
            } header: {
                sectionHeader("Scanning")
            }
            .listRowBackground(Color.clear)
            
            // Appearance Section (iOS 26)
            Section {
                SettingsToggle(
                    icon: "sparkles",
                    title: "Reduce Liquid Glass",
                    subtitle: "Simplify visual effects",
                    isOn: $liquidGlassReduced
                )
            } header: {
                sectionHeader("Appearance")
            }
            .listRowBackground(Color.clear)
            
            // Privacy Section
            Section {
                SettingsToggle(
                    icon: "clock.arrow.circlepath",
                    title: "Save scan history",
                    isOn: $saveHistory
                )
                
                Button(action: clearHistory) {
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
            } header: {
                sectionHeader("Privacy")
            }
            .listRowBackground(Color.clear)
            
            // About Section
            Section {
                aboutRow(icon: "info.circle", title: "Version", value: "1.0.0")
                aboutRow(icon: "hammer", title: "Build", value: "iOS 26 • KotlinConf 2026")
                
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
                sectionHeader("About")
            }
            .listRowBackground(Color.clear)
            
            // Credits Section
            Section {
                VStack(spacing: 16) {
                    // Animated Logo
                    ZStack {
                        Circle()
                            .fill(.ultraThinMaterial)
                            .frame(width: 80, height: 80)
                        
                        Image(systemName: "shield.fill")
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
                    
                    // iOS 26 badge
                    HStack(spacing: 6) {
                        Image(systemName: "apple.logo")
                        Text("Built for iOS 26")
                    }
                    .font(.caption2)
                    .foregroundColor(.brandPrimary)
                    .padding(.horizontal, 12)
                    .padding(.vertical, 6)
                    .background(.ultraThinMaterial, in: Capsule())
                }
                .frame(maxWidth: .infinity)
                .padding(.vertical, 24)
            }
            .listRowBackground(Color.clear)
        }
        .listStyle(.insetGrouped)
        .scrollContentBackground(.hidden)
        .background {
            MeshGradient.liquidGlassBackground
                .ignoresSafeArea()
        }
        .navigationTitle("Settings")
    }
    
    // MARK: - Components
    
    private func sectionHeader(_ title: String) -> some View {
        Text(title)
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
        }
        .padding(.vertical, 4)
    }
    
    private func clearHistory() {
        // Clear history logic with haptic
        let generator = UINotificationFeedbackGenerator()
        generator.notificationOccurred(.warning)
    }
}

// MARK: - Settings Toggle (iOS 26 Liquid Glass)

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
