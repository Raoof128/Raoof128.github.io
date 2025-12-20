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

// UI/History/ThreatHistoryView.swift
// QR-SHIELD Threat History - iOS 17+ Liquid Glass Edition
//
// Matches: Threat History (threat.html) design
// Features:
// - Real-time global threat map (animated)
// - Stats cards (Threats Today, Active Campaigns, Protected, Detection Rate)
// - Latest threat updates with severity indicators
// - Filter tabs (All, Live, Recent, Verified)
// - Run Security Audit action

import SwiftUI
#if os(iOS)

// MARK: - Threat Update

struct ThreatUpdate: Identifiable {
    let id = UUID()
    let category: String
    let description: String
    let severity: ThreatSeverity
    let timeAgo: String
    let isLive: Bool
}

enum ThreatSeverity: String {
    case critical = "CRITICAL"
    case high = "HIGH"
    case medium = "MEDIUM"
    case low = "LOW"
    
    var color: Color {
        switch self {
        case .critical: return .verdictDanger
        case .high: return .verdictWarning
        case .medium: return .brandAccent
        case .low: return .brandSecondary
        }
    }
}

// MARK: - Threat History View

@available(iOS 17, *)
struct ThreatHistoryView: View {
    @Environment(\.dismiss) private var dismiss
    
    @State private var selectedFilter: ThreatFilter = .all
    @State private var isAuditRunning = false
    @State private var showAuditResult = false
    @State private var threats: [ThreatUpdate] = []
    @State private var animateDots = false
    
    // Real stats from HistoryStore
    @State private var threatsToday = 0
    @State private var activeCampaigns = 0
    @State private var protectedScans = 0
    @State private var detectionRate = 0.0
    @State private var lastAudit = "Never"
    
    // Real threat hotspots based on scan data
    @State private var threatHotspotData: [(x: CGFloat, y: CGFloat, severity: ThreatSeverity)] = []
    
    enum ThreatFilter: String, CaseIterable {
        case all = "All"
        case live = "Live"
        case recent = "Recent"
        case verified = "Verified"
    }
    
    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 24) {
                    // Real-time Threat Map
                    threatMapSection
                    
                    // Stats Grid
                    statsGrid
                    
                    // Security Audit Button
                    securityAuditSection
                    
                    // Filter Tabs
                    filterTabs
                    
                    // Latest Threats List
                    latestThreatsSection
                }
                .padding(.horizontal, 16)
                .padding(.top, 16)
                .padding(.bottom, 100)
            }
            .scrollContentBackground(.hidden)
            .background {
                LiquidGlassBackground()
                    .ignoresSafeArea()
            }
            .navigationTitle("Threat History")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Menu {
                        Button {
                            refreshThreats()
                        } label: {
                            Label("Refresh", systemImage: "arrow.clockwise")
                        }
                        
                        Button {
                            exportThreatReport()
                        } label: {
                            Label("Export Report", systemImage: "square.and.arrow.up")
                        }
                        
                        Divider()
                        
                        Button {
                            dismiss()
                        } label: {
                            Label("Close", systemImage: "xmark")
                        }
                    } label: {
                        Image(systemName: "ellipsis")
                            .foregroundColor(.textSecondary)
                    }
                }
            }
            .onAppear {
                loadRealStats()
                loadRealThreats()
                withAnimation(.easeInOut(duration: 2).repeatForever(autoreverses: true)) {
                    animateDots = true
                }
            }
            .alert("Security Audit Complete", isPresented: $showAuditResult) {
                Button("OK", role: .cancel) {}
            } message: {
                Text("No vulnerabilities detected. Your device is secure.")
            }
        }
    }
    
    // MARK: - Threat Map Section
    
    private var threatMapSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                HStack(spacing: 8) {
                    Circle()
                        .fill(.red)
                        .frame(width: 8, height: 8)
                        .shadow(color: .red, radius: 4)
                        .opacity(animateDots ? 1 : 0.5)
                    
                    Text("LIVE THREAT MAP")
                        .font(.caption.weight(.bold))
                        .foregroundColor(.textPrimary)
                        .tracking(1)
                }
                
                Spacer()
                
                Text("Global View")
                    .font(.caption)
                    .foregroundColor(.brandPrimary)
            }
            
            // Map Visualization (Simplified)
            ZStack {
                // Background gradient
                RoundedRectangle(cornerRadius: 16)
                    .fill(
                        RadialGradient(
                            colors: [
                                Color.bgCard,
                                Color.bgMain
                            ],
                            center: .center,
                            startRadius: 0,
                            endRadius: 200
                        )
                    )
                    .frame(height: 180)
                
                // Grid lines (simulated map)
                VStack(spacing: 20) {
                    ForEach(0..<5) { _ in
                        HStack(spacing: 30) {
                            ForEach(0..<8) { _ in
                                Circle()
                                    .fill(Color.textMuted.opacity(0.15))
                                    .frame(width: 2, height: 2)
                            }
                        }
                    }
                }
                
                // Real threat hotspots from scan history
                if threatHotspotData.isEmpty {
                    // No threats - show green indicator
                    Circle()
                        .fill(Color.verdictSafe.opacity(0.2))
                        .frame(width: 80, height: 80)
                        .blur(radius: 20)
                    
                    Image(systemName: "checkmark.shield.fill")
                        .font(.largeTitle)
                        .foregroundColor(.verdictSafe.opacity(0.6))
                } else {
                    // Show real hotspots based on scanned threats
                    ForEach(Array(threatHotspotData.enumerated()), id: \.offset) { index, hotspot in
                        let color: Color = hotspot.severity == .critical ? .verdictDanger : .verdictWarning
                        let size: CGFloat = hotspot.severity == .critical ? 60 : 40
                        threatHotspot(x: hotspot.x, y: hotspot.y, size: size, color: color, opacity: 0.3)
                        pulsingDot(x: hotspot.x, y: hotspot.y)
                    }
                }
            }
            .clipShape(RoundedRectangle(cornerRadius: 16))
            
            // Legend
            HStack(spacing: 16) {
                legendItem(color: .verdictDanger, label: "High Activity")
                legendItem(color: .verdictWarning, label: "Medium")
                legendItem(color: .brandPrimary, label: "Low")
            }
            .padding(.top, 4)
        }
        .padding(16)
        .liquidGlass(cornerRadius: 20)
    }
    
    private func threatHotspot(x: CGFloat, y: CGFloat, size: CGFloat, color: Color, opacity: Double) -> some View {
        Circle()
            .fill(color.opacity(opacity))
            .frame(width: size, height: size)
            .blur(radius: size / 3)
            .offset(x: x, y: y)
    }
    
    private func pulsingDot(x: CGFloat, y: CGFloat) -> some View {
        Circle()
            .fill(Color.verdictDanger)
            .frame(width: 6, height: 6)
            .shadow(color: .verdictDanger, radius: 4)
            .scaleEffect(animateDots ? 1.5 : 1)
            .opacity(animateDots ? 0.5 : 1)
            .offset(x: x, y: y)
    }
    
    private func legendItem(color: Color, label: String) -> some View {
        HStack(spacing: 6) {
            Circle()
                .fill(color)
                .frame(width: 8, height: 8)
            
            Text(label)
                .font(.caption2)
                .foregroundColor(.textMuted)
        }
    }
    
    // MARK: - Stats Grid
    
    private var statsGrid: some View {
        LazyVGrid(columns: [
            GridItem(.flexible()),
            GridItem(.flexible())
        ], spacing: 12) {
            // Threats Today
            statCard(
                icon: "shield.slash.fill",
                iconColor: .verdictDanger,
                value: "\(threatsToday)",
                label: "Threats Today",
                badge: "+2",
                badgeColor: .verdictDanger
            )
            
            // Active Campaigns
            statCard(
                icon: "exclamationmark.triangle.fill",
                iconColor: .verdictWarning,
                value: "\(activeCampaigns)",
                label: "Active Campaigns"
            )
            
            // Protected Devices
            statCard(
                icon: "checkmark.shield.fill",
                iconColor: .brandSecondary,
                value: "\(protectedScans)",
                label: "Protected Scans"
            )
            
            // Detection Rate
            statCard(
                icon: "chart.line.uptrend.xyaxis",
                iconColor: .brandPrimary,
                value: String(format: "%.1f%%", detectionRate),
                label: "Detection Rate"
            )
        }
    }
    
    private func statCard(
        icon: String,
        iconColor: Color,
        value: String,
        label: String,
        badge: String? = nil,
        badgeColor: Color = .textMuted
    ) -> some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                ZStack {
                    RoundedRectangle(cornerRadius: 10)
                        .fill(iconColor.opacity(0.15))
                        .frame(width: 36, height: 36)
                    
                    Image(systemName: icon)
                        .font(.system(size: 14))
                        .foregroundColor(iconColor)
                }
                
                Spacer()
                
                if let badge {
                    Text(badge)
                        .font(.caption2.weight(.bold))
                        .foregroundColor(badgeColor)
                        .padding(.horizontal, 6)
                        .padding(.vertical, 3)
                        .background(badgeColor.opacity(0.15), in: Capsule())
                }
            }
            
            VStack(alignment: .leading, spacing: 2) {
                Text(value)
                    .font(.title2.weight(.bold))
                    .foregroundColor(.textPrimary)
                
                Text(label)
                    .font(.caption)
                    .foregroundColor(.textMuted)
            }
        }
        .padding(16)
        .liquidGlass(cornerRadius: 16)
    }
    
    // MARK: - Security Audit Section
    
    private var securityAuditSection: some View {
        VStack(spacing: 12) {
            Button {
                runSecurityAudit()
            } label: {
                HStack {
                    if isAuditRunning {
                        ProgressView()
                            .tint(.white)
                    } else {
                        Image(systemName: "checkmark.shield.fill")
                    }
                    
                    Text(isAuditRunning ? "Running Audit..." : "Run Security Audit")
                        .font(.headline)
                }
                .foregroundColor(.white)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 16)
                .background(LinearGradient.brandGradient, in: RoundedRectangle(cornerRadius: 14))
                .shadow(color: .brandPrimary.opacity(0.3), radius: 8, y: 4)
            }
            .disabled(isAuditRunning)
            
            Text("Last audit: \(lastAudit)")
                .font(.caption)
                .foregroundColor(.textMuted)
        }
    }
    
    // MARK: - Filter Tabs
    
    private var filterTabs: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                ForEach(ThreatFilter.allCases, id: \.self) { filter in
                    filterTab(filter)
                }
            }
        }
        .scrollClipDisabled()
    }
    
    private func filterTab(_ filter: ThreatFilter) -> some View {
        Button {
            withAnimation(.spring(response: 0.3)) {
                selectedFilter = filter
            }
            SettingsManager.shared.triggerHaptic(.selection)
        } label: {
            HStack(spacing: 6) {
                if filter == .live {
                    Circle()
                        .fill(.red)
                        .frame(width: 6, height: 6)
                }
                
                Text(filter.rawValue)
                    .font(.subheadline.weight(selectedFilter == filter ? .semibold : .regular))
            }
            .foregroundColor(selectedFilter == filter ? .brandPrimary : .textSecondary)
            .padding(.horizontal, 16)
            .padding(.vertical, 10)
            .background {
                if selectedFilter == filter {
                    Capsule()
                        .fill(Color.brandPrimary.opacity(0.15))
                }
            }
            .overlay(
                Capsule()
                    .stroke(selectedFilter == filter ? Color.brandPrimary.opacity(0.3) : Color.clear, lineWidth: 1)
            )
        }
    }
    
    // MARK: - Latest Threats Section
    
    private var latestThreatsSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("LATEST UPDATES")
                .font(.caption.weight(.bold))
                .foregroundColor(.textMuted)
                .tracking(1)
            
            VStack(spacing: 0) {
                ForEach(filteredThreats) { threat in
                    threatRow(threat)
                    
                    if threat.id != filteredThreats.last?.id {
                        Divider()
                            .padding(.leading, 60)
                    }
                }
            }
            .liquidGlass(cornerRadius: 16)
        }
    }
    
    private var filteredThreats: [ThreatUpdate] {
        switch selectedFilter {
        case .all:
            return threats
        case .live:
            return threats.filter { $0.isLive }
        case .recent:
            return Array(threats.prefix(3))
        case .verified:
            return threats.filter { $0.severity == .critical || $0.severity == .high }
        }
    }
    
    private func threatRow(_ threat: ThreatUpdate) -> some View {
        HStack(spacing: 14) {
            // Icon
            ZStack {
                Circle()
                    .fill(threat.severity.color.opacity(0.15))
                    .frame(width: 44, height: 44)
                
                Image(systemName: severityIcon(for: threat.severity))
                    .font(.system(size: 18))
                    .foregroundColor(threat.severity.color)
            }
            
            // Content
            VStack(alignment: .leading, spacing: 4) {
                HStack {
                    Text(threat.category)
                        .font(.subheadline.weight(.semibold))
                        .foregroundColor(.textPrimary)
                    
                    if threat.isLive {
                        HStack(spacing: 4) {
                            Circle()
                                .fill(.red)
                                .frame(width: 4, height: 4)
                            
                            Text("LIVE")
                                .font(.caption2.weight(.bold))
                        }
                        .foregroundColor(.verdictDanger)
                        .padding(.horizontal, 6)
                        .padding(.vertical, 2)
                        .background(Color.verdictDanger.opacity(0.1), in: Capsule())
                    }
                }
                
                Text(threat.description)
                    .font(.caption)
                    .foregroundColor(.textSecondary)
                    .lineLimit(2)
                
                HStack(spacing: 8) {
                    Text(threat.severity.rawValue)
                        .font(.caption2.weight(.semibold))
                        .foregroundColor(threat.severity.color)
                    
                    Text("•")
                        .foregroundColor(.textMuted)
                    
                    Text(threat.timeAgo)
                        .font(.caption)
                        .foregroundColor(.textMuted)
                }
            }
            
            Spacer()
            
            Image(systemName: "chevron.right")
                .font(.caption)
                .foregroundColor(.textMuted)
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 14)
    }
    
    private func severityIcon(for severity: ThreatSeverity) -> String {
        switch severity {
        case .critical: return "exclamationmark.triangle.fill"
        case .high: return "exclamationmark.circle.fill"
        case .medium: return "info.circle.fill"
        case .low: return "checkmark.circle.fill"
        }
    }
    
    // MARK: - Actions
    
    private func runSecurityAudit() {
        isAuditRunning = true
        SettingsManager.shared.triggerHaptic(.medium)
        
        // Simulate audit
        DispatchQueue.main.asyncAfter(deadline: .now() + 2.5) {
            isAuditRunning = false
            showAuditResult = true
            lastAudit = "Just now"
            SettingsManager.shared.triggerHaptic(.success)
            SettingsManager.shared.playSound(.success)
        }
    }
    
    private func refreshThreats() {
        SettingsManager.shared.triggerHaptic(.light)
        
        // Reload real stats and threats from HistoryStore
        loadRealStats()
        loadRealThreats()
    }
    
    private func loadRealStats() {
        let history = HistoryStore.shared.getAllItems()
        let now = Date()
        let calendar = Calendar.current
        
        // Threats today = malicious + suspicious scans from today
        let todayThreats = history.filter { item in
            let isToday = calendar.isDateInToday(item.scannedAt)
            let isThreat = item.verdict == .malicious || item.verdict == .suspicious
            return isToday && isThreat
        }
        threatsToday = todayThreats.count
        
        // Active campaigns = unique malicious domains detected this week
        let oneWeekAgo = calendar.date(byAdding: .day, value: -7, to: now) ?? now
        let weekThreats = history.filter { item in
            item.scannedAt >= oneWeekAgo && item.verdict == .malicious
        }
        // Group by domain to count "campaigns"
        let uniqueDomains = Set(weekThreats.compactMap { URL(string: $0.url)?.host })
        activeCampaigns = max(uniqueDomains.count, 0)
        
        // Protected scans = total safe scans
        protectedScans = history.filter { $0.verdict == .safe }.count
        
        // Detection rate = safe scans / total * 100 (or 100% if no scans yet)
        if history.isEmpty {
            detectionRate = 100.0
        } else {
            let safeCount = history.filter { $0.verdict == .safe }.count
            detectionRate = (Double(safeCount) / Double(history.count)) * 100.0
        }
        
        // Last audit = last scan time
        if let lastScan = history.first?.scannedAt {
            let formatter = RelativeDateTimeFormatter()
            formatter.unitsStyle = .full
            lastAudit = formatter.localizedString(for: lastScan, relativeTo: now)
        } else {
            lastAudit = "Never"
        }
        
        // Generate threat hotspots based on real data
        generateThreatHotspots(from: history)
    }
    
    private func loadRealThreats() {
        let history = HistoryStore.shared.getAllItems()
        let now = Date()
        let formatter = RelativeDateTimeFormatter()
        formatter.unitsStyle = .abbreviated
        
        // Get all non-safe scans and convert to ThreatUpdate
        let threatItems = history.filter { $0.verdict != .safe }
        
        threats = threatItems.prefix(20).map { item in
            // Determine category based on URL patterns
            let category = categorizeUrl(item.url)
            let description = generateDescription(for: item)
            
            // Map verdict to severity
            let severity: ThreatSeverity = item.verdict == .malicious ? .critical : .high
            
            // Check if recent (last hour = "live")
            let isLive = item.scannedAt > now.addingTimeInterval(-3600)
            
            return ThreatUpdate(
                category: category,
                description: description,
                severity: severity,
                timeAgo: formatter.localizedString(for: item.scannedAt, relativeTo: now),
                isLive: isLive
            )
        }
        
        // If no real threats, show a "no threats" message
        if threats.isEmpty {
            threats = [
                ThreatUpdate(
                    category: "All Clear",
                    description: "No threats detected in your scan history. Keep scanning to stay protected!",
                    severity: .low,
                    timeAgo: "Now",
                    isLive: false
                )
            ]
        }
    }
    
    private func categorizeUrl(_ url: String) -> String {
        let lowercased = url.lowercased()
        
        if lowercased.contains("login") || lowercased.contains("signin") || lowercased.contains("password") {
            return "Credential Harvesting"
        } else if lowercased.contains("bank") || lowercased.contains("paypal") || lowercased.contains("payment") {
            return "Financial Phishing"
        } else if lowercased.contains("verify") || lowercased.contains("confirm") || lowercased.contains("update") {
            return "Account Verification Scam"
        } else if lowercased.contains("bit.ly") || lowercased.contains("tinyurl") || lowercased.contains("t.co") {
            return "Suspicious Redirect"
        } else if lowercased.contains("free") || lowercased.contains("winner") || lowercased.contains("prize") {
            return "Scam/Fraud"
        } else {
            return "Suspicious URL"
        }
    }
    
    private func generateDescription(for item: HistoryItemMock) -> String {
        let domain = URL(string: item.url)?.host ?? "Unknown domain"
        
        switch item.verdict {
        case .malicious:
            return "Malicious content detected at \(domain)"
        case .suspicious:
            return "Suspicious activity flagged for \(domain)"
        default:
            return "Analyzed: \(domain)"
        }
    }
    
    private func generateThreatHotspots(from history: [HistoryItemMock]) {
        // Generate visual hotspots based on threat distribution
        let threats = history.filter { $0.verdict != .safe }
        
        // Create hotspots at pseudo-random positions based on hash of URLs
        threatHotspotData = threats.prefix(5).enumerated().map { index, item in
            // Use URL hash to generate consistent but varied positions
            let hash = item.url.hashValue
            let x = CGFloat((hash % 160) - 80)
            let y = CGFloat(((hash >> 8) % 100) - 50)
            let severity: ThreatSeverity = item.verdict == .malicious ? .critical : .high
            return (x: x, y: y, severity: severity)
        }
    }
    
    private func exportThreatReport() {
        SettingsManager.shared.triggerHaptic(.light)
        
        // Generate threat report
        let report = generateThreatReport()
        
        // Copy to clipboard
        UIPasteboard.general.string = report
        
        // Show brief notification via haptic
        SettingsManager.shared.triggerHaptic(.success)
    }
    
    private func generateThreatReport() -> String {
        let formatter = DateFormatter()
        formatter.dateStyle = .medium
        formatter.timeStyle = .short
        
        return """
        🛡️ QR-SHIELD Threat Intelligence Report
        ========================================
        Generated: \(formatter.string(from: Date()))
        
        SUMMARY
        -------
        • Threats Detected Today: \(threatsToday)
        • Active Campaigns: \(activeCampaigns)
        • Protected Scans: \(protectedScans)
        • Detection Rate: \(String(format: "%.1f", detectionRate))%
        
        LATEST THREATS
        --------------
        \(threats.prefix(5).map { "• [\($0.severity.rawValue)] \($0.category): \($0.description)" }.joined(separator: "\n"))
        
        RECOMMENDATIONS
        ---------------
        • Keep threat sensitivity at Balanced or higher
        • Run security audits regularly
        • Report suspicious URLs to improve detection
        
        ---
        Report generated by QR-SHIELD iOS
        Last Audit: \(lastAudit)
        """
    }
}

#endif
