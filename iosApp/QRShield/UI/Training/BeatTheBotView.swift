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

// UI/Training/BeatTheBotView.swift
// QR-SHIELD Training & Games - iOS 17+ Liquid Glass Edition
//
// Matches: Training & Games HTML design
// Features:
// - Timer with level indicator
// - Browser preview mockup
// - Phishing/Legitimate decision buttons
// - Live session stats (Points, Streak, Accuracy)
// - Live hints with explanations
// - Bot confidence indicator

import SwiftUI
#if os(iOS)

// MARK: - Game Difficulty

enum GameDifficulty: String, CaseIterable {
    case beginner = "BEGINNER"
    case intermediate = "INTERMEDIATE"
    case advanced = "ADVANCED"
    case nightmare = "NIGHTMARE"
    
    var color: Color {
        switch self {
        case .beginner: return .verdictSafe
        case .intermediate: return .brandPrimary
        case .advanced: return .verdictWarning
        case .nightmare: return .verdictDanger
        }
    }
    
    var levelNumber: Int {
        switch self {
        case .beginner: return 1
        case .intermediate: return 2
        case .advanced: return 3
        case .nightmare: return 4
        }
    }
}

// MARK: - Challenge Data

struct PhishingChallenge: Identifiable {
    let id = UUID()
    let url: String
    let isPhishing: Bool
    let hint: String
    let explanation: String
}

// MARK: - Beat The Bot View

@available(iOS 17, *)
struct BeatTheBotView: View {
    @Environment(\.dismiss) private var dismiss
    
    // Game State
    @State private var timeRemaining = 12
    @State private var currentLevel: GameDifficulty = .intermediate
    @State private var points = 1250
    @State private var streak = 5
    @State private var accuracy: Double = 0.92
    @State private var botConfidence: Double = 0.88
    @State private var latency = 12
    
    @State private var isPlaying = false
    @State private var showResult = false
    @State private var lastAnswer: Bool?
    @State private var isCorrect: Bool?
    
    @State private var currentChallenge: PhishingChallenge = sampleChallenges[0]
    @State private var timer: Timer?
    
    // Progress animation
    @State private var ringProgress: CGFloat = 0.76
    
    private static let sampleChallenges: [PhishingChallenge] = [
        PhishingChallenge(
            url: "https://account-verify-appleid.support.co.uk",
            isPhishing: true,
            hint: "Check the domain extension carefully.",
            explanation: "Legitimate Apple support sites typically end in apple.com, not support.co.uk."
        ),
        PhishingChallenge(
            url: "https://secure-paypa1.com/login",
            isPhishing: true,
            hint: "Look closely at the spelling of the domain.",
            explanation: "The letter 'l' is replaced with '1' (typosquatting attack)."
        ),
        PhishingChallenge(
            url: "https://github.com/login",
            isPhishing: false,
            hint: "Verify the domain matches the official site.",
            explanation: "This is the legitimate GitHub login page."
        ),
        PhishingChallenge(
            url: "https://amazon.customer-service.net",
            isPhishing: true,
            hint: "Check the main domain, not the subdomain.",
            explanation: "The real domain is customer-service.net, not amazon.com."
        ),
        PhishingChallenge(
            url: "https://www.google.com",
            isPhishing: false,
            hint: "Standard domain structure with valid TLD.",
            explanation: "This is the legitimate Google homepage."
        )
    ]
    
    var body: some View {
        NavigationStack {
            VStack(spacing: 24) {
                // Header with Timer
                headerSection
                
                // Browser Preview
                browserPreview
                
                // Decision Buttons
                decisionButtons
                
                // Live Session Stats
                liveSessionCard
                
                Spacer()
                
                // Hint Card
                hintCard
            }
            .padding(.horizontal, 20)
            .padding(.top, 12)
            .padding(.bottom, 20)
            .background {
                LiquidGlassBackground()
                    .ignoresSafeArea()
            }
            .navigationTitle("")
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
                
                ToolbarItem(placement: .principal) {
                    VStack(spacing: 2) {
                        Text("TRAINING MODE")
                            .font(.caption2.weight(.semibold))
                            .foregroundColor(.textMuted)
                            .tracking(1)
                        
                        Text("Beat the Bot")
                            .font(.headline)
                            .foregroundColor(.textPrimary)
                    }
                }
                
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button {
                        togglePause()
                    } label: {
                        Image(systemName: isPlaying ? "pause.fill" : "play.fill")
                            .foregroundColor(.textSecondary)
                    }
                }
            }
        }
        .onAppear {
            startGame()
        }
        .onDisappear {
            stopTimer()
        }
    }
    
    // MARK: - Header Section
    
    private var headerSection: some View {
        VStack(spacing: 16) {
            // Timer Ring
            ZStack {
                // Background Ring
                Circle()
                    .stroke(Color.bgSurface, lineWidth: 8)
                    .frame(width: 100, height: 100)
                
                // Progress Ring
                Circle()
                    .trim(from: 0, to: ringProgress)
                    .stroke(
                        currentLevel.color,
                        style: StrokeStyle(lineWidth: 8, lineCap: .round)
                    )
                    .frame(width: 100, height: 100)
                    .rotationEffect(.degrees(-90))
                    .animation(.spring(response: 0.3), value: ringProgress)
                
                // Timer Text
                VStack(spacing: 2) {
                    Text("\(timeRemaining)")
                        .font(.largeTitle.weight(.bold).monospacedDigit())
                        .foregroundColor(.textPrimary)
                        .contentTransition(.numericText())
                    
                    Text("Seconds")
                        .font(.caption2)
                        .foregroundColor(.textMuted)
                        .textCase(.uppercase)
                }
                
                // Level Badge
                Text("LEVEL \(currentLevel.levelNumber)")
                    .font(.caption2.weight(.bold))
                    .foregroundColor(.white)
                    .padding(.horizontal, 12)
                    .padding(.vertical, 6)
                    .background(
                        LinearGradient(
                            colors: [.brandPrimary, .brandAccent],
                            startPoint: .leading,
                            endPoint: .trailing
                        ),
                        in: Capsule()
                    )
                    .shadow(color: .brandPrimary.opacity(0.4), radius: 8)
                    .offset(y: 58)
            }
        }
    }
    
    // MARK: - Browser Preview
    
    private var browserPreview: some View {
        VStack(spacing: 0) {
            // Browser Chrome
            HStack(spacing: 8) {
                // Window Controls
                HStack(spacing: 6) {
                    Circle().fill(Color.verdictDanger).frame(width: 10, height: 10)
                    Circle().fill(Color.verdictWarning).frame(width: 10, height: 10)
                    Circle().fill(Color.verdictSafe).frame(width: 10, height: 10)
                }
                
                // URL Bar
                HStack(spacing: 6) {
                    Image(systemName: "lock.fill")
                        .font(.caption2)
                        .foregroundColor(.textMuted)
                    
                    Text(currentChallenge.url)
                        .font(.caption.monospaced())
                        .foregroundColor(.textSecondary)
                        .lineLimit(1)
                        .truncationMode(.middle)
                }
                .padding(.horizontal, 12)
                .padding(.vertical, 6)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(Color.bgDark, in: RoundedRectangle(cornerRadius: 8))
            }
            .padding(12)
            .background(Color.bgSurface)
            
            // Page Content
            VStack(spacing: 16) {
                ZStack {
                    Circle()
                        .fill(Color.bgSurface)
                        .frame(width: 60, height: 60)
                    
                    Image(systemName: "exclamationmark.shield")
                        .font(.title)
                        .foregroundColor(.textMuted)
                }
                
                VStack(spacing: 8) {
                    Text("Unusual Sign-in Attempt")
                        .font(.headline)
                        .foregroundColor(.textPrimary)
                    
                    Text("We detected a login from a new device. Please verify your identity immediately to prevent account suspension.")
                        .font(.caption)
                        .foregroundColor(.textSecondary)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 20)
                }
                
                Button {} label: {
                    Text("Verify Identity")
                        .font(.subheadline.weight(.medium))
                        .foregroundColor(.white)
                        .padding(.horizontal, 24)
                        .padding(.vertical, 10)
                        .background(Color.brandPrimary.opacity(0.9), in: Capsule())
                }
                .disabled(true)
            }
            .padding(24)
            .frame(maxWidth: .infinity)
            .background(Color.bgDark)
        }
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(Color.glassBorder, lineWidth: 1)
        )
        .shadow(color: .black.opacity(0.2), radius: 10, y: 5)
    }
    
    // MARK: - Decision Buttons
    
    private var decisionButtons: some View {
        HStack(spacing: 16) {
            // Phishing Button
            Button {
                makeDecision(isPhishing: true)
            } label: {
                VStack(spacing: 8) {
                    Image(systemName: "xmark.shield.fill")
                        .font(.largeTitle)
                        .foregroundColor(.verdictDanger)
                        .symbolEffect(.bounce, value: lastAnswer == true)
                    
                    Text("Phishing")
                        .font(.headline)
                        .foregroundColor(.verdictDanger)
                }
                .frame(maxWidth: .infinity)
                .padding(.vertical, 24)
                .background(Color.verdictDanger.opacity(0.1), in: RoundedRectangle(cornerRadius: 20))
                .overlay(
                    RoundedRectangle(cornerRadius: 20)
                        .stroke(Color.verdictDanger.opacity(0.3), lineWidth: 2)
                )
            }
            .sensoryFeedback(.impact(weight: .medium), trigger: lastAnswer == true)
            
            // Legitimate Button
            Button {
                makeDecision(isPhishing: false)
            } label: {
                VStack(spacing: 8) {
                    Image(systemName: "checkmark.shield.fill")
                        .font(.largeTitle)
                        .foregroundColor(.verdictSafe)
                        .symbolEffect(.bounce, value: lastAnswer == false)
                    
                    Text("Legitimate")
                        .font(.headline)
                        .foregroundColor(.verdictSafe)
                }
                .frame(maxWidth: .infinity)
                .padding(.vertical, 24)
                .background(Color.verdictSafe.opacity(0.1), in: RoundedRectangle(cornerRadius: 20))
                .overlay(
                    RoundedRectangle(cornerRadius: 20)
                        .stroke(Color.verdictSafe.opacity(0.3), lineWidth: 2)
                )
            }
            .sensoryFeedback(.impact(weight: .medium), trigger: lastAnswer == false)
        }
    }
    
    // MARK: - Live Session Card
    
    private var liveSessionCard: some View {
        VStack(spacing: 16) {
            // Header
            HStack {
                Text("LIVE SESSION")
                    .font(.caption.weight(.bold))
                    .foregroundColor(.textMuted)
                    .tracking(1)
                
                Spacer()
                
                HStack(spacing: 6) {
                    Circle()
                        .fill(.green)
                        .frame(width: 6, height: 6)
                        .shadow(color: .green, radius: 3)
                    
                    Text("ONLINE")
                        .font(.caption2.weight(.bold))
                        .foregroundColor(.verdictSafe)
                }
                .padding(.horizontal, 8)
                .padding(.vertical, 4)
                .background(Color.verdictSafe.opacity(0.1), in: Capsule())
            }
            
            // Stats Grid
            HStack(spacing: 8) {
                statBox(title: "Points", value: "\(points)", color: .brandPrimary)
                
                statBox(title: "Streak", value: "\(streak)", icon: "flame.fill", color: .verdictWarning)
                
                statBox(title: "Accuracy", value: "\(Int(accuracy * 100))%", color: .textPrimary)
            }
            
            // Bot Confidence
            HStack(spacing: 12) {
                Text("Bot Confidence:")
                    .font(.caption)
                    .foregroundColor(.textMuted)
                
                GeometryReader { geometry in
                    ZStack(alignment: .leading) {
                        RoundedRectangle(cornerRadius: 3)
                            .fill(Color.bgSurface)
                        
                        RoundedRectangle(cornerRadius: 3)
                            .fill(Color.brandPrimary)
                            .frame(width: geometry.size.width * botConfidence)
                    }
                }
                .frame(width: 60, height: 6)
                
                Text("\(Int(botConfidence * 100))%")
                    .font(.caption.weight(.semibold))
                    .foregroundColor(.brandPrimary)
                
                Spacer()
                
                HStack(spacing: 4) {
                    Image(systemName: "bolt.fill")
                        .font(.caption2)
                    Text("\(latency)ms")
                        .font(.caption)
                }
                .foregroundColor(.textMuted)
            }
        }
        .padding(16)
        .liquidGlass(cornerRadius: 16)
    }
    
    private func statBox(title: String, value: String, icon: String? = nil, color: Color) -> some View {
        VStack(spacing: 6) {
            Text(title)
                .font(.caption2)
                .foregroundColor(.textMuted)
            
            HStack(spacing: 4) {
                Text(value)
                    .font(.title3.weight(.bold))
                    .foregroundColor(color)
                
                if let icon {
                    Image(systemName: icon)
                        .font(.caption)
                        .foregroundColor(color)
                }
            }
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 12)
        .background(Color.bgSurface.opacity(0.5), in: RoundedRectangle(cornerRadius: 12))
    }
    
    // MARK: - Hint Card
    
    private var hintCard: some View {
        HStack(alignment: .top, spacing: 12) {
            ZStack {
                RoundedRectangle(cornerRadius: 12)
                    .fill(Color.brandPrimary.opacity(0.15))
                    .frame(width: 40, height: 40)
                
                Image(systemName: "lightbulb.fill")
                    .font(.title3)
                    .foregroundColor(.brandPrimary)
            }
            
            VStack(alignment: .leading, spacing: 4) {
                Text("Live Hint")
                    .font(.subheadline.weight(.semibold))
                    .foregroundColor(.textPrimary)
                
                Text(currentChallenge.hint)
                    .font(.caption)
                    .foregroundColor(.textSecondary)
                
                HStack(spacing: 4) {
                    Text("Tip:")
                        .font(.caption)
                        .foregroundColor(.textMuted)
                    
                    Text("apple.com")
                        .font(.caption.monospaced())
                        .foregroundColor(.textMuted)
                        .padding(.horizontal, 6)
                        .padding(.vertical, 2)
                        .background(Color.bgSurface, in: RoundedRectangle(cornerRadius: 4))
                    
                    Text("≠")
                        .foregroundColor(.textMuted)
                    
                    Text("support.co.uk")
                        .font(.caption.monospaced())
                        .foregroundColor(.verdictDanger)
                        .padding(.horizontal, 6)
                        .padding(.vertical, 2)
                        .background(Color.verdictDanger.opacity(0.1), in: RoundedRectangle(cornerRadius: 4))
                }
            }
        }
        .padding(16)
        .background {
            LinearGradient(
                colors: [
                    Color.brandPrimary.opacity(0.05),
                    Color.brandAccent.opacity(0.02)
                ],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
        }
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(Color.brandPrimary.opacity(0.2), lineWidth: 1)
        )
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .overlay(alignment: .topTrailing) {
            Image(systemName: "lightbulb")
                .font(.system(size: 60))
                .foregroundColor(.brandPrimary.opacity(0.05))
                .offset(x: 10, y: -10)
        }
    }
    
    // MARK: - Actions
    
    private func startGame() {
        isPlaying = true
        timeRemaining = 12
        ringProgress = 1.0
        startTimer()
    }
    
    private func togglePause() {
        isPlaying.toggle()
        if isPlaying {
            startTimer()
        } else {
            stopTimer()
        }
    }
    
    private func startTimer() {
        timer = Timer.scheduledTimer(withTimeInterval: 1.0, repeats: true) { _ in
            if timeRemaining > 0 {
                withAnimation {
                    timeRemaining -= 1
                    ringProgress = CGFloat(timeRemaining) / 12.0
                }
            } else {
                // Time's up - count as wrong
                handleTimeout()
            }
        }
    }
    
    private func stopTimer() {
        timer?.invalidate()
        timer = nil
    }
    
    private func makeDecision(isPhishing: Bool) {
        stopTimer()
        lastAnswer = isPhishing
        
        let correct = currentChallenge.isPhishing == isPhishing
        isCorrect = correct
        
        if correct {
            points += 100 + (streak * 10)
            streak += 1
            SettingsManager.shared.triggerHaptic(.success)
            SettingsManager.shared.playSound(.success)
        } else {
            streak = 0
            SettingsManager.shared.triggerHaptic(.error)
            SettingsManager.shared.playSound(.warning)
        }
        
        // Update accuracy
        // In real implementation, track total correct/total attempts
        
        // Load next challenge after delay
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) {
            loadNextChallenge()
        }
    }
    
    private func handleTimeout() {
        stopTimer()
        streak = 0
        SettingsManager.shared.triggerHaptic(.warning)
        
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) {
            loadNextChallenge()
        }
    }
    
    private func loadNextChallenge() {
        // Get random challenge
        let challenges = Self.sampleChallenges.filter { $0.id != currentChallenge.id }
        currentChallenge = challenges.randomElement() ?? Self.sampleChallenges[0]
        
        // Reset timer
        lastAnswer = nil
        isCorrect = nil
        timeRemaining = 12
        ringProgress = 1.0
        
        startTimer()
    }
}

#endif
