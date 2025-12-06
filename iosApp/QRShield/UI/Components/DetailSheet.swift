// UI/Components/DetailSheet.swift
// QR-SHIELD Detail Sheet - iOS 26 Liquid Glass Edition
//
// UPDATED: December 2025
// - Liquid Glass design
// - Enhanced sheet styling
// - iOS 26 share functionality

import SwiftUI

/// Full analysis detail sheet with Liquid Glass design
struct DetailSheet: View {
    let assessment: RiskAssessmentMock
    @Environment(\.dismiss) private var dismiss
    @State private var showShareSheet = false
    
    var themeColor: Color {
        switch assessment.verdict {
        case .safe: return .verdictSafe
        case .suspicious: return .verdictWarning
        case .malicious: return .verdictDanger
        case .unknown: return .verdictUnknown
        }
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
                    
                    // Actions
                    actionsSection
                }
                .padding(.horizontal, 20)
                .padding(.top, 20)
                .padding(.bottom, 40)
            }
            .scrollContentBackground(.hidden)
            .background {
                MeshGradient.liquidGlassBackground
                    .ignoresSafeArea()
            }
            .navigationTitle("Analysis Details")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
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
    }
    
    // MARK: - Header Section (Liquid Glass)
    
    private var headerSection: some View {
        VStack(spacing: 16) {
            // Large verdict icon with glass container
            ZStack {
                Circle()
                    .fill(themeColor.opacity(0.15))
                    .frame(width: 100, height: 100)
                
                Circle()
                    .stroke(themeColor.opacity(0.3), lineWidth: 2)
                    .frame(width: 100, height: 100)
                
                Image(systemName: verdictIcon)
                    .font(.system(size: 50))
                    .foregroundColor(themeColor)
                    .symbolEffect(.pulse)
            }
            .shadow(color: themeColor.opacity(0.4), radius: 15)
            
            // Verdict Text
            Text(assessment.verdict.rawValue)
                .font(.title)
                .fontWeight(.bold)
                .foregroundColor(themeColor)
            
            // Score
            HStack(spacing: 20) {
                scoreItem(title: "Risk Score", value: "\(assessment.score)", color: themeColor)
                
                Divider()
                    .frame(height: 40)
                
                scoreItem(title: "Confidence", value: "\(Int(assessment.confidence * 100))%", color: .brandPrimary)
            }
            .padding(.horizontal, 24)
            .padding(.vertical, 16)
            .liquidGlass(cornerRadius: 16)
        }
    }
    
    private var verdictIcon: String {
        switch assessment.verdict {
        case .safe: return "checkmark.shield.fill"
        case .suspicious: return "exclamationmark.shield.fill"
        case .malicious: return "xmark.shield.fill"
        case .unknown: return "questionmark.circle"
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
    
    // MARK: - Score Breakdown (Liquid Glass)
    
    private var scoreBreakdownSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            sectionTitle("Score Breakdown")
            
            VStack(spacing: 12) {
                breakdownRow(title: "URL Analysis", score: min(assessment.score * 2, 100), color: .brandPrimary)
                breakdownRow(title: "Domain Reputation", score: assessment.score, color: .brandSecondary)
                breakdownRow(title: "Pattern Detection", score: max(0, assessment.score - 10), color: .verdictWarning)
            }
            .padding(16)
            .liquidGlass(cornerRadius: 16)
        }
    }
    
    private func breakdownRow(title: String, score: Int, color: Color) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            HStack {
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
                        .frame(width: geometry.size.width * CGFloat(score) / 100, height: 6)
                }
            }
            .frame(height: 6)
        }
    }
    
    // MARK: - Risk Flags (Liquid Glass)
    
    private var riskFlagsSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            sectionTitle("Risk Flags")
            
            VStack(spacing: 8) {
                ForEach(assessment.flags, id: \.self) { flag in
                    HStack(spacing: 12) {
                        Image(systemName: "exclamationmark.triangle.fill")
                            .foregroundColor(.verdictWarning)
                            .symbolEffect(.pulse)
                        
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
            sectionTitle("URL Details")
            
            VStack(alignment: .leading, spacing: 16) {
                // Full URL
                VStack(alignment: .leading, spacing: 4) {
                    Text("Full URL")
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
                    Text("Domain")
                        .font(.caption)
                        .foregroundColor(.textMuted)
                    
                    Text(extractDomain(from: assessment.url))
                        .font(.subheadline.weight(.medium))
                        .foregroundColor(.textPrimary)
                }
                
                // Protocol
                HStack {
                    VStack(alignment: .leading, spacing: 4) {
                        Text("Protocol")
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
    
    // MARK: - Actions (Liquid Glass iOS 26)
    
    private var actionsSection: some View {
        VStack(spacing: 12) {
            // Share Button
            ShareLink(item: assessment.url) {
                HStack {
                    Image(systemName: "square.and.arrow.up")
                    Text("Share Analysis")
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
            
            // Copy URL Button
            Button {
                UIPasteboard.general.string = assessment.url
                let generator = UINotificationFeedbackGenerator()
                generator.notificationOccurred(.success)
            } label: {
                HStack {
                    Image(systemName: "doc.on.doc")
                    Text("Copy URL")
                }
                .font(.headline)
                .foregroundColor(.textPrimary)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 16)
                .background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: 14))
            }
            .sensoryFeedback(.impact(weight: .light), trigger: UUID())
            
            // Report Button
            Button {
                // Report logic
            } label: {
                HStack {
                    Image(systemName: "flag")
                    Text("Report False Positive")
                }
                .font(.subheadline)
                .foregroundColor(.textSecondary)
            }
            .padding(.top, 8)
        }
    }
    
    // MARK: - Helpers
    
    private func sectionTitle(_ title: String) -> some View {
        Text(title)
            .font(.headline)
            .foregroundColor(.textPrimary)
    }
    
    private func extractDomain(from url: String) -> String {
        guard let urlObj = URL(string: url) else { return url }
        return urlObj.host ?? url
    }
}

#Preview {
    DetailSheet(assessment: RiskAssessmentMock(
        score: 72,
        verdict: .suspicious,
        flags: ["Suspicious domain pattern", "Recently registered domain"],
        confidence: 0.85,
        url: "https://suspicious-site.xyz/login"
    ))
}
