// App/QRShieldApp.swift
// QR-SHIELD iOS Application - iOS 26 Liquid Glass Edition
//
// UPDATED: December 2025 - iOS 26 / Xcode 26
// - Liquid Glass system integration
// - Modern app lifecycle
// - Enhanced tab bar styling

import SwiftUI

@main
struct QRShieldApp: App {
    @State private var hasCompletedOnboarding = UserDefaults.standard.bool(forKey: "hasCompletedOnboarding")
    
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
    }
    
    private func configureAppearance() {
        // iOS 26: Liquid Glass navigation bar
        let navAppearance = UINavigationBarAppearance()
        navAppearance.configureWithTransparentBackground()
        navAppearance.backgroundEffect = UIBlurEffect(style: .systemUltraThinMaterialDark)
        navAppearance.backgroundColor = UIColor(Color.bgDark.opacity(0.5))
        
        navAppearance.titleTextAttributes = [
            .foregroundColor: UIColor.white,
            .font: UIFont.systemFont(ofSize: 17, weight: .semibold)
        ]
        navAppearance.largeTitleTextAttributes = [
            .foregroundColor: UIColor.white,
            .font: UIFont.systemFont(ofSize: 34, weight: .bold)
        ]
        
        UINavigationBar.appearance().standardAppearance = navAppearance
        UINavigationBar.appearance().scrollEdgeAppearance = navAppearance
        UINavigationBar.appearance().compactAppearance = navAppearance
        UINavigationBar.appearance().tintColor = UIColor(Color.brandPrimary)
        
        // iOS 26: Liquid Glass tab bar
        let tabAppearance = UITabBarAppearance()
        tabAppearance.configureWithTransparentBackground()
        tabAppearance.backgroundEffect = UIBlurEffect(style: .systemUltraThinMaterialDark)
        tabAppearance.backgroundColor = UIColor(Color.bgDark.opacity(0.5))
        
        // Tab bar item styling
        let itemAppearance = UITabBarItemAppearance()
        itemAppearance.normal.iconColor = UIColor.gray
        itemAppearance.normal.titleTextAttributes = [.foregroundColor: UIColor.gray]
        itemAppearance.selected.iconColor = UIColor(Color.brandPrimary)
        itemAppearance.selected.titleTextAttributes = [.foregroundColor: UIColor(Color.brandPrimary)]
        
        tabAppearance.stackedLayoutAppearance = itemAppearance
        tabAppearance.inlineLayoutAppearance = itemAppearance
        tabAppearance.compactInlineLayoutAppearance = itemAppearance
        
        UITabBar.appearance().standardAppearance = tabAppearance
        UITabBar.appearance().scrollEdgeAppearance = tabAppearance
    }
}

// MARK: - Root Content View (iOS 26)

struct ContentView: View {
    @State private var selectedTab = 0
    
    var body: some View {
        TabView(selection: $selectedTab) {
            // Scan Tab
            NavigationStack {
                ScannerView()
            }
            .tabItem {
                Label("Scan", systemImage: "qrcode.viewfinder")
            }
            .tag(0)
            
            // History Tab
            NavigationStack {
                HistoryView()
            }
            .tabItem {
                Label("History", systemImage: "clock.fill")
            }
            .tag(1)
            
            // Settings Tab
            NavigationStack {
                SettingsView()
            }
            .tabItem {
                Label("Settings", systemImage: "gearshape.fill")
            }
            .tag(2)
        }
        .tint(.brandPrimary)
        .sensoryFeedback(.selection, trigger: selectedTab)
    }
}

// MARK: - Preview

#Preview("Main App") {
    ContentView()
}

#Preview("Onboarding") {
    OnboardingView(isComplete: .constant(false))
}
