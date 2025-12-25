//
// Copyright 2025-2026 QR-SHIELD Contributors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//

// UI/Components/BrainVisualizer.swift
// QR-SHIELD Brain Visualizer - iOS 17+ SwiftUI Edition
//
// Matches: CommonBrainVisualizer.kt from Android/Desktop
// Features:
// - 80 node neural network grid in circular distribution
// - Signal-to-cluster mapping (deterministic hashing)
// - Pulsing animation on active nodes (red)
// - Ripple effect on active clusters
// - Explanation badges below the visual
// - VoiceOver accessible description

import SwiftUI

// MARK: - Brain Node

struct BrainNode: Identifiable {
    let id = UUID()
    let x: CGFloat  // Normalized -1 to 1
    let y: CGFloat  // Normalized -1 to 1
    let phaseOffset: CGFloat
}

// MARK: - Brain Visualizer View

@available(iOS 17, *)
struct BrainVisualizer: View {
    let detectedSignals: [String]
    
    @State private var animationPhase: CGFloat = 0
    
    private let timer = Timer.publish(every: 1/60, on: .main, in: .common).autoconnect()
    
    // Memoized nodes (stable across renders)
    private let nodes: [BrainNode] = BrainVisualizer.generateNodes(count: 80, seed: 12345)
    
    // Computed accessibility description
    private var accessibilityDescription: String {
        if detectedSignals.isEmpty {
            return "AI Neural Net: No threats detected. Brain pattern is calm and blue."
        } else {
            return "AI Neural Net: Active alert. Detected signals: \(detectedSignals.joined(separator: ", ")). Brain pattern is pulsing red."
        }
    }
    
    var body: some View {
        VStack(spacing: 16) {
            // Brain Canvas
            Canvas { context, size in
                let centerX = size.width / 2
                let centerY = size.height / 2
                let activeIndices = mapSignalsToNodes(signals: detectedSignals, nodeCount: nodes.count)
                let hasSignals = !detectedSignals.isEmpty
                
                // Draw connections first (background)
                for (index, node) in nodes.enumerated() where index % 3 == 0 {
                    let neighborIndex = (index + 3) % nodes.count
                    let neighbor = nodes[neighborIndex]
                    
                    let isActive = activeIndices.contains(index) || activeIndices.contains(neighborIndex)
                    let lineColor: Color = isActive && hasSignals
                        ? .red.opacity(0.3)
                        : .gray.opacity(0.1)
                    
                    let x1 = centerX + node.x * size.width * 0.4
                    let y1 = centerY + node.y * size.height * 0.4
                    let x2 = centerX + neighbor.x * size.width * 0.4
                    let y2 = centerY + neighbor.y * size.height * 0.4
                    
                    var path = Path()
                    path.move(to: CGPoint(x: x1, y: y1))
                    path.addLine(to: CGPoint(x: x2, y: y2))
                    context.stroke(path, with: .color(lineColor), lineWidth: 1)
                }
                
                // Draw nodes
                for (index, node) in nodes.enumerated() {
                    let isActive = activeIndices.contains(index)
                    
                    // Subtle floating animation
                    let floatOffset = sin(animationPhase + node.phaseOffset) * 5
                    
                    let x = centerX + node.x * size.width * 0.4
                    let y = centerY + node.y * size.height * 0.4 + floatOffset
                    
                    // Determine color
                    let color: Color
                    let opacity: Double
                    if !hasSignals {
                        color = .blue
                        opacity = 0.6
                    } else if isActive {
                        color = .red
                        opacity = 1.0
                    } else {
                        color = .gray
                        opacity = 0.3
                    }
                    
                    // Calculate radius with pulse
                    var radius: CGFloat = isActive ? 6 : 3
                    if isActive {
                        let pulseScale = 1 + 0.3 * sin(animationPhase * 2 + node.phaseOffset)
                        radius *= pulseScale
                    }
                    
                    // Draw ripple for active nodes
                    if isActive {
                        let rippleRadius = radius * 2.5
                        let rippleRect = CGRect(
                            x: x - rippleRadius,
                            y: y - rippleRadius,
                            width: rippleRadius * 2,
                            height: rippleRadius * 2
                        )
                        context.fill(
                            Path(ellipseIn: rippleRect),
                            with: .color(color.opacity(0.2))
                        )
                    }
                    
                    // Draw node
                    let nodeRect = CGRect(x: x - radius, y: y - radius, width: radius * 2, height: radius * 2)
                    context.fill(
                        Path(ellipseIn: nodeRect),
                        with: .color(color.opacity(opacity))
                    )
                }
            }
            .frame(height: 200)
            .background(Color.bgSurface.opacity(0.3))
            .cornerRadius(12)
            .accessibilityLabel(accessibilityDescription)
            
            // Signal Badges
            if !detectedSignals.isEmpty {
                SignalBadgesView(signals: detectedSignals)
            }
        }
        .onReceive(timer) { _ in
            // Animate at 2 seconds per full cycle (matching KMP)
            animationPhase += CGFloat.pi * 2 / 120 // 60fps, 2 second cycle
            if animationPhase > CGFloat.pi * 2 {
                animationPhase -= CGFloat.pi * 2
            }
        }
    }
    
    // MARK: - Node Generation
    
    private static func generateNodes(count: Int, seed: Int) -> [BrainNode] {
        var rng = seed
        var nodes: [BrainNode] = []
        
        // Simple seeded random for reproducibility
        func random() -> CGFloat {
            rng = (rng &* 1103515245 &+ 12345) & 0x7fffffff
            return CGFloat(rng) / CGFloat(0x7fffffff)
        }
        
        for _ in 0..<count {
            var x: CGFloat
            var y: CGFloat
            
            // Rejection sampling for circular distribution
            repeat {
                x = random() * 2 - 1
                y = random() * 2 - 1
            } while x * x + y * y > 1
            
            nodes.append(BrainNode(
                x: x,
                y: y,
                phaseOffset: random() * CGFloat.pi * 2
            ))
        }
        
        return nodes
    }
    
    // MARK: - Signal Mapping
    
    private func mapSignalsToNodes(signals: [String], nodeCount: Int) -> Set<Int> {
        guard !signals.isEmpty else { return [] }
        
        var activeNodes = Set<Int>()
        let seed = signals.joined().reduce(0) { $0 + Int($1.asciiValue ?? 0) }
        var rng = seed
        
        func random(min: Int, max: Int) -> Int {
            rng = (rng &* 1103515245 &+ 12345) & 0x7fffffff
            return min + (rng % (max - min))
        }
        
        for signal in signals {
            // Hash signal to center node
            let signalHash = signal.reduce(0) { $0 + Int($1.asciiValue ?? 0) }
            let centerIndex = abs(signalHash) % nodeCount
            activeNodes.insert(centerIndex)
            
            // Add 8 nearby nodes
            for _ in 0..<8 {
                let neighbor = max(0, min(nodeCount - 1, centerIndex + random(min: -10, max: 10)))
                activeNodes.insert(neighbor)
            }
        }
        
        return activeNodes
    }
}

// MARK: - Signal Badges View

@available(iOS 17, *)
struct SignalBadgesView: View {
    let signals: [String]
    
    var body: some View {
        LazyVGrid(columns: [GridItem(.adaptive(minimum: 100))], spacing: 8) {
            ForEach(signals, id: \.self) { signal in
                Text(signal.replacingOccurrences(of: "_", with: " "))
                    .font(.caption2.weight(.semibold))
                    .foregroundColor(.verdictDanger)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 4)
                    .background(
                        RoundedRectangle(cornerRadius: 4)
                            .fill(Color.verdictDanger.opacity(0.1))
                            .overlay(
                                RoundedRectangle(cornerRadius: 4)
                                    .stroke(Color.verdictDanger.opacity(0.3), lineWidth: 1)
                            )
                    )
            }
        }
        .padding(.horizontal, 16)
    }
}

// MARK: - Preview

#if DEBUG
@available(iOS 17, *)
struct BrainVisualizer_Previews: PreviewProvider {
    static var previews: some View {
        VStack(spacing: 32) {
            // Idle state
            VStack {
                Text("Idle State").font(.caption).foregroundColor(.secondary)
                BrainVisualizer(detectedSignals: [])
            }
            
            // Active state
            VStack {
                Text("Active State").font(.caption).foregroundColor(.secondary)
                BrainVisualizer(detectedSignals: [
                    "SUSPICIOUS_DOMAIN",
                    "TYPOSQUATTING",
                    "RISKY_TLD"
                ])
            }
        }
        .padding()
        .background(Color.bgMain)
    }
}
#endif
