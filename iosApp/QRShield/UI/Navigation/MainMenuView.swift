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

// UI/Navigation/MainMenuView.swift
// QR-SHIELD Main Menu - iOS 17+ Liquid Glass Edition
//
// Provides navigation to all app sections:
// - Dashboard
// - Scanner
// - History (Threat Monitor)
// - Trust Centre
// - Training (Beat the Bot)
// - Export Report
// - Settings

import SwiftUI
#if os(iOS)

// MARK: - Menu Item

struct MenuItem: Identifiable {
    let id = UUID()
    let titleKey: String
    let subtitleKey: String
    let icon: String
    let iconColor: Color
    let destination: MenuDestination
    let badge: String?
    
    init(titleKey: String, subtitleKey: String, icon: String, iconColor: Color, destination: MenuDestination, badge: String? = nil) {
        self.titleKey = titleKey
        self.subtitleKey = subtitleKey
        self.icon = icon
        self.iconColor = iconColor
        self.destination = destination
        self.badge = badge
    }
    
    var title: String { NSLocalizedString(titleKey, comment: "") }
    var subtitle: String { NSLocalizedString(subtitleKey, comment: "") }
}

enum MenuDestination {
    case dashboard
    case scanner
    case history
    case threatHistory
    case trustCentre
    case training
    case export
    case settings
}

// MARK: - Main Menu View

@available(iOS 17, *)
struct MainMenuView: View {
    @Environment(\.dismiss) private var dismiss
    @State private var selectedDestination: MenuDestination?
    @AppStorage("useDarkMode") private var useDarkMode = true
    
    private let menuItems: [MenuItem] = [
        MenuItem(
            titleKey: "nav.menu.dashboard",
            subtitleKey: "nav.menu.dashboard_desc",
            icon: "square.grid.2x2.fill",
            iconColor: .brandPrimary,
            destination: .dashboard
        ),
        MenuItem(
            titleKey: "nav.menu.scan_qr",
            subtitleKey: "nav.menu.scan_qr_desc",
            icon: "qrcode.viewfinder",
            iconColor: .brandSecondary,
            destination: .scanner
        ),
        MenuItem(
            titleKey: "nav.menu.scan_history",
            subtitleKey: "nav.menu.scan_history_desc",
            icon: "clock.fill",
            iconColor: .brandAccent,
            destination: .history
        ),
        MenuItem(
            titleKey: "nav.menu.threat_monitor",
            subtitleKey: "nav.menu.threat_monitor_desc",
            icon: "shield.slash.fill",
            iconColor: .verdictDanger,
            destination: .threatHistory,
            badge: "3"
        ),
        MenuItem(
            titleKey: "nav.menu.trust_centre",
            subtitleKey: "nav.menu.trust_centre_desc",
            icon: "lock.shield.fill",
            iconColor: .verdictSafe,
            destination: .trustCentre
        ),
        MenuItem(
            titleKey: "nav.menu.beat_the_bot",
            subtitleKey: "nav.menu.beat_the_bot_desc",
            icon: "gamecontroller.fill",
            iconColor: .verdictWarning,
            destination: .training
        ),
        MenuItem(
            titleKey: "nav.menu.export_report",
            subtitleKey: "nav.menu.export_report_desc",
            icon: "doc.text.fill",
            iconColor: .textSecondary,
            destination: .export
        ),
        MenuItem(
            titleKey: "nav.menu.settings",
            subtitleKey: "nav.menu.settings_desc",
            icon: "gearshape.fill",
            iconColor: .textMuted,
            destination: .settings
        )
    ]
    
    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 24) {
                    // Header
                    headerSection
                    
                    // Menu Grid
                    menuGrid
                    
                    // Quick Actions
                    quickActionsSection
                    
                    // Footer
                    footerSection
                }
                .padding(.horizontal, 20)
                .padding(.top, 20)
                .padding(.bottom, 40)
            }
            .scrollContentBackground(.hidden)
            .background {
                LiquidGlassBackground()
                    .ignoresSafeArea()
            }
            .navigationTitle("")
            .navigationBarHidden(true)
            .navigationDestination(item: $selectedDestination) { destination in
                destinationView(for: destination)
            }
        }
    }
    
    // MARK: - Header Section
    
    private var headerSection: some View {
        HStack {
            VStack(alignment: .leading, spacing: 4) {
                HStack(spacing: 8) {
                    Image("Logo")
                        .resizable()
                        .scaledToFit()
                        .frame(width: 32, height: 32)
                    
                    Text(NSLocalizedString("app.name", comment: ""))
                        .font(.title2.weight(.bold))
                        .foregroundColor(.textPrimary)
                }
                
                Text(NSLocalizedString("dashboard.hero.tagline", comment: ""))
                    .font(.caption)
                    .foregroundColor(.textMuted)
            }
            
            Spacer()
            
            Button {
                dismiss()
            } label: {
                Image(systemName: "xmark")
                    .font(.title3)
                    .foregroundColor(.textSecondary)
                    .frame(width: 36, height: 36)
                    .background(Color.bgSurface, in: Circle())
            }
        }
    }
    
    // MARK: - Menu Grid
    
    private var menuGrid: some View {
        LazyVGrid(columns: [
            GridItem(.flexible()),
            GridItem(.flexible())
        ], spacing: 12) {
            ForEach(menuItems) { item in
                menuCard(item)
            }
        }
    }
    
    private func menuCard(_ item: MenuItem) -> some View {
        Button {
            selectedDestination = item.destination
            SettingsManager.shared.triggerHaptic(.light)
        } label: {
            VStack(alignment: .leading, spacing: 12) {
                HStack {
                    ZStack {
                        RoundedRectangle(cornerRadius: 10)
                            .fill(item.iconColor.opacity(0.15))
                            .frame(width: 40, height: 40)
                        
                        Image(systemName: item.icon)
                            .font(.system(size: 16))
                            .foregroundColor(item.iconColor)
                    }
                    
                    Spacer()
                    
                    if let badge = item.badge {
                        Text(badge)
                            .font(.caption2.weight(.bold))
                            .foregroundColor(.white)
                            .padding(.horizontal, 6)
                            .padding(.vertical, 3)
                            .background(Color.verdictDanger, in: Capsule())
                    }
                }
                
                VStack(alignment: .leading, spacing: 2) {
                    Text(item.title)
                        .font(.subheadline.weight(.semibold))
                        .foregroundColor(.textPrimary)
                    
                    Text(item.subtitle)
                        .font(.caption)
                        .foregroundColor(.textMuted)
                }
            }
            .padding(14)
            .frame(maxWidth: .infinity, alignment: .leading)
            .liquidGlass(cornerRadius: 16)
        }
    }
    
    // MARK: - Quick Actions
    
    @State private var showImagePicker = false
    @State private var pastedURL: String?
    
    private var quickActionsSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text(NSLocalizedString("nav.quick_actions", comment: ""))
                .font(.caption.weight(.bold))
                .foregroundColor(.textMuted)
                .tracking(1)
            
            HStack(spacing: 12) {
                quickActionButton(
                    icon: "qrcode.viewfinder",
                    title: NSLocalizedString("nav.quick.scan", comment: ""),
                    color: .brandPrimary
                ) {
                    selectedDestination = .scanner
                }
                
                quickActionButton(
                    icon: "photo.on.rectangle",
                    title: NSLocalizedString("nav.quick.import", comment: ""),
                    color: .brandSecondary
                ) {
                    showImagePicker = true
                    SettingsManager.shared.triggerHaptic(.light)
                }
                
                quickActionButton(
                    icon: "doc.text.viewfinder",
                    title: NSLocalizedString("nav.quick.paste_url", comment: ""),
                    color: .brandAccent
                ) {
                    // Get URL from clipboard and validate it
                    if let clipboardString = UIPasteboard.general.string,
                       !clipboardString.isEmpty {
                        // Validate that it looks like a URL
                        let trimmed = clipboardString.trimmingCharacters(in: .whitespacesAndNewlines)
                        var isValidURL = false
                        
                        // Check if it has a valid scheme
                        if let url = URL(string: trimmed),
                           let scheme = url.scheme?.lowercased(),
                           (scheme == "http" || scheme == "https"),
                           url.host != nil {
                            isValidURL = true
                        } else if let url = URL(string: "https://\(trimmed)"),
                                  url.host != nil {
                            // Accept URLs without scheme (will be normalized)
                            isValidURL = true
                        }
                        
                        if isValidURL {
                            // Navigate to dashboard with pasted URL
                            selectedDestination = .dashboard
                            SettingsManager.shared.triggerHaptic(.success)
                        } else {
                            // Invalid URL format
                            SettingsManager.shared.triggerHaptic(.error)
                        }
                    } else {
                        // Empty clipboard
                        SettingsManager.shared.triggerHaptic(.error)
                    }
                }
            }
        }
        .sheet(isPresented: $showImagePicker) {
            ImagePicker { image in
                // Analyze the image for QR codes
                ScannerViewModel.shared.analyzeImage(image)
            }
            .preferredColorScheme(useDarkMode ? .dark : .light)
        }
    }
    
    private func quickActionButton(
        icon: String,
        title: String,
        color: Color,
        action: @escaping () -> Void
    ) -> some View {
        Button(action: action) {
            VStack(spacing: 8) {
                Image(systemName: icon)
                    .font(.title2)
                    .foregroundColor(color)
                
                Text(title)
                    .font(.caption)
                    .foregroundColor(.textSecondary)
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 16)
            .liquidGlass(cornerRadius: 14)
        }
    }
    
    // MARK: - Footer
    
    private var footerSection: some View {
        VStack(spacing: 8) {
            HStack(spacing: 6) {
                Circle()
                    .fill(.green)
                    .frame(width: 8, height: 8)
                    .shadow(color: .green, radius: 3)
                
                Text(NSLocalizedString("nav.systems_operational", comment: ""))
                    .font(.caption)
                    .foregroundColor(.textSecondary)
            }
            
            Text(NSLocalizedString("nav.version_offline", comment: ""))
                .font(.caption2)
                .foregroundColor(.textMuted)
        }
        .padding(.top, 20)
    }
    
    // MARK: - Destination View
    
    @ViewBuilder
    private func destinationView(for destination: MenuDestination) -> some View {
        switch destination {
        case .dashboard:
            DashboardView()
        case .scanner:
            ScannerView()
        case .history:
            HistoryView()
        case .threatHistory:
            ThreatHistoryView()
        case .trustCentre:
            TrustCentreView()
        case .training:
            BeatTheBotView()
        case .export:
            ReportExportView()
        case .settings:
            SettingsView()
        }
    }
}

// MARK: - MenuDestination Hashable Conformance

extension MenuDestination: Hashable {}

#endif
