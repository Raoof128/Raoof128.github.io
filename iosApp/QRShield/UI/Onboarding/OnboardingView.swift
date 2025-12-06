// UI/Onboarding/OnboardingView.swift
// QR-SHIELD Onboarding - iOS 26 Liquid Glass Edition
//
// UPDATED: December 2025
// - Liquid Glass effects throughout
// - Enhanced page transitions
// - iOS 26 symbol animations

import SwiftUI

struct OnboardingView: View {
    @Binding var isComplete: Bool
    @State private var currentPage = 0
    @State private var isAnimating = false
    
    private let pages: [OnboardingPage] = [
        OnboardingPage(
            icon: "qrcode.viewfinder",
            title: "Scan Any QR Code",
            description: "Point your camera at any QR code to instantly analyze its contents for potential threats.",
            color: .brandPrimary
        ),
        OnboardingPage(
            icon: "shield.lefthalf.filled",
            title: "Real-Time Protection",
            description: "Our AI-powered engine analyzes URLs using 25+ security heuristics and machine learning.",
            color: .brandSecondary
        ),
        OnboardingPage(
            icon: "lock.shield",
            title: "Privacy First",
            description: "All analysis happens on-device. Your data never leaves your phone.",
            color: .verdictSafe
        ),
        OnboardingPage(
            icon: "sparkles",
            title: "Liquid Glass Design",
            description: "Experience the beautiful new iOS 26 Liquid Glass interface with smooth animations.",
            color: .brandAccent
        )
    ]
    
    var body: some View {
        ZStack {
            // iOS 26: Animated mesh gradient background
            MeshGradient.liquidGlassBackground
                .ignoresSafeArea()
            
            VStack(spacing: 0) {
                // Skip button
                HStack {
                    Spacer()
                    
                    Button("Skip") {
                        completeOnboarding()
                    }
                    .font(.subheadline)
                    .foregroundColor(.textSecondary)
                    .padding()
                }
                
                // Page Content
                TabView(selection: $currentPage) {
                    ForEach(Array(pages.enumerated()), id: \.offset) { index, page in
                        pageView(page)
                            .tag(index)
                    }
                }
                .tabViewStyle(.page(indexDisplayMode: .never))
                .animation(.spring(response: 0.5), value: currentPage)
                
                // Page Indicators
                HStack(spacing: 10) {
                    ForEach(0..<pages.count, id: \.self) { index in
                        Capsule()
                            .fill(index == currentPage ? pages[currentPage].color : Color.textMuted.opacity(0.5))
                            .frame(width: index == currentPage ? 24 : 8, height: 8)
                            .animation(.spring(response: 0.3), value: currentPage)
                    }
                }
                .padding(.bottom, 40)
                
                // Action Button
                Button(action: nextAction) {
                    HStack(spacing: 8) {
                        Text(currentPage == pages.count - 1 ? "Get Started" : "Continue")
                        
                        Image(systemName: currentPage == pages.count - 1 ? "arrow.right.circle.fill" : "arrow.right")
                            .symbolEffect(.bounce, value: currentPage)
                    }
                    .font(.headline)
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 18)
                    .background {
                        RoundedRectangle(cornerRadius: 16)
                            .fill(pages[currentPage].color)
                            .overlay {
                                // Liquid Glass highlight
                                RoundedRectangle(cornerRadius: 16)
                                    .stroke(
                                        LinearGradient(
                                            colors: [.white.opacity(0.3), .clear],
                                            startPoint: .topLeading,
                                            endPoint: .bottomTrailing
                                        ),
                                        lineWidth: 1
                                    )
                            }
                    }
                    .shadow(color: pages[currentPage].color.opacity(0.4), radius: 10, y: 5)
                }
                .padding(.horizontal, 24)
                .padding(.bottom, 40)
                .sensoryFeedback(.impact(weight: .medium), trigger: currentPage)
            }
        }
        .onAppear {
            isAnimating = true
        }
    }
    
    // MARK: - Page View (Liquid Glass iOS 26)
    
    private func pageView(_ page: OnboardingPage) -> some View {
        VStack(spacing: 40) {
            Spacer()
            
            // Icon with Liquid Glass container
            ZStack {
                // Outer glow
                Circle()
                    .fill(page.color.opacity(0.1))
                    .frame(width: 220, height: 220)
                    .blur(radius: 20)
                
                // Glass container
                Circle()
                    .fill(.ultraThinMaterial)
                    .frame(width: 180, height: 180)
                    .overlay {
                        Circle()
                            .stroke(
                                LinearGradient(
                                    colors: [.white.opacity(0.3), page.color.opacity(0.3), .clear],
                                    startPoint: .topLeading,
                                    endPoint: .bottomTrailing
                                ),
                                lineWidth: 2
                            )
                    }
                    .shadow(color: page.color.opacity(0.3), radius: 20)
                
                // Icon with animation
                Image(systemName: page.icon)
                    .font(.system(size: 70))
                    .foregroundStyle(
                        LinearGradient(
                            colors: [page.color, page.color.opacity(0.7)],
                            startPoint: .top,
                            endPoint: .bottom
                        )
                    )
                    .symbolEffect(.variableColor.iterative, isActive: isAnimating)
            }
            
            // Text content with glass background
            VStack(spacing: 16) {
                Text(page.title)
                    .font(.title.weight(.bold))
                    .foregroundColor(.textPrimary)
                    .multilineTextAlignment(.center)
                
                Text(page.description)
                    .font(.body)
                    .foregroundColor(.textSecondary)
                    .multilineTextAlignment(.center)
                    .lineSpacing(4)
            }
            .padding(.horizontal, 40)
            
            Spacer()
            Spacer()
        }
    }
    
    // MARK: - Actions
    
    private func nextAction() {
        if currentPage < pages.count - 1 {
            withAnimation(.spring(response: 0.4)) {
                currentPage += 1
            }
        } else {
            completeOnboarding()
        }
    }
    
    private func completeOnboarding() {
        let generator = UINotificationFeedbackGenerator()
        generator.notificationOccurred(.success)
        
        withAnimation(.spring(response: 0.4)) {
            isComplete = true
        }
    }
}

// MARK: - Onboarding Page Model

struct OnboardingPage {
    let icon: String
    let title: String
    let description: String
    let color: Color
}

#Preview {
    OnboardingView(isComplete: .constant(false))
}
