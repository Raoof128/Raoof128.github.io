// UI/History/HistoryView.swift
// QR-SHIELD Scan History - iOS 26.2 Liquid Glass Edition
//
// UPDATED: December 2025 - iOS 26.2 RC
// - scrollEdgeEffectStyle for soft edges
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
            // iOS 26: Mesh gradient background
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
            
            // iOS 26.2: Scan count in toolbar
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
    
    // MARK: - Filter Bar (Liquid Glass iOS 26.2)
    
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
        // iOS 26.2: Soft edge scroll effect
        .scrollClipDisabled()
    }
    
    // MARK: - History List (iOS 26.2 Enhanced)
    
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

// MARK: - Filter Chip (Liquid Glass iOS 26.2)

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

// MARK: - History Row (Liquid Glass iOS 26.2)

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

// MARK: - View Model (iOS 26 @Observable)

@Observable
@MainActor
final class HistoryViewModel {
    var filteredHistory: [HistoryItemMock] = []
    private var allHistory: [HistoryItemMock] = []
    
    init() {
        loadMockData()
    }
    
    func filter(by filter: VerdictFilter) {
        withAnimation(.spring(response: 0.3)) {
            if filter == .all {
                filteredHistory = allHistory
            } else {
                filteredHistory = allHistory.filter { 
                    $0.verdict.rawValue == filter.rawValue.uppercased() 
                }
            }
        }
    }
    
    func search(query: String) {
        withAnimation(.spring(response: 0.3)) {
            if query.isEmpty {
                filteredHistory = allHistory
            } else {
                filteredHistory = allHistory.filter { 
                    $0.url.localizedCaseInsensitiveContains(query) 
                }
            }
        }
    }
    
    func delete(_ item: HistoryItemMock) {
        allHistory.removeAll { $0.id == item.id }
        filteredHistory.removeAll { $0.id == item.id }
    }
    
    func clearAll() {
        withAnimation {
            allHistory.removeAll()
            filteredHistory.removeAll()
        }
    }
    
    func sortByDate() {
        withAnimation {
            filteredHistory.sort { $0.scannedAt > $1.scannedAt }
        }
    }
    
    func sortByRisk() {
        withAnimation {
            filteredHistory.sort { $0.score > $1.score }
        }
    }
    
    func exportHistory() {
        // In production: Generate CSV/JSON export
        let haptic = UINotificationFeedbackGenerator()
        haptic.notificationOccurred(.success)
    }
    
    private func loadMockData() {
        allHistory = [
            HistoryItemMock(id: "1", url: "https://google.com", score: 12, verdict: .safe, scannedAt: Date()),
            HistoryItemMock(id: "2", url: "https://suspicious-link.xyz", score: 55, verdict: .suspicious, scannedAt: Date().addingTimeInterval(-3600)),
            HistoryItemMock(id: "3", url: "https://apple.com/store", score: 8, verdict: .safe, scannedAt: Date().addingTimeInterval(-7200)),
            HistoryItemMock(id: "4", url: "https://phishing-attack.com/login", score: 92, verdict: .malicious, scannedAt: Date().addingTimeInterval(-86400)),
            HistoryItemMock(id: "5", url: "https://amazon.com/product", score: 5, verdict: .safe, scannedAt: Date().addingTimeInterval(-172800)),
            HistoryItemMock(id: "6", url: "http://bank-verify.com/secure", score: 78, verdict: .malicious, scannedAt: Date().addingTimeInterval(-259200))
        ]
        filteredHistory = allHistory
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
