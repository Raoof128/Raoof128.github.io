// UI/Components/DetailSheet.swift
// QR-SHIELD Analysis Details Bottom Sheet
//
// Presents comprehensive analysis breakdown in a modal sheet.
// Shows heuristic details, ML scores, brand detection, and more.

import SwiftUI

/// Bottom sheet that shows full analysis details
struct DetailSheet: View {
    let assessment: RiskAssessmentMock // Replace with common.RiskAssessment
    
    @Environment(\.dismiss) private var dismiss
    
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
                    // Header Card
                    headerCard
                    
                    // Score Breakdown
                    scoreBreakdownSection
                    
                    // Risk Flags
                    if !assessment.flags.isEmpty {
                        flagsSection
                    }
                    
                    // URL Details
                    urlSection
                    
                    // Actions
                    actionsSection
                }
                .padding()
            }
            .background(Color.bgDark)
            .navigationTitle("Analysis Details")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Done") { dismiss() }
                        .foregroundColor(.brandPrimary)
                }
            }
        }
        .presentationDetents([.medium, .large])
        .presentationDragIndicator(.visible)
    }
    
    // MARK: - Header Card
    
    private var headerCard: some View {
        VStack(spacing: 16) {
            // Icon
            ZStack {
                Circle()
                    .fill(themeColor.opacity(0.2))
                    .frame(width: 80, height: 80)
                
                Image(systemName: assessment.verdict == .safe ? "checkmark.shield.fill" : "xmark.shield.fill")
                    .font(.system(size: 40))
                    .foregroundColor(themeColor)
            }
            
            // Score
            Text("\(assessment.score)")
                .font(.system(size: 48, weight: .bold, design: .rounded))
                .foregroundColor(themeColor)
            
            Text(assessment.verdict.rawValue)
                .font(.title3.weight(.semibold))
                .foregroundColor(.textPrimary)
            
            // Confidence
            HStack {
                Image(systemName: "chart.bar.fill")
                    .foregroundColor(.brandSecondary)
                Text("Confidence: \(Int(assessment.confidence * 100))%")
                    .foregroundColor(.textSecondary)
            }
            .font(.subheadline)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 24)
        .background(Color.bgCard)
        .cornerRadius(20)
    }
    
    // MARK: - Score Breakdown
    
    private var scoreBreakdownSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            sectionHeader("Score Breakdown")
            
            VStack(spacing: 8) {
                ScoreRow(label: "Heuristics", score: 45, maxScore: 100, color: .brandPrimary)
                ScoreRow(label: "ML Model", score: 38, maxScore: 100, color: .brandSecondary)
                ScoreRow(label: "Brand Check", score: 0, maxScore: 100, color: .verdictSafe)
                ScoreRow(label: "TLD Risk", score: 10, maxScore: 100, color: .verdictWarning)
            }
            .padding()
            .background(Color.bgCard)
            .cornerRadius(16)
        }
    }
    
    // MARK: - Flags Section
    
    private var flagsSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            sectionHeader("Risk Factors")
            
            VStack(spacing: 0) {
                ForEach(Array(assessment.flags.enumerated()), id: \.offset) { index, flag in
                    HStack {
                        Image(systemName: "exclamationmark.triangle.fill")
                            .foregroundColor(.verdictWarning)
                        
                        Text(flag)
                            .foregroundColor(.textPrimary)
                        
                        Spacer()
                    }
                    .padding()
                    
                    if index < assessment.flags.count - 1 {
                        Divider()
                            .background(Color.bgSurface)
                    }
                }
            }
            .background(Color.bgCard)
            .cornerRadius(16)
        }
    }
    
    // MARK: - URL Section
    
    private var urlSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            sectionHeader("Scanned URL")
            
            VStack(alignment: .leading, spacing: 8) {
                Text(assessment.url)
                    .font(.system(.body, design: .monospaced))
                    .foregroundColor(.textPrimary)
                    .lineLimit(5)
                
                Button(action: copyUrl) {
                    HStack {
                        Image(systemName: "doc.on.doc")
                        Text("Copy URL")
                    }
                    .font(.caption.weight(.medium))
                    .foregroundColor(.brandPrimary)
                }
            }
            .padding()
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(Color.bgCard)
            .cornerRadius(16)
        }
    }
    
    // MARK: - Actions Section
    
    private var actionsSection: some View {
        VStack(spacing: 12) {
            Button(action: shareAnalysis) {
                HStack {
                    Image(systemName: "square.and.arrow.up")
                    Text("Share Analysis")
                }
                .font(.headline)
                .foregroundColor(.white)
                .frame(maxWidth: .infinity)
                .padding()
                .background(LinearGradient.brandGradient)
                .cornerRadius(14)
            }
            
            Button(action: reportFalsePositive) {
                HStack {
                    Image(systemName: "flag")
                    Text("Report Issue")
                }
                .font(.subheadline)
                .foregroundColor(.textSecondary)
            }
        }
        .padding(.top, 8)
    }
    
    // MARK: - Helpers
    
    private func sectionHeader(_ title: String) -> some View {
        Text(title)
            .font(.headline)
            .foregroundColor(.textPrimary)
    }
    
    private func copyUrl() {
        UIPasteboard.general.string = assessment.url
        // Show toast feedback
    }
    
    private func shareAnalysis() {
        // Trigger share sheet
    }
    
    private func reportFalsePositive() {
        // Open report form
    }
}

// MARK: - Score Row Component

struct ScoreRow: View {
    let label: String
    let score: Int
    let maxScore: Int
    let color: Color
    
    var body: some View {
        VStack(spacing: 4) {
            HStack {
                Text(label)
                    .font(.subheadline)
                    .foregroundColor(.textSecondary)
                Spacer()
                Text("\(score)")
                    .font(.subheadline.weight(.medium))
                    .foregroundColor(.textPrimary)
            }
            
            GeometryReader { geometry in
                ZStack(alignment: .leading) {
                    RoundedRectangle(cornerRadius: 4)
                        .fill(Color.bgSurface)
                        .frame(height: 6)
                    
                    RoundedRectangle(cornerRadius: 4)
                        .fill(color)
                        .frame(width: geometry.size.width * CGFloat(score) / CGFloat(maxScore), height: 6)
                }
            }
            .frame(height: 6)
        }
    }
}

// MARK: - Preview

#Preview {
    DetailSheet(assessment: RiskAssessmentMock(
        score: 78,
        verdict: .malicious,
        flags: ["Known phishing domain", "Suspicious URL pattern", "HTTP instead of HTTPS"],
        confidence: 0.87,
        url: "https://g00gle-secure.com/login?redirect=account"
    ))
}
