// UI/Components/ResultCard.swift
// QR-SHIELD Animated Result Card
//
// Displays phishing analysis results with beautiful animations,
// verdict-specific colors, and confidence scores.

import SwiftUI

/// Animated result card that displays phishing analysis results
struct ResultCard: View {
    let assessment: RiskAssessmentMock // Replace with common.RiskAssessment
    
    @State private var isAppearing = false
    
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
            HStack(alignment: .center, spacing: 12) {
                // Verdict Icon (Animated)
                ZStack {
                    Circle()
                        .fill(themeColor.opacity(0.2))
                        .frame(width: 60, height: 60)
                    
                    Image(systemName: iconName)
                        .font(.system(size: 32))
                        .foregroundColor(themeColor)
                        .scaleEffect(isAppearing ? 1.0 : 0.5)
                }
                
                // Text Info
                VStack(alignment: .leading, spacing: 4) {
                    Text(assessment.verdict.rawValue)
                        .font(.title2)
                        .fontWeight(.bold)
                        .foregroundColor(themeColor)
                    
                    Text("Risk Score: \(assessment.score)/100")
                        .font(.subheadline)
                        .foregroundColor(.textSecondary)
                }
                
                Spacer()
                
                // Confidence Badge
                VStack(spacing: 2) {
                    Text("\(Int(assessment.confidence * 100))%")
                        .font(.headline)
                        .fontWeight(.semibold)
                        .foregroundColor(.white)
                    
                    Text("Confidence")
                        .font(.caption2)
                        .foregroundColor(.textMuted)
                }
                .padding(.horizontal, 12)
                .padding(.vertical, 8)
                .background(Color.bgSurface)
                .cornerRadius(10)
            }
            
            // Risk Score Bar
            GeometryReader { geometry in
                ZStack(alignment: .leading) {
                    // Background
                    RoundedRectangle(cornerRadius: 4)
                        .fill(Color.bgSurface)
                        .frame(height: 8)
                    
                    // Filled portion
                    RoundedRectangle(cornerRadius: 4)
                        .fill(themeColor)
                        .frame(
                            width: isAppearing ? geometry.size.width * CGFloat(assessment.score) / 100 : 0,
                            height: 8
                        )
                }
            }
            .frame(height: 8)
            
            // Divider
            Divider()
                .background(Color.bgSurface)
            
            // Flags Section
            if !assessment.flags.isEmpty {
                VStack(alignment: .leading, spacing: 8) {
                    ForEach(assessment.flags.prefix(3), id: \.self) { flag in
                        HStack(spacing: 8) {
                            Image(systemName: "exclamationmark.triangle.fill")
                                .font(.caption)
                                .foregroundColor(.verdictWarning)
                            
                            Text(flag)
                                .font(.caption)
                                .foregroundColor(.textSecondary)
                                .lineLimit(1)
                            
                            Spacer()
                        }
                    }
                }
            }
            
            // URL Preview
            Text(assessment.url)
                .font(.caption)
                .foregroundColor(.textMuted)
                .lineLimit(1)
                .truncationMode(.middle)
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(10)
                .background(Color.bgSurface)
                .cornerRadius(8)
            
            // Action Button
            Button(action: {}) {
                HStack {
                    Image(systemName: "arrow.up.forward.square")
                    Text("View Full Analysis")
                }
                .font(.subheadline.weight(.semibold))
                .foregroundColor(.white)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 12)
                .background(themeColor)
                .cornerRadius(10)
            }
        }
        .padding(20)
        .background(Color.bgCard)
        .cornerRadius(24)
        .shadow(color: themeColor.opacity(0.3), radius: 20, x: 0, y: 10)
        .padding(.horizontal, 24)
        .onAppear {
            withAnimation(.spring(response: 0.6, dampingFraction: 0.7)) {
                isAppearing = true
            }
        }
    }
}

// MARK: - Preview

#Preview {
    ZStack {
        Color.bgDark.ignoresSafeArea()
        
        VStack(spacing: 20) {
            ResultCard(assessment: RiskAssessmentMock(
                score: 15,
                verdict: .safe,
                flags: [],
                confidence: 0.92,
                url: "https://google.com/search?q=hello"
            ))
            
            ResultCard(assessment: RiskAssessmentMock(
                score: 78,
                verdict: .malicious,
                flags: ["Known phishing domain", "Suspicious URL pattern"],
                confidence: 0.87,
                url: "https://g00gle-secure.com/login"
            ))
        }
    }
}
