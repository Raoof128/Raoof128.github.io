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

// UI/Components/ResultCard.swift
// QR-SHIELD Result Card - iOS 17+ Liquid Glass Edition
//
// UPDATED: December 2025
// - Full Liquid Glass styling
// - Custom asset icons with SF Symbol fallback
// - Animated danger pulse background
// - iOS 17+ symbol effects

import SwiftUI
#if os(iOS)

/// Animated result card with Liquid Glass design and verdict-specific theming
struct ResultCard: View {
    let assessment: RiskAssessmentMock
    var onTap: (() -> Void)? = nil
    
    @State private var isAppearing = false
    @State private var showingFullFlags = false
    
    var themeColor: Color {
        Color.forVerdict(assessment.verdict)
    }
    
    var body: some View {
        ZStack {
            // Danger pulse background (only for malicious)
            if assessment.verdict == .malicious {
                DangerBackground(isActive: isAppearing)
            }
            
            // Main Card
            cardContent
        }
        .onAppear {
            withAnimation(.spring(response: 0.6, dampingFraction: 0.7)) {
                isAppearing = true
            }
        }
    }
    
    private var cardContent: some View {
        VStack(spacing: 16) {
            // Header Row
            headerSection
            
            // Risk Score Bar
            scoreBar
            
            // Divider with gradient
            Rectangle()
                .fill(
                    LinearGradient(
                        colors: [.clear, themeColor.opacity(0.3), .clear],
                        startPoint: .leading,
                        endPoint: .trailing
                    )
                )
                .frame(height: 1)
            
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
        .background {
            // Liquid Glass background
            RoundedRectangle(cornerRadius: 24)
                .fill(.ultraThinMaterial)
                .overlay {
                    // Inner border gradient
                    RoundedRectangle(cornerRadius: 24)
                        .stroke(
                            LinearGradient(
                                colors: [
                                    themeColor.opacity(0.4),
                                    .white.opacity(0.2),
                                    themeColor.opacity(0.2)
                                ],
                                startPoint: .topLeading,
                                endPoint: .bottomTrailing
                            ),
                            lineWidth: 1.5
                        )
                }
        }
        .shadow(color: themeColor.opacity(0.3), radius: 20, x: 0, y: 10)
        .padding(.horizontal, 24)
    }
    
    // MARK: - Header Section (With Custom Assets)
    
    private var headerSection: some View {
        HStack(alignment: .center, spacing: 14) {
            // Verdict Icon - Uses VerdictIcon component
            VerdictIcon(
                verdict: assessment.verdict,
                size: 40,
                useSFSymbols: true // Set to false to use custom assets
            )
            .frame(width: 64, height: 64)
            .background {
                Circle()
                    .fill(themeColor.opacity(0.15))
                Circle()
                    .stroke(themeColor.opacity(0.3), lineWidth: 1)
            }
            .shadow(color: themeColor.opacity(0.4), radius: 8)
            
            // Text Info
            VStack(alignment: .leading, spacing: 4) {
                Text(assessment.verdict.rawValue)
                    .font(.title2)
                    .fontWeight(.bold)
                    .foregroundColor(themeColor)
                    .contentTransition(.numericText())
                
                Text(String(format: "Risk Score: %d/100", assessment.score))
                    .font(.subheadline)
                    .foregroundColor(.textSecondary)
            }
            .accessibilityElement(children: .combine)
            .accessibilityLabel(Text(assessment.verdict.rawValue))
            .accessibilityValue(Text(String(format: "Risk Score: %d", assessment.score)))
            
            Spacer()
            
            // Confidence Badge with Liquid Glass
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
            .padding(.horizontal, 14)
            .padding(.vertical, 10)
            .background {
                RoundedRectangle(cornerRadius: 12)
                    .fill(.ultraThinMaterial)
                    .overlay {
                        RoundedRectangle(cornerRadius: 12)
                            .stroke(Color.white.opacity(0.15), lineWidth: 1)
                    }
            }
        }
    }
    
    // MARK: - Score Bar (iOS 17+ Animated)
    
    private var scoreBar: some View {
        GeometryReader { geometry in
            ZStack(alignment: .leading) {
                // Background
                RoundedRectangle(cornerRadius: 5)
                    .fill(Color.bgSurface)
                    .frame(height: 10)
                
                // Filled portion with gradient and animation
                RoundedRectangle(cornerRadius: 5)
                    .fill(
                        LinearGradient(
                            colors: [themeColor, themeColor.opacity(0.6)],
                            startPoint: .leading,
                            endPoint: .trailing
                        )
                    )
                    .frame(
                        width: isAppearing ? geometry.size.width * CGFloat(assessment.score) / 100 : 0,
                        height: 10
                    )
                    .shadow(color: themeColor.opacity(0.5), radius: 4, x: 0, y: 0)
                    .animation(.spring(response: 0.8, dampingFraction: 0.7).delay(0.2), value: isAppearing)
            }
        }
        .frame(height: 10)
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(Text("Risk Score"))
        .accessibilityValue(Text("\(assessment.score) / 100"))
    }
    
    // MARK: - Flags Section
    
    private var flagsSection: some View {
        VStack(alignment: .leading, spacing: 10) {
            ForEach(Array(assessment.flags.prefix(showingFullFlags ? 10 : 3).enumerated()), id: \.offset) { index, flag in
                HStack(spacing: 10) {
                    Image(systemName: "exclamationmark.triangle.fill")
                        .font(.caption)
                        .foregroundColor(.verdictWarning)
                        .symbolEffect(.pulse, isActive: assessment.verdict == .malicious)
                    
                    Text(flag)
                        .font(.subheadline)
                        .foregroundColor(.textSecondary)
                        .lineLimit(1)
                    
                    Spacer()
                }
                .padding(.horizontal, 12)
                .padding(.vertical, 8)
                .background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: 8))
                .transition(.asymmetric(
                    insertion: .move(edge: .top).combined(with: .opacity),
                    removal: .opacity
                ))
                .accessibilityElement(children: .combine)
                .accessibilityLabel(Text("Risk Flags"))
                .accessibilityValue(Text(flag))
            }
            
            if assessment.flags.count > 3 {
                Button {
                    withAnimation(.spring(response: 0.4)) {
                        showingFullFlags.toggle()
                    }
                } label: {
                    HStack {
                        Text(showingFullFlags
                             ? "Show less"
                             : String(format: "Show %d more...", assessment.flags.count - 3)
                        )
                            .font(.caption)
                            .foregroundColor(.brandPrimary)
                        
                        Image(systemName: showingFullFlags ? "chevron.up" : "chevron.down")
                            .font(.caption2)
                            .foregroundColor(.brandPrimary)
                    }
                }
                .sensoryFeedback(.selection, trigger: showingFullFlags)
            }
        }
    }
    
    // MARK: - URL Preview (Liquid Glass)
    
    private var urlPreview: some View {
        HStack {
            Image(systemName: "link")
                .font(.caption)
                .foregroundColor(.textMuted)
            
            Text(assessment.url)
                .font(.caption)
                .foregroundColor(.textMuted)
                .lineLimit(1)
                .truncationMode(.middle)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(12)
        .background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: 10))
        .textSelection(.enabled)
    }
    
    // MARK: - Action Button (Liquid Glass iOS 17+)
    
    private var actionButton: some View {
        Button(action: {
            onTap?()
            SettingsManager.shared.triggerHaptic(.light)
        }) {
            HStack(spacing: 8) {
                Image(systemName: "doc.text.magnifyingglass")
                    .symbolEffect(.bounce, value: isAppearing)
                Text("View Full Analysis")
            }
            .font(.subheadline.weight(.semibold))
            .foregroundColor(.white)
            .frame(maxWidth: .infinity)
            .padding(.vertical, 14)
            .background {
                RoundedRectangle(cornerRadius: 12)
                    .fill(themeColor)
                    .overlay {
                        // Liquid Glass highlight
                        RoundedRectangle(cornerRadius: 12)
                            .stroke(
                                LinearGradient(
                                    colors: [.white.opacity(0.4), .clear],
                                    startPoint: .topLeading,
                                    endPoint: .bottomTrailing
                                ),
                                lineWidth: 1
                            )
                    }
            }
            .shadow(color: themeColor.opacity(0.4), radius: 8, y: 4)
        }
        .sensoryFeedback(.impact(weight: .light), trigger: isAppearing)
        .accessibilityLabel(Text("View Full Analysis"))
    }
}

#endif
