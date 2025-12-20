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

// UI/Onboarding/OnboardingView.swift
// QR-SHIELD Onboarding - iOS 17+ Liquid Glass Edition
//
// UPDATED: December 2025 - iOS 17+ Compatible
// - Liquid Glass effects throughout
// - Enhanced page transitions
// - iOS 17+ symbol animations
// - Camera permission request integration

import SwiftUI
import AVFoundation
#if os(iOS)

struct OnboardingView: View {
    @Binding var isComplete: Bool
    @State private var currentPage = 0
    @State private var isAnimating = false
    @State private var showCameraPermission = false
    
    private let pages: [OnboardingPage] = [
        OnboardingPage(
            icon: "qrcode.viewfinder",
            title: "Scan Any QR Code",
            description: "Point your camera at any QR code to instantly analyze its contents for potential threats.",
            color: .brandPrimary,
            asset: "OnboardScan"
        ),
        OnboardingPage(
            icon: "shield.lefthalf.filled",
            title: "Real-Time Protection",
            description: "Our AI-powered engine analyzes URLs using 25+ security heuristics and machine learning algorithms.",
            color: .brandSecondary,
            asset: "OnboardProtect"
        ),
        OnboardingPage(
            icon: "lock.shield",
            title: "Privacy First",
            description: "All analysis happens on-device using Kotlin Multiplatform. Your data never leaves your phone.",
            color: .verdictSafe,
            asset: "OnboardPrivacy"
        ),
        OnboardingPage(
            icon: "sparkles",
            title: "Beautiful iOS 17+ Design",
            description: "Experience the stunning Liquid Glass interface with smooth animations and modern aesthetics.",
            color: .brandAccent,
            asset: nil
        )
    ]
    
    var body: some View {
        ZStack {
            // iOS 17+: Animated mesh gradient background
            LiquidGlassBackground()
                .ignoresSafeArea()
            
            VStack(spacing: 0) {
                // Skip button
                HStack {
                    // Page counter
                    Text(String(format: "%d/%d", currentPage + 1, pages.count))
                        .font(.caption)
                        .foregroundColor(.textMuted)
                        .padding()
                    
                    Spacer()
                    
                    Button("Skip") {
                        completeOnboarding()
                    }
                    .font(.subheadline.weight(.medium))
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
                .accessibilityElement(children: .ignore)
                .accessibilityLabel(Text("Page \(currentPage + 1) of \(pages.count)"))
                
                // Action Button
                InteractiveGlassButton(
                    currentPage == pages.count - 1 ? "Get Started" : "Continue",
                    icon: currentPage == pages.count - 1 ? "arrow.right.circle.fill" : "arrow.right",
                    color: pages[currentPage].color
                ) {
                    nextAction()
                }
                .padding(.horizontal, 24)
                .padding(.bottom, 40)
            }
        }
        .onAppear {
            isAnimating = true
        }
        .alert("Camera Permission", isPresented: $showCameraPermission) {
            Button("Allow Camera") {
                requestCameraPermission()
            }
            Button("Maybe Later", role: .cancel) {
                withAnimation {
                    isComplete = true
                }
            }
        } message: {
            Text("QR-SHIELD needs camera access to scan QR codes. You can change this later in Settings.")
        }
    }
    
    // MARK: - Page View (Liquid Glass iOS 17+)
    
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
            
            // Text content
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
            // Last page - check camera permission
            checkCameraAndComplete()
        }
    }
    
    private func checkCameraAndComplete() {
        let status = AVCaptureDevice.authorizationStatus(for: .video)
        
        switch status {
        case .notDetermined:
            showCameraPermission = true
        case .authorized:
            completeOnboarding()
        default:
            completeOnboarding()
        }
    }
    
    private func requestCameraPermission() {
        Task {
            _ = await AVCaptureDevice.requestAccess(for: .video)
            await MainActor.run {
                completeOnboarding()
            }
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
    var asset: String? = nil
}
#endif
