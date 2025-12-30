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

// UI/History/HistoryView.swift
// QR-SHIELD Scan History - iOS 17+ Liquid Glass Edition
//
// UPDATED: December 2025 - iOS 17+ Compatible
// - Enhanced list animations
// - VerdictIcon asset integration
// - ShareLink for history items

#if os(iOS)
import SwiftUI

@available(iOS 17, *)
struct HistoryView: View {
    @State private var viewModel = HistoryViewModel()
    @State private var searchText = ""
    @State private var selectedFilter: VerdictFilter = .all
    @State private var showExportedToast = false
    @State private var showClearConfirmation = false
    @State private var showStatsPopover = false
    
    var body: some View {
        VStack(spacing: 0) {
            // Filter Bar with Liquid Glass
            filterBar
                .padding(.horizontal)
                .padding(.vertical, 8)
            
            // History List
            if viewModel.filteredHistory.isEmpty {
                emptyState
            } else {
                historyList
            }
        }
        .background {
            // iOS 17+: Mesh gradient background
            LiquidGlassBackground()
                .ignoresSafeArea()
        }
        .navigationTitle("Scan History")
        .searchable(text: $searchText, prompt: "Search URLs")
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Menu {
                    // Sort options
                    Section(NSLocalizedString("history.sort_menu", comment: "")) {
                        Button {
                            viewModel.sortByDate()
                        } label: {
                            Label(NSLocalizedString("history.sort.by_date", comment: ""), systemImage: "calendar")
                        }
                        
                        Button {
                            viewModel.sortByRisk()
                        } label: {
                            Label(NSLocalizedString("history.sort.by_risk", comment: ""), systemImage: "shield")
                        }
                    }
                    
                    Divider()
                    
                    // Export option
                    Button {
                        viewModel.exportHistory()
                        showExportedToast = true
                        // Auto-hide toast after 2 seconds
                        DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
                            showExportedToast = false
                        }
                    } label: {
                        Label(NSLocalizedString("history.export", comment: ""), systemImage: "square.and.arrow.up")
                    }
                    
                    Divider()
                    
                    // Clear all with confirmation
                    Button(role: .destructive) {
                        showClearConfirmation = true
                    } label: {
                        Label(NSLocalizedString("history.clear_all_action", comment: ""), systemImage: "trash")
                    }
                } label: {
                    Image(systemName: "ellipsis.circle")
                        .foregroundColor(.brandPrimary)
                        .symbolEffect(.pulse)
                }
            }
            
            // iOS 17+: Scan count badge as functional button
            ToolbarItem(placement: .navigationBarLeading) {
                Button {
                    showStatsPopover.toggle()
                    SettingsManager.shared.triggerHaptic(.light)
                } label: {
                    HStack(spacing: 4) {
                        Image(systemName: "doc.text")
                            .font(.caption2)
                        Text("\(viewModel.filteredHistory.count)")
                            .font(.caption.weight(.medium))
                    }
                    .foregroundColor(.textSecondary)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 4)
                    .background(Color.bgSurface.opacity(0.5), in: Capsule())
                }
                .popover(isPresented: $showStatsPopover) {
                    VStack(alignment: .leading, spacing: 12) {
                        Text(NSLocalizedString("history.scan_statistics", comment: ""))
                            .font(.headline)
                        
                        Divider()
                        
                        let safeCount = viewModel.filteredHistory.filter { $0.verdict == .safe }.count
                        let suspiciousCount = viewModel.filteredHistory.filter { $0.verdict == .suspicious }.count
                        let maliciousCount = viewModel.filteredHistory.filter { $0.verdict == .malicious }.count
                        
                        HStack {
                            Circle().fill(Color.verdictSafe).frame(width: 10, height: 10)
                            Text(String(format: NSLocalizedString("history.safe_format", comment: ""), safeCount))
                        }
                        HStack {
                            Circle().fill(Color.verdictWarning).frame(width: 10, height: 10)
                            Text(String(format: NSLocalizedString("history.suspicious_format", comment: ""), suspiciousCount))
                        }
                        HStack {
                            Circle().fill(Color.verdictDanger).frame(width: 10, height: 10)
                            Text(String(format: NSLocalizedString("history.malicious_format", comment: ""), maliciousCount))
                        }
                        
                        Divider()
                        
                        Text(String(format: NSLocalizedString("history.total_format", comment: ""), viewModel.filteredHistory.count))
                            .font(.footnote)
                            .foregroundColor(.textSecondary)
                    }
                    .padding()
                    .presentationCompactAdaptation(.popover)
                }
            }
        }
        .onChange(of: searchText) { _, newValue in
            viewModel.search(query: newValue)
        }
        .onAppear {
            viewModel.refreshHistory()
        }
        .onChange(of: HistoryStore.shared.items.count) { _, _ in
            // Refresh when items are added or removed (e.g., after a new scan)
            viewModel.refreshHistory()
        }
        .confirmationDialog(
            "Clear All History?",
            isPresented: $showClearConfirmation,
            titleVisibility: .visible
        ) {
            Button(NSLocalizedString("history.clear_all_action", comment: ""), role: .destructive) {
                viewModel.clearAll()
            }
            Button(NSLocalizedString("common.cancel", comment: ""), role: .cancel) {}
        } message: {
            Text(NSLocalizedString("settings.clear_confirm_message", comment: ""))
        }
        .overlay(alignment: .top) {
            if showExportedToast {
                HStack(spacing: 8) {
                    Image(systemName: "checkmark.circle.fill")
                        .foregroundColor(.verdictSafe)
                    Text(NSLocalizedString("history.exported_clipboard", comment: ""))
                        .font(.subheadline)
                        .foregroundColor(.textPrimary)
                }
                .padding(.horizontal, 20)
                .padding(.vertical, 12)
                .background(.ultraThinMaterial, in: Capsule())
                .shadow(color: .black.opacity(0.2), radius: 8, y: 4)
                .transition(.move(edge: .top).combined(with: .opacity))
                .padding(.top, 60)
                .animation(.spring(response: 0.3), value: showExportedToast)
            }
        }
        .accessibilityLabel(Text(NSLocalizedString("accessibility.scan_history", comment: "")))
    }
    
    // MARK: - Filter Bar (Liquid Glass iOS 17+)
    
    private var filterBar: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                ForEach(VerdictFilter.allCases, id: \.self) { filter in
                    FilterChip(
                        title: filter.title,
                        icon: filter.icon,
                        color: filter.color,
                        isSelected: selectedFilter == filter
                    ) {
                        withAnimation(.spring(response: 0.3)) {
                            selectedFilter = filter
                            viewModel.filter(by: filter)
                        }
                    }
                }
            }
        }
        // iOS 17+: Soft edge scroll effect
        .scrollClipDisabled()
    }
    
    // MARK: - History List (iOS 17+ Enhanced)
    
    private var historyList: some View {
        ScrollView {
            LazyVStack(spacing: 8) {
                ForEach(viewModel.filteredHistory) { item in
                    NavigationLink(destination: ScanResultView(assessment: item.toRiskAssessment())) {
                        HistoryRow(item: item)
                    }
                    .buttonStyle(.plain)
                    .contextMenu {
                        Button {
                            UIPasteboard.general.string = item.url
                            SettingsManager.shared.triggerHaptic(.success)
                        } label: {
                            Label(NSLocalizedString("result.copy_url", comment: ""), systemImage: "doc.on.doc")
                        }
                        
                        ShareLink(item: item.url) {
                            Label(NSLocalizedString("result.share", comment: ""), systemImage: "square.and.arrow.up")
                        }
                        
                        Divider()
                        
                        Button(role: .destructive) {
                            withAnimation(.spring(response: 0.3)) {
                                viewModel.delete(item)
                            }
                        } label: {
                            Label(NSLocalizedString("history.delete", comment: ""), systemImage: "trash")
                        }
                    }
                    .transition(.move(edge: .trailing).combined(with: .opacity))
                }
            }
            .padding(.horizontal)
        }
        .scrollContentBackground(.hidden)
    }
    
    // MARK: - Empty State (Liquid Glass)
    
    private var emptyState: some View {
        VStack(spacing: 20) {
            Spacer()
            
            ZStack {
                Circle()
                    .fill(.ultraThinMaterial)
                    .frame(width: 120, height: 120)
                
                Image(systemName: "clock.badge.questionmark")
                    .font(.system(size: 50))
                    .foregroundStyle(LinearGradient.brandGradient)
                    .symbolEffect(.pulse)
            }
            
            Text(searchText.isEmpty ?
                 "No Scans Yet" :
                 "No Results")
                .font(.title3.weight(.semibold))
                .foregroundColor(.textPrimary)
            
            Text(searchText.isEmpty ?
                 "Scanned QR codes will appear here" :
                 "Try adjusting your search or filters")
                .font(.subheadline)
                .foregroundColor(.textSecondary)
                .multilineTextAlignment(.center)
            
            // Scan Now button when empty
            if searchText.isEmpty {
                NavigationLink(destination: ScannerView()) {
                    HStack(spacing: 8) {
                        Image(systemName: "qrcode.viewfinder")
                        Text(NSLocalizedString("history.scan_now", comment: ""))
                    }
                    .font(.headline)
                    .foregroundColor(.white)
                    .padding(.horizontal, 24)
                    .padding(.vertical, 12)
                    .background(LinearGradient.brandGradient, in: Capsule())
                }
                .padding(.top, 16)
            }
            
            Spacer()
        }
    }
}

// MARK: - Filter Chip (Liquid Glass iOS 17+)

struct FilterChip: View {
    let title: String
    let icon: String
    let color: Color
    let isSelected: Bool
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            HStack(spacing: 6) {
                Image(systemName: icon)
                    .font(.caption)
                    .symbolEffect(.bounce, value: isSelected)
                
                Text(title)
                    .font(.subheadline.weight(isSelected ? .semibold : .regular))
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 10)
            .background {
                if isSelected {
                    Capsule()
                        .fill(color.opacity(0.2))
                        .overlay {
                            Capsule()
                                .stroke(color.opacity(0.4), lineWidth: 1)
                        }
                } else {
                    Capsule()
                        .fill(.ultraThinMaterial)
                }
            }
            .foregroundColor(isSelected ? color : .textSecondary)
        }
        .sensoryFeedback(.selection, trigger: isSelected)
    }
}

// MARK: - History Row (Liquid Glass iOS 17+)

struct HistoryRow: View {
    let item: HistoryItemMock
    
    var verdictColor: Color {
        Color.forVerdict(item.verdict)
    }
    
    var body: some View {
        HStack(spacing: 12) {
            // Verdict Icon with VerdictIcon component
            VerdictIcon(
                verdict: item.verdict,
                size: 20,
                useSFSymbols: true
            )
            .frame(width: 40, height: 40)
            .background {
                Circle()
                    .fill(verdictColor.opacity(0.15))
            }
            
            // Content
            VStack(alignment: .leading, spacing: 4) {
                Text(item.url)
                    .font(.subheadline)
                    .foregroundColor(.textPrimary)
                    .lineLimit(1)
                    .truncationMode(.middle)
                
                HStack(spacing: 8) {
                    Text(item.verdict.rawValue)
                        .font(.caption2)
                        .fontWeight(.semibold)
                        .foregroundColor(verdictColor)
                    
                    Text("•")
                        .foregroundColor(.textMuted)
                    
                    Text(item.formattedDate)
                        .font(.caption2)
                        .foregroundColor(.textMuted)
                }
            }
            
            Spacer()
            
            // Score Badge with Liquid Glass
            Text("\(item.score)")
                .font(.caption.weight(.bold))
                .foregroundColor(verdictColor)
                .padding(.horizontal, 12)
                .padding(.vertical, 6)
                .background {
                    Capsule()
                        .fill(verdictColor.opacity(0.15))
                        .overlay {
                            Capsule()
                                .stroke(verdictColor.opacity(0.3), lineWidth: 1)
                        }
                }
            
            Image(systemName: "chevron.right")
                .font(.caption)
                .foregroundColor(.textMuted)
        }
        .padding(.vertical, 12)
        .padding(.horizontal, 16)
        .liquidGlass(cornerRadius: 16)
        .accessibilityElement(children: .ignore)
        .accessibilityLabel("\(item.verdict.rawValue), \(item.url)")
        .accessibilityValue("Score \(item.score)")
    }
}

// MARK: - History Detail Sheet

struct HistoryDetailSheet: View {
    let item: HistoryItemMock
    
    var body: some View {
        VStack(spacing: 20) {
            // Header
            VStack(spacing: 8) {
                VerdictIcon(verdict: item.verdict, size: 40)
                
                Text(item.verdict.rawValue)
                    .font(.title2.weight(.bold))
                    .foregroundColor(Color.forVerdict(item.verdict))
                
                Text(String(format: "Risk Score: %d/100", item.score))
                    .font(.subheadline)
                    .foregroundColor(.textSecondary)
            }
            .padding(.top, 20)
            
            // URL
            VStack(alignment: .leading, spacing: 8) {
                Text(NSLocalizedString("history.url_label", comment: ""))
                    .font(.caption)
                    .foregroundColor(.textMuted)
                
                Text(item.url)
                    .font(.body)
                    .foregroundColor(.textPrimary)
                    .textSelection(.enabled)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding()
            .liquidGlass(cornerRadius: 12)
            
            // Scanned At
            HStack {
                Image(systemName: "clock")
                    .foregroundColor(.textMuted)
                Text(String(format: "Scanned %@", item.formattedDate))
                    .font(.subheadline)
                    .foregroundColor(.textSecondary)
            }
            
            // Actions
            HStack(spacing: 16) {
                ShareLink(item: item.url) {
                    Label("Share", systemImage: "square.and.arrow.up")
                        .frame(maxWidth: .infinity)
                }
                .buttonStyle(.glass)
                
                Button {
                    UIPasteboard.general.string = item.url
                    SettingsManager.shared.triggerHaptic(.success)
                } label: {
                    Label(NSLocalizedString("result.copy_url", comment: ""), systemImage: "doc.on.doc")
                        .frame(maxWidth: .infinity)
                }
                .buttonStyle(.glass(color: .brandSecondary))
            }
            .padding(.horizontal)
            
            Spacer()
        }
        .padding()
    }
}

// MARK: - View Model (iOS 17+ @Observable)

@Observable
@MainActor
final class HistoryViewModel {
    var filteredHistory: [HistoryItemMock] = []
    private var currentFilter: VerdictFilter = .all
    private var currentSearchQuery: String = ""
    
    init() {
        refreshHistory()
    }
    
    func refreshHistory() {
        applyFiltersAndSearch()
    }
    
    func filter(by filter: VerdictFilter) {
        currentFilter = filter
        applyFiltersAndSearch()
    }
    
    func search(query: String) {
        currentSearchQuery = query
        applyFiltersAndSearch()
    }
    
    private func applyFiltersAndSearch() {
        withAnimation(.spring(response: 0.3)) {
            var items = HistoryStore.shared.items
            
            // Apply verdict filter - match by verdict enum correctly
            if currentFilter != .all {
                items = items.filter { item in
                    switch currentFilter {
                    case .safe: return item.verdict == .safe
                    case .suspicious: return item.verdict == .suspicious
                    case .malicious: return item.verdict == .malicious
                    case .all: return true  // Already handled above
                    }
                }
            }
            
            // Apply search
            if !currentSearchQuery.isEmpty {
                items = items.filter { 
                    $0.url.localizedCaseInsensitiveContains(currentSearchQuery) 
                }
            }
            
            filteredHistory = items
        }
    }
    
    func delete(_ item: HistoryItemMock) {
        HistoryStore.shared.delete(item)
        refreshHistory()
    }
    
    func clearAll() {
        HistoryStore.shared.clearAll()
        refreshHistory()
    }
    
    func sortByDate() {
        withAnimation {
            filteredHistory = filteredHistory.sorted { $0.scannedAt > $1.scannedAt }
        }
        SettingsManager.shared.triggerHaptic(.selection)
    }
    
    func sortByRisk() {
        withAnimation {
            filteredHistory = filteredHistory.sorted { $0.score > $1.score }
        }
        SettingsManager.shared.triggerHaptic(.selection)
    }
    
    func exportHistory() {
        // Export functionality
        let text = HistoryStore.shared.exportAsText()
        UIPasteboard.general.string = text
        SettingsManager.shared.triggerHaptic(.success)
        SettingsManager.shared.playSound(.success)
        
        #if DEBUG
        print("📋 Exported history to clipboard")
        #endif
    }
}

// MARK: - Supporting Types

enum VerdictFilter: String, CaseIterable {
    case all, safe, suspicious, malicious
    
    var title: String { rawValue.capitalized }
    
    var icon: String {
        switch self {
        case .all: return "line.3.horizontal.decrease.circle"
        case .safe: return "checkmark.shield"
        case .suspicious: return "exclamationmark.shield"
        case .malicious: return "xmark.shield"
        }
    }
    
    var color: Color {
        switch self {
        case .all: return .brandPrimary
        case .safe: return .verdictSafe
        case .suspicious: return .verdictWarning
        case .malicious: return .verdictDanger
        }
    }
}

// NOTE: HistoryItemMock is now defined in Models/MockTypes.swift

#endif
