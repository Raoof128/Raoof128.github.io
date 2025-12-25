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

// UI/Export/ReportExportView.swift
// QR-SHIELD Report Generation - iOS 17+ Liquid Glass Edition
//
// Matches: Report Generation HTML design
// Features:
// - Format selection (PDF/JSON)
// - Live document preview
// - Export/Share/Copy actions
// - Animated background

import SwiftUI
#if os(iOS)
import UniformTypeIdentifiers

// MARK: - Export Format

enum ExportFormat: String, CaseIterable {
    case pdf = "PDF"
    case json = "JSON"
    
    var title: String {
        switch self {
        case .pdf: return NSLocalizedString("export.format.pdf_title", comment: "")
        case .json: return NSLocalizedString("export.format.json_title", comment: "")
        }
    }
    
    var subtitle: String {
        switch self {
        case .pdf: return NSLocalizedString("export.format.pdf_subtitle", comment: "")
        case .json: return NSLocalizedString("export.format.json_subtitle", comment: "")
        }
    }
    
    var icon: String {
        switch self {
        case .pdf: return "doc.text.fill"
        case .json: return "curlybraces"
        }
    }
    
    var iconColor: Color {
        switch self {
        case .pdf: return .verdictDanger
        case .json: return .verdictSafe
        }
    }
}

// MARK: - Report Export View

@available(iOS 17, *)
struct ReportExportView: View {
    @Environment(\.dismiss) private var dismiss
    
    let assessment: RiskAssessmentMock?
    
    @State private var selectedFormat: ExportFormat = .pdf
    @State private var isExporting = false
    @State private var showShareSheet = false
    @State private var copiedToClipboard = false
    @State private var showPreviewExpanded = false
    @State private var showHelp = false
    
    init(assessment: RiskAssessmentMock? = nil) {
        self.assessment = assessment
    }
    
    // Sample assessment for preview
    private var displayAssessment: RiskAssessmentMock {
        assessment ?? RiskAssessmentMock(
            score: 87,
            verdict: .malicious,
            flags: ["Homograph Attack", "Suspicious Redirect", "Newly Registered Domain"],
            confidence: 0.94,
            url: "https://paypa1-secure.com/login"
        )
    }
    
    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                ScrollView {
                    VStack(spacing: 24) {
                        // Title Section
                        titleSection
                        
                        // Format Selection
                        formatSelectionSection
                        
                        // Live Preview
                        livePreviewSection
                    }
                    .padding(.horizontal, 20)
                    .padding(.top, 16)
                    .padding(.bottom, 120)
                }
                .scrollContentBackground(.hidden)
                
                // Bottom Action Bar
                actionBar
            }
            .background {
                animatedBackground
            }
            .navigationTitle(NSLocalizedString("export.report_generation", comment: ""))
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
                
                ToolbarItem(placement: .navigationBarTrailing) {
                    Menu {
                        Button {
                            showHelp = true
                        } label: {
                            Label(NSLocalizedString("export.help", comment: ""), systemImage: "questionmark.circle")
                        }
                        
                        Button {
                            // Switch format as a quick action
                            withAnimation {
                                selectedFormat = selectedFormat == .pdf ? .json : .pdf
                            }
                            SettingsManager.shared.triggerHaptic(.selection)
                        } label: {
                            Label(String(format: NSLocalizedString("export.switch_to", comment: ""), selectedFormat == .pdf ? "JSON" : "PDF"), systemImage: "arrow.triangle.swap")
                        }
                    } label: {
                        Image(systemName: "ellipsis")
                            .foregroundColor(.textSecondary)
                    }
                }
            }
            .sheet(isPresented: $showPreviewExpanded) {
                ExpandedPreviewSheet(
                    assessment: displayAssessment,
                    format: selectedFormat
                )
            }
            .sheet(isPresented: $showHelp) {
                ExportHelpSheet()
            }
            .sheet(isPresented: $showShareSheet) {
                ShareSheet(items: [generateReportContent()])
            }
        }
    }
    
    // MARK: - Animated Background
    
    private var animatedBackground: some View {
        ZStack {
            LiquidGlassBackground()
            
            // Additional animated orbs for this page
            Circle()
                .fill(Color.brandPrimary.opacity(0.1))
                .frame(width: 200, height: 200)
                .blur(radius: 60)
                .offset(x: 100, y: -100)
            
            Circle()
                .fill(Color.brandAccent.opacity(0.08))
                .frame(width: 150, height: 150)
                .blur(radius: 50)
                .offset(x: -80, y: 200)
        }
        .ignoresSafeArea()
    }
    
    // MARK: - Title Section
    
    private var titleSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(NSLocalizedString("export.title", comment: ""))
                .font(.title.weight(.bold))
                .foregroundColor(.textPrimary)
            
            Text(NSLocalizedString("export.subtitle", comment: ""))
                .font(.subheadline)
                .foregroundColor(.textSecondary)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }
    
    // MARK: - Format Selection
    
    private var formatSelectionSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text(NSLocalizedString("export.select_format", comment: ""))
                .font(.caption.weight(.semibold))
                .foregroundColor(.textMuted)
                .tracking(1)
            
            HStack(spacing: 16) {
                ForEach(ExportFormat.allCases, id: \.self) { format in
                    formatCard(format)
                }
            }
        }
    }
    
    private func formatCard(_ format: ExportFormat) -> some View {
        Button {
            withAnimation(.spring(response: 0.3)) {
                selectedFormat = format
            }
            SettingsManager.shared.triggerHaptic(.selection)
        } label: {
            VStack(alignment: .leading, spacing: 12) {
                ZStack {
                    Circle()
                        .fill(format.iconColor.opacity(0.15))
                        .frame(width: 44, height: 44)
                    
                    Image(systemName: format.icon)
                        .font(.title3)
                        .foregroundColor(format.iconColor)
                }
                
                VStack(alignment: .leading, spacing: 4) {
                    Text(format.title)
                        .font(.subheadline.weight(.semibold))
                        .foregroundColor(.textPrimary)
                    
                    Text(format.subtitle)
                        .font(.caption)
                        .foregroundColor(.textMuted)
                }
            }
            .padding(16)
            .frame(maxWidth: .infinity, alignment: .leading)
            .liquidGlass(cornerRadius: 16)
            .overlay {
                if selectedFormat == format {
                    RoundedRectangle(cornerRadius: 16)
                        .stroke(Color.brandPrimary, lineWidth: 2)
                }
            }
            .overlay(alignment: .topTrailing) {
                if selectedFormat == format {
                    ZStack {
                        Circle()
                            .fill(Color.brandPrimary)
                            .frame(width: 24, height: 24)
                        
                        Image(systemName: "checkmark")
                            .font(.caption.weight(.bold))
                            .foregroundColor(.white)
                    }
                    .offset(x: -8, y: 8)
                }
            }
        }
    }
    
    // MARK: - Live Preview Section
    
    private var livePreviewSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text(NSLocalizedString("export.live_preview", comment: ""))
                    .font(.caption.weight(.semibold))
                    .foregroundColor(.textMuted)
                    .tracking(1)
                
                Spacer()
                
                Button {
                    showPreviewExpanded = true
                } label: {
                    HStack(spacing: 4) {
                        Text(NSLocalizedString("export.expand", comment: ""))
                            .font(.caption)
                        Image(systemName: "arrow.up.left.and.arrow.down.right")
                            .font(.caption2)
                    }
                    .foregroundColor(.brandPrimary)
                }
            }
            
            // Preview Container
            VStack(spacing: 0) {
                // Preview mockup
                documentPreview
            }
            .liquidGlass(cornerRadius: 20)
            .shadow(color: .black.opacity(0.1), radius: 20, y: 10)
        }
    }
    
    private var documentPreview: some View {
        VStack(spacing: 16) {
            // Document Header
            HStack {
                HStack(spacing: 8) {
                    ZStack {
                        RoundedRectangle(cornerRadius: 6)
                            .fill(Color.brandPrimary)
                            .frame(width: 28, height: 28)
                        
                        Image(systemName: "shield.fill")
                            .font(.caption)
                            .foregroundColor(.white)
                    }
                    
                    // Placeholder bars
                    RoundedRectangle(cornerRadius: 3)
                        .fill(Color.bgSurface)
                        .frame(width: 80, height: 8)
                }
                
                Spacer()
                
                RoundedRectangle(cornerRadius: 3)
                    .fill(Color.bgSurface.opacity(0.5))
                    .frame(width: 50, height: 8)
            }
            .padding(.bottom, 8)
            
            Divider()
            
            // Content placeholders
            VStack(alignment: .leading, spacing: 12) {
                // Title placeholder
                RoundedRectangle(cornerRadius: 3)
                    .fill(Color.bgSurface)
                    .frame(width: 150, height: 12)
                
                // Warning box
                HStack(spacing: 12) {
                    ZStack {
                        RoundedRectangle(cornerRadius: 8)
                            .fill(Color.verdictDanger.opacity(0.1))
                            .frame(width: 50, height: 50)
                        
                        Image(systemName: "exclamationmark.triangle.fill")
                            .foregroundColor(.verdictDanger)
                    }
                    
                    VStack(alignment: .leading, spacing: 6) {
                        RoundedRectangle(cornerRadius: 2)
                            .fill(Color.bgSurface)
                            .frame(height: 6)
                        RoundedRectangle(cornerRadius: 2)
                            .fill(Color.bgSurface)
                            .frame(width: 100, height: 6)
                        RoundedRectangle(cornerRadius: 2)
                            .fill(Color.bgSurface)
                            .frame(width: 80, height: 6)
                    }
                }
                
                // Progress bar placeholder
                VStack(alignment: .leading, spacing: 8) {
                    HStack {
                        RoundedRectangle(cornerRadius: 2)
                            .fill(Color.bgSurface)
                            .frame(width: 60, height: 6)
                        
                        Spacer()
                        
                        RoundedRectangle(cornerRadius: 2)
                            .fill(Color.verdictSafe.opacity(0.5))
                            .frame(width: 30, height: 6)
                    }
                    
                    GeometryReader { geometry in
                        ZStack(alignment: .leading) {
                            RoundedRectangle(cornerRadius: 3)
                                .fill(Color.bgSurface)
                            
                            RoundedRectangle(cornerRadius: 3)
                                .fill(Color.verdictSafe)
                                .frame(width: geometry.size.width * 0.8)
                        }
                    }
                    .frame(height: 6)
                }
                .padding(12)
                .background(Color.bgSurface.opacity(0.3), in: RoundedRectangle(cornerRadius: 10))
                .overlay(
                    RoundedRectangle(cornerRadius: 10)
                        .strokeBorder(style: StrokeStyle(lineWidth: 1, dash: [5]))
                        .foregroundColor(.bgSurface)
                )
            }
            
            // Hover overlay
            Color.clear
                .contentShape(Rectangle())
                .onTapGesture {
                    showPreviewExpanded = true
                }
        }
        .padding(20)
        .background(Color.bgCard)
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .scaleEffect(0.98)
        .opacity(0.9)
    }
    
    // MARK: - Action Bar
    
    private var actionBar: some View {
        VStack(spacing: 0) {
            // Separator with glass effect
            Rectangle()
                .fill(Color.glassBorder)
                .frame(height: 1)
            
            HStack(spacing: 12) {
                // Share Button
                Button {
                    shareReport()
                } label: {
                    VStack(spacing: 6) {
                        ZStack {
                            Circle()
                                .fill(Color.bgSurface)
                                .frame(width: 44, height: 44)
                            
                            Image(systemName: "square.and.arrow.up")
                                .foregroundColor(.textSecondary)
                        }
                        
                        Text(NSLocalizedString("export.share", comment: ""))
                            .font(.caption2)
                            .foregroundColor(.textMuted)
                    }
                }
                
                // Copy Button
                Button {
                    copyToClipboard()
                } label: {
                    VStack(spacing: 6) {
                        ZStack {
                            Circle()
                                .fill(Color.bgSurface)
                                .frame(width: 44, height: 44)
                            
                            Image(systemName: copiedToClipboard ? "checkmark" : "doc.on.doc")
                                .foregroundColor(copiedToClipboard ? .verdictSafe : .textSecondary)
                                .contentTransition(.symbolEffect(.replace))
                        }
                        
                        Text(copiedToClipboard ? NSLocalizedString("export.copied", comment: "") : NSLocalizedString("export.copy", comment: ""))
                            .font(.caption2)
                            .foregroundColor(.textMuted)
                    }
                }
                .sensoryFeedback(.success, trigger: copiedToClipboard)
                
                // Export Button
                Button {
                    exportReport()
                } label: {
                    HStack(spacing: 8) {
                        if isExporting {
                            ProgressView()
                                .tint(.white)
                        } else {
                            Text(NSLocalizedString("export.export_button", comment: ""))
                                .font(.headline)
                            
                            Image(systemName: "arrow.right")
                                .font(.subheadline)
                        }
                    }
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 16)
                    .background(LinearGradient.brandGradient, in: RoundedRectangle(cornerRadius: 14))
                    .shadow(color: .brandPrimary.opacity(0.3), radius: 8, y: 4)
                }
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 16)
            .background(.ultraThinMaterial)
        }
    }
    
    // MARK: - Actions
    
    private func shareReport() {
        SettingsManager.shared.triggerHaptic(.light)
        showShareSheet = true
    }
    
    private func copyToClipboard() {
        let content = generateReportContent()
        UIPasteboard.general.string = content
        
        withAnimation {
            copiedToClipboard = true
        }
        
        SettingsManager.shared.triggerHaptic(.success)
        
        DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
            withAnimation {
                copiedToClipboard = false
            }
        }
    }
    
    private func exportReport() {
        isExporting = true
        SettingsManager.shared.triggerHaptic(.medium)
        
        // Generate and copy the report
        let content = generateReportContent()
        UIPasteboard.general.string = content
        
        // Simulate brief processing then show share sheet
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
            isExporting = false
            SettingsManager.shared.triggerHaptic(.success)
            SettingsManager.shared.playSound(.success)
            
            // Show the share sheet
            showShareSheet = true
        }
    }
    
    private func generateReportContent() -> String {
        let a = displayAssessment
        
        switch selectedFormat {
        case .pdf:
            return """
            🛡️ QR-SHIELD Security Report
            =============================
            
            URL: \(a.url)
            Verdict: \(a.verdict.rawValue)
            Risk Score: \(a.score)/100
            Confidence: \(Int(a.confidence * 100))%
            
            Risk Flags:
            \(a.flags.map { "• \($0)" }.joined(separator: "\n"))
            
            Scanned: \(a.shortDate)
            
            Generated by QR-SHIELD iOS
            """
        case .json:
            return """
            {
              "url": "\(a.url)",
              "verdict": "\(a.verdict.rawValue)",
              "score": \(a.score),
              "confidence": \(a.confidence),
              "flags": [\(a.flags.map { "\"\($0)\"" }.joined(separator: ", "))],
              "scannedAt": "\(a.scannedAt.ISO8601Format())",
              "engine": "QR-SHIELD v2.4.0"
            }
            """
        }
    }
}

// MARK: - Expanded Preview Sheet

struct ExpandedPreviewSheet: View {
    let assessment: RiskAssessmentMock
    let format: ExportFormat
    
    @Environment(\.dismiss) private var dismiss
    
    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    Text(generateContent())
                        .font(.system(.body, design: format == .json ? .monospaced : .default))
                        .foregroundColor(.textPrimary)
                        .textSelection(.enabled)
                }
                .padding(20)
            }
            .background(Color.bgMain.ignoresSafeArea())
            .navigationTitle(NSLocalizedString("export.report_preview", comment: ""))
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(NSLocalizedString("common.done", comment: "")) {
                        dismiss()
                    }
                    .foregroundColor(.brandPrimary)
                }
            }
        }
    }
    
    private func generateContent() -> String {
        switch format {
        case .pdf:
            return """
            🛡️ QR-SHIELD Security Report
            =============================
            
            Analysis Summary
            ----------------
            URL: \(assessment.url)
            Verdict: \(assessment.verdict.rawValue)
            Risk Score: \(assessment.score)/100
            Confidence: \(Int(assessment.confidence * 100))%
            
            Risk Factors Detected
            --------------------
            \(assessment.flags.map { "⚠️ \($0)" }.joined(separator: "\n"))
            
            Detailed Analysis
            -----------------
            This URL has been flagged as potentially malicious based on multiple security indicators detected by the QR-SHIELD offline analysis engine.
            
            The domain structure, TLD reputation, and content patterns match known phishing signatures in our local threat database.
            
            Recommendation: Block access and report to security team.
            
            Scan Information
            ----------------
            Scanned: \(assessment.shortDate)
            Engine: QR-SHIELD v2.4.0
            Mode: Offline Analysis
            
            ---
            Generated by QR-SHIELD for iOS 17+
            """
        case .json:
            return """
            {
              "report": {
                "version": "2.4.0",
                "generatedAt": "\(Date().ISO8601Format())",
                "mode": "offline"
              },
              "analysis": {
                "url": "\(assessment.url)",
                "verdict": "\(assessment.verdict.rawValue)",
                "score": \(assessment.score),
                "confidence": \(assessment.confidence),
                "flags": [
                  \(assessment.flags.map { "\"\($0)\"" }.joined(separator: ",\n      "))
                ]
              },
              "metadata": {
                "scannedAt": "\(assessment.scannedAt.ISO8601Format())",
                "platform": "iOS",
                "engine": "PhishingEngine",
                "database": "local_v2023.10.24"
              }
            }
            """
        }
    }
}

// MARK: - Export Help Sheet

struct ExportHelpSheet: View {
    @Environment(\.dismiss) private var dismiss
    
    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 24) {
                    // PDF Format
                    helpSection(
                        icon: "doc.text.fill",
                        iconColor: .verdictDanger,
                        title: "PDF Report",
                        description: "Exports a human-readable security report suitable for sharing with team members, management, or for your records. Includes verdict, risk factors, and recommendations."
                    )
                    
                    // JSON Format
                    helpSection(
                        icon: "curlybraces",
                        iconColor: .verdictSafe,
                        title: "JSON Data",
                        description: "Exports raw analysis data in JSON format. Perfect for integration with security tools, SIEMs, or for further automated processing."
                    )
                    
                    Divider()
                    
                    // Actions
                    VStack(alignment: .leading, spacing: 16) {
                        Text(NSLocalizedString("export.available_actions", comment: ""))
                            .font(.caption.weight(.bold))
                            .foregroundColor(.textMuted)
                            .tracking(1)
                        
                        actionHelp(icon: "square.and.arrow.up", title: "Share", description: "Open the iOS share sheet to send via Messages, Mail, AirDrop, or save to Files.")
                        
                        actionHelp(icon: "doc.on.doc", title: "Copy", description: "Copy the report content to your clipboard for pasting into other apps.")
                        
                        actionHelp(icon: "arrow.right", title: "Export", description: "Save the report directly to your device.")
                    }
                    
                    Divider()
                    
                    // Privacy Note
                    HStack(alignment: .top, spacing: 12) {
                        Image(systemName: "lock.shield.fill")
                            .font(.title3)
                            .foregroundColor(.verdictSafe)
                        
                        VStack(alignment: .leading, spacing: 4) {
                            Text(NSLocalizedString("export.privacy_note", comment: ""))
                                .font(.subheadline.weight(.semibold))
                                .foregroundColor(.textPrimary)
                            
                            Text(NSLocalizedString("export.privacy_message", comment: ""))
                                .font(.caption)
                                .foregroundColor(.textSecondary)
                        }
                    }
                    .padding(16)
                    .background(Color.verdictSafe.opacity(0.1), in: RoundedRectangle(cornerRadius: 12))
                }
                .padding(20)
            }
            .scrollContentBackground(.hidden)
            .background {
                LiquidGlassBackground()
                    .ignoresSafeArea()
            }
            .navigationTitle(NSLocalizedString("export.help_title", comment: ""))
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(NSLocalizedString("common.done", comment: "")) {
                        dismiss()
                    }
                    .foregroundColor(.brandPrimary)
                }
            }
        }
    }
    
    private func helpSection(icon: String, iconColor: Color, title: String, description: String) -> some View {
        HStack(alignment: .top, spacing: 16) {
            ZStack {
                Circle()
                    .fill(iconColor.opacity(0.15))
                    .frame(width: 44, height: 44)
                
                Image(systemName: icon)
                    .font(.title3)
                    .foregroundColor(iconColor)
            }
            
            VStack(alignment: .leading, spacing: 4) {
                Text(title)
                    .font(.headline)
                    .foregroundColor(.textPrimary)
                
                Text(description)
                    .font(.subheadline)
                    .foregroundColor(.textSecondary)
            }
        }
    }
    
    private func actionHelp(icon: String, title: String, description: String) -> some View {
        HStack(spacing: 12) {
            Image(systemName: icon)
                .font(.subheadline)
                .foregroundColor(.brandPrimary)
                .frame(width: 24)
            
            VStack(alignment: .leading, spacing: 2) {
                Text(title)
                    .font(.subheadline.weight(.medium))
                    .foregroundColor(.textPrimary)
                
                Text(description)
                    .font(.caption)
                    .foregroundColor(.textMuted)
            }
        }
    }
}

// MARK: - Share Sheet

struct ShareSheet: UIViewControllerRepresentable {
    let items: [Any]
    
    func makeUIViewController(context: Context) -> UIActivityViewController {
        UIActivityViewController(activityItems: items, applicationActivities: nil)
    }
    
    func updateUIViewController(_ uiViewController: UIActivityViewController, context: Context) {}
}

#endif
