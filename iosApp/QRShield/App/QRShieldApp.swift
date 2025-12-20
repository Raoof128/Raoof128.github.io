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

// App/QRShieldApp.swift
// QR-SHIELD iOS Application - iOS 17+ Liquid Glass Edition
//
// UPDATED: December 2025 - iOS 17+ Compatible
// - Liquid Glass system integration
// - Enhanced tab bar with glass styling
// - Scene phase handling
// - User preference support

#if os(iOS)
import SwiftUI
import UIKit

@main
struct QRShieldApp: App {
    @State private var hasCompletedOnboarding = UserDefaults.standard.bool(forKey: "hasCompletedOnboarding")
    @Environment(\.scenePhase) private var scenePhase
    @AppStorage("useDarkMode") private var useDarkMode = true
    
    /// Deep link action from widget
    @State private var shouldOpenScanner = false
    
    init() {
        configureAppearance()
    }
    
    var body: some Scene {
        WindowGroup {
            Group {
                if hasCompletedOnboarding {
                    ContentView(shouldOpenScanner: $shouldOpenScanner)
                } else {
                    OnboardingView(isComplete: $hasCompletedOnboarding)
                        .onChange(of: hasCompletedOnboarding) { _, newValue in
                            UserDefaults.standard.set(newValue, forKey: "hasCompletedOnboarding")
                        }
                }
            }
            .preferredColorScheme(useDarkMode ? .dark : .light)
            .onOpenURL { url in
                handleDeepLink(url)
            }
        }
        .onChange(of: scenePhase) { _, newPhase in
            handleScenePhase(newPhase)
        }
    }
    
    // MARK: - Deep Link Handling
    
    /// Handle deep links from widgets and shortcuts.
    /// URL scheme: qrshield://scan
    private func handleDeepLink(_ url: URL) {
        guard url.scheme == "qrshield" else { return }
        
        switch url.host {
        case "scan":
            // Navigate to scanner tab
            shouldOpenScanner = true
        default:
            break
        }
    }
    
    // MARK: - Scene Phase Handling
    
    private func handleScenePhase(_ phase: ScenePhase) {
        switch phase {
        case .active:
            // App became active - resume camera if needed
            break
        case .inactive:
            // App is transitioning - pause camera
            break
        case .background:
            // App went to background - save state
            break
        @unknown default:
            break
        }
    }
    
    // MARK: - Appearance Configuration (iOS 17+)
    
    private func configureAppearance() {
        // iOS 17+: Liquid Glass navigation bar
        let navAppearance = UINavigationBarAppearance()
        navAppearance.configureWithTransparentBackground()
        navAppearance.backgroundEffect = UIBlurEffect(style: .systemUltraThinMaterialDark)
        navAppearance.backgroundColor = UIColor(Color.bgDark.opacity(0.3))
        
        // Title styling
        navAppearance.titleTextAttributes = [
            .foregroundColor: UIColor.white,
            .font: UIFont.systemFont(ofSize: 17, weight: .semibold)
        ]
        navAppearance.largeTitleTextAttributes = [
            .foregroundColor: UIColor.white,
            .font: UIFont.systemFont(ofSize: 34, weight: .bold)
        ]
        
        // Shadow line removal for glass effect
        navAppearance.shadowColor = .clear
        navAppearance.shadowImage = UIImage()
        
        UINavigationBar.appearance().standardAppearance = navAppearance
        UINavigationBar.appearance().scrollEdgeAppearance = navAppearance
        UINavigationBar.appearance().compactAppearance = navAppearance
        UINavigationBar.appearance().tintColor = UIColor(Color.brandPrimary)
        
        // iOS 17+: Liquid Glass tab bar
        let tabAppearance = UITabBarAppearance()
        tabAppearance.configureWithTransparentBackground()
        tabAppearance.backgroundEffect = UIBlurEffect(style: .systemUltraThinMaterialDark)
        tabAppearance.backgroundColor = UIColor(Color.bgDark.opacity(0.3))
        
        // Remove separator line
        tabAppearance.shadowColor = .clear
        tabAppearance.shadowImage = UIImage()
        
        // Tab bar item styling
        let itemAppearance = UITabBarItemAppearance()
        itemAppearance.normal.iconColor = UIColor(Color.textMuted)
        itemAppearance.normal.titleTextAttributes = [
            .foregroundColor: UIColor(Color.textMuted),
            .font: UIFont.systemFont(ofSize: 10, weight: .medium)
        ]
        itemAppearance.selected.iconColor = UIColor(Color.brandPrimary)
        itemAppearance.selected.titleTextAttributes = [
            .foregroundColor: UIColor(Color.brandPrimary),
            .font: UIFont.systemFont(ofSize: 10, weight: .semibold)
        ]
        
        tabAppearance.stackedLayoutAppearance = itemAppearance
        tabAppearance.inlineLayoutAppearance = itemAppearance
        tabAppearance.compactInlineLayoutAppearance = itemAppearance
        
        UITabBar.appearance().standardAppearance = tabAppearance
        UITabBar.appearance().scrollEdgeAppearance = tabAppearance
        
        // iOS 17+: Tint color for system controls
        UIView.appearance(whenContainedInInstancesOf: [UIAlertController.self]).tintColor = UIColor(Color.brandPrimary)
    }
}

// MARK: - Root Content View (iOS 17+)

struct ContentView: View {
    @State private var selectedTab = 0
    @AppStorage("autoScan") private var autoScan = true
    @State private var showMainMenu = false
    @State private var showTrustCentre = false
    @State private var showExport = false
    
    /// Binding to trigger scanner from widget deep link
    @Binding var shouldOpenScanner: Bool
    
    var body: some View {
        TabView(selection: $selectedTab) {
            // Dashboard Tab
            NavigationStack {
                DashboardView()
            }
            .tabItem {
                Label("Dashboard", systemImage: selectedTab == 0 ? "square.grid.2x2.fill" : "square.grid.2x2")
            }
            .tag(0)
            
            // Scan Tab
            NavigationStack {
                ScannerView()
            }
            .tabItem {
                Label("Scan", systemImage: selectedTab == 1 ? "qrcode.viewfinder" : "qrcode")
            }
            .tag(1)
            
            // History Tab
            NavigationStack {
                HistoryView()
            }
            .tabItem {
                Label("History", systemImage: selectedTab == 2 ? "clock.fill" : "clock")
            }
            .tag(2)
            
            // Training Tab (Beat the Bot)
            NavigationStack {
                BeatTheBotView()
            }
            .tabItem {
                Label("Training", systemImage: selectedTab == 3 ? "gamecontroller.fill" : "gamecontroller")
            }
            .tag(3)
            
            // Settings Tab
            NavigationStack {
                SettingsView()
                    .toolbar {
                        ToolbarItem(placement: .navigationBarTrailing) {
                            Button {
                                showTrustCentre = true
                            } label: {
                                Image(systemName: "lock.shield")
                                    .foregroundColor(.brandPrimary)
                            }
                        }
                    }
            }
            .tabItem {
                Label("Settings", systemImage: selectedTab == 4 ? "gearshape.fill" : "gearshape")
            }
            .tag(4)
        }
        .tint(.brandPrimary)
        .sensoryFeedback(.selection, trigger: selectedTab)
        .onAppear {
            // Log app launch
            #if DEBUG
            print("🛡️ QR-SHIELD launched - iOS 17+")
            #endif
        }
        .onChange(of: shouldOpenScanner) { _, shouldOpen in
            if shouldOpen {
                selectedTab = 1  // Navigate to Scanner tab
                shouldOpenScanner = false  // Reset
            }
        }
        .sheet(isPresented: $showMainMenu) {
            MainMenuView()
        }
        .sheet(isPresented: $showTrustCentre) {
            TrustCentreView()
        }
        .sheet(isPresented: $showExport) {
            ReportExportView()
        }
    }
}

#endif
