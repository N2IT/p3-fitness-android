import SwiftUI
import SwiftData
import Charts

struct BodyWeightView: View {
    let userId: Int
    let unitPreference: String
    @State private var viewModel = ProgressViewModel()
    @Environment(\.modelContext) private var modelContext

    @State private var weightInput: String = ""
    @State private var isLogging: Bool = false
    @State private var showSuccessBanner: Bool = false

    private var unitLabel: String { unitPreference == "kg" ? "kg" : "lbs" }

    var body: some View {
        ZStack {
            Color(hex: "#0A0A0A").ignoresSafeArea()

            ScrollView {
                VStack(spacing: 20) {
                    // Log section
                    logSection

                    // Chart section
                    if !viewModel.bodyWeightEntries.isEmpty {
                        bodyWeightChartSection
                    }

                    // Recent entries list
                    if !viewModel.bodyWeightEntries.isEmpty {
                        recentEntriesSection
                    } else {
                        emptyStateView
                    }

                    Color.clear.frame(height: 20)
                }
                .padding(.top, 16)
            }

            // Success banner
            if showSuccessBanner {
                VStack {
                    Spacer()
                    HStack(spacing: 10) {
                        Image(systemName: "checkmark.circle.fill")
                            .foregroundColor(Color(hex: "#00CC66"))
                        Text("Weight logged!")
                            .font(.system(size: 15, weight: .semibold))
                            .foregroundColor(.white)
                    }
                    .padding(.horizontal, 24)
                    .padding(.vertical, 14)
                    .background(Color(hex: "#1E1E1E"))
                    .clipShape(Capsule())
                    .shadow(color: .black.opacity(0.4), radius: 12, y: 4)
                    .padding(.bottom, 40)
                    .transition(.move(edge: .bottom).combined(with: .opacity))
                }
                .ignoresSafeArea()
            }
        }
        .navigationTitle("Body Weight")
        .navigationBarTitleDisplayMode(.large)
        .onAppear {
            Task {
                await viewModel.loadBodyWeightEntries(userId: userId, context: modelContext)
            }
        }
    }

    // MARK: - Log Section

    private var logSection: some View {
        VStack(alignment: .leading, spacing: 14) {
            Text("LOG WEIGHT")
                .font(.system(size: 11, weight: .semibold))
                .foregroundColor(Color(white: 0.4))
                .tracking(2)

            HStack(spacing: 12) {
                HStack(spacing: 10) {
                    Image(systemName: "scalemass.fill")
                        .foregroundColor(Color(hex: "#0055FF"))
                        .font(.system(size: 16))

                    TextField("", text: $weightInput, prompt: Text("0.0")
                        .foregroundColor(Color(white: 0.3)))
                        .foregroundColor(.white)
                        .font(.system(size: 20, weight: .semibold, design: .monospaced))
                        .keyboardType(.decimalPad)

                    Text(unitLabel)
                        .font(.system(size: 16, weight: .medium))
                        .foregroundColor(Color(white: 0.5))
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 14)
                .background(Color(hex: "#141414"))
                .clipShape(RoundedRectangle(cornerRadius: 12))
                .overlay(
                    RoundedRectangle(cornerRadius: 12)
                        .stroke(weightInput.isEmpty ? Color(white: 0.15) : Color(hex: "#0055FF").opacity(0.4), lineWidth: 1)
                )

                Button {
                    logWeight()
                } label: {
                    if isLogging {
                        ProgressView()
                            .tint(.white)
                            .scaleEffect(0.9)
                            .frame(width: 70, height: 48)
                    } else {
                        Text("Log")
                            .font(.system(size: 16, weight: .bold))
                            .foregroundColor(.white)
                            .frame(width: 70, height: 48)
                            .background(
                                weightInput.isEmpty
                                    ? Color(white: 0.2)
                                    : LinearGradient(
                                        colors: [Color(hex: "#0055FF"), Color(hex: "#0033CC")],
                                        startPoint: .leading,
                                        endPoint: .trailing
                                    )
                            )
                            .clipShape(RoundedRectangle(cornerRadius: 12))
                    }
                }
                .disabled(weightInput.isEmpty || isLogging)
            }

            // Current / trend stats
            if !viewModel.bodyWeightEntries.isEmpty {
                HStack(spacing: 0) {
                    if let latest = viewModel.bodyWeightEntries.first {
                        WeightStatPill(label: "Current", value: formatWeight(latest.weight), unit: unitLabel)
                    }
                    if viewModel.bodyWeightEntries.count >= 2 {
                        Divider().frame(width: 1, height: 28).background(Color(white: 0.1))
                        let delta = (viewModel.bodyWeightEntries.first?.weight ?? 0) - (viewModel.bodyWeightEntries.last?.weight ?? 0)
                        WeightStatPill(
                            label: "Change",
                            value: (delta >= 0 ? "+" : "") + formatWeight(delta),
                            unit: unitLabel,
                            color: delta < 0 ? Color(hex: "#00CC66") : Color(hex: "#FF6600")
                        )
                    }
                    if let min = viewModel.bodyWeightEntries.map(\.weight).min(),
                       let max = viewModel.bodyWeightEntries.map(\.weight).max() {
                        Divider().frame(width: 1, height: 28).background(Color(white: 0.1))
                        WeightStatPill(label: "Low/High", value: "\(formatWeight(min))/\(formatWeight(max))", unit: unitLabel)
                    }
                }
                .padding(12)
                .background(Color(hex: "#141414"))
                .clipShape(RoundedRectangle(cornerRadius: 12))
                .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color(white: 0.1), lineWidth: 1))
            }
        }
        .padding(16)
        .background(Color(hex: "#141414"))
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(Color(white: 0.1), lineWidth: 1))
        .padding(.horizontal, 16)
    }

    // MARK: - Chart Section

    private var bodyWeightChartSection: some View {
        VStack(alignment: .leading, spacing: 14) {
            Text("WEIGHT OVER TIME")
                .font(.system(size: 11, weight: .semibold))
                .foregroundColor(Color(white: 0.4))
                .tracking(2)

            let entries = viewModel.bodyWeightEntries.sorted { $0.loggedAt < $1.loggedAt }

            Chart(entries) { entry in
                AreaMark(
                    x: .value("Date", entry.loggedAt),
                    y: .value("Weight", entry.weight)
                )
                .foregroundStyle(
                    LinearGradient(
                        colors: [Color(hex: "#FF6600").opacity(0.3), Color(hex: "#FF6600").opacity(0.03)],
                        startPoint: .top,
                        endPoint: .bottom
                    )
                )
                .interpolationMethod(.catmullRom)

                LineMark(
                    x: .value("Date", entry.loggedAt),
                    y: .value("Weight", entry.weight)
                )
                .foregroundStyle(Color(hex: "#FF6600"))
                .lineStyle(StrokeStyle(lineWidth: 2.5))
                .interpolationMethod(.catmullRom)

                PointMark(
                    x: .value("Date", entry.loggedAt),
                    y: .value("Weight", entry.weight)
                )
                .foregroundStyle(Color(hex: "#FF6600"))
                .symbolSize(25)
            }
            .frame(height: 180)
            .chartXAxis {
                AxisMarks(values: .stride(by: .month)) { _ in
                    AxisGridLine(stroke: StrokeStyle(lineWidth: 0.5))
                        .foregroundStyle(Color(white: 0.1))
                    AxisValueLabel(format: .dateTime.month(.abbreviated))
                        .foregroundStyle(Color(white: 0.4))
                }
            }
            .chartYAxis {
                AxisMarks { _ in
                    AxisGridLine(stroke: StrokeStyle(lineWidth: 0.5, dash: [4, 4]))
                        .foregroundStyle(Color(white: 0.1))
                    AxisValueLabel()
                        .foregroundStyle(Color(white: 0.4))
                }
            }
            .chartPlotStyle { plot in
                plot.background(Color.clear)
            }
        }
        .padding(16)
        .background(Color(hex: "#141414"))
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(Color(white: 0.1), lineWidth: 1))
        .padding(.horizontal, 16)
    }

    // MARK: - Recent Entries

    private var recentEntriesSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text("RECENT ENTRIES")
                    .font(.system(size: 11, weight: .semibold))
                    .foregroundColor(Color(white: 0.4))
                    .tracking(2)
                Spacer()
                Text("\(viewModel.bodyWeightEntries.count) entries")
                    .font(.system(size: 11))
                    .foregroundColor(Color(white: 0.3))
            }
            .padding(.horizontal, 16)

            LazyVStack(spacing: 8) {
                ForEach(viewModel.bodyWeightEntries.prefix(20)) { entry in
                    BodyWeightEntryRow(entry: entry, unitLabel: unitLabel) {
                        Task {
                            await viewModel.deleteBodyWeightEntry(id: entry.id, context: modelContext)
                        }
                    }
                    .padding(.horizontal, 16)
                }
            }
        }
    }

    // MARK: - Empty State

    private var emptyStateView: some View {
        VStack(spacing: 20) {
            ZStack {
                Circle()
                    .fill(Color(hex: "#141414"))
                    .frame(width: 90, height: 90)
                Image(systemName: "scalemass")
                    .font(.system(size: 36))
                    .foregroundColor(Color(white: 0.2))
            }
            VStack(spacing: 8) {
                Text("No Entries Yet")
                    .font(.system(size: 20, weight: .bold))
                    .foregroundColor(.white)
                Text("Log your weight above to start\ntracking your progress")
                    .font(.system(size: 14))
                    .foregroundColor(Color(white: 0.4))
                    .multilineTextAlignment(.center)
            }
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 40)
    }

    // MARK: - Helpers

    private func logWeight() {
        guard let weight = Double(weightInput.replacingOccurrences(of: ",", with: ".")),
              weight > 0 else { return }
        isLogging = true
        Task {
            await viewModel.logBodyWeight(userId: userId, weight: weight, context: modelContext)
            weightInput = ""
            isLogging = false
            withAnimation(.spring(response: 0.4, dampingFraction: 0.8)) {
                showSuccessBanner = true
            }
            try? await Task.sleep(nanoseconds: 2_000_000_000)
            withAnimation {
                showSuccessBanner = false
            }
        }
    }

    private func formatWeight(_ w: Double) -> String {
        String(format: "%.1f", w)
    }
}

// MARK: - Weight Stat Pill

struct WeightStatPill: View {
    let label: String
    let value: String
    let unit: String
    var color: Color = .white

    var body: some View {
        VStack(spacing: 3) {
            Text(label)
                .font(.system(size: 10))
                .foregroundColor(Color(white: 0.4))
            HStack(spacing: 3) {
                Text(value)
                    .font(.system(size: 14, weight: .bold, design: .monospaced))
                    .foregroundColor(color)
                Text(unit)
                    .font(.system(size: 10))
                    .foregroundColor(Color(white: 0.4))
            }
        }
        .frame(maxWidth: .infinity)
    }
}

// MARK: - Body Weight Entry Row

struct BodyWeightEntryRow: View {
    let entry: BodyWeightEntry
    let unitLabel: String
    let onDelete: () -> Void

    var body: some View {
        HStack {
            VStack(alignment: .leading, spacing: 3) {
                Text(formattedDate(entry.loggedAt))
                    .font(.system(size: 15, weight: .medium))
                    .foregroundColor(.white)
                Text(relativeDate(entry.loggedAt))
                    .font(.system(size: 12))
                    .foregroundColor(Color(white: 0.4))
            }
            Spacer()
            HStack(spacing: 4) {
                Text(String(format: "%.1f", entry.weight))
                    .font(.system(size: 18, weight: .bold, design: .monospaced))
                    .foregroundColor(Color(hex: "#FF6600"))
                Text(unitLabel)
                    .font(.system(size: 13))
                    .foregroundColor(Color(white: 0.5))
            }
        }
        .padding(.horizontal, 14)
        .padding(.vertical, 12)
        .background(Color(hex: "#141414"))
        .clipShape(RoundedRectangle(cornerRadius: 12))
        .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color(white: 0.08), lineWidth: 1))
        .swipeActions(edge: .trailing, allowsFullSwipe: true) {
            Button(role: .destructive, action: onDelete) {
                Label("Delete", systemImage: "trash")
            }
        }
    }

    private func formattedDate(_ date: Date) -> String {
        let fmt = DateFormatter()
        fmt.dateStyle = .medium
        return fmt.string(from: date)
    }

    private func relativeDate(_ date: Date) -> String {
        let fmt = RelativeDateTimeFormatter()
        fmt.unitsStyle = .short
        return fmt.localizedString(for: date, relativeTo: Date())
    }
}

#Preview {
    NavigationStack {
        BodyWeightView(userId: 1, unitPreference: "lbs")
    }
    .modelContainer(for: [], inMemory: true)
}
