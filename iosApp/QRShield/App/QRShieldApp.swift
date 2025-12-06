// App/QRShieldApp.swift
// QR-SHIELD iOS Application Entry Point - iOS 18+ / Swift 6
//
// UPDATED: December 2024
// - Modern app lifecycle handling
// - iOS 18 scene configuration
// - Proper privacy manifest compliance

import SwiftUI

@main
struct QRShieldApp: App {
    // iOS 17+: Use @State for app-level observable state
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
        // Navigation bar styling
        let navAppearance = UINavigationBarAppearance()
        navAppearance.configureWithOpaqueBackground()
        navAppearance.backgroundColor = UIColor(Color.bgDark)
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
        
        // Tab bar styling
        let tabAppearance = UITabBarAppearance()
        tabAppearance.configureWithOpaqueBackground()
        tabAppearance.backgroundColor = UIColor(Color.bgDark)
        
        // iOS 18: Enhanced tab bar item appearance
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
        
        // Sheet presentation styling
        if #available(iOS 16.4, *) {
            // Configure default sheet detent
        }
    }
}

// MARK: - Root Content View

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
        // iOS 18: Add sensory feedback for tab changes
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
