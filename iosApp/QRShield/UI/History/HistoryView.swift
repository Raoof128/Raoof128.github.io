//
// Copyright 2024 QR-SHIELD Contributors
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

import SwiftUI

struct HistoryView: View {
    @State private var viewModel = HistoryViewModel()
    @State private var searchText = ""
    @State private var selectedFilter: VerdictFilter = .all
    @State private var selectedItem: HistoryItemMock?
    
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
        .navigationTitle("History")
        .searchable(text: $searchText, prompt: "Search URLs")
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Menu {
                    // Sort options
                    Section("Sort") {
                        Button {
                            viewModel.sortByDate()
                        } label: {
                            Label("By Date", systemImage: "calendar")
                        }
                        
                        Button {
                            viewModel.sortByRisk()
                        } label: {
                            Label("By Risk", systemImage: "shield")
                        }
                    }
                    
                    Divider()
                    
                    // Export option
                    Button {
                        viewModel.exportHistory()
                    } label: {
                        Label("Export", systemImage: "square.and.arrow.up")
                    }
                    
                    Divider()
                    
                    // Clear all
                    Button(role: .destructive) {
                        viewModel.clearAll()
                    } label: {
                        Label("Clear All", systemImage: "trash")
                    }
                } label: {
                    Image(systemName: "ellipsis.circle")
                        .foregroundColor(.brandPrimary)
                        .symbolEffect(.pulse)
                }
            }
            
            // iOS 17+: Scan count in toolbar
            ToolbarItem(placement: .navigationBarLeading) {
                Text("\(viewModel.filteredHistory.count) scans")
                    .font(.caption)
                    .foregroundColor(.textSecondary)
            }
        }
        .onChange(of: searchText) { _, newValue in
            viewModel.search(query: newValue)
        }
        .sheet(item: $selectedItem) { item in
            HistoryDetailSheet(item: item)
                .presentationDetents([.medium])
                .presentationBackground(.ultraThinMaterial)
        }
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
                    HistoryRow(item: item)
                        .onTapGesture {
                            selectedItem = item
                        }
                        .contextMenu {
                            Button {
                                UIPasteboard.general.string = item.url
                            } label: {
                                Label("Copy URL", systemImage: "doc.on.doc")
                            }
                            
                            ShareLink(item: item.url) {
                                Label("Share", systemImage: "square.and.arrow.up")
                            }
                            
                            Divider()
                            
                            Button(role: .destructive) {
                                withAnimation(.spring(response: 0.3)) {
                                    viewModel.delete(item)
                                }
                            } label: {
                                Label("Delete", systemImage: "trash")
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
            
            Text(searchText.isEmpty ? "No Scans Yet" : "No Results")
                .font(.title3.weight(.semibold))
                .foregroundColor(.textPrimary)
            
            Text(searchText.isEmpty ? 
                 "Your scan history will appear here" : 
                 "Try a different search term")
                .font(.subheadline)
                .foregroundColor(.textSecondary)
            
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
                
                Text("Risk Score: \(item.score)/100")
                    .font(.subheadline)
                    .foregroundColor(.textSecondary)
            }
            .padding(.top, 20)
            
            // URL
            VStack(alignment: .leading, spacing: 8) {
                Text("URL")
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
                Text("Scanned \(item.formattedDate)")
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
                } label: {
                    Label("Copy", systemImage: "doc.on.doc")
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
            
            // Apply verdict filter
            if currentFilter != .all {
                let verdictString = currentFilter.rawValue.uppercased()
                items = items.filter { $0.verdict.rawValue == verdictString }
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

#Preview {
    NavigationStack {
        HistoryView()
    }
}
