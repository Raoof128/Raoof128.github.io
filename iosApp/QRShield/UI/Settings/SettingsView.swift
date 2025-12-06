// UI/Settings/SettingsView.swift
// QR-SHIELD Settings
//
// User preferences and app configuration.

import SwiftUI

struct SettingsView: View {
    @AppStorage("hapticEnabled") private var hapticEnabled = true
    @AppStorage("soundEnabled") private var soundEnabled = true
    @AppStorage("autoScan") private var autoScan = true
    @AppStorage("saveHistory") private var saveHistory = true
    
    var body: some View {
        List {
            // Scanning Section
            Section {
                Toggle("Auto-scan on launch", isOn: $autoScan)
                Toggle("Haptic feedback", isOn: $hapticEnabled)
                Toggle("Sound effects", isOn: $soundEnabled)
            } header: {
                sectionHeader("Scanning")
            }
            .listRowBackground(Color.bgCard)
            
            // Privacy Section
            Section {
                Toggle("Save scan history", isOn: $saveHistory)
                
                Button(action: clearHistory) {
                    HStack {
                        Text("Clear History")
                            .foregroundColor(.verdictDanger)
                        Spacer()
                        Image(systemName: "trash")
                            .foregroundColor(.verdictDanger)
                    }
                }
            } header: {
                sectionHeader("Privacy")
            }
            .listRowBackground(Color.bgCard)
            
            // About Section
            Section {
                HStack {
                    Text("Version")
                    Spacer()
                    Text("1.0.0")
                        .foregroundColor(.textSecondary)
                }
                
                HStack {
                    Text("Build")
                    Spacer()
                    Text("KotlinConf 2026")
                        .foregroundColor(.textSecondary)
                }
                
                Link(destination: URL(string: "https://github.com/Raoof128/QDKMP-KotlinConf-2026-")!) {
                    HStack {
                        Text("Source Code")
                        Spacer()
                        Image(systemName: "arrow.up.right.square")
                            .foregroundColor(.brandPrimary)
                    }
                }
            } header: {
                sectionHeader("About")
            }
            .listRowBackground(Color.bgCard)
            
            // Credits Section
            Section {
                VStack(alignment: .center, spacing: 12) {
                    Image(systemName: "shield.fill")
                        .font(.system(size: 40))
                        .foregroundColor(.brandPrimary)
                    
                    Text("QR-SHIELD")
                        .font(.headline)
                        .foregroundColor(.textPrimary)
                    
                    Text("Kotlin Multiplatform QRishing Detector")
                        .font(.caption)
                        .foregroundColor(.textSecondary)
                    
                    Text("Made with ❤️ for KotlinConf 2026")
                        .font(.caption2)
                        .foregroundColor(.textMuted)
                }
                .frame(maxWidth: .infinity)
                .padding(.vertical, 20)
            }
            .listRowBackground(Color.clear)
        }
        .listStyle(.insetGrouped)
        .scrollContentBackground(.hidden)
        .background(Color.bgDark)
        .navigationTitle("Settings")
    }
    
    private func sectionHeader(_ title: String) -> some View {
        Text(title)
            .font(.subheadline.weight(.semibold))
            .foregroundColor(.brandPrimary)
            .textCase(nil)
    }
    
    private func clearHistory() {
        // Clear history logic
    }
}

#Preview {
    NavigationStack {
        SettingsView()
    }
}
