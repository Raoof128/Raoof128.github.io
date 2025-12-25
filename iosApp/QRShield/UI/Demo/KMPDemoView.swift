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

// KMPDemoView.swift
// Minimal SwiftUI view demonstrating Kotlin Multiplatform integration
//
// This view calls the KMP HeuristicsEngine to analyze URLs,
// proving that shared Kotlin code is reused on iOS.

#if os(iOS)
import SwiftUI

struct KMPDemoView: View {
    @StateObject private var analyzer = KMPAnalyzer()
    @State private var urlInput = ""
    
    // Sample URLs for testing
    private let sampleURLs = [
        ("Safe URL", "https://www.google.com"),
        ("Suspicious", "http://bit.ly/abc123"),
        ("Malicious", "https://paypa1-secure.tk/login")
    ]
    
    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 24) {
                    // Header
                    headerSection
                    
                    // Input section
                    inputSection
                    
                    // Sample URLs
                    sampleURLsSection
                    
                    // Result display
                    if let result = analyzer.lastResult {
                        resultSection(result)
                    }
                    
                    // KMP Info
                    kmpInfoSection
                }
                .padding()
            }
            .navigationTitle("KMP Demo")
            .background(Color.black.ignoresSafeArea())
        }
        .preferredColorScheme(.dark)
    }
    
    // MARK: - Sections
    
    private var headerSection: some View {
        VStack(spacing: 8) {
            Text("🛡️ QR-SHIELD")
                .font(.largeTitle.bold())
                .foregroundColor(.white)
            
            Text(NSLocalizedString("kmp.integration", comment: ""))
                .font(.subheadline)
                .foregroundColor(.gray)
        }
    }
    
    private var inputSection: some View {
        VStack(spacing: 12) {
            TextField("Enter URL to analyze", text: $urlInput)
                .textFieldStyle(.roundedBorder)
                .autocapitalization(.none)
                .keyboardType(.URL)
            
            Button(action: analyzeURL) {
                HStack {
                    if analyzer.isAnalyzing {
                        ProgressView()
                            .progressViewStyle(CircularProgressViewStyle(tint: .white))
                    } else {
                        Image(systemName: "magnifyingglass")
                    }
                    Text(NSLocalizedString("kmp.analyze_button", comment: ""))
                }
                .frame(maxWidth: .infinity)
                .padding()
                .background(Color.purple)
                .foregroundColor(.white)
                .cornerRadius(12)
            }
            .disabled(urlInput.isEmpty || analyzer.isAnalyzing)
        }
    }
    
    private var sampleURLsSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(NSLocalizedString("kmp.quick_test", comment: ""))
                .font(.caption)
                .foregroundColor(.gray)
            
            HStack(spacing: 8) {
                ForEach(sampleURLs, id: \.0) { name, url in
                    Button(name) {
                        urlInput = url
                        analyzeURL()
                    }
                    .font(.caption)
                    .padding(.horizontal, 12)
                    .padding(.vertical, 6)
                    .background(Color.gray.opacity(0.3))
                    .foregroundColor(.white)
                    .cornerRadius(8)
                }
            }
        }
    }
    
    private func resultSection(_ result: KMPAnalyzer.AnalysisResult) -> some View {
        VStack(alignment: .leading, spacing: 16) {
            Text(NSLocalizedString("kmp.analysis_result", comment: ""))
                .font(.headline)
                .foregroundColor(.white)
            
            VStack(alignment: .leading, spacing: 12) {
                // Verdict badge
                HStack {
                    Text(result.verdict)
                        .font(.title2.bold())
                        .foregroundColor(verdictColor(result.verdict))
                    
                    Spacer()
                    
                    Text("Score: \(result.score)/100")
                        .font(.headline)
                        .foregroundColor(.white)
                }
                
                // URL
                Text(result.url)
                    .font(.caption)
                    .foregroundColor(.gray)
                    .lineLimit(2)
                
                // Flags
                if !result.flags.isEmpty {
                    VStack(alignment: .leading, spacing: 4) {
                        Text(NSLocalizedString("kmp.risk_flags", comment: ""))
                            .font(.caption.bold())
                            .foregroundColor(.orange)
                        
                        ForEach(result.flags, id: \.self) { flag in
                            HStack {
                                Image(systemName: "exclamationmark.triangle.fill")
                                    .foregroundColor(.orange)
                                    .font(.caption)
                                Text(flag)
                                    .font(.caption)
                                    .foregroundColor(.white)
                            }
                        }
                    }
                }
            }
            .padding()
            .background(Color.gray.opacity(0.2))
            .cornerRadius(12)
        }
    }
    
    private var kmpInfoSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            Divider()
                .background(Color.gray)
            
            HStack {
                Image(systemName: "cpu")
                    .foregroundColor(.purple)
                Text(NSLocalizedString("kmp.powered_by", comment: ""))
                    .font(.caption)
                    .foregroundColor(.gray)
            }
            
            #if canImport(common)
            Text("✅ KMP Framework Linked")
                .font(.caption)
                .foregroundColor(.green)
            #else
            Text("⚠️ Using Mock (link common.framework)")
                .font(.caption)
                .foregroundColor(.orange)
            #endif
        }
        .padding(.top)
    }
    
    // MARK: - Helpers
    
    private func analyzeURL() {
        guard !urlInput.isEmpty else { return }
        analyzer.analyze(url: urlInput)
    }
    
    private func verdictColor(_ verdict: String) -> Color {
        switch verdict {
        case "SAFE": return .green
        case "SUSPICIOUS": return .orange
        case "MALICIOUS": return .red
        default: return .gray
        }
    }
}

#Preview {
    KMPDemoView()
}

#endif
