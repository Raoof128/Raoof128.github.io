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

// UI/Trust/TrustCentreView.swift
// QR-SHIELD Trust Centre - iOS 17+ Liquid Glass Edition
//
// Matches: Trust Centre HTML design
// Features:
// - Strict Offline Guarantee banner
// - Threat Sensitivity slider (Low/Balanced/Paranoia)
// - Privacy Controls toggles
// - Trusted/Blocked domains lists
// - About section with reset functionality

import SwiftUI
#if os(iOS)

// MARK: - Sensitivity Level

enum ThreatSensitivity: Int, CaseIterable {
    case low = 1
    case balanced = 2
    case paranoia = 3
    
    var title: String {
        switch self {
        case .low: return "Low"
        case .balanced: return "Balanced"
        case .paranoia: return "Paranoia"
        }
    }
    
    var description: String {
        switch self {
        case .low: 
            return "Minimal false positives. Only blocks high-confidence threats."
        case .balanced: 
            return "Uses standard heuristics to detect phishing patterns. Best for daily use with minimal false positives."
        case .paranoia: 
            return "Maximum protection. May produce more false positives but catches subtle attacks."
        }
    }
    
    var badgeText: String {
        title.uppercased()
    }
}

// MARK: - Trust Centre View

@available(iOS 17, *)
struct TrustCentreView: View {
    @Environment(\.dismiss) private var dismiss
    
    // Settings
    @AppStorage("threatSensitivity") private var sensitivityLevel: Int = 2
    @AppStorage("strictOfflineMode") private var strictOfflineMode = true
    @AppStorage("anonymousTelemetry") private var anonymousTelemetry = false
    @AppStorage("autoCopySafeLinks") private var autoCopySafeLinks = true
    
    // Lists
    @State private var trustedDomains: [String] = ["apple.com", "github.com", "google.com"]
    @State private var blockedDomains: [String] = ["suspicious-site.com"]
    
    @State private var showTrustedDomains = false
    @State private var showBlockedDomains = false
    @State private var showResetConfirmation = false
    
    private var currentSensitivity: ThreatSensitivity {
        ThreatSensitivity(rawValue: sensitivityLevel) ?? .balanced
    }
    
    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 20) {
                    // Offline Guarantee Banner
                    offlineGuaranteeBanner
                    
                    // Threat Sensitivity Section
                    sensitivitySection
                    
                    // Privacy Controls Section
                    privacyControlsSection
                    
                    // Lists Section
                    listsSection
                    
                    // About Section
                    aboutSection
                }
                .padding(.horizontal, 16)
                .padding(.top, 12)
                .padding(.bottom, 40)
            }
            .scrollContentBackground(.hidden)
            .background {
                LiquidGlassBackground()
                    .ignoresSafeArea()
            }
            .navigationTitle("Trust Centre")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button {
                        dismiss()
                    } label: {
                        Image(systemName: "arrow.left")
                            .foregroundColor(.textSecondary)
                    }
                }
                
                ToolbarItem(placement: .navigationBarTrailing) {
                    Image(systemName: "verified.user.fill")
                        .foregroundColor(.brandPrimary)
                        .symbolEffect(.pulse)
                }
            }
        }
        .confirmationDialog(
            "Reset to Defaults",
            isPresented: $showResetConfirmation,
            titleVisibility: .visible
        ) {
            Button("Reset All Settings", role: .destructive) {
                resetToDefaults()
            }
            Button("Cancel", role: .cancel) {}
        } message: {
            Text("This will reset all Trust Centre settings to their defaults.")
        }
        .sheet(isPresented: $showTrustedDomains) {
            DomainListSheet(
                title: "Trusted Domains",
                domains: $trustedDomains,
                accentColor: .verdictSafe
            )
        }
        .sheet(isPresented: $showBlockedDomains) {
            DomainListSheet(
                title: "Blocked Domains",
                domains: $blockedDomains,
                accentColor: .verdictDanger
            )
        }
    }
    
    // MARK: - Offline Guarantee Banner
    
    private var offlineGuaranteeBanner: some View {
        HStack(alignment: .top, spacing: 16) {
            // Icon
            ZStack {
                Circle()
                    .fill(Color.verdictSafe.opacity(0.15))
                    .frame(width: 48, height: 48)
                
                Image(systemName: "cloud.slash")
                    .font(.title2)
                    .foregroundColor(.verdictSafe)
            }
            
            // Content
            VStack(alignment: .leading, spacing: 4) {
                Text("Strict Offline Guarantee")
                    .font(.headline)
                    .foregroundColor(.textPrimary)
                
                Text("QR-SHIELD analysis runs entirely on your device's Neural Engine. No URL data ever leaves your phone.")
                    .font(.subheadline)
                    .foregroundColor(.textSecondary)
                    .fixedSize(horizontal: false, vertical: true)
            }
        }
        .padding(16)
        .liquidGlass(cornerRadius: 16)
        .overlay(alignment: .topTrailing) {
            Circle()
                .fill(Color.verdictSafe.opacity(0.2))
                .frame(width: 80, height: 80)
                .blur(radius: 30)
                .offset(x: 20, y: -20)
        }
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }
    
    // MARK: - Sensitivity Section
    
    private var sensitivitySection: some View {
        VStack(alignment: .leading, spacing: 16) {
            // Header
            HStack {
                Text("Threat Sensitivity")
                    .font(.headline)
                    .foregroundColor(.textPrimary)
                
                Spacer()
                
                Text(currentSensitivity.badgeText)
                    .font(.caption.weight(.bold))
                    .foregroundColor(.brandPrimary)
                    .padding(.horizontal, 10)
                    .padding(.vertical, 5)
                    .background(Color.brandPrimary.opacity(0.15), in: RoundedRectangle(cornerRadius: 8))
            }
            
            // Slider
            VStack(spacing: 8) {
                Slider(value: Binding(
                    get: { Double(sensitivityLevel) },
                    set: { sensitivityLevel = Int($0) }
                ), in: 1...3, step: 1)
                .tint(.brandPrimary)
                
                // Labels
                HStack {
                    Text("Low")
                        .font(.caption)
                        .foregroundColor(sensitivityLevel == 1 ? .brandPrimary : .textMuted)
                    
                    Spacer()
                    
                    Text("Balanced")
                        .font(.caption)
                        .foregroundColor(sensitivityLevel == 2 ? .brandPrimary : .textMuted)
                    
                    Spacer()
                    
                    Text("Paranoia")
                        .font(.caption)
                        .foregroundColor(sensitivityLevel == 3 ? .brandPrimary : .textMuted)
                }
            }
            
            // Description
            HStack(alignment: .top, spacing: 12) {
                Image(systemName: "info.circle")
                    .font(.caption)
                    .foregroundColor(.textMuted)
                
                Text(currentSensitivity.description)
                    .font(.caption)
                    .foregroundColor(.textSecondary)
            }
            .padding(12)
            .background(Color.bgSurface.opacity(0.5), in: RoundedRectangle(cornerRadius: 12))
        }
        .padding(16)
        .liquidGlass(cornerRadius: 16)
        .sensoryFeedback(.selection, trigger: sensitivityLevel)
    }
    
    // MARK: - Privacy Controls Section
    
    private var privacyControlsSection: some View {
        VStack(alignment: .leading, spacing: 4) {
            sectionHeader("Privacy Controls")
            
            VStack(spacing: 0) {
                // Strict Offline Mode
                privacyToggleRow(
                    icon: "globe.badge.minus.fill",
                    iconColor: .brandPrimary,
                    title: "Strict Offline Mode",
                    isOn: $strictOfflineMode
                )
                
                Divider()
                    .padding(.leading, 52)
                
                // Anonymous Telemetry
                privacyToggleRow(
                    icon: "chart.bar.fill",
                    iconColor: .brandAccent,
                    title: "Anonymous Telemetry",
                    subtitle: "Help improve detection",
                    isOn: $anonymousTelemetry
                )
                
                Divider()
                    .padding(.leading, 52)
                
                // Auto-Copy Safe Links
                privacyToggleRow(
                    icon: "doc.on.doc.fill",
                    iconColor: .verdictWarning,
                    title: "Auto-Copy Safe Links",
                    isOn: $autoCopySafeLinks
                )
            }
            .liquidGlass(cornerRadius: 16)
        }
    }
    
    private func privacyToggleRow(
        icon: String,
        iconColor: Color,
        title: String,
        subtitle: String? = nil,
        isOn: Binding<Bool>
    ) -> some View {
        Toggle(isOn: isOn) {
            HStack(spacing: 12) {
                ZStack {
                    RoundedRectangle(cornerRadius: 8)
                        .fill(iconColor.opacity(0.15))
                        .frame(width: 32, height: 32)
                    
                    Image(systemName: icon)
                        .font(.system(size: 14))
                        .foregroundColor(iconColor)
                }
                
                VStack(alignment: .leading, spacing: 2) {
                    Text(title)
                        .font(.subheadline)
                        .foregroundColor(.textPrimary)
                    
                    if let subtitle {
                        Text(subtitle)
                            .font(.caption2)
                            .foregroundColor(.textMuted)
                    }
                }
            }
        }
        .tint(.brandPrimary)
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .onChange(of: isOn.wrappedValue) { _, _ in
            SettingsManager.shared.triggerHaptic(.selection)
        }
    }
    
    // MARK: - Lists Section
    
    private var listsSection: some View {
        VStack(alignment: .leading, spacing: 4) {
            sectionHeader("Lists")
            
            VStack(spacing: 0) {
                // Trusted Domains
                listRow(
                    icon: "checkmark.circle.fill",
                    iconColor: .verdictSafe,
                    title: "Trusted Domains",
                    count: trustedDomains.count
                ) {
                    showTrustedDomains = true
                }
                
                Divider()
                    .padding(.leading, 52)
                
                // Blocked Domains
                listRow(
                    icon: "nosign",
                    iconColor: .verdictDanger,
                    title: "Blocked Domains",
                    count: blockedDomains.count
                ) {
                    showBlockedDomains = true
                }
            }
            .liquidGlass(cornerRadius: 16)
        }
    }
    
    private func listRow(
        icon: String,
        iconColor: Color,
        title: String,
        count: Int,
        action: @escaping () -> Void
    ) -> some View {
        Button(action: action) {
            HStack(spacing: 12) {
                Image(systemName: icon)
                    .font(.title3)
                    .foregroundColor(iconColor)
                    .frame(width: 32)
                
                Text(title)
                    .font(.subheadline)
                    .foregroundColor(.textPrimary)
                
                Spacer()
                
                Text("\(count)")
                    .font(.caption)
                    .foregroundColor(.textMuted)
                
                Image(systemName: "chevron.right")
                    .font(.caption)
                    .foregroundColor(.textMuted)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 14)
        }
    }
    
    // MARK: - About Section
    
    private var aboutSection: some View {
        VStack(alignment: .leading, spacing: 4) {
            sectionHeader("About QR-SHIELD")
            
            VStack(spacing: 0) {
                aboutRow(title: "Open Source Licences")
                Divider().padding(.leading, 16)
                aboutRow(title: "Privacy Policy")
                Divider().padding(.leading, 16)
                aboutRow(title: "Acknowledgements")
                Divider().padding(.leading, 16)
                
                // Reset Button
                Button {
                    showResetConfirmation = true
                } label: {
                    Text("Reset to Defaults")
                        .font(.subheadline)
                        .foregroundColor(.verdictDanger)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 14)
                }
                .background(Color.verdictDanger.opacity(0.08))
            }
            .liquidGlass(cornerRadius: 16)
            
            // Version
            Text("QR-SHIELD v2.4.0 (Build 892)")
                .font(.caption2)
                .foregroundColor(.textMuted)
                .frame(maxWidth: .infinity)
                .padding(.top, 16)
        }
    }
    
    private func aboutRow(title: String) -> some View {
        Button {
            SettingsManager.shared.triggerHaptic(.light)
        } label: {
            HStack {
                Text(title)
                    .font(.subheadline)
                    .foregroundColor(.textPrimary)
                
                Spacer()
                
                Image(systemName: "chevron.right")
                    .font(.caption)
                    .foregroundColor(.textMuted)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 14)
        }
    }
    
    // MARK: - Helpers
    
    private func sectionHeader(_ title: String) -> some View {
        Text(title.uppercased())
            .font(.caption.weight(.semibold))
            .foregroundColor(.textMuted)
            .tracking(1)
            .padding(.horizontal, 4)
            .padding(.bottom, 8)
    }
    
    private func resetToDefaults() {
        withAnimation {
            sensitivityLevel = 2
            strictOfflineMode = true
            anonymousTelemetry = false
            autoCopySafeLinks = true
        }
        SettingsManager.shared.triggerHaptic(.warning)
    }
}

// MARK: - Domain List Sheet

struct DomainListSheet: View {
    let title: String
    @Binding var domains: [String]
    let accentColor: Color
    
    @Environment(\.dismiss) private var dismiss
    @State private var newDomain = ""
    @State private var showAddSheet = false
    
    var body: some View {
        NavigationStack {
            List {
                ForEach(domains, id: \.self) { domain in
                    HStack {
                        Image(systemName: accentColor == .verdictSafe ? "checkmark.shield.fill" : "xmark.shield.fill")
                            .foregroundColor(accentColor)
                        
                        Text(domain)
                            .font(.subheadline)
                            .foregroundColor(.textPrimary)
                        
                        Spacer()
                    }
                    .listRowBackground(Color.bgCard)
                }
                .onDelete { indexSet in
                    domains.remove(atOffsets: indexSet)
                }
            }
            .scrollContentBackground(.hidden)
            .background {
                LiquidGlassBackground()
                    .ignoresSafeArea()
            }
            .navigationTitle(title)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Done") {
                        dismiss()
                    }
                    .foregroundColor(.brandPrimary)
                }
                
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button {
                        showAddSheet = true
                    } label: {
                        Image(systemName: "plus")
                            .foregroundColor(.brandPrimary)
                    }
                }
            }
            .alert("Add Domain", isPresented: $showAddSheet) {
                TextField("example.com", text: $newDomain)
                    .textInputAutocapitalization(.never)
                    .autocorrectionDisabled()
                
                Button("Cancel", role: .cancel) {
                    newDomain = ""
                }
                
                Button("Add") {
                    if !newDomain.isEmpty && !domains.contains(newDomain) {
                        domains.append(newDomain)
                        newDomain = ""
                    }
                }
            }
        }
    }
}

#endif
