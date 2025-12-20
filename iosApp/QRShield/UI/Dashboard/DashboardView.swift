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

// UI/Dashboard/DashboardView.swift
// QR-SHIELD Dashboard - iOS 17+ Liquid Glass Edition
//
// Matches: Dashboard HTML design
// Features:
// - Hero section with tagline
// - URL input for analysis
// - Stats grid (Threats Blocked, Safe Scans)
// - Engine Features carousel
// - Recent Scans list
// - Threat Database status

import SwiftUI
#if os(iOS)

@available(iOS 17, *)
struct DashboardView: View {
    @State private var urlInput = ""
    @State private var isAnalyzing = false
    @State private var showScanner = false
    @State private var showImagePicker = false
    @State private var showResults = false
    @State private var analysisResult: RiskAssessmentMock?
    @State private var showThreatHistory = false
    @State private var showMainMenu = false
    
    @AppStorage("useDarkMode") private var useDarkMode = true
    
    // Real stats from history
    @State private var threatsBlocked = 0
    @State private var safeScans = 0
    @State private var recentHistory: [HistoryItemMock] = []
    
    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 24) {
                    // Hero Section
                    heroSection
                    
                    // System Health
                    systemHealthCard
                    
                    // Stats Grid
                    statsGrid
                    
                    // Engine Features
                    engineFeaturesSection
                    
                    // Recent Scans
                    recentScansSection
                    
                    // Threat Database
                    threatDatabaseCard
                }
                .padding(.horizontal, 16)
                .padding(.top, 20)
                .padding(.bottom, 100)
            }
            .scrollContentBackground(.hidden)
            .background {
                LiquidGlassBackground()
                    .ignoresSafeArea()
            }
            .navigationTitle("")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button {
                        showMainMenu = true
                        SettingsManager.shared.triggerHaptic(.light)
                    } label: {
                        HStack(spacing: 8) {
                            Image(systemName: "shield.fill")
                                .foregroundStyle(LinearGradient.brandGradient)
                            Text("QR-SHIELD")
                                .font(.headline)
                                .foregroundColor(.textPrimary)
                        }
                    }
                    .accessibilityLabel("Open main menu")
                }
                
                ToolbarItemGroup(placement: .navigationBarTrailing) {
                    Button {
                        withAnimation {
                            useDarkMode.toggle()
                        }
                        SettingsManager.shared.triggerHaptic(.light)
                    } label: {
                        Image(systemName: useDarkMode ? "moon.fill" : "sun.max.fill")
                            .foregroundColor(.brandPrimary)
                            .contentTransition(.symbolEffect(.replace))
                    }
                    
                    Button {
                        showThreatHistory = true
                    } label: {
                        ZStack(alignment: .topTrailing) {
                            Image(systemName: "bell")
                                .foregroundColor(.textSecondary)
                            
                            if threatsBlocked > 0 {
                                Circle()
                                    .fill(.red)
                                    .frame(width: 8, height: 8)
                                    .offset(x: 2, y: -2)
                            }
                        }
                    }
                }
            }
            .sheet(isPresented: $showScanner) {
                ScannerView()
            }
            .sheet(isPresented: $showThreatHistory) {
                ThreatHistoryView()
            }
            .sheet(isPresented: $showResults) {
                if let result = analysisResult {
                    ScanResultView(assessment: result)
                }
            }
            .sheet(isPresented: $showImagePicker) {
                ImagePicker { image in
                    // Analyze the imported image for QR codes
                    ScannerViewModel.shared.analyzeImage(image)
                }
            }
            .sheet(isPresented: $showMainMenu) {
                MainMenuView()
                    .preferredColorScheme(useDarkMode ? .dark : .light)
            }
            .onAppear {
                loadStats()
            }
        }
    }
    
    private func loadStats() {
        let history = HistoryStore.shared.getAllItems()
        recentHistory = Array(history.prefix(3))
        
        threatsBlocked = history.filter { $0.verdict == .malicious || $0.verdict == .suspicious }.count
        safeScans = history.filter { $0.verdict == .safe }.count
    }
    
    // MARK: - Hero Section
    
    private var heroSection: some View {
        VStack(alignment: .leading, spacing: 20) {
            // Enterprise Badge
            HStack(spacing: 8) {
                Image(systemName: "verified.user.fill")
                    .font(.caption)
                    .foregroundColor(.brandPrimary)
                
                Text("ENTERPRISE PROTECTION ACTIVE")
                    .font(.caption2.weight(.bold))
                    .foregroundColor(.brandPrimary)
                    .tracking(0.5)
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 8)
            .background(Color.brandPrimary.opacity(0.15), in: Capsule())
            
            // Tagline
            VStack(alignment: .leading, spacing: 4) {
                Text("Secure. Offline.")
                    .font(.system(size: 32, weight: .bold))
                    .foregroundColor(.textPrimary)
                
                Text("Explainable Defence.")
                    .font(.system(size: 32, weight: .bold))
                    .foregroundStyle(LinearGradient.brandGradient)
                    .fixedSize(horizontal: false, vertical: true)
            }
            .minimumScaleFactor(0.8)
            
            // Description
            Text("QR-SHIELD analyses potential threats directly on your hardware. Experience ")
                .foregroundColor(.textSecondary) +
            Text("zero-latency phishing detection")
                .foregroundColor(.textPrimary)
                .fontWeight(.semibold) +
            Text(" without compromising data privacy.")
                .foregroundColor(.textSecondary)
            
            // URL Input
            VStack(spacing: 16) {
                HStack(spacing: 12) {
                    Image(systemName: "magnifyingglass")
                        .foregroundColor(.textMuted)
                    
                    TextField("Paste URL to analyze...", text: $urlInput)
                        .font(.subheadline)
                        .foregroundColor(.textPrimary)
                        .textInputAutocapitalization(.never)
                        .autocorrectionDisabled()
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 14)
                .background(Color.bgSurface, in: RoundedRectangle(cornerRadius: 12))
                
                Button {
                    analyzeURL()
                } label: {
                    HStack(spacing: 8) {
                        if isAnalyzing {
                            ProgressView()
                                .tint(.white)
                        } else {
                            Image(systemName: "shield.fill")
                        }
                        Text("Analyze")
                            .fontWeight(.semibold)
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 14)
                    .background(LinearGradient.brandGradient, in: RoundedRectangle(cornerRadius: 12))
                    .foregroundColor(.white)
                }
                .disabled(urlInput.isEmpty || isAnalyzing)
            }
            .padding(16)
            .background(Color.bgCard, in: RoundedRectangle(cornerRadius: 16))
            
            // Action Buttons
            VStack(spacing: 12) {
                Button {
                    showScanner = true
                } label: {
                    HStack(spacing: 8) {
                        Image(systemName: "qrcode.viewfinder")
                        Text("Scan QR Code")
                            .fontWeight(.semibold)
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 16)
                    .background(LinearGradient.brandGradient, in: RoundedRectangle(cornerRadius: 14))
                    .foregroundColor(.white)
                    .shadow(color: .brandPrimary.opacity(0.4), radius: 10, y: 4)
                }
                
                Button {
                    showImagePicker = true
                } label: {
                    HStack(spacing: 8) {
                        Image(systemName: "photo.on.rectangle")
                        Text("Import Image")
                            .fontWeight(.semibold)
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 16)
                    .liquidGlass(cornerRadius: 14)
                    .foregroundColor(.textPrimary)
                }
            }
        }
        .padding(20)
        .liquidGlass(cornerRadius: 24)
        .overlay(alignment: .topTrailing) {
            Circle()
                .fill(Color.brandPrimary.opacity(0.2))
                .frame(width: 150, height: 150)
                .blur(radius: 60)
                .offset(x: 40, y: -40)
        }
        .clipShape(RoundedRectangle(cornerRadius: 24))
    }
    
    // MARK: - System Health Card
    
    private var systemHealthCard: some View {
        HStack {
            HStack(spacing: 12) {
                ZStack {
                    Circle()
                        .fill(Color.verdictSafe.opacity(0.15))
                        .frame(width: 44, height: 44)
                    
                    Image(systemName: "heart.fill")
                        .font(.title3)
                        .foregroundColor(.verdictSafe)
                }
                
                VStack(alignment: .leading, spacing: 2) {
                    Text("System Optimal")
                        .font(.subheadline.weight(.semibold))
                        .foregroundColor(.textPrimary)
                    
                    Text("Engine v2.4 • Updated 2h ago")
                        .font(.caption)
                        .foregroundColor(.textSecondary)
                }
            }
            
            Spacer()
            
            Circle()
                .fill(.green)
                .frame(width: 10, height: 10)
                .shadow(color: .green, radius: 4)
        }
        .padding(16)
        .liquidGlass(cornerRadius: 16)
    }
    
    // MARK: - Stats Grid
    
    private var statsGrid: some View {
        HStack(spacing: 16) {
            // Threats Blocked
            VStack(alignment: .leading, spacing: 12) {
                HStack {
                    ZStack {
                        RoundedRectangle(cornerRadius: 10)
                            .fill(Color.verdictDanger.opacity(0.15))
                            .frame(width: 36, height: 36)
                        
                        Image(systemName: "shield.slash.fill")
                            .font(.system(size: 16))
                            .foregroundColor(.verdictDanger)
                    }
                    
                    Spacer()
                    
                    Text("+2")
                        .font(.caption.weight(.semibold))
                        .foregroundColor(.verdictDanger)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 4)
                        .background(Color.verdictDanger.opacity(0.15), in: Capsule())
                }
                
                VStack(alignment: .leading, spacing: 4) {
                    Text("\(threatsBlocked)")
                        .font(.title.weight(.bold))
                        .foregroundColor(.textPrimary)
                    
                    Text("Threats Blocked")
                        .font(.caption)
                        .foregroundColor(.textMuted)
                }
            }
            .padding(16)
            .frame(maxWidth: .infinity, alignment: .leading)
            .liquidGlass(cornerRadius: 16)
            .overlay(alignment: .topTrailing) {
                Circle()
                    .fill(Color.verdictDanger.opacity(0.2))
                    .frame(width: 50, height: 50)
                    .blur(radius: 20)
                    .offset(x: 10, y: -10)
            }
            .clipShape(RoundedRectangle(cornerRadius: 16))
            
            // Safe Scans
            VStack(alignment: .leading, spacing: 12) {
                ZStack {
                    RoundedRectangle(cornerRadius: 10)
                        .fill(Color.brandPrimary.opacity(0.15))
                        .frame(width: 36, height: 36)
                    
                    Image(systemName: "checkmark.seal.fill")
                        .font(.system(size: 16))
                        .foregroundColor(.brandPrimary)
                }
                
                VStack(alignment: .leading, spacing: 4) {
                    Text("\(safeScans)")
                        .font(.title.weight(.bold))
                        .foregroundColor(.textPrimary)
                    
                    Text("Safe Scans")
                        .font(.caption)
                        .foregroundColor(.textMuted)
                }
            }
            .padding(16)
            .frame(maxWidth: .infinity, alignment: .leading)
            .liquidGlass(cornerRadius: 16)
            .overlay(alignment: .topTrailing) {
                Circle()
                    .fill(Color.brandPrimary.opacity(0.2))
                    .frame(width: 50, height: 50)
                    .blur(radius: 20)
                    .offset(x: 10, y: -10)
            }
            .clipShape(RoundedRectangle(cornerRadius: 16))
        }
    }
    
    // MARK: - Engine Features
    
    private var engineFeaturesSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("ENGINE FEATURES")
                .font(.caption.weight(.bold))
                .foregroundColor(.textMuted)
                .tracking(1)
            
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 12) {
                    engineFeatureCard(
                        icon: "cpu",
                        title: "Neural Core",
                        subtitle: "On-device AI processing",
                        color: .brandAccent
                    )
                    
                    engineFeatureCard(
                        icon: "lock.shield",
                        title: "Sandbox",
                        subtitle: "Isolated execution env",
                        color: .brandSecondary
                    )
                    
                    engineFeatureCard(
                        icon: "bolt.fill",
                        title: "Flash Engine",
                        subtitle: "< 10ms latency",
                        color: .verdictWarning
                    )
                }
            }
            .scrollClipDisabled()
        }
    }
    
    private func engineFeatureCard(icon: String, title: String, subtitle: String, color: Color) -> some View {
        VStack(alignment: .leading, spacing: 12) {
            Image(systemName: icon)
                .font(.title2)
                .foregroundColor(color)
            
            VStack(alignment: .leading, spacing: 4) {
                Text(title)
                    .font(.subheadline.weight(.semibold))
                    .foregroundColor(.textPrimary)
                
                Text(subtitle)
                    .font(.caption2)
                    .foregroundColor(.textMuted)
            }
        }
        .padding(16)
        .frame(width: 140, alignment: .leading)
        .liquidGlass(cornerRadius: 16)
    }
    
    // MARK: - Recent Scans
    
    private var recentScansSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text("RECENT SCANS")
                    .font(.caption.weight(.bold))
                    .foregroundColor(.textMuted)
                    .tracking(1)
                
                Spacer()
                
                NavigationLink {
                    HistoryView()
                } label: {
                    Text("View All")
                        .font(.caption.weight(.semibold))
                        .foregroundColor(.brandPrimary)
                }
            }
            
            if recentHistory.isEmpty {
                // Empty state
                VStack(spacing: 12) {
                    Image(systemName: "qrcode")
                        .font(.title)
                        .foregroundColor(.textMuted)
                    
                    Text("No scans yet")
                        .font(.subheadline)
                        .foregroundColor(.textSecondary)
                    
                    Text("Scan a QR code or paste a URL to get started")
                        .font(.caption)
                        .foregroundColor(.textMuted)
                        .multilineTextAlignment(.center)
                }
                .frame(maxWidth: .infinity)
                .padding(.vertical, 32)
                .liquidGlass(cornerRadius: 16)
            } else {
                VStack(spacing: 0) {
                    ForEach(Array(recentHistory.enumerated()), id: \.element.id) { index, item in
                        historyRow(item)
                        
                        if index < recentHistory.count - 1 {
                            Divider().padding(.leading, 60)
                        }
                    }
                }
                .liquidGlass(cornerRadius: 16)
            }
        }
    }
    
    private func historyRow(_ item: HistoryItemMock) -> some View {
        let iconBg = Color.forVerdict(item.verdict)
        let icon = item.verdict == .safe ? "checkmark.shield.fill" : 
                   item.verdict == .malicious ? "exclamationmark.triangle.fill" : "questionmark.circle.fill"
        let status = item.verdict == .safe ? "Safe" :
                     item.verdict == .malicious ? "Phishing Detected" : "Suspicious"
        
        return HStack(spacing: 16) {
            ZStack {
                Circle()
                    .fill(iconBg.opacity(0.15))
                    .frame(width: 44, height: 44)
                
                Image(systemName: icon)
                    .font(.system(size: 18))
                    .foregroundColor(iconBg)
            }
            
            VStack(alignment: .leading, spacing: 2) {
                Text(item.domain.isEmpty ? item.url : item.domain)
                    .font(.subheadline.weight(.medium))
                    .foregroundColor(.textPrimary)
                    .lineLimit(1)
                
                Text("\(status) • \(item.relativeDate)")
                    .font(.caption)
                    .foregroundColor(item.verdict == .safe ? .textSecondary : iconBg)
            }
            
            Spacer()
            
            Image(systemName: "chevron.right")
                .font(.caption)
                .foregroundColor(.textMuted)
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 14)
    }
    
    
    // MARK: - Threat Database Card
    
    private var threatDatabaseCard: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Threat Database")
                .font(.headline)
                .foregroundColor(.white)
            
            Text("Local database updated successfully.")
                .font(.subheadline)
                .foregroundColor(.white.opacity(0.7))
            
            HStack(spacing: 8) {
                Circle()
                    .fill(.green)
                    .frame(width: 8, height: 8)
                
                Text("Version 2023.10.24")
                    .font(.caption.monospaced())
                    .foregroundColor(.white.opacity(0.6))
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(20)
        .background {
            RoundedRectangle(cornerRadius: 16)
                .fill(Color.bgCard)
            
            Image(systemName: "server.rack")
                .font(.system(size: 80))
                .foregroundColor(.white.opacity(0.05))
                .offset(x: 80, y: 20)
        }
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }
    
    // MARK: - Actions
    
    private func analyzeURL() {
        guard !urlInput.isEmpty else { return }
        
        isAnalyzing = true
        SettingsManager.shared.triggerHaptic(.medium)
        
        // Use UnifiedAnalysisService for consistent analysis across all views
        // This centralizes all heuristic logic and supports KMP when available
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
            // Call the unified analysis service
            let result = UnifiedAnalysisService.shared.analyze(url: urlInput)
            
            // Store result
            analysisResult = result
            
            // Extract domain for history
            var normalizedUrl = urlInput.lowercased()
            if !normalizedUrl.hasPrefix("http") {
                normalizedUrl = "https://" + normalizedUrl
            }
            let urlObj = URL(string: normalizedUrl)
            
            // Save to history
            let historyItem = HistoryItemMock(
                id: UUID().uuidString,
                url: urlInput,
                score: result.score,
                verdict: result.verdict,
                scannedAt: Date(),
                domain: urlObj?.host ?? urlInput
            )
            HistoryStore.shared.addItem(historyItem)
            
            isAnalyzing = false
            
            // Play feedback based on verdict
            if result.verdict == .safe {
                SettingsManager.shared.triggerHaptic(.success)
                SettingsManager.shared.playSound(.success)
            } else {
                SettingsManager.shared.triggerHaptic(.warning)
                SettingsManager.shared.playSound(.warning)
            }
            
            // Show results
            showResults = true
            
            // Reload stats
            loadStats()
            
            // Clear input
            urlInput = ""
            
            #if DEBUG
            print("📊 [Dashboard] Analysis complete via \(UnifiedAnalysisService.shared.lastEngineUsed)")
            #endif
        }
    }
}

// MARK: - Offline Badge

struct OfflineBadge: View {
    var body: some View {
        HStack(spacing: 8) {
            Image(systemName: "wifi.slash")
                .font(.caption)
                .foregroundColor(.verdictSafe)
            
            Text("OFFLINE READY")
                .font(.caption2.weight(.bold))
                .foregroundColor(.white)
                .tracking(0.5)
        }
        .padding(.horizontal, 12)
        .padding(.vertical, 8)
        .background(Color.bgCard.opacity(0.9), in: Capsule())
    }
}

#endif
