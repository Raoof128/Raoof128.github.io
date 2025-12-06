// UI/History/HistoryView.swift
// QR-SHIELD Scan History - iOS 26 Liquid Glass Edition
//
// UPDATED: December 2025 - iOS 26 / Xcode 26
// - Liquid Glass design
// - Observable in UIKit patterns
// - Enhanced list animations

import SwiftUI

struct HistoryView: View {
    @State private var viewModel = HistoryViewModel()
    @State private var searchText = ""
    @State private var selectedFilter: VerdictFilter = .all
    
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
            // iOS 26: Mesh gradient background
            MeshGradient.liquidGlassBackground
                .ignoresSafeArea()
        }
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
                        .symbolEffect(.pulse)
                }
            }
        }
        .onChange(of: searchText) { _, newValue in
            viewModel.search(query: newValue)
        }
    }
    
    // MARK: - Filter Bar (Liquid Glass iOS 26)
    
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
                    .listRowBackground(Color.clear)
                    .listRowSeparator(.hidden)
                    .swipeActions(edge: .trailing, allowsFullSwipe: true) {
                        Button(role: .destructive) {
                            withAnimation {
                                viewModel.delete(item)
                            }
                        } label: {
                            Label("Delete", systemImage: "trash")
                        }
                    }
            }
        }
        .listStyle(.plain)
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

// MARK: - Filter Chip (Liquid Glass iOS 26)

struct FilterChip: View {
    let title: String
    let color: Color
    let isSelected: Bool
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            HStack(spacing: 6) {
                Circle()
                    .fill(color)
                    .frame(width: 8, height: 8)
                    .shadow(color: isSelected ? color.opacity(0.5) : .clear, radius: 4)
                
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

// MARK: - History Row (Liquid Glass iOS 26)

struct HistoryRow: View {
    let item: HistoryItemMock
    @State private var isPressed = false
    
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
            // Verdict Indicator with glow
            ZStack {
                Circle()
                    .fill(verdictColor.opacity(0.2))
                    .frame(width: 40, height: 40)
                
                Circle()
                    .fill(verdictColor)
                    .frame(width: 12, height: 12)
                    .shadow(color: verdictColor.opacity(0.5), radius: 4)
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
        .padding(.horizontal, 4)
        .padding(.vertical, 4)
    }
}

// MARK: - View Model (iOS 26 @Observable)

@Observable
final class HistoryViewModel {
    var filteredHistory: [HistoryItemMock] = []
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
            HistoryItemMock(id: "4", url: "https://phishing-attack.com/login", score: 92, verdict: .malicious, scannedAt: Date().addingTimeInterval(-86400)),
            HistoryItemMock(id: "5", url: "https://amazon.com/product", score: 5, verdict: .safe, scannedAt: Date().addingTimeInterval(-172800))
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
