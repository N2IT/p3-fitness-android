import Foundation
import SwiftUI

// MARK: - Date Extensions

extension Date {
    /// Returns elapsed time as "HH:MM:SS" from a given start date.
    static func formattedDuration(from startDate: Date) -> String {
        let elapsed = Int(Date().timeIntervalSince(startDate))
        let hours   = elapsed / 3600
        let minutes = (elapsed % 3600) / 60
        let seconds = elapsed % 60
        if hours > 0 {
            return String(format: "%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            return String(format: "%02d:%02d", minutes, seconds)
        }
    }

    /// Returns elapsed seconds formatted as "HH:MM:SS".
    static func formattedDuration(seconds: Int) -> String {
        let hours   = seconds / 3600
        let minutes = (seconds % 3600) / 60
        let secs    = seconds % 60
        if hours > 0 {
            return String(format: "%02d:%02d:%02d", hours, minutes, secs)
        } else {
            return String(format: "%02d:%02d", minutes, secs)
        }
    }

    /// Human-readable relative time: "Just now", "5 min ago", "2 days ago", etc.
    var timeAgo: String {
        let interval = Date().timeIntervalSince(self)
        switch interval {
        case ..<60:
            return "Just now"
        case 60..<3600:
            let minutes = Int(interval / 60)
            return "\(minutes) \(minutes == 1 ? "min" : "mins") ago"
        case 3600..<86400:
            let hours = Int(interval / 3600)
            return "\(hours) \(hours == 1 ? "hour" : "hours") ago"
        case 86400..<604800:
            let days = Int(interval / 86400)
            return "\(days) \(days == 1 ? "day" : "days") ago"
        case 604800..<2592000:
            let weeks = Int(interval / 604800)
            return "\(weeks) \(weeks == 1 ? "week" : "weeks") ago"
        default:
            let months = Int(interval / 2592000)
            if months < 12 {
                return "\(months) \(months == 1 ? "month" : "months") ago"
            } else {
                let years = Int(interval / 31536000)
                return "\(years) \(years == 1 ? "year" : "years") ago"
            }
        }
    }

    /// Short date string, e.g. "Jan 15".
    var shortFormatted: String {
        let formatter = DateFormatter()
        formatter.dateFormat = "MMM d"
        return formatter.string(from: self)
    }

    /// Full date string, e.g. "January 15, 2025".
    var longFormatted: String {
        let formatter = DateFormatter()
        formatter.dateStyle = .long
        formatter.timeStyle = .none
        return formatter.string(from: self)
    }

    /// Short date + time string, e.g. "Jan 15, 9:30 AM".
    var shortDateTimeFormatted: String {
        let formatter = DateFormatter()
        formatter.dateFormat = "MMM d, h:mm a"
        return formatter.string(from: self)
    }

    /// ISO 8601 string for CSV export.
    var iso8601String: String {
        let formatter = ISO8601DateFormatter()
        return formatter.string(from: self)
    }
}

// MARK: - Double Extensions

extension Double {
    /// Formats a weight value with its unit suffix.
    /// - Parameter unit: "lbs" or "kg"
    /// - Returns: e.g. "135 lbs" or "61.2 kg"
    func formattedWeight(unit: String) -> String {
        if self == self.rounded() {
            return "\(Int(self)) \(unit)"
        } else {
            return String(format: "%.1f \(unit)", self)
        }
    }

    /// Rounds to one decimal place.
    var roundedOneDecimal: Double {
        (self * 10).rounded() / 10
    }

    /// Compact display: 135.0 → "135", 61.5 → "61.5"
    var compactString: String {
        if self == self.rounded() {
            return "\(Int(self))"
        }
        return String(format: "%.1f", self)
    }
}

// MARK: - Int Extensions

extension Int {
    /// Formats seconds as a short duration string.
    var durationString: String {
        Date.formattedDuration(seconds: self)
    }
}

// MARK: - String Extensions

extension String {
    /// True if the string is not empty after trimming whitespace.
    var isNotBlank: Bool {
        !trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
    }

    /// Trims whitespace and newlines.
    var trimmed: String {
        trimmingCharacters(in: .whitespacesAndNewlines)
    }
}

// MARK: - View Extensions

extension View {
    /// Applies a standard card style used across the app.
    func cardStyle(padding: CGFloat = 16) -> some View {
        self
            .padding(padding)
            .background(Color(hex: "#141414"))
            .cornerRadius(16)
            .overlay(
                RoundedRectangle(cornerRadius: 16)
                    .stroke(Color(white: 0.12), lineWidth: 1)
            )
    }

    /// Hides the view (but preserves layout space) when condition is false.
    @ViewBuilder
    func isHidden(_ hidden: Bool) -> some View {
        if hidden {
            self.hidden()
        } else {
            self
        }
    }
}

// MARK: - Array Extensions

extension Array {
    /// Safe subscript — returns nil for out-of-bounds index.
    subscript(safe index: Int) -> Element? {
        indices.contains(index) ? self[index] : nil
    }
}
