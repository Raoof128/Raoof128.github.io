//
// Copyright 2025-2026 Mehr Guard Contributors
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

// Widget/MehrGuardWidget.swift
// Mehr Guard Lock Screen Widget - iOS 17+
//
// Provides a quick-access widget for the Lock Screen and Home Screen
// that deep-links directly to the QR scanner.
//
// To add this widget to your project:
// 1. In Xcode: File > New > Target > Widget Extension
// 2. Name it "MehrGuardWidget"
// 3. Replace the generated code with this file
// 4. Add the URL scheme "mehrguard://" to the main app's Info.plist

import WidgetKit
import SwiftUI

// MARK: - Widget Timeline Provider

struct MehrGuardProvider: TimelineProvider {
    func placeholder(in context: Context) -> MehrGuardEntry {
        MehrGuardEntry(date: Date())
    }

    func getSnapshot(in context: Context, completion: @escaping (MehrGuardEntry) -> ()) {
        let entry = MehrGuardEntry(date: Date())
        completion(entry)
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<Entry>) -> ()) {
        // Static widget - doesn't need updates
        let entry = MehrGuardEntry(date: Date())
        let timeline = Timeline(entries: [entry], policy: .never)
        completion(timeline)
    }
}

// MARK: - Widget Entry

struct MehrGuardEntry: TimelineEntry {
    let date: Date
}

// MARK: - Widget Views

struct MehrGuardWidgetEntryView: View {
    var entry: MehrGuardProvider.Entry
    @Environment(\.widgetFamily) var family
    
    var body: some View {
        switch family {
        case .accessoryCircular:
            AccessoryCircularView()
        case .accessoryRectangular:
            AccessoryRectangularView()
        case .accessoryInline:
            AccessoryInlineView()
        case .systemSmall:
            SmallWidgetView()
        case .systemMedium:
            MediumWidgetView()
        default:
            SmallWidgetView()
        }
    }
}

// MARK: - Lock Screen Widgets (iOS 16+)

/// Circular Lock Screen widget - just the shield icon
struct AccessoryCircularView: View {
    var body: some View {
        ZStack {
            AccessoryWidgetBackground()
            Image(systemName: "qrcode.viewfinder")
                .font(.system(size: 24, weight: .semibold))
                .foregroundStyle(.primary)
        }
        .widgetURL(URL(string: "mehrguard://scan"))
    }
}

/// Rectangular Lock Screen widget - icon + text
struct AccessoryRectangularView: View {
    var body: some View {
        HStack(spacing: 8) {
            Image(systemName: "shield.checkered")
                .font(.system(size: 28, weight: .bold))
                .foregroundStyle(.primary)
            
            VStack(alignment: .leading, spacing: 2) {
                Text("Mehr Guard")
                    .font(.system(size: 14, weight: .bold))
                    .foregroundStyle(.primary)
                Text("Tap to Scan")
                    .font(.system(size: 11))
                    .foregroundStyle(.secondary)
            }
        }
        .widgetURL(URL(string: "mehrguard://scan"))
    }
}

/// Inline Lock Screen widget - compact text only
struct AccessoryInlineView: View {
    var body: some View {
        Label("Scan QR", systemImage: "qrcode.viewfinder")
            .widgetURL(URL(string: "mehrguard://scan"))
    }
}

// MARK: - Home Screen Widgets

/// Small Home Screen widget
struct SmallWidgetView: View {
    var body: some View {
        ZStack {
            // Background gradient
            LinearGradient(
                colors: [
                    Color(red: 0.05, green: 0.07, blue: 0.09),
                    Color(red: 0.09, green: 0.11, blue: 0.13)
                ],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
            
            VStack(spacing: 12) {
                // Shield icon
                ZStack {
                    Circle()
                        .fill(Color(red: 0.42, green: 0.36, blue: 0.91).opacity(0.3))
                        .frame(width: 56, height: 56)
                    
                    Image(systemName: "shield.checkered")
                        .font(.system(size: 28, weight: .bold))
                        .foregroundColor(Color(red: 0.42, green: 0.36, blue: 0.91))
                }
                
                Text("Scan QR")
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(.white)
            }
        }
        .widgetURL(URL(string: "mehrguard://scan"))
    }
}

/// Medium Home Screen widget
struct MediumWidgetView: View {
    var body: some View {
        ZStack {
            // Background gradient
            LinearGradient(
                colors: [
                    Color(red: 0.05, green: 0.07, blue: 0.09),
                    Color(red: 0.09, green: 0.11, blue: 0.13)
                ],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
            
            HStack(spacing: 20) {
                // Left: Shield icon
                ZStack {
                    Circle()
                        .fill(Color(red: 0.42, green: 0.36, blue: 0.91).opacity(0.3))
                        .frame(width: 64, height: 64)
                    
                    Image(systemName: "shield.checkered")
                        .font(.system(size: 32, weight: .bold))
                        .foregroundColor(Color(red: 0.42, green: 0.36, blue: 0.91))
                }
                
                // Center: Text
                VStack(alignment: .leading, spacing: 4) {
                    Text("Mehr Guard")
                        .font(.system(size: 18, weight: .bold))
                        .foregroundColor(.white)
                    
                    Text("Detect phishing in QR codes")
                        .font(.system(size: 12))
                        .foregroundColor(Color(red: 0.55, green: 0.58, blue: 0.62))
                        .lineLimit(2)
                }
                
                Spacer()
                
                // Right: Scan button
                ZStack {
                    RoundedRectangle(cornerRadius: 12)
                        .fill(Color(red: 0.42, green: 0.36, blue: 0.91))
                        .frame(width: 48, height: 48)
                    
                    Image(systemName: "qrcode.viewfinder")
                        .font(.system(size: 22, weight: .semibold))
                        .foregroundColor(.white)
                }
            }
            .padding(.horizontal, 20)
        }
        .widgetURL(URL(string: "mehrguard://scan"))
    }
}

// MARK: - Widget Configuration

@main
struct MehrGuardWidget: Widget {
    let kind: String = "MehrGuardWidget"

    var body: some WidgetConfiguration {
        StaticConfiguration(kind: kind, provider: MehrGuardProvider()) { entry in
            MehrGuardWidgetEntryView(entry: entry)
                .containerBackground(.fill.tertiary, for: .widget)
        }
        .configurationDisplayName("Mehr Guard")
        .description("Quick scan for phishing detection")
        .supportedFamilies([
            .accessoryCircular,      // Lock Screen - circular
            .accessoryRectangular,   // Lock Screen - rectangular
            .accessoryInline,        // Lock Screen - inline
            .systemSmall,            // Home Screen - small
            .systemMedium            // Home Screen - medium
        ])
    }
}

// MARK: - Preview

#Preview(as: .accessoryCircular) {
    MehrGuardWidget()
} timeline: {
    MehrGuardEntry(date: .now)
}

#Preview(as: .accessoryRectangular) {
    MehrGuardWidget()
} timeline: {
    MehrGuardEntry(date: .now)
}

#Preview(as: .systemSmall) {
    MehrGuardWidget()
} timeline: {
    MehrGuardEntry(date: .now)
}

#Preview(as: .systemMedium) {
    MehrGuardWidget()
} timeline: {
    MehrGuardEntry(date: .now)
}
