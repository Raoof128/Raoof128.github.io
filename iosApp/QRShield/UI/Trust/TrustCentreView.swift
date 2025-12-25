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
    
    // Lists - persisted in UserDefaults
    @AppStorage("trustedDomains") private var trustedDomainsData: Data = Data()
    @AppStorage("blockedDomains") private var blockedDomainsData: Data = Data()
    
    @State private var trustedDomains: [String] = []
    @State private var blockedDomains: [String] = []
    
    @State private var showTrustedDomains = false
    @State private var showBlockedDomains = false
    @State private var showResetConfirmation = false
    @State private var showPrivacyPolicy = false
    @State private var showLicenses = false
    @State private var showAcknowledgements = false
    
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
            .navigationTitle(NSLocalizedString("trust.title", comment: ""))
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
                    Image(systemName: "checkmark.shield.fill")
                        .foregroundColor(.verdictSafe)
                        .symbolEffect(.pulse)
                }
            }
        }
        .confirmationDialog(
            "Reset to Defaults",
            isPresented: $showResetConfirmation,
            titleVisibility: .visible
        ) {
            Button(NSLocalizedString("common.reset_all_settings", comment: ""), role: .destructive) {
                resetToDefaults()
            }
            Button(NSLocalizedString("common.cancel", comment: ""), role: .cancel) {}
        } message: {
            Text(NSLocalizedString("trust.reset_confirm", comment: ""))
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
        .sheet(isPresented: $showPrivacyPolicy) {
            InfoSheet(title: "Privacy Policy", content: privacyPolicyText)
        }
        .sheet(isPresented: $showLicenses) {
            InfoSheet(title: "Open Source Licenses", content: licensesText)
        }
        .sheet(isPresented: $showAcknowledgements) {
            InfoSheet(title: "Acknowledgements", content: acknowledgementsText)
        }
        .onAppear {
            loadDomainLists()
        }
        .onChange(of: trustedDomains) { _, newValue in
            saveDomainList(newValue, to: "trustedDomains")
        }
        .onChange(of: blockedDomains) { _, newValue in
            saveDomainList(newValue, to: "blockedDomains")
        }
    }
    
    // MARK: - Persistence
    
    private func loadDomainLists() {
        if let trusted = try? JSONDecoder().decode([String].self, from: trustedDomainsData), !trusted.isEmpty {
            trustedDomains = trusted
        } else {
            trustedDomains = ["apple.com", "github.com", "google.com"]
        }
        
        if let blocked = try? JSONDecoder().decode([String].self, from: blockedDomainsData), !blocked.isEmpty {
            blockedDomains = blocked
        } else {
            blockedDomains = ["suspicious-site.com"]
        }
    }
    
    private func saveDomainList(_ list: [String], to key: String) {
        if let data = try? JSONEncoder().encode(list) {
            if key == "trustedDomains" {
                trustedDomainsData = data
            } else {
                blockedDomainsData = data
            }
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
                Text(NSLocalizedString("trust.offline_guarantee", comment: ""))
                    .font(.headline)
                    .foregroundColor(.textPrimary)
                
                Text(NSLocalizedString("trust.offline_description", comment: ""))
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
                Text(NSLocalizedString("trust.threat_sensitivity", comment: ""))
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
                    Text(NSLocalizedString("trust.low", comment: ""))
                        .font(.caption)
                        .foregroundColor(sensitivityLevel == 1 ? .brandPrimary : .textMuted)
                    
                    Spacer()
                    
                    Text(NSLocalizedString("trust.balanced", comment: ""))
                        .font(.caption)
                        .foregroundColor(sensitivityLevel == 2 ? .brandPrimary : .textMuted)
                    
                    Spacer()
                    
                    Text(NSLocalizedString("trust.paranoia", comment: ""))
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
                    icon: "wifi.slash",
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
                aboutRow(title: "Open Source Licenses") {
                    showLicenses = true
                }
                Divider().padding(.leading, 16)
                aboutRow(title: "Privacy Policy") {
                    showPrivacyPolicy = true
                }
                Divider().padding(.leading, 16)
                aboutRow(title: "Acknowledgements") {
                    showAcknowledgements = true
                }
                Divider().padding(.leading, 16)
                
                // Reset Button
                Button {
                    showResetConfirmation = true
                } label: {
                    Text(NSLocalizedString("trust.reset_defaults", comment: ""))
                        .font(.subheadline)
                        .foregroundColor(.verdictDanger)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 14)
                }
                .background(Color.verdictDanger.opacity(0.08))
            }
            .liquidGlass(cornerRadius: 16)
            
            // Version
            Text(NSLocalizedString("trust.version_info", comment: ""))
                .font(.caption2)
                .foregroundColor(.textMuted)
                .frame(maxWidth: .infinity)
                .padding(.top, 16)
        }
    }
    
    private func aboutRow(title: String, action: @escaping () -> Void) -> some View {
        Button {
            SettingsManager.shared.triggerHaptic(.light)
            action()
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
    
    // MARK: - Content Text
    
    private var privacyPolicyText: String {
        """
        QR-SHIELD Privacy Policy
        ========================
        
        Last Updated: December 2025
        
        1. DATA COLLECTION
        ------------------
        QR-SHIELD is designed with privacy as a core principle. We do NOT collect, store, or transmit any of your scanned URLs or personal data.
        
        All URL analysis happens ENTIRELY on your device using our offline phishing detection engine. No internet connection is required for scanning.
        
        2. OFFLINE GUARANTEE
        -------------------
        When "Strict Offline Mode" is enabled (default), the app operates completely without network access. This means:
        • No URLs are ever sent to any server
        • No analytics or tracking data is transmitted
        • All threat intelligence is contained within the app
        
        3. ANONYMOUS TELEMETRY
        ---------------------
        If you opt-in to Anonymous Telemetry, we may collect:
        • Aggregated detection statistics (no URLs)
        • Crash reports (no personal information)
        • Feature usage patterns
        
        This data is fully anonymized and used only to improve detection accuracy.
        
        4. THIRD-PARTY SERVICES
        ----------------------
        QR-SHIELD does not integrate with any third-party analytics, advertising, or tracking services.
        
        5. CONTACT
        ----------
        For privacy inquiries: privacy@qr-shield.app
        """
    }
    
    private var licensesText: String {
        """
        Open Source Licenses
        ====================
        
        QR-SHIELD uses the following open source components:
        
        Swift Standard Library
        ----------------------
        Copyright © Apple Inc.
        Licensed under Apache License 2.0
        
        Kotlin Multiplatform
        --------------------
        Copyright © JetBrains s.r.o.
        Licensed under Apache License 2.0
        
        QR-SHIELD Core Engine
        ---------------------
        Copyright © 2025-2026 QR-SHIELD Contributors
        Licensed under Apache License 2.0
        
        Full license texts are available at:
        https://opensource.org/licenses/Apache-2.0
        
        ---
        
        QR-SHIELD is open source software.
        View the source code at: github.com/Raoof128
        """
    }
    
    private var acknowledgementsText: String {
        """
        Acknowledgements
        ================
        
        QR-SHIELD was created for the KotlinConf '26 Student Coding Competition.
        
        SPECIAL THANKS
        --------------
        • JetBrains for Kotlin Multiplatform
        • Apple for SwiftUI and iOS development tools
        • The open source security research community
        • APWG for phishing data resources
        • PhishTank for threat intelligence inspiration
        
        DESIGN INSPIRATION
        ------------------
        • Liquid Glass UI design system
        • Modern cybersecurity dashboard aesthetics
        • iOS Human Interface Guidelines
        
        CONTRIBUTORS
        ------------
        This project was developed by passionate students dedicated to making the internet safer for everyone.
        
        ---
        
        QR-SHIELD: Scan. Detect. Protect.
        """
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
                    Button(NSLocalizedString("common.done", comment: "")) {
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
                
                Button(NSLocalizedString("common.cancel", comment: ""), role: .cancel) {
                    newDomain = ""
                }
                
                Button(NSLocalizedString("common.add", comment: "")) {
                    if !newDomain.isEmpty && !domains.contains(newDomain) {
                        domains.append(newDomain)
                        newDomain = ""
                    }
                }
            }
        }
    }
}

// MARK: - Info Sheet

struct InfoSheet: View {
    let title: String
    let content: String
    
    @Environment(\.dismiss) private var dismiss
    
    var body: some View {
        NavigationStack {
            ScrollView {
                Text(content)
                    .font(.system(.body, design: .monospaced))
                    .foregroundColor(.textSecondary)
                    .padding(20)
                    .textSelection(.enabled)
            }
            .scrollContentBackground(.hidden)
            .background {
                LiquidGlassBackground()
                    .ignoresSafeArea()
            }
            .navigationTitle(title)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(NSLocalizedString("common.done", comment: "")) {
                        dismiss()
                    }
                    .foregroundColor(.brandPrimary)
                }
            }
        }
    }
}

#endif
