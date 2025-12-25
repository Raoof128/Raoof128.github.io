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

// UI/Results/ScanResultView.swift
// QR-SHIELD Scan Result - iOS 17+ Liquid Glass Edition
//
// Matches: Scan Result HTML design
// Features:
// - High Risk Verdict hero section
// - Threat tags (Phishing, Obfuscated Script, Homograph Attack)
// - Recommended Actions (Block & Report, Quarantine)
// - Attack Breakdown with expandable sections
// - Explainable Security panel

import SwiftUI
#if os(iOS)

// MARK: - Attack Type

struct AttackBreakdown: Identifiable {
    let id = UUID()
    let title: String
    let severity: AttackSeverity
    let icon: String
    let description: String
    let technicalDetail: String?
    var isExpanded: Bool = false
}

enum AttackSeverity: String {
    case critical = "Critical Severity"
    case high = "High Severity"
    case medium = "Medium Severity"
    case low = "Low Severity"
    
    var color: Color {
        switch self {
        case .critical: return .verdictDanger
        case .high: return .verdictWarning
        case .medium: return .brandAccent
        case .low: return .brandPrimary
        }
    }
}

// MARK: - Scan Result View

@available(iOS 17, *)
struct ScanResultView: View {
    @Environment(\.dismiss) private var dismiss
    
    let assessment: RiskAssessmentMock
    
    @State private var attackBreakdowns: [AttackBreakdown] = []
    @State private var showBlockConfirmation = false
    @State private var showSandbox = false
    @State private var showExport = false
    
    @AppStorage("useDarkMode") private var useDarkMode = true
    
    private var themeColor: Color {
        Color.forVerdict(assessment.verdict)
    }
    
    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 24) {
                    // Verdict Hero
                    verdictHero
                    
                    // Recommended Actions
                    recommendedActions
                    
                    // Attack Breakdown
                    attackBreakdownSection
                    
                    // Explainable Security
                    explainableSecuritySection
                    
                    // Scan Metadata
                    scanMetadata
                }
                .padding(.horizontal, 20)
                .padding(.top, 16)
                .padding(.bottom, 100)
            }
            .scrollContentBackground(.hidden)
            .background {
                meshBackground
            }
            .navigationTitle("Scan Result")
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
                    ShareLink(item: shareText) {
                        Image(systemName: "square.and.arrow.up")
                            .foregroundColor(.textSecondary)
                    }
                }
            }
            .onAppear {
                setupAttackBreakdowns()
            }
            .confirmationDialog(
                "Block & Report",
                isPresented: $showBlockConfirmation,
                titleVisibility: .visible
            ) {
                Button("Block & Add to Blocklist", role: .destructive) {
                    blockAndReport()
                }
                Button("Cancel", role: .cancel) {}
            } message: {
                Text(NSLocalizedString("result.block_warning", comment: ""))
            }
            .sheet(isPresented: $showExport) {
                ReportExportView(assessment: assessment)
                    .preferredColorScheme(useDarkMode ? .dark : .light)
            }
            .sheet(isPresented: $showSandbox) {
                SandboxPreviewSheet(url: assessment.url)
                    .preferredColorScheme(useDarkMode ? .dark : .light)
            }
        }
        // Floating scan button
        .overlay(alignment: .bottomTrailing) {
            Button {
                dismiss()
            } label: {
                Image(systemName: "qrcode.viewfinder")
                    .font(.title2)
                    .foregroundColor(.white)
                    .frame(width: 56, height: 56)
                    .background(LinearGradient.brandGradient, in: Circle())
                    .shadow(color: .brandPrimary.opacity(0.4), radius: 10, y: 4)
            }
            .padding(.trailing, 24)
            .padding(.bottom, 24)
        }
    }
    
    // MARK: - Mesh Background
    
    private var meshBackground: some View {
        ZStack {
            // Use adaptive background
            LiquidGlassBackground()
            
            // Gradient orbs based on verdict
            if assessment.verdict == .malicious {
                Circle()
                    .fill(Color.verdictDanger.opacity(0.15))
                    .frame(width: 300, height: 300)
                    .blur(radius: 80)
                    .offset(x: 150, y: -100)
            } else if assessment.verdict == .suspicious {
                Circle()
                    .fill(Color.verdictWarning.opacity(0.15))
                    .frame(width: 300, height: 300)
                    .blur(radius: 80)
                    .offset(x: 150, y: -100)
            }
        }
        .ignoresSafeArea()
    }
    
    // MARK: - Verdict Hero
    
    private var verdictHero: some View {
        VStack(spacing: 16) {
            // Large Icon
            ZStack {
                Circle()
                    .fill(themeColor.opacity(0.15))
                    .frame(width: 80, height: 80)
                
                Circle()
                    .stroke(themeColor.opacity(0.3), lineWidth: 2)
                    .frame(width: 80, height: 80)
                
                VerdictIcon(verdict: assessment.verdict, size: 40)
            }
            .shadow(color: themeColor.opacity(0.4), radius: 20)
            
            // Verdict Text
            Text(verdictTitle)
                .font(.title2.weight(.bold))
                .foregroundColor(.textPrimary)
            
            // Confidence Badge
            HStack(spacing: 6) {
                Image(systemName: "checkmark.shield.fill")
                    .font(.caption)
                    .foregroundColor(themeColor)
                
                Text("\(Int(assessment.confidence * 100))% Confidence")
                    .font(.subheadline.weight(.medium))
                    .foregroundColor(themeColor)
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 6)
            .background(themeColor.opacity(0.1), in: Capsule())
            
            // Threat Tags
            FlowLayout(spacing: 8) {
                ForEach(assessment.flags.prefix(3), id: \.self) { flag in
                    threatTag(flag)
                }
            }
            .padding(.top, 8)
        }
        .padding(24)
        .frame(maxWidth: .infinity)
        .background {
            LinearGradient(
                colors: [
                    themeColor.opacity(0.08),
                    themeColor.opacity(0.02)
                ],
                startPoint: .top,
                endPoint: .bottom
            )
        }
        .liquidGlass(cornerRadius: 24)
        .overlay(alignment: .topTrailing) {
            Circle()
                .fill(themeColor.opacity(0.2))
                .frame(width: 100, height: 100)
                .blur(radius: 40)
                .offset(x: 20, y: -20)
        }
        .clipShape(RoundedRectangle(cornerRadius: 24))
    }
    
    private var verdictTitle: String {
        switch assessment.verdict {
        case .malicious: return "HIGH RISK DETECTED"
        case .suspicious: return "SUSPICIOUS ACTIVITY"
        case .safe: return "VERIFIED SAFE"
        case .unknown: return "ANALYSIS COMPLETE"
        }
    }
    
    private func threatTag(_ text: String) -> some View {
        HStack(spacing: 6) {
            Circle()
                .fill(tagColor(for: text))
                .frame(width: 6, height: 6)
            
            Text(text)
                .font(.caption.weight(.semibold))
                .foregroundColor(.textSecondary)
        }
        .padding(.horizontal, 12)
        .padding(.vertical, 8)
        .background(.ultraThinMaterial, in: Capsule())
        .overlay(
            Capsule()
                .stroke(tagColor(for: text).opacity(0.3), lineWidth: 1)
        )
    }
    
    private func tagColor(for text: String) -> Color {
        if text.lowercased().contains("phishing") { return .verdictDanger }
        if text.lowercased().contains("obfuscated") { return .verdictWarning }
        if text.lowercased().contains("homograph") { return .brandAccent }
        return .textMuted
    }
    
    // MARK: - Recommended Actions
    
    private var recommendedActions: some View {
        VStack(alignment: .leading, spacing: 12) {
            sectionHeader("Recommended Actions", icon: "hand.thumbsup.fill")
            
            VStack(spacing: 12) {
                // Block & Report (Primary)
                Button {
                    showBlockConfirmation = true
                } label: {
                    HStack {
                        HStack(spacing: 12) {
                            ZStack {
                                RoundedRectangle(cornerRadius: 12)
                                    .fill(Color.white.opacity(0.2))
                                    .frame(width: 44, height: 44)
                                
                                Image(systemName: "nosign")
                                    .font(.title3)
                                    .foregroundColor(.white)
                            }
                            
                            VStack(alignment: .leading, spacing: 2) {
                                Text("result.block_report")
                                    .font(.headline)
                                    .foregroundColor(.white)
                                
                                Text("result.block_action")
                                    .font(.caption)
                                    .foregroundColor(.white.opacity(0.7))
                            }
                        }
                        
                        Spacer()
                        
                        Image(systemName: "arrow.right")
                            .foregroundColor(.white)
                    }
                    .padding(16)
                    .background(Color.verdictDanger, in: RoundedRectangle(cornerRadius: 16))
                    .shadow(color: .verdictDanger.opacity(0.4), radius: 10, y: 4)
                }
                
                // Quarantine in Sandbox
                Button {
                    showSandbox = true
                } label: {
                    HStack {
                        HStack(spacing: 12) {
                            ZStack {
                                RoundedRectangle(cornerRadius: 12)
                                    .fill(Color.brandPrimary.opacity(0.15))
                                    .frame(width: 44, height: 44)
                                
                                Image(systemName: "testtube.2")
                                    .font(.title3)
                                    .foregroundColor(.brandPrimary)
                            }
                            
                            VStack(alignment: .leading, spacing: 2) {
                                Text("result.quarantine")
                                    .font(.headline)
                                    .foregroundColor(.textPrimary)
                                
                                Text("result.quarantine_action")
                                    .font(.caption)
                                    .foregroundColor(.textMuted)
                            }
                        }
                        
                        Spacer()
                        
                        Image(systemName: "chevron.right")
                            .foregroundColor(.textMuted)
                    }
                    .padding(16)
                    .liquidGlass(cornerRadius: 16)
                }
            }
        }
    }
    
    // MARK: - Attack Breakdown
    
    private var attackBreakdownSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            sectionHeader("Attack Breakdown", icon: "chart.bar.fill")
            
            VStack(spacing: 8) {
                ForEach($attackBreakdowns) { $breakdown in
                    attackBreakdownCard(breakdown: $breakdown)
                }
            }
        }
    }
    
    private func attackBreakdownCard(breakdown: Binding<AttackBreakdown>) -> some View {
        VStack(spacing: 0) {
            // Header
            Button {
                withAnimation(.spring(response: 0.3)) {
                    breakdown.wrappedValue.isExpanded.toggle()
                }
            } label: {
                HStack(spacing: 12) {
                    ZStack {
                        RoundedRectangle(cornerRadius: 10)
                            .fill(breakdown.wrappedValue.severity.color.opacity(0.15))
                            .frame(width: 36, height: 36)
                        
                        Image(systemName: breakdown.wrappedValue.icon)
                            .font(.system(size: 14))
                            .foregroundColor(breakdown.wrappedValue.severity.color)
                    }
                    
                    VStack(alignment: .leading, spacing: 2) {
                        Text(breakdown.wrappedValue.title)
                            .font(.subheadline.weight(.semibold))
                            .foregroundColor(.textPrimary)
                        
                        Text(breakdown.wrappedValue.severity.rawValue)
                            .font(.caption)
                            .foregroundColor(breakdown.wrappedValue.severity.color)
                    }
                    
                    Spacer()
                    
                    Image(systemName: "chevron.down")
                        .font(.caption)
                        .foregroundColor(.textMuted)
                        .rotationEffect(.degrees(breakdown.wrappedValue.isExpanded ? 180 : 0))
                }
                .padding(16)
            }
            
            // Expanded Content
            if breakdown.wrappedValue.isExpanded {
                VStack(alignment: .leading, spacing: 12) {
                    Divider()
                    
                    Text(breakdown.wrappedValue.description)
                        .font(.subheadline)
                        .foregroundColor(.textSecondary)
                    
                    if breakdown.wrappedValue.technicalDetail != nil {
                        HStack(spacing: 0) {
                            Text("result.expected")
                                .foregroundColor(.verdictSafe)
                            Text("paypal.com")
                                .foregroundColor(.verdictSafe)
                        }
                        .font(.system(.caption, design: .monospaced))
                        .padding(8)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .background(Color.bgSurface, in: RoundedRectangle(cornerRadius: 8))
                        
                        HStack(spacing: 0) {
                            Text("result.detected")
                                .foregroundColor(.verdictDanger)
                            Text("p")
                                .foregroundColor(.verdictDanger)
                            Text("а")
                                .foregroundColor(.black)
                                .padding(.horizontal, 2)
                                .background(Color.verdictWarning.opacity(0.5), in: RoundedRectangle(cornerRadius: 2))
                            Text("ypal.com")
                                .foregroundColor(.verdictDanger)
                        }
                        .font(.system(.caption, design: .monospaced))
                        .padding(8)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .background(Color.bgSurface, in: RoundedRectangle(cornerRadius: 8))
                    }
                }
                .padding(.horizontal, 16)
                .padding(.bottom, 16)
            }
        }
        .liquidGlass(cornerRadius: 16)
    }
    
    // MARK: - Explainable Security
    
    private var explainableSecuritySection: some View {
        VStack(alignment: .leading, spacing: 16) {
            HStack(spacing: 8) {
                Image(systemName: "shield.fill")
                    .foregroundColor(.brandPrimary)
                Text("result.explainable_security")
                    .font(.subheadline.weight(.bold))
                    .foregroundColor(.brandPrimary)
            }
            
            VStack(alignment: .leading, spacing: 16) {
                explainableRow(
                    title: "Zero-Day Engine:",
                    description: "This pattern matches a known phishing kit (Kit-X29) used in recent financial sector attacks."
                )
                
                explainableRow(
                    title: "Logo Analysis:",
                    description: "Found distorted visual assets matching standard banking login screens."
                )
            }
        }
        .padding(20)
        .background {
            LinearGradient(
                colors: [
                    Color.brandPrimary.opacity(0.05),
                    Color.white.opacity(0.02)
                ],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
        }
        .overlay(
            RoundedRectangle(cornerRadius: 20)
                .stroke(Color.brandPrimary.opacity(0.2), lineWidth: 1)
        )
        .clipShape(RoundedRectangle(cornerRadius: 20))
    }
    
    private func explainableRow(title: String, description: String) -> some View {
        HStack(alignment: .top, spacing: 12) {
            Image(systemName: "checkmark.circle.fill")
                .font(.caption)
                .foregroundColor(.verdictSafe)
                .padding(.top, 2)
            
            Text(title)
                .font(.subheadline.weight(.semibold))
                .foregroundColor(.textPrimary) +
            Text(" " + description)
                .font(.subheadline)
                .foregroundColor(.textSecondary)
        }
    }
    
    // MARK: - Scan Metadata
    
    private var scanMetadata: some View {
        VStack(spacing: 12) {
            metadataRow(label: "Scan ID", value: "#992-AX-291")
            metadataRow(label: "Engine Version", value: "v4.2.1 (Offline)")
            metadataRow(label: "Time Elapsed", value: "124ms")
        }
        .padding(16)
        .background(Color.bgSurface.opacity(0.3), in: RoundedRectangle(cornerRadius: 16))
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .strokeBorder(style: StrokeStyle(lineWidth: 1, dash: [5]))
                .foregroundColor(Color.bgSurface)
        )
    }
    
    private func metadataRow(label: String, value: String) -> some View {
        HStack {
            Text(label)
                .font(.caption)
                .foregroundColor(.textMuted)
            
            Spacer()
            
            Text(value)
                .font(.caption.monospaced())
                .foregroundColor(.textSecondary)
        }
    }
    
    // MARK: - Helpers
    
    private func sectionHeader(_ title: String, icon: String) -> some View {
        HStack(spacing: 8) {
            Image(systemName: icon)
                .font(.caption)
                .foregroundColor(.brandPrimary)
            
            Text(title.uppercased())
                .font(.caption.weight(.bold))
                .foregroundColor(.textMuted)
                .tracking(1)
        }
    }
    
    private func setupAttackBreakdowns() {
        attackBreakdowns = [
            AttackBreakdown(
                title: "Homograph / IDN Attack",
                severity: .critical,
                icon: "textformat",
                description: "The URL uses Cyrillic characters (e.g., 'a', 'o') to mimic the legitimate domain \"paypal.com\".",
                technicalDetail: "Cyrillic 'а' used instead of Latin 'a'",
                isExpanded: true
            ),
            AttackBreakdown(
                title: "Suspicious Redirect Chain",
                severity: .high,
                icon: "arrow.triangle.branch",
                description: "Multiple redirects detected through URL shorteners and tracking domains.",
                technicalDetail: nil
            ),
            AttackBreakdown(
                title: "Obfuscated JavaScript",
                severity: .medium,
                icon: "chevron.left.forwardslash.chevron.right",
                description: "Heavily encoded JavaScript detected that attempts to bypass security filters.",
                technicalDetail: nil
            )
        ]
    }
    
    private func blockAndReport() {
        SettingsManager.shared.triggerHaptic(.warning)
        SettingsManager.shared.playSound(.warning)
        
        // In real implementation: Add to blocklist, report to backend
        #if DEBUG
        print("🚫 Blocked and reported: \(assessment.url)")
        #endif
        
        dismiss()
    }
    
    private var shareText: String {
        """
        🛡️ QR-SHIELD Security Alert
        
        URL: \(assessment.url)
        Verdict: \(assessment.verdict.rawValue)
        Risk Score: \(assessment.score)/100
        
        Flags: \(assessment.flags.joined(separator: ", "))
        
        Analyzed by QR-SHIELD for iOS
        """
    }
}

// MARK: - Flow Layout (for tags)

struct FlowLayout: Layout {
    var spacing: CGFloat = 8
    
    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        let result = FlowResult(in: proposal.width ?? 0, subviews: subviews, spacing: spacing)
        return result.size
    }
    
    func placeSubviews(in bounds: CGRect, proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) {
        let result = FlowResult(in: bounds.width, subviews: subviews, spacing: spacing)
        
        for (index, subview) in subviews.enumerated() {
            subview.place(at: CGPoint(x: bounds.minX + result.positions[index].x,
                                       y: bounds.minY + result.positions[index].y),
                         proposal: .unspecified)
        }
    }
    
    struct FlowResult {
        var size: CGSize = .zero
        var positions: [CGPoint] = []
        
        init(in maxWidth: CGFloat, subviews: Subviews, spacing: CGFloat) {
            var x: CGFloat = 0
            var y: CGFloat = 0
            var rowHeight: CGFloat = 0
            
            for subview in subviews {
                let size = subview.sizeThatFits(.unspecified)
                
                if x + size.width > maxWidth, x > 0 {
                    x = 0
                    y += rowHeight + spacing
                    rowHeight = 0
                }
                
                positions.append(CGPoint(x: x, y: y))
                rowHeight = max(rowHeight, size.height)
                x += size.width + spacing
                
                self.size.width = max(self.size.width, x - spacing)
                self.size.height = y + rowHeight
            }
        }
    }
}

// MARK: - Sandbox Preview Sheet

struct SandboxPreviewSheet: View {
    let url: String
    
    @Environment(\.dismiss) private var dismiss
    @State private var copiedURL = false
    @State private var showOpenConfirmation = false
    
    private var urlComponents: URLComponents? {
        URLComponents(string: url)
    }
    
    private var isHTTPS: Bool {
        urlComponents?.scheme?.lowercased() == "https"
    }
    
    private var domain: String {
        urlComponents?.host ?? "Unknown"
    }
    
    private var path: String {
        urlComponents?.path ?? ""
    }
    
    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 24) {
                    // Security Warning
                    securityWarning
                    
                    // URL Analysis Card
                    urlAnalysisCard
                    
                    // Actions
                    actionsSection
                }
                .padding(20)
            }
            .scrollContentBackground(.hidden)
            .background {
                LiquidGlassBackground()
                    .ignoresSafeArea()
            }
            .navigationTitle("URL Analysis")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Close") {
                        dismiss()
                    }
                    .foregroundColor(.brandPrimary)
                }
            }
            .confirmationDialog(
                "⚠️ Security Warning",
                isPresented: $showOpenConfirmation,
                titleVisibility: .visible
            ) {
                Button("Open Anyway", role: .destructive) {
                    if let url = URL(string: url) {
                        UIApplication.shared.open(url)
                    }
                }
                Button("Cancel", role: .cancel) {}
            } message: {
                Text(NSLocalizedString("result.dangerous_full_warning", comment: ""))
            }
        }
    }
    
    private var securityWarning: some View {
        HStack(spacing: 12) {
            Image(systemName: "exclamationmark.shield.fill")
                .font(.title2)
                .foregroundColor(.verdictWarning)
            
            VStack(alignment: .leading, spacing: 4) {
                Text("result.restricted_mode")
                    .font(.headline)
                    .foregroundColor(.textPrimary)
                
                Text("result.restricted_description")
                    .font(.caption)
                    .foregroundColor(.textSecondary)
            }
        }
        .padding(16)
        .background(Color.verdictWarning.opacity(0.1), in: RoundedRectangle(cornerRadius: 16))
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(Color.verdictWarning.opacity(0.3), lineWidth: 1)
        )
    }
    
    private var urlAnalysisCard: some View {
        VStack(alignment: .leading, spacing: 16) {
            // Security Status
            HStack(spacing: 12) {
                Image(systemName: isHTTPS ? "lock.fill" : "lock.open.fill")
                    .font(.title3)
                    .foregroundColor(isHTTPS ? .verdictSafe : .verdictDanger)
                
                VStack(alignment: .leading, spacing: 2) {
                    Text(isHTTPS ? "Encrypted Connection" : "Unencrypted Connection")
                        .font(.subheadline.weight(.semibold))
                        .foregroundColor(.textPrimary)
                    
                    Text(isHTTPS ? "HTTPS" : "HTTP - Not Secure")
                        .font(.caption)
                        .foregroundColor(isHTTPS ? .verdictSafe : .verdictDanger)
                }
                
                Spacer()
            }
            .padding(12)
            .background(Color.bgSurface.opacity(0.5), in: RoundedRectangle(cornerRadius: 12))
            
            Divider()
            
            // URL Breakdown
            VStack(alignment: .leading, spacing: 12) {
                Text("result.url_breakdown")
                    .font(.caption.weight(.bold))
                    .foregroundColor(.textMuted)
                    .tracking(1)
                
                breakdownRow(label: "Domain", value: domain)
                
                if !path.isEmpty && path != "/" {
                    breakdownRow(label: "Path", value: path)
                }
                
                if let query = urlComponents?.query, !query.isEmpty {
                    breakdownRow(label: "Parameters", value: query)
                }
            }
            
            Divider()
            
            // Full URL
            VStack(alignment: .leading, spacing: 8) {
                HStack {
                    Text("result.full_url")
                        .font(.caption.weight(.bold))
                        .foregroundColor(.textMuted)
                        .tracking(1)
                    
                    Spacer()
                    
                    Button {
                        UIPasteboard.general.string = url
                        copiedURL = true
                        SettingsManager.shared.triggerHaptic(.success)
                        
                        DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
                            copiedURL = false
                        }
                    } label: {
                        HStack(spacing: 4) {
                            Image(systemName: copiedURL ? "checkmark" : "doc.on.doc")
                            Text(copiedURL ? "Copied!" : "Copy")
                        }
                        .font(.caption)
                        .foregroundColor(.brandPrimary)
                    }
                    .contentTransition(.symbolEffect(.replace))
                }
                
                Text(url)
                    .font(.system(.caption, design: .monospaced))
                    .foregroundColor(.textSecondary)
                    .textSelection(.enabled)
                    .padding(12)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .background(Color.bgMain, in: RoundedRectangle(cornerRadius: 8))
            }
        }
        .padding(16)
        .liquidGlass(cornerRadius: 16)
    }
    
    private func breakdownRow(label: String, value: String) -> some View {
        HStack(alignment: .top) {
            Text(label)
                .font(.caption)
                .foregroundColor(.textMuted)
                .frame(width: 80, alignment: .leading)
            
            Text(value)
                .font(.system(.caption, design: .monospaced))
                .foregroundColor(.textPrimary)
                .textSelection(.enabled)
        }
    }
    
    private var actionsSection: some View {
        VStack(spacing: 12) {
            // Open in Browser (with warning and confirmation)
            Button {
                showOpenConfirmation = true
            } label: {
                HStack {
                    Image(systemName: "safari")
                    Text(NSLocalizedString("result.open_safari_risky", comment: ""))
                }
                .font(.headline)
                .foregroundColor(.verdictDanger)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 16)
                .background(Color.verdictDanger.opacity(0.1), in: RoundedRectangle(cornerRadius: 14))
                .overlay(
                    RoundedRectangle(cornerRadius: 14)
                        .stroke(Color.verdictDanger.opacity(0.3), lineWidth: 1)
                )
            }
            
            Text(NSLocalizedString("result.open_warning_browser", comment: ""))
                .font(.caption)
                .foregroundColor(.textMuted)
                .multilineTextAlignment(.center)
        }
    }
}

#endif
