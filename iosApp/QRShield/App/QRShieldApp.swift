// App/QRShieldApp.swift
// QR-SHIELD iOS Application - iOS 26.2 Liquid Glass Edition
//
// UPDATED: December 2025 - iOS 26.2 RC
// - Liquid Glass system integration
// - Enhanced tab bar with glass styling
// - Scene phase handling
// - App shortcuts support ready

import SwiftUI

@main
struct QRShieldApp: App {
    @State private var hasCompletedOnboarding = UserDefaults.standard.bool(forKey: "hasCompletedOnboarding")
    @Environment(\.scenePhase) private var scenePhase
    
    init() {
        configureAppearance()
    }
    
    var body: some Scene {
        WindowGroup {
            Group {
                if hasCompletedOnboarding {
                    ContentView()
                } else {
                    OnboardingView(isComplete: $hasCompletedOnboarding)
                        .onChange(of: hasCompletedOnboarding) { _, newValue in
                            UserDefaults.standard.set(newValue, forKey: "hasCompletedOnboarding")
                        }
                }
            }
            .preferredColorScheme(.dark)
        }
        .onChange(of: scenePhase) { _, newPhase in
            handleScenePhase(newPhase)
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
    
    // MARK: - Appearance Configuration (iOS 26.2)
    
    private func configureAppearance() {
        // iOS 26.2: Liquid Glass navigation bar
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
        
        // iOS 26.2: Liquid Glass tab bar
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
        
        // iOS 26.2: Tint color for system controls
        UIView.appearance(whenContainedInInstancesOf: [UIAlertController.self]).tintColor = UIColor(Color.brandPrimary)
    }
}

// MARK: - Root Content View (iOS 26.2)

struct ContentView: View {
    @State private var selectedTab = 0
    @AppStorage("autoScan") private var autoScan = true
    
    var body: some View {
        TabView(selection: $selectedTab) {
            // Scan Tab
            NavigationStack {
                ScannerView()
            }
            .tabItem {
                Label("Scan", systemImage: selectedTab == 0 ? "qrcode.viewfinder" : "qrcode")
            }
            .tag(0)
            
            // History Tab
            NavigationStack {
                HistoryView()
            }
            .tabItem {
                Label("History", systemImage: selectedTab == 1 ? "clock.fill" : "clock")
            }
            .tag(1)
            
            // Settings Tab
            NavigationStack {
                SettingsView()
            }
            .tabItem {
                Label("Settings", systemImage: selectedTab == 2 ? "gearshape.fill" : "gearshape")
            }
            .tag(2)
        }
        .tint(.brandPrimary)
        .sensoryFeedback(.selection, trigger: selectedTab)
        .onAppear {
            // Log app launch
            #if DEBUG
            print("🛡️ QR-SHIELD launched - iOS 26.2")
            #endif
        }
    }
}

// MARK: - App Shortcuts (iOS 26.2 Ready)

/*
 iOS 26.2 App Shortcuts can be added here for Siri integration:
 
 @AppShortcutsProvider
 struct QRShieldShortcuts: AppShortcutsProvider {
     static var appShortcuts: [AppShortcut] {
         AppShortcut(
             intent: ScanQRCodeIntent(),
             phrases: ["Scan a QR code with \(.applicationName)"],
             shortTitle: "Scan QR",
             systemImageName: "qrcode.viewfinder"
         )
     }
 }
 */

// MARK: - Preview

#Preview("Main App") {
    ContentView()
}

#Preview("Onboarding") {
    OnboardingView(isComplete: .constant(false))
}
