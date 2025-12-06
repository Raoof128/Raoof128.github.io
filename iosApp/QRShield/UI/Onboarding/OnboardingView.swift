// UI/Onboarding/OnboardingView.swift
// QR-SHIELD Onboarding Experience
//
// Beautiful first-run experience explaining the app's purpose.

import SwiftUI

struct OnboardingView: View {
    @Binding var isComplete: Bool
    @State private var currentPage = 0
    
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
        )
    ]
    
    var body: some View {
        VStack(spacing: 0) {
            // Page Content
            TabView(selection: $currentPage) {
                ForEach(Array(pages.enumerated()), id: \.offset) { index, page in
                    pageView(page)
                        .tag(index)
                }
            }
            .tabViewStyle(.page(indexDisplayMode: .never))
            .animation(.spring(), value: currentPage)
            
            // Page Indicators
            HStack(spacing: 8) {
                ForEach(0..<pages.count, id: \.self) { index in
                    Circle()
                        .fill(index == currentPage ? pages[currentPage].color : Color.textMuted)
                        .frame(width: index == currentPage ? 10 : 8, height: index == currentPage ? 10 : 8)
                        .animation(.spring(), value: currentPage)
                }
            }
            .padding(.bottom, 40)
            
            // Action Button
            Button(action: nextAction) {
                Text(currentPage == pages.count - 1 ? "Get Started" : "Continue")
                    .font(.headline)
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(LinearGradient.brandGradient)
                    .cornerRadius(16)
            }
            .padding(.horizontal, 24)
            .padding(.bottom, 40)
        }
        .background(Color.bgDark)
    }
    
    private func pageView(_ page: OnboardingPage) -> some View {
        VStack(spacing: 30) {
            Spacer()
            
            ZStack {
                Circle()
                    .fill(page.color.opacity(0.15))
                    .frame(width: 180, height: 180)
                
                Circle()
                    .fill(page.color.opacity(0.1))
                    .frame(width: 140, height: 140)
                
                Image(systemName: page.icon)
                    .font(.system(size: 60))
                    .foregroundColor(page.color)
            }
            
            VStack(spacing: 16) {
                Text(page.title)
                    .font(.title.weight(.bold))
                    .foregroundColor(.textPrimary)
                    .multilineTextAlignment(.center)
                
                Text(page.description)
                    .font(.body)
                    .foregroundColor(.textSecondary)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 32)
            }
            
            Spacer()
            Spacer()
        }
    }
    
    private func nextAction() {
        if currentPage < pages.count - 1 {
            withAnimation {
                currentPage += 1
            }
        } else {
            isComplete = true
        }
    }
}

struct OnboardingPage {
    let icon: String
    let title: String
    let description: String
    let color: Color
}

#Preview {
    OnboardingView(isComplete: .constant(false))
}
