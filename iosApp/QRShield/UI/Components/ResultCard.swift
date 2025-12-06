// UI/Components/ResultCard.swift
// QR-SHIELD Animated Result Card - iOS 18+ Optimized
//
// UPDATED: December 2024
// - SF Symbol animations with symbolEffect
// - contentTransition for smooth state changes
// - phaseAnimator for complex animations

import SwiftUI

/// Animated result card with verdict-specific theming
struct ResultCard: View {
    let assessment: RiskAssessmentMock
    
    @State private var isAppearing = false
    @State private var showingFullFlags = false
    
    var themeColor: Color {
        switch assessment.verdict {
        case .safe: return .verdictSafe
        case .suspicious: return .verdictWarning
        case .malicious: return .verdictDanger
        case .unknown: return .verdictUnknown
        }
    }
    
    var iconName: String {
        switch assessment.verdict {
        case .safe: return "checkmark.shield.fill"
        case .suspicious: return "exclamationmark.shield.fill"
        case .malicious: return "xmark.shield.fill"
        case .unknown: return "questionmark.circle"
        }
    }
    
    var body: some View {
        VStack(spacing: 16) {
            // Header Row
            headerSection
            
            // Risk Score Bar
            scoreBar
            
            // Divider
            Divider()
                .background(Color.bgSurface)
            
            // Flags Section
            if !assessment.flags.isEmpty {
                flagsSection
            }
            
            // URL Preview
            urlPreview
            
            // Action Button
            actionButton
        }
        .padding(20)
        .background(Color.bgCard)
        .clipShape(RoundedRectangle(cornerRadius: 24))
        .shadow(color: themeColor.opacity(0.3), radius: 20, x: 0, y: 10)
        .padding(.horizontal, 24)
        .onAppear {
            withAnimation(.spring(response: 0.6, dampingFraction: 0.7)) {
                isAppearing = true
            }
        }
    }
    
    // MARK: - Header Section
    
    private var headerSection: some View {
        HStack(alignment: .center, spacing: 12) {
            // Verdict Icon with Animation
            ZStack {
                Circle()
                    .fill(themeColor.opacity(0.2))
                    .frame(width: 60, height: 60)
                
                Image(systemName: iconName)
                    .font(.system(size: 32))
                    .foregroundColor(themeColor)
                    .scaleEffect(isAppearing ? 1.0 : 0.5)
                    // iOS 17+: SF Symbol animation
                    .symbolEffect(.bounce, value: isAppearing)
            }
            
            // Text Info
            VStack(alignment: .leading, spacing: 4) {
                Text(assessment.verdict.rawValue)
                    .font(.title2)
                    .fontWeight(.bold)
                    .foregroundColor(themeColor)
                    .contentTransition(.numericText())
                
                Text("Risk Score: \(assessment.score)/100")
                    .font(.subheadline)
                    .foregroundColor(.textSecondary)
                    .contentTransition(.numericText(countsDown: assessment.score > 50))
            }
            
            Spacer()
            
            // Confidence Badge
            VStack(spacing: 2) {
                Text("\(Int(assessment.confidence * 100))%")
                    .font(.headline)
                    .fontWeight(.semibold)
                    .foregroundColor(.white)
                    .contentTransition(.numericText())
                
                Text("Confidence")
                    .font(.caption2)
                    .foregroundColor(.textMuted)
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 8)
            .background(Color.bgSurface, in: RoundedRectangle(cornerRadius: 10))
        }
    }
    
    // MARK: - Score Bar
    
    private var scoreBar: some View {
        GeometryReader { geometry in
            ZStack(alignment: .leading) {
                // Background
                RoundedRectangle(cornerRadius: 4)
                    .fill(Color.bgSurface)
                    .frame(height: 8)
                
                // Filled portion with animation
                RoundedRectangle(cornerRadius: 4)
                    .fill(
                        LinearGradient(
                            colors: [themeColor, themeColor.opacity(0.7)],
                            startPoint: .leading,
                            endPoint: .trailing
                        )
                    )
                    .frame(
                        width: isAppearing ? geometry.size.width * CGFloat(assessment.score) / 100 : 0,
                        height: 8
                    )
                    .animation(.spring(response: 0.8, dampingFraction: 0.7).delay(0.2), value: isAppearing)
            }
        }
        .frame(height: 8)
    }
    
    // MARK: - Flags Section
    
    private var flagsSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            ForEach(Array(assessment.flags.prefix(showingFullFlags ? 10 : 3).enumerated()), id: \.offset) { index, flag in
                HStack(spacing: 8) {
                    Image(systemName: "exclamationmark.triangle.fill")
                        .font(.caption)
                        .foregroundColor(.verdictWarning)
                        .symbolEffect(.pulse, isActive: assessment.verdict == .malicious)
                    
                    Text(flag)
                        .font(.caption)
                        .foregroundColor(.textSecondary)
                        .lineLimit(1)
                    
                    Spacer()
                }
                .transition(.asymmetric(
                    insertion: .move(edge: .top).combined(with: .opacity),
                    removal: .opacity
                ))
            }
            
            if assessment.flags.count > 3 {
                Button {
                    withAnimation(.spring(response: 0.4)) {
                        showingFullFlags.toggle()
                    }
                } label: {
                    Text(showingFullFlags ? "Show less" : "Show \(assessment.flags.count - 3) more...")
                        .font(.caption)
                        .foregroundColor(.brandPrimary)
                }
            }
        }
    }
    
    // MARK: - URL Preview
    
    private var urlPreview: some View {
        Text(assessment.url)
            .font(.caption)
            .foregroundColor(.textMuted)
            .lineLimit(1)
            .truncationMode(.middle)
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(10)
            .background(Color.bgSurface, in: RoundedRectangle(cornerRadius: 8))
            .textSelection(.enabled) // iOS 15+: Allow text selection
    }
    
    // MARK: - Action Button
    
    private var actionButton: some View {
        Button(action: {}) {
            HStack {
                Image(systemName: "arrow.up.forward.square")
                    .symbolEffect(.bounce, value: isAppearing)
                Text("View Full Analysis")
            }
            .font(.subheadline.weight(.semibold))
            .foregroundColor(.white)
            .frame(maxWidth: .infinity)
            .padding(.vertical, 12)
            .background(themeColor, in: RoundedRectangle(cornerRadius: 10))
        }
        .sensoryFeedback(.impact(weight: .light), trigger: \._button)
    }
}

// MARK: - Verdict Extension

extension VerdictMock {
    var displayEmoji: String {
        switch self {
        case .safe: return "✅"
        case .suspicious: return "⚠️"
        case .malicious: return "🚨"
        case .unknown: return "❓"
        }
    }
}

// MARK: - Preview

#Preview("Safe Result") {
    ZStack {
        Color.bgDark.ignoresSafeArea()
        
        ResultCard(assessment: RiskAssessmentMock(
            score: 15,
            verdict: .safe,
            flags: [],
            confidence: 0.92,
            url: "https://google.com/search?q=hello"
        ))
    }
}

#Preview("Malicious Result") {
    ZStack {
        Color.bgDark.ignoresSafeArea()
        
        ResultCard(assessment: RiskAssessmentMock(
            score: 78,
            verdict: .malicious,
            flags: ["Known phishing domain", "Suspicious URL pattern", "IP address instead of domain", "HTTP instead of HTTPS"],
            confidence: 0.87,
            url: "https://g00gle-secure.com/login"
        ))
    }
}
