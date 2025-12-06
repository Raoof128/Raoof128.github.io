// UI/History/HistoryView.swift
// QR-SHIELD Scan History
//
// Native SwiftUI list showing past scans with filtering and search.
// Integrates with Kotlin Multiplatform HistoryRepository.

import SwiftUI

struct HistoryView: View {
    @StateObject private var viewModel = HistoryViewModel()
    @State private var searchText = ""
    @State private var selectedFilter: VerdictFilter = .all
    
    var body: some View {
        VStack(spacing: 0) {
            // Filter Bar
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
        .background(Color.bgDark)
        .navigationTitle("History")
        .searchable(text: $searchText, prompt: "Search URLs")
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Menu {
                    Button(role: .destructive) {
                        viewModel.clearAll()
                    } label: {
                        Label("Clear All", systemImage: "trash")
                    }
                    
                    Button {
                        viewModel.exportHistory()
                    } label: {
                        Label("Export", systemImage: "square.and.arrow.up")
                    }
                } label: {
                    Image(systemName: "ellipsis.circle")
                        .foregroundColor(.brandPrimary)
                }
            }
        }
        .onChange(of: searchText) { _, newValue in
            viewModel.search(query: newValue)
        }
    }
    
    // MARK: - Filter Bar
    
    private var filterBar: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                ForEach(VerdictFilter.allCases, id: \.self) { filter in
                    FilterChip(
                        title: filter.title,
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
    }
    
    // MARK: - History List
    
    private var historyList: some View {
        List {
            ForEach(viewModel.filteredHistory) { item in
                HistoryRow(item: item)
                    .listRowBackground(Color.bgCard)
                    .listRowSeparator(.hidden)
                    .swipeActions(edge: .trailing, allowsFullSwipe: true) {
                        Button(role: .destructive) {
                            viewModel.delete(item)
                        } label: {
                            Label("Delete", systemImage: "trash")
                        }
                    }
            }
        }
        .listStyle(.plain)
        .scrollContentBackground(.hidden)
    }
    
    // MARK: - Empty State
    
    private var emptyState: some View {
        VStack(spacing: 16) {
            Spacer()
            
            Image(systemName: "clock.badge.questionmark")
                .font(.system(size: 60))
                .foregroundColor(.textMuted)
            
            Text("No Scans Yet")
                .font(.title3.weight(.semibold))
                .foregroundColor(.textPrimary)
            
            Text("Your scan history will appear here")
                .font(.subheadline)
                .foregroundColor(.textSecondary)
            
            Spacer()
        }
    }
}

// MARK: - Filter Chip

struct FilterChip: View {
    let title: String
    let color: Color
    let isSelected: Bool
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            HStack(spacing: 4) {
                Circle()
                    .fill(color)
                    .frame(width: 8, height: 8)
                
                Text(title)
                    .font(.subheadline.weight(isSelected ? .semibold : .regular))
            }
            .padding(.horizontal, 14)
            .padding(.vertical, 8)
            .background(isSelected ? color.opacity(0.2) : Color.bgCard)
            .foregroundColor(isSelected ? color : .textSecondary)
            .cornerRadius(20)
            .overlay(
                RoundedRectangle(cornerRadius: 20)
                    .stroke(isSelected ? color.opacity(0.5) : Color.clear, lineWidth: 1)
            )
        }
    }
}

// MARK: - History Row

struct HistoryRow: View {
    let item: HistoryItemMock
    
    var verdictColor: Color {
        switch item.verdict {
        case .safe: return .verdictSafe
        case .suspicious: return .verdictWarning
        case .malicious: return .verdictDanger
        case .unknown: return .verdictUnknown
        }
    }
    
    var body: some View {
        HStack(spacing: 12) {
            // Verdict Indicator
            Circle()
                .fill(verdictColor)
                .frame(width: 10, height: 10)
            
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
                        .fontWeight(.medium)
                        .foregroundColor(verdictColor)
                    
                    Text("•")
                        .foregroundColor(.textMuted)
                    
                    Text(item.formattedDate)
                        .font(.caption2)
                        .foregroundColor(.textMuted)
                }
            }
            
            Spacer()
            
            // Score Badge
            Text("\(item.score)")
                .font(.caption.weight(.bold))
                .foregroundColor(verdictColor)
                .padding(.horizontal, 10)
                .padding(.vertical, 4)
                .background(verdictColor.opacity(0.15))
                .cornerRadius(8)
        }
        .padding(.vertical, 8)
    }
}

// MARK: - View Model

class HistoryViewModel: ObservableObject {
    @Published var filteredHistory: [HistoryItemMock] = []
    
    private var allHistory: [HistoryItemMock] = []
    
    init() {
        loadMockData()
    }
    
    func filter(by filter: VerdictFilter) {
        if filter == .all {
            filteredHistory = allHistory
        } else {
            filteredHistory = allHistory.filter { $0.verdict.rawValue == filter.rawValue.uppercased() }
        }
    }
    
    func search(query: String) {
        if query.isEmpty {
            filteredHistory = allHistory
        } else {
            filteredHistory = allHistory.filter { $0.url.localizedCaseInsensitiveContains(query) }
        }
    }
    
    func delete(_ item: HistoryItemMock) {
        allHistory.removeAll { $0.id == item.id }
        filteredHistory.removeAll { $0.id == item.id }
    }
    
    func clearAll() {
        allHistory.removeAll()
        filteredHistory.removeAll()
    }
    
    func exportHistory() {
        // Export logic
    }
    
    private func loadMockData() {
        allHistory = [
            HistoryItemMock(id: "1", url: "https://google.com", score: 12, verdict: .safe, scannedAt: Date()),
            HistoryItemMock(id: "2", url: "https://suspicious-link.xyz", score: 55, verdict: .suspicious, scannedAt: Date().addingTimeInterval(-3600)),
            HistoryItemMock(id: "3", url: "https://apple.com/store", score: 8, verdict: .safe, scannedAt: Date().addingTimeInterval(-7200)),
            HistoryItemMock(id: "4", url: "https://phishing-attack.com/login", score: 92, verdict: .malicious, scannedAt: Date().addingTimeInterval(-86400))
        ]
        filteredHistory = allHistory
    }
}

// MARK: - Supporting Types

enum VerdictFilter: String, CaseIterable {
    case all, safe, suspicious, malicious
    
    var title: String { rawValue.capitalized }
    
    var color: Color {
        switch self {
        case .all: return .brandPrimary
        case .safe: return .verdictSafe
        case .suspicious: return .verdictWarning
        case .malicious: return .verdictDanger
        }
    }
}

struct HistoryItemMock: Identifiable {
    let id: String
    let url: String
    let score: Int
    let verdict: VerdictMock
    let scannedAt: Date
    
    var formattedDate: String {
        let formatter = RelativeDateTimeFormatter()
        formatter.unitsStyle = .abbreviated
        return formatter.localizedString(for: scannedAt, relativeTo: Date())
    }
}

#Preview {
    NavigationStack {
        HistoryView()
    }
}
