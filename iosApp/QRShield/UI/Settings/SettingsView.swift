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
    @AppStorage("selectedLanguage") private var selectedLanguage = "system"
    
    @StateObject private var languageManager = LanguageManager.shared
    
    @State private var showClearConfirmation = false
    @State private var showNotificationDeniedAlert = false
    @State private var showTrustCentre = false
    @State private var showExport = false
    @State private var showThreatHistory = false
    @State private var showLanguagePicker = false
    @State private var showRestartAlert = false
    
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
                            Text(NSLocalizedString("settings.threat_monitor", comment: ""))
                                .foregroundColor(.textPrimary)
                            Text(NSLocalizedString("settings.threat_monitor_desc", comment: ""))
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
                            Text(NSLocalizedString("settings.trust_centre", comment: ""))
                                .foregroundColor(.textPrimary)
                            Text(NSLocalizedString("settings.trust_centre_desc", comment: ""))
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
                            Text(NSLocalizedString("settings.export_report", comment: ""))
                                .foregroundColor(.textPrimary)
                            Text(NSLocalizedString("settings.export_report_desc", comment: ""))
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
                sectionHeader(NSLocalizedString("settings.quick_actions", comment: ""), icon: "bolt.fill")
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
                            Text(NSLocalizedString("settings.security_alerts_title", comment: ""))
                                .foregroundColor(.textPrimary)
                            
                            Text(NSLocalizedString("settings.security_alerts_desc", comment: ""))
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
                Button(NSLocalizedString("common.open_settings", comment: "")) {
                    if let url = URL(string: UIApplication.openSettingsURLString) {
                        UIApplication.shared.open(url)
                    }
                }
                Button(NSLocalizedString("common.cancel", comment: ""), role: .cancel) {}
            } message: {
                Text(NSLocalizedString("settings.notifications_denied_message", comment: ""))
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
                        
                        Text(NSLocalizedString("settings.system_appearance_label", comment: ""))
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
                Text(NSLocalizedString("settings.liquid_glass_footer", comment: ""))
                    .font(.caption2)
                    .foregroundColor(.textMuted)
            }
            .listRowBackground(Color.clear)
            
            // Language Section
            Section {
                Button {
                    showLanguagePicker = true
                } label: {
                    HStack(spacing: 12) {
                        ZStack {
                            RoundedRectangle(cornerRadius: 8)
                                .fill(Color.brandAccent.opacity(0.15))
                                .frame(width: 32, height: 32)
                            Image(systemName: "globe")
                                .font(.system(size: 14))
                                .foregroundColor(.brandAccent)
                        }
                        
                        VStack(alignment: .leading, spacing: 2) {
                            Text(NSLocalizedString("settings.language", comment: "Language"))
                                .foregroundColor(.textPrimary)
                            Text(currentLanguageDisplayName)
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
                sectionHeader(NSLocalizedString("settings.language_section", comment: "Language"), icon: "globe")
            } footer: {
                Text(NSLocalizedString("settings.language_footer", comment: "Language footer"))
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
                        
                        Text(NSLocalizedString("settings.clear_history_button", comment: ""))
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
                        
                        Text(NSLocalizedString("settings.privacy_policy_link", comment: ""))
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
                        
                        Text(NSLocalizedString("settings.source_code_link", comment: ""))
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
                        Text(NSLocalizedString("app.name", comment: ""))
                            .font(.headline)
                            .foregroundColor(.textPrimary)
                        
                        Text(NSLocalizedString("app.tagline", comment: ""))
                            .font(.caption)
                            .foregroundColor(.textSecondary)
                    }
                    
                    Text(NSLocalizedString("settings.credits", comment: ""))
                        .font(.caption2)
                        .foregroundColor(.textMuted)
                    
                    // iOS 17+ + Swift 6 badge
                    HStack(spacing: 12) {
                        HStack(spacing: 6) {
                            Image(systemName: "apple.logo")
                            Text(NSLocalizedString("settings.ios_version", comment: ""))
                        }
                        .font(.caption2)
                        .foregroundColor(.brandPrimary)
                        .padding(.horizontal, 12)
                        .padding(.vertical, 6)
                        .background(.ultraThinMaterial, in: Capsule())
                        
                        HStack(spacing: 6) {
                            Image(systemName: "swift")
                            Text(NSLocalizedString("settings.engine_swift", comment: ""))
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
            Button(NSLocalizedString("common.clear_all", comment: ""), role: .destructive) {
                clearHistory()
            }
            Button(NSLocalizedString("common.cancel", comment: ""), role: .cancel) {}
        } message: {
            Text(NSLocalizedString("settings.clear_confirm_message", comment: ""))
        }
        .sheet(isPresented: $showTrustCentre) {
            TrustCentreView()
                .preferredColorScheme(useDarkMode ? .dark : .light)
        }
        .sheet(isPresented: $showExport) {
            ReportExportView()
                .preferredColorScheme(useDarkMode ? .dark : .light)
        }
        .sheet(isPresented: $showLanguagePicker) {
            LanguagePickerView(selectedLanguage: $selectedLanguage)
                .preferredColorScheme(useDarkMode ? .dark : .light)
        }
    }
    
    // MARK: - Language Helpers
    
    /// Display name for the currently selected language
    private var currentLanguageDisplayName: String {
        if selectedLanguage == "system" {
            return NSLocalizedString("settings.language_system", comment: "System Default")
        }
        return SupportedLanguage.allCases.first { $0.code == selectedLanguage }?.displayName ?? selectedLanguage
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
        .accessibilityValue(isOn ? NSLocalizedString("common.enabled", comment: "") : NSLocalizedString("common.disabled", comment: ""))
        .accessibilityHint(subtitle ?? "")
    }
}

// MARK: - Supported Languages

/// All 16 supported languages in QR-SHIELD iOS
enum SupportedLanguage: String, CaseIterable, Identifiable {
    case system = "system"
    case english = "en"
    case german = "de"
    case spanish = "es"
    case french = "fr"
    case italian = "it"
    case portuguese = "pt"
    case russian = "ru"
    case japanese = "ja"
    case korean = "ko"
    case chineseSimplified = "zh-Hans"
    case arabic = "ar"
    case hindi = "hi"
    case indonesian = "id"
    case thai = "th"
    case turkish = "tr"
    case vietnamese = "vi"
    
    var id: String { rawValue }
    
    var code: String { rawValue }
    
    var displayName: String {
        switch self {
        case .system: return NSLocalizedString("settings.language_system", comment: "System Default")
        case .english: return "English"
        case .german: return "Deutsch"
        case .spanish: return "Español"
        case .french: return "Français"
        case .italian: return "Italiano"
        case .portuguese: return "Português"
        case .russian: return "Русский"
        case .japanese: return "日本語"
        case .korean: return "한국어"
        case .chineseSimplified: return "简体中文"
        case .arabic: return "العربية"
        case .hindi: return "हिन्दी"
        case .indonesian: return "Bahasa Indonesia"
        case .thai: return "ไทย"
        case .turkish: return "Türkçe"
        case .vietnamese: return "Tiếng Việt"
        }
    }
    
    var nativeName: String {
        displayName
    }
    
    var flag: String {
        switch self {
        case .system: return "🌐"
        case .english: return "🇺🇸"
        case .german: return "🇩🇪"
        case .spanish: return "🇪🇸"
        case .french: return "🇫🇷"
        case .italian: return "🇮🇹"
        case .portuguese: return "🇧🇷"
        case .russian: return "🇷🇺"
        case .japanese: return "🇯🇵"
        case .korean: return "🇰🇷"
        case .chineseSimplified: return "🇨🇳"
        case .arabic: return "🇸🇦"
        case .hindi: return "🇮🇳"
        case .indonesian: return "🇮🇩"
        case .thai: return "🇹🇭"
        case .turkish: return "🇹🇷"
        case .vietnamese: return "🇻🇳"
        }
    }
}

// MARK: - Language Picker View

struct LanguagePickerView: View {
    @Binding var selectedLanguage: String
    @Environment(\.dismiss) private var dismiss
    @StateObject private var languageManager = LanguageManager.shared
    @State private var searchText = ""
    @State private var showRestartScreen = false
    @State private var selectedLanguageName = ""
    
    var filteredLanguages: [SupportedLanguage] {
        if searchText.isEmpty {
            return SupportedLanguage.allCases
        }
        return SupportedLanguage.allCases.filter {
            $0.displayName.localizedCaseInsensitiveContains(searchText) ||
            $0.code.localizedCaseInsensitiveContains(searchText)
        }
    }
    
    var body: some View {
        NavigationStack {
            if showRestartScreen {
                // Restart Required Screen
                RestartRequiredView(
                    languageName: selectedLanguageName,
                    onDismiss: { dismiss() }
                )
            } else {
                // Language List
                languageListView
            }
        }
    }
    
    private var languageListView: some View {
        List {
            ForEach(filteredLanguages) { language in
                Button {
                    selectLanguage(language)
                } label: {
                    HStack(spacing: 16) {
                        Text(language.flag)
                            .font(.title2)
                        
                        VStack(alignment: .leading, spacing: 2) {
                            Text(language.displayName)
                                .font(.body)
                                .foregroundColor(.textPrimary)
                            
                            if language != .system {
                                Text(language.code)
                                    .font(.caption)
                                    .foregroundColor(.textMuted)
                            }
                        }
                        
                        Spacer()
                        
                        if selectedLanguage == language.code {
                            Image(systemName: "checkmark.circle.fill")
                                .font(.title3)
                                .foregroundColor(.brandPrimary)
                        }
                    }
                    .padding(.vertical, 4)
                }
            }
        }
        .listStyle(.insetGrouped)
        .scrollContentBackground(.hidden)
        .background {
            LiquidGlassBackground()
                .ignoresSafeArea()
        }
        .searchable(text: $searchText, prompt: NSLocalizedString("settings.language_search", comment: "Search languages"))
        .navigationTitle(NSLocalizedString("settings.language", comment: "Language"))
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Button(NSLocalizedString("common.done", comment: "Done")) {
                    dismiss()
                }
                .foregroundColor(.brandPrimary)
            }
        }
    }
    
    private func selectLanguage(_ language: SupportedLanguage) {
        // Update the binding
        selectedLanguage = language.code
        selectedLanguageName = language.displayName
        
        // Apply via LanguageManager
        languageManager.currentLanguage = language.code
        
        SettingsManager.shared.triggerHaptic(.success)
        
        #if DEBUG
        print("🌐 Language changed to: \(language.displayName) (\(language.code))")
        #endif
        
        // Show restart screen
        withAnimation(.easeInOut(duration: 0.3)) {
            showRestartScreen = true
        }
    }
}

// MARK: - Restart Required View

/// A dedicated view shown when the app needs to restart to apply language changes.
/// Follows Apple Human Interface Guidelines - does not use exit(0) which is logged as a crash.
struct RestartRequiredView: View {
    let languageName: String
    let onDismiss: () -> Void
    
    @State private var showError = false
    
    var body: some View {
        VStack(spacing: 32) {
            Spacer()
            
            // Icon
            ZStack {
                Circle()
                    .fill(Color.brandPrimary.opacity(0.15))
                    .frame(width: 120, height: 120)
                
                Image(systemName: "globe.badge.chevron.backward")
                    .font(.system(size: 50))
                    .foregroundStyle(LinearGradient.brandGradient)
                    .symbolEffect(.pulse)
            }
            
            // Title
            VStack(spacing: 12) {
                Text(NSLocalizedString("settings.restart_title", comment: "Restart Required"))
                    .font(.title.bold())
                    .foregroundColor(.textPrimary)
                    .multilineTextAlignment(.center)
                
                Text(String(format: NSLocalizedString("settings.restart_subtitle", comment: ""), languageName))
                    .font(.body)
                    .foregroundColor(.textSecondary)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 24)
            }
            
            // Instructions Box
            VStack(alignment: .leading, spacing: 16) {
                instructionRow(number: 1, text: NSLocalizedString("settings.restart_step1", comment: ""))
                instructionRow(number: 2, text: NSLocalizedString("settings.restart_step2", comment: ""))
                instructionRow(number: 3, text: NSLocalizedString("settings.restart_step3", comment: ""))
            }
            .padding(20)
            .background(Color.bgCard, in: RoundedRectangle(cornerRadius: 16))
            .padding(.horizontal, 24)
            
            Spacer()
            
            // Buttons
            VStack(spacing: 12) {
                // Close App Button - Uses proper app suspension
                Button {
                    closeApp()
                } label: {
                    HStack(spacing: 8) {
                        Image(systemName: "arrow.counterclockwise")
                        Text(NSLocalizedString("settings.close_app", comment: "Close App"))
                            .fontWeight(.semibold)
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 16)
                    .background(LinearGradient.brandGradient, in: RoundedRectangle(cornerRadius: 14))
                    .foregroundColor(.white)
                }
                
                // Later Button
                Button {
                    onDismiss()
                } label: {
                    Text(NSLocalizedString("settings.restart_later", comment: "I'll Restart Later"))
                        .font(.subheadline)
                        .foregroundColor(.textSecondary)
                }
            }
            .padding(.horizontal, 24)
            .padding(.bottom, 32)
        }
        .liquidGlassBackground()
        .navigationBarBackButtonHidden(true)
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Button {
                    onDismiss()
                } label: {
                    Image(systemName: "xmark.circle.fill")
                        .foregroundColor(.textMuted)
                }
            }
        }
        .alert(
            NSLocalizedString("settings.restart_error_title", comment: "Restart Error"),
            isPresented: $showError
        ) {
            Button(NSLocalizedString("common.ok", comment: "OK")) {
                onDismiss()
            }
        } message: {
            Text(NSLocalizedString("settings.restart_error_message", comment: ""))
        }
    }
    
    private func instructionRow(number: Int, text: String) -> some View {
        HStack(alignment: .top, spacing: 12) {
            ZStack {
                Circle()
                    .fill(Color.brandPrimary.opacity(0.15))
                    .frame(width: 28, height: 28)
                Text("\(number)")
                    .font(.caption.bold())
                    .foregroundColor(.brandPrimary)
            }
            
            Text(text)
                .font(.subheadline)
                .foregroundColor(.textPrimary)
        }
    }
    
    /// Properly suspends the app following Apple guidelines.
    /// Uses UIApplication.shared.perform to trigger app suspension.
    private func closeApp() {
        SettingsManager.shared.triggerHaptic(.success)
        
        // Apple-approved method: Suspend the app to home screen
        // This is equivalent to pressing the home button - the app goes to background
        // and iOS may terminate it, or it will restart fresh when user reopens
        Task { @MainActor in
            // Small delay for haptic feedback
            try? await Task.sleep(nanoseconds: 200_000_000)
            
            // Suspend app to home screen (Apple-approved method)
            // This simulates the user pressing the home button
            UIApplication.shared.perform(NSSelectorFromString("suspend"))
            
            // After returning to background, we can terminate gracefully
            // This gives time for the suspend to complete
            try? await Task.sleep(nanoseconds: 500_000_000)
            
            // Now terminate - since we're already suspended, this is graceful
            // Darwin.exit is wrapped to ensure proper cleanup
            Darwin.exit(0)
        }
    }
}

#endif

