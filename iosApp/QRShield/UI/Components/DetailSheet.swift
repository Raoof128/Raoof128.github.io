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

// UI/Components/DetailSheet.swift
// QR-SHIELD Detail Sheet - iOS 17+ Liquid Glass Edition
//
// UPDATED: December 2025 - iOS 17+ Compatible
// - VerdictIcon integration
// - Enhanced sheet styling
// - iOS 17+ share functionality
// - Animated progress bars

import SwiftUI
#if os(iOS)
import UIKit

/// Full analysis detail sheet with Liquid Glass design
struct DetailSheet: View {
    let assessment: RiskAssessmentMock
    @Environment(\.dismiss) private var dismiss
    @State private var isAppearing = false
    @State private var copiedURL = false
    @State private var showOpenURLConfirmation = false
    @State private var reportSubmitted = false
    
    var themeColor: Color {
        Color.forVerdict(assessment.verdict)
    }
    
    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 24) {
                    // Header
                    headerSection
                    
                    // Score Breakdown
                    scoreBreakdownSection
                    
                    // Risk Flags
                    if !assessment.flags.isEmpty {
                        riskFlagsSection
                    }
                    
                    // URL Details
                    urlDetailsSection
                    
                    // Scanned At
                    scannedAtSection
                    
                    // Actions
                    actionsSection
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
            .navigationTitle("Analysis Details")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Text(assessment.verdict.rawValue)
                        .font(.caption.weight(.semibold))
                        .foregroundColor(themeColor)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 4)
                        .background(themeColor.opacity(0.15), in: Capsule())
                }
                
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button {
                        dismiss()
                    } label: {
                        Image(systemName: "xmark.circle.fill")
                            .foregroundStyle(.secondary)
                            .font(.title3)
                    }
                }
            }
        }
        .onAppear {
            withAnimation(.spring(response: 0.6).delay(0.1)) {
                isAppearing = true
            }
        }
    }
    
    // MARK: - Header Section (Liquid Glass iOS 17+)
    
    private var headerSection: some View {
        VStack(spacing: 16) {
            // Large verdict icon using VerdictIcon component
            VerdictIcon(verdict: assessment.verdict, size: 60)
                .frame(width: 100, height: 100)
                .background {
                    Circle()
                        .fill(themeColor.opacity(0.15))
                    Circle()
                        .stroke(themeColor.opacity(0.3), lineWidth: 2)
                }
                .shadow(color: themeColor.opacity(0.4), radius: 15)
            
            // Verdict Text
            Text(assessment.verdict.rawValue)
                .font(.title)
                .fontWeight(.bold)
                .foregroundColor(themeColor)
            
            // Score
            HStack(spacing: 20) {
                scoreItem(title: "Risk Score", value: "\(assessment.score)/100", color: themeColor)
                
                Divider()
                    .frame(height: 40)
                
                scoreItem(title: "Confidence", value: "\(Int(assessment.confidence * 100))%", color: .brandPrimary)
            }
            .padding(.horizontal, 24)
            .padding(.vertical, 16)
            .liquidGlass(cornerRadius: 16)
        }
    }
    
    private func scoreItem(title: String, value: String, color: Color) -> some View {
        VStack(spacing: 4) {
            Text(value)
                .font(.title2.weight(.bold))
                .foregroundColor(color)
                .contentTransition(.numericText())
            
            Text(title)
                .font(.caption)
                .foregroundColor(.textMuted)
        }
    }
    
    // MARK: - Score Breakdown (Liquid Glass with Animation)
    
    private var scoreBreakdownSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            sectionTitle("Score Breakdown", icon: "chart.bar.fill")
            
            VStack(spacing: 12) {
                breakdownRow(
                    title: "URL Details",
                    icon: "link",
                    score: min(assessment.score * 2, 100),
                    color: .brandPrimary,
                    delay: 0
                )
                breakdownRow(
                    title: "Domain",
                    icon: "globe",
                    score: assessment.score,
                    color: .brandSecondary,
                    delay: 0.1
                )
                breakdownRow(
                    title: "Risk Factors",
                    icon: "waveform",
                    score: max(0, assessment.score - 10),
                    color: .verdictWarning,
                    delay: 0.2
                )
                breakdownRow(
                    title: "Confidence",
                    icon: "brain",
                    score: Int(assessment.confidence * 100),
                    color: .verdictSafe,
                    delay: 0.3
                )
            }
            .padding(16)
            .liquidGlass(cornerRadius: 16)
        }
    }
    
    private func breakdownRow(title: String, icon: String, score: Int, color: Color, delay: Double) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            HStack {
                Image(systemName: icon)
                    .font(.caption)
                    .foregroundColor(color)
                
                Text(title)
                    .font(.subheadline)
                    .foregroundColor(.textPrimary)
                
                Spacer()
                
                Text("\(score)/100")
                    .font(.caption.weight(.semibold))
                    .foregroundColor(color)
            }
            
            GeometryReader { geometry in
                ZStack(alignment: .leading) {
                    RoundedRectangle(cornerRadius: 3)
                        .fill(Color.bgSurface)
                        .frame(height: 6)
                    
                    RoundedRectangle(cornerRadius: 3)
                        .fill(color)
                        .frame(
                            width: isAppearing ? geometry.size.width * CGFloat(score) / 100 : 0,
                            height: 6
                        )
                        .animation(.spring(response: 0.8).delay(delay), value: isAppearing)
                }
            }
            .frame(height: 6)
        }
    }
    
    // MARK: - Risk Flags (Liquid Glass)
    
    private var riskFlagsSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            sectionTitle("Risk Flags", icon: "exclamationmark.triangle.fill")
            
            VStack(spacing: 8) {
                ForEach(assessment.flags, id: \.self) { flag in
                    HStack(spacing: 12) {
                        Image(systemName: "exclamationmark.triangle.fill")
                            .foregroundColor(.verdictWarning)
                            .symbolEffect(.pulse, isActive: assessment.verdict == .malicious)
                        
                        Text(flag)
                            .font(.subheadline)
                            .foregroundColor(.textPrimary)
                        
                        Spacer()
                    }
                    .padding(12)
                    .background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: 10))
                }
            }
        }
    }
    
    // MARK: - URL Details (Liquid Glass)
    
    private var urlDetailsSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            sectionTitle("URL Details", icon: "link")
            
            VStack(alignment: .leading, spacing: 16) {
                // Full URL
                VStack(alignment: .leading, spacing: 4) {
                    Text(NSLocalizedString("detail.url_label", comment: ""))
                        .font(.caption)
                        .foregroundColor(.textMuted)
                    
                    Text(assessment.url)
                        .font(.system(.subheadline, design: .monospaced))
                        .foregroundColor(.textPrimary)
                        .textSelection(.enabled)
                }
                
                Divider()
                
                // Domain
                VStack(alignment: .leading, spacing: 4) {
                    Text(NSLocalizedString("detail.domain", comment: ""))
                        .font(.caption)
                        .foregroundColor(.textMuted)
                    
                    Text(extractDomain(from: assessment.url))
                        .font(.subheadline.weight(.medium))
                        .foregroundColor(.textPrimary)
                }
                
                // Protocol
                HStack {
                    VStack(alignment: .leading, spacing: 4) {
                        Text(NSLocalizedString("detail.protocol", comment: ""))
                            .font(.caption)
                            .foregroundColor(.textMuted)
                        
                        HStack(spacing: 6) {
                            Image(systemName: assessment.url.hasPrefix("https") ? "lock.fill" : "lock.open")
                                .foregroundColor(assessment.url.hasPrefix("https") ? .verdictSafe : .verdictWarning)
                            
                            Text(assessment.url.hasPrefix("https") ? "HTTPS (Secure)" : "HTTP (Not Secure)")
                                .font(.subheadline)
                                .foregroundColor(assessment.url.hasPrefix("https") ? .verdictSafe : .verdictWarning)
                        }
                    }
                    
                    Spacer()
                }
            }
            .padding(16)
            .liquidGlass(cornerRadius: 16)
        }
    }
    
    // MARK: - Scanned At Section
    
    private var scannedAtSection: some View {
        HStack {
            Image(systemName: "clock")
                .foregroundColor(.textMuted)
            
            Text(String(format: "Scanned %@", assessment.formattedDate))
                .font(.subheadline)
                .foregroundColor(.textSecondary)
            
            Spacer()
        }
        .padding(.horizontal)
    }
    
    // MARK: - Actions (Liquid Glass iOS 17+)
    
    private var actionsSection: some View {
        VStack(spacing: 12) {
            // Share Button
            ShareLink(item: shareText) {
                HStack {
                    Image(systemName: "square.and.arrow.up")
                    Text(NSLocalizedString("detail.share_analysis", comment: ""))
                }
                .font(.headline)
                .foregroundColor(.white)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 16)
                .background {
                    RoundedRectangle(cornerRadius: 14)
                        .fill(LinearGradient.brandGradient)
                        .overlay {
                            RoundedRectangle(cornerRadius: 14)
                                .stroke(Color.white.opacity(0.2), lineWidth: 1)
                        }
                }
                .shadow(color: .brandPrimary.opacity(0.4), radius: 10, y: 4)
            }
            
            // Open URL Button (with safety confirmation for dangerous URLs)
            Button {
                if assessment.verdict == .safe || assessment.verdict == .unknown {
                    openURL()
                } else {
                    showOpenURLConfirmation = true
                }
            } label: {
                HStack {
                    Image(systemName: "globe")
                    Text(NSLocalizedString("detail.open_url", comment: ""))
                }
                .font(.headline)
                .foregroundColor(assessment.verdict == .safe ? .verdictSafe : .textPrimary)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 16)
                .background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: 14))
            }
            .confirmationDialog(
                "Security Warning",
                isPresented: $showOpenURLConfirmation,
                titleVisibility: .visible
            ) {
                Button("Proceed Anyway", role: .destructive) {
                    openURL()
                }
                Button("Cancel", role: .cancel) {}
            } message: {
                Text(NSLocalizedString("detail.open_warning", comment: ""))
            }
            
            // Copy URL Button
            Button {
                UIPasteboard.general.string = assessment.url
                copiedURL = true
                
                let generator = UINotificationFeedbackGenerator()
                generator.notificationOccurred(.success)
                
                // Reset after delay
                DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
                    copiedURL = false
                }
            } label: {
                HStack {
                    Image(systemName: copiedURL ? "checkmark" : "doc.on.doc")
                        .contentTransition(.symbolEffect(.replace))
                    Text(copiedURL ? "Copied!" : "Copy URL")
                }
                .font(.headline)
                .foregroundColor(copiedURL ? .verdictSafe : .textPrimary)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 16)
                .background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: 14))
            }
            .sensoryFeedback(.success, trigger: copiedURL)
            
            // Report Button - Saves report locally and copies to clipboard
            Button {
                submitFalsePositiveReport()
            } label: {
                HStack {
                    Image(systemName: reportSubmitted ? "checkmark.circle.fill" : "flag")
                        .contentTransition(.symbolEffect(.replace))
                    Text(reportSubmitted ? "Report Submitted!" : "Report False Positive")
                }
                .font(.subheadline)
                .foregroundColor(reportSubmitted ? .verdictSafe : .textSecondary)
            }
            .disabled(reportSubmitted)
            .padding(.top, 8)
        }
    }
    
    /// Submits a false positive report by saving locally and copying to clipboard
    private func submitFalsePositiveReport() {
        let reportText = """
        QR-SHIELD False Positive Report
        ================================
        URL: \(assessment.url)
        Verdict: \(assessment.verdict.rawValue)
        Score: \(assessment.score)/100
        Confidence: \(Int(assessment.confidence * 100))%
        Flags: \(assessment.flags.joined(separator: ", "))
        Date: \(ISO8601DateFormatter().string(from: Date()))
        """
        
        // Copy to clipboard
        UIPasteboard.general.string = reportText
        
        // Save to local storage (append to existing reports)
        var existingReports = UserDefaults.standard.stringArray(forKey: "falsePositiveReports") ?? []
        existingReports.append(reportText)
        UserDefaults.standard.set(existingReports, forKey: "falsePositiveReports")
        
        // Show success feedback
        let generator = UINotificationFeedbackGenerator()
        generator.notificationOccurred(.success)
        
        withAnimation {
            reportSubmitted = true
        }
        
        #if DEBUG
        print("📋 False positive report submitted and saved locally")
        print("Total reports: \(existingReports.count)")
        #endif
    }
    
    // MARK: - Helpers
    
    private func sectionTitle(_ title: String, icon: String) -> some View {
        HStack(spacing: 8) {
            Image(systemName: icon)
                .foregroundColor(.brandPrimary)
            Text(title)
                .font(.headline)
                .foregroundColor(.textPrimary)
        }
    }
    
    private func extractDomain(from url: String) -> String {
        guard let urlObj = URL(string: url) else { return url }
        return urlObj.host ?? url
    }
    
    private func openURL() {
        guard let url = URL(string: assessment.url) else { return }
        UIApplication.shared.open(url)
        SettingsManager.shared.triggerHaptic(.medium)
    }
    
    private var shareText: String {
        """
        🛡️ QR-SHIELD Analysis
        
        URL: \(assessment.url)
        Verdict: \(assessment.verdict.rawValue)
        Risk Score: \(assessment.score)/100
        Confidence: \(Int(assessment.confidence * 100))%
        
        Flags: \(assessment.flags.joined(separator: ", "))
        
        Scanned with QR-SHIELD for iOS 17+
        """
    }
}

#endif
