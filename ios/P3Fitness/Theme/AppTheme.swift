import SwiftUI

struct AppTheme {
    // MARK: - Background colors
    static let background        = Color(hex: "#0A0A0A")
    static let surface           = Color(hex: "#141414")
    static let surfaceElevated   = Color(hex: "#1E1E1E")

    // MARK: - Brand colors
    static let primary           = Color(hex: "#0055FF")
    static let primaryLight      = Color(hex: "#4488FF")
    static let accent            = Color(hex: "#FF6600")

    // MARK: - Text colors
    static let textPrimary       = Color.white
    static let textSecondary     = Color(white: 0.6)
    static let textTertiary      = Color(white: 0.35)

    // MARK: - Semantic colors
    static let success           = Color(hex: "#00CC66")
    static let error             = Color(hex: "#FF3333")
    static let warning           = Color(hex: "#FFAA00")

    // MARK: - Gradients
    static let backgroundGradient = LinearGradient(
        colors: [Color(hex: "#0A0A0A"), Color(hex: "#0D1A33")],
        startPoint: .top,
        endPoint: .bottom
    )

    static let primaryGradient = LinearGradient(
        colors: [Color(hex: "#0055FF"), Color(hex: "#0033CC")],
        startPoint: .leading,
        endPoint: .trailing
    )

    static let accentGradient = LinearGradient(
        colors: [Color(hex: "#FF6600"), Color(hex: "#CC4400")],
        startPoint: .leading,
        endPoint: .trailing
    )

    static let successGradient = LinearGradient(
        colors: [Color(hex: "#00CC66"), Color(hex: "#009944")],
        startPoint: .leading,
        endPoint: .trailing
    )

    // MARK: - Corner radii
    static let cornerRadiusSmall: CGFloat  = 8
    static let cornerRadiusMedium: CGFloat = 12
    static let cornerRadiusLarge: CGFloat  = 16
    static let cornerRadiusXL: CGFloat     = 20

    // MARK: - Shadows
    static func primaryShadow(radius: CGFloat = 12) -> some ViewModifier {
        ShadowModifier(color: Color(hex: "#0055FF").opacity(0.35), radius: radius)
    }

    static func accentShadow(radius: CGFloat = 12) -> some ViewModifier {
        ShadowModifier(color: Color(hex: "#FF6600").opacity(0.35), radius: radius)
    }
}

// MARK: - Shadow Modifier helper

struct ShadowModifier: ViewModifier {
    let color: Color
    let radius: CGFloat

    func body(content: Content) -> some View {
        content.shadow(color: color, radius: radius, x: 0, y: 6)
    }
}

// MARK: - Color from hex string

extension Color {
    init(hex: String) {
        let hex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        let a, r, g, b: UInt64
        switch hex.count {
        case 3:
            (a, r, g, b) = (255, (int >> 8) * 17, (int >> 4 & 0xF) * 17, (int & 0xF) * 17)
        case 6:
            (a, r, g, b) = (255, int >> 16, int >> 8 & 0xFF, int & 0xFF)
        case 8:
            (a, r, g, b) = (int >> 24, int >> 16 & 0xFF, int >> 8 & 0xFF, int & 0xFF)
        default:
            (a, r, g, b) = (255, 255, 255, 255)
        }
        self.init(
            .sRGB,
            red:     Double(r) / 255,
            green:   Double(g) / 255,
            blue:    Double(b) / 255,
            opacity: Double(a) / 255
        )
    }
}

// MARK: - Reusable styled components

/// A primary-styled action button.
struct PrimaryButton: View {
    let title: String
    let isLoading: Bool
    let isDisabled: Bool
    let action: () -> Void

    init(title: String, isLoading: Bool = false, isDisabled: Bool = false, action: @escaping () -> Void) {
        self.title = title
        self.isLoading = isLoading
        self.isDisabled = isDisabled
        self.action = action
    }

    var body: some View {
        Button(action: action) {
            ZStack {
                if isLoading {
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle(tint: .white))
                } else {
                    Text(title)
                        .font(.system(size: 17, weight: .bold))
                        .foregroundColor(.white)
                }
            }
            .frame(maxWidth: .infinity)
            .frame(height: 52)
            .background(
                isDisabled
                    ? LinearGradient(colors: [Color(white: 0.2), Color(white: 0.15)], startPoint: .leading, endPoint: .trailing)
                    : AppTheme.primaryGradient
            )
            .cornerRadius(AppTheme.cornerRadiusMedium)
            .shadow(color: isDisabled ? .clear : AppTheme.primary.opacity(0.4), radius: 10, x: 0, y: 5)
        }
        .disabled(isDisabled || isLoading)
    }
}

/// A destructive-red action button.
struct DestructiveButton: View {
    let title: String
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.system(size: 15, weight: .semibold))
                .foregroundColor(AppTheme.error)
                .frame(maxWidth: .infinity)
                .frame(height: 44)
                .background(AppTheme.error.opacity(0.1))
                .cornerRadius(AppTheme.cornerRadiusSmall)
                .overlay(
                    RoundedRectangle(cornerRadius: AppTheme.cornerRadiusSmall)
                        .stroke(AppTheme.error.opacity(0.3), lineWidth: 1)
                )
        }
    }
}

/// Section header used in lists.
struct SectionHeader: View {
    let title: String

    var body: some View {
        Text(title.uppercased())
            .font(.system(size: 11, weight: .bold))
            .foregroundColor(AppTheme.textTertiary)
            .kerning(1.2)
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.horizontal, 4)
    }
}

/// Badge pill for labels (muscle group, equipment).
struct BadgePill: View {
    let text: String
    var color: Color = AppTheme.primary

    var body: some View {
        Text(text)
            .font(.system(size: 11, weight: .semibold))
            .foregroundColor(color)
            .padding(.horizontal, 8)
            .padding(.vertical, 3)
            .background(color.opacity(0.15))
            .cornerRadius(20)
    }
}
