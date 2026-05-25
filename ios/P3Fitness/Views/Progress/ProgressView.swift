import SwiftUI
import SwiftData
import Charts

struct ProgressView: View {
    let userId: Int
    @State private var viewModel = ProgressViewModel()
    @Environment(\.modelContext) private var modelContext

    @State private var showExercisePicker: Bool = false
    @State private var showBodyWeight: Bool = false

    var body: some View {
        ZStack {
            Color(hex: "#0A0A0A").ignoresSafeArea()

            ScrollView {
                VStack(spacing: 20) {
                    // Exercise selector
                    exerciseSelectorButton

                    if viewModel.selectedExercise != nil {
                        // Metric picker
                        metricPicker

                        // Time range picker
                        timeRangePicker

                        // Chart
                        chartSection

                        // Personal Records section
                        if !viewModel.personalRecords.isEmpty {
                            personalRecordsSection
                        }
                    } else {
                        noExerciseSelectedView
                    }

                    // Body Weight button
                    NavigationLink(destination: BodyWeightView(userId: userId, unitPreference: viewModel.unitPreference)) {
                        HStack(spacing: 12) {
                            Image(systemName: "figure.walk")
                                .font(.system(size: 18))
                                .foregroundColor(Color(hex: "#FF6600"))
                            Text("Body Weight Tracker")
                                .font(.system(size: 16, weight: .semibold))
                                .foregroundColor(.white)
                            Spacer()
                            Image(systemName: "chevron.right")
                                .font(.system(size: 13))
                                .foregroundColor(Color(white: 0.3))
                        }
                        .padding(.horizontal, 16)
                        .padding(.vertical, 14)
                        .background(Color(hex: "#141414"))
                        .clipShape(RoundedRectangle(cornerRadius: 14))
                        .overlay(RoundedRectangle(cornerRadius: 14).stroke(Color(white: 0.1), lineWidth: 1))
                    }
                    .buttonStyle(PlainButtonStyle())
                    .padding(.horizontal, 16)

                    Color.clear.frame(height: 20)
                }
                .padding(.top, 16)
            }
        }
        .navigationTitle("Progress")
        .navigationBarTitleDisplayMode(.large)
        .sheet(isPresented: $showExercisePicker) {
            ExerciseProgressPicker(
                exercises: viewModel.allExercises,
                selected: viewModel.selectedExercise,
                onSelect: { exercise in
                    Task {
                        await viewModel.selectExercise(exercise, context: modelContext)
                    }
                    showExercisePicker = false
                }
            )
        }
        .onAppear {
            Task {
                await viewModel.loadData(userId: userId, context: modelContext)
            }
        }
    }

    // MARK: - Exercise Selector

    private var exerciseSelectorButton: some View {
        Button {
            showExercisePicker = true
        } label: {
            HStack(spacing: 12) {
                Image(systemName: "dumbbell.fill")
                    .font(.system(size: 16))
                    .foregroundColor(Color(hex: "#0055FF"))

                if let ex = viewModel.selectedExercise {
                    Text(ex.name)
                        .font(.system(size: 16, weight: .semibold))
                        .foregroundColor(.white)
                } else {
                    Text("Select Exercise")
                        .font(.system(size: 16, weight: .medium))
                        .foregroundColor(Color(white: 0.4))
                }

                Spacer()

                Image(systemName: "chevron.up.chevron.down")
                    .font(.system(size: 13))
                    .foregroundColor(Color(white: 0.4))
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 14)
            .background(Color(hex: "#141414"))
            .clipShape(RoundedRectangle(cornerRadius: 14))
            .overlay(
                RoundedRectangle(cornerRadius: 14)
                    .stroke(Color(hex: "#0055FF").opacity(0.3), lineWidth: 1)
            )
        }
        .buttonStyle(PlainButtonStyle())
        .padding(.horizontal, 16)
    }

    // MARK: - Metric Picker

    private var metricPicker: some View {
        HStack(spacing: 1) {
            ForEach(ProgressMetric.allCases, id: \.self) { metric in
                Button {
                    withAnimation(.easeInOut(duration: 0.2)) {
                        viewModel.selectedMetric = metric
                    }
                    Task {
                        await viewModel.reloadChart(context: modelContext)
                    }
                } label: {
                    Text(metric.rawValue)
                        .font(.system(size: 13, weight: viewModel.selectedMetric == metric ? .semibold : .regular))
                        .foregroundColor(viewModel.selectedMetric == metric ? .white : Color(white: 0.5))
                        .frame(maxWidth: .infinity)
                        .frame(height: 36)
                        .background(
                            viewModel.selectedMetric == metric
                                ? LinearGradient(colors: [Color(hex: "#0055FF"), Color(hex: "#0044CC")], startPoint: .leading, endPoint: .trailing)
                                : LinearGradient(colors: [Color.clear, Color.clear], startPoint: .leading, endPoint: .trailing)
                        )
                        .clipShape(RoundedRectangle(cornerRadius: 8))
                }
                .buttonStyle(PlainButtonStyle())
            }
        }
        .padding(4)
        .background(Color(hex: "#141414"))
        .clipShape(RoundedRectangle(cornerRadius: 12))
        .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color(white: 0.1), lineWidth: 1))
        .padding(.horizontal, 16)
    }

    // MARK: - Time Range Picker

    private var timeRangePicker: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                ForEach(TimeRange.allCases, id: \.self) { range in
                    Button {
                        withAnimation(.easeInOut(duration: 0.2)) {
                            viewModel.selectedTimeRange = range
                        }
                        Task {
                            await viewModel.reloadChart(context: modelContext)
                        }
                    } label: {
                        Text(range.rawValue)
                            .font(.system(size: 14, weight: viewModel.selectedTimeRange == range ? .bold : .regular))
                            .foregroundColor(viewModel.selectedTimeRange == range ? .white : Color(white: 0.5))
                            .frame(minWidth: 44)
                            .padding(.horizontal, 16)
                            .padding(.vertical, 8)
                            .background(
                                viewModel.selectedTimeRange == range
                                    ? LinearGradient(colors: [Color(hex: "#0055FF"), Color(hex: "#0044CC")], startPoint: .leading, endPoint: .trailing)
                                    : LinearGradient(colors: [Color(hex: "#1E1E1E"), Color(hex: "#1E1E1E")], startPoint: .leading, endPoint: .trailing)
                            )
                            .clipShape(Capsule())
                    }
                    .buttonStyle(PlainButtonStyle())
                }
            }
            .padding(.horizontal, 16)
        }
    }

    // MARK: - Chart Section

    private var chartSection: some View {
        VStack(alignment: .leading, spacing: 14) {
            HStack {
                VStack(alignment: .leading, spacing: 2) {
                    Text(viewModel.selectedMetric.rawValue)
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(.white)
                    if let latest = viewModel.chartData.last {
                        Text(formatValue(latest.value))
                            .font(.system(size: 28, weight: .black, design: .monospaced))
                            .foregroundColor(Color(hex: "#0055FF"))
                    }
                }
                Spacer()
                if let change = viewModel.trendChange {
                    HStack(spacing: 4) {
                        Image(systemName: change >= 0 ? "arrow.up.right" : "arrow.down.right")
                            .font(.system(size: 12, weight: .bold))
                        Text(String(format: "%.1f%%", abs(change)))
                            .font(.system(size: 13, weight: .bold))
                    }
                    .foregroundColor(change >= 0 ? Color(hex: "#00CC66") : Color(hex: "#FF3333"))
                    .padding(.horizontal, 10)
                    .padding(.vertical, 5)
                    .background(
                        (change >= 0 ? Color(hex: "#00CC66") : Color(hex: "#FF3333")).opacity(0.1)
                    )
                    .clipShape(Capsule())
                }
            }

            if viewModel.isLoadingChart {
                HStack {
                    Spacer()
                    ProgressView()
                        .tint(Color(hex: "#0055FF"))
                    Spacer()
                }
                .frame(height: 200)
            } else if viewModel.chartData.isEmpty {
                VStack(spacing: 12) {
                    Image(systemName: "chart.line.downtrend.xyaxis")
                        .font(.system(size: 36))
                        .foregroundColor(Color(white: 0.2))
                    Text("Log some workouts to see progress")
                        .font(.system(size: 14))
                        .foregroundColor(Color(white: 0.4))
                        .multilineTextAlignment(.center)
                }
                .frame(height: 200)
                .frame(maxWidth: .infinity)
            } else {
                progressLineChart
            }
        }
        .padding(16)
        .background(Color(hex: "#141414"))
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(Color(white: 0.1), lineWidth: 1))
        .padding(.horizontal, 16)
    }

    @ViewBuilder
    private var progressLineChart: some View {
        Chart {
            ForEach(viewModel.chartData) { point in
                AreaMark(
                    x: .value("Date", point.date),
                    y: .value("Value", point.value)
                )
                .foregroundStyle(
                    LinearGradient(
                        colors: [
                            Color(hex: "#0055FF").opacity(0.35),
                            Color(hex: "#0055FF").opacity(0.05)
                        ],
                        startPoint: .top,
                        endPoint: .bottom
                    )
                )
                .interpolationMethod(.catmullRom)

                LineMark(
                    x: .value("Date", point.date),
                    y: .value("Value", point.value)
                )
                .foregroundStyle(Color(hex: "#0055FF"))
                .lineStyle(StrokeStyle(lineWidth: 2.5))
                .interpolationMethod(.catmullRom)

                PointMark(
                    x: .value("Date", point.date),
                    y: .value("Value", point.value)
                )
                .foregroundStyle(Color(hex: "#0055FF"))
                .symbolSize(30)
            }
        }
        .frame(height: 200)
        .chartXAxis {
            AxisMarks(values: .stride(by: .month)) { _ in
                AxisGridLine(stroke: StrokeStyle(lineWidth: 0.5))
                    .foregroundStyle(Color(white: 0.1))
                AxisValueLabel(format: .dateTime.month(.abbreviated))
                    .foregroundStyle(Color(white: 0.4))
            }
        }
        .chartYAxis {
            AxisMarks { value in
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

    // MARK: - Personal Records

    private var personalRecordsSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("PERSONAL RECORDS")
                .font(.system(size: 11, weight: .semibold))
                .foregroundColor(Color(white: 0.4))
                .tracking(2)
                .padding(.horizontal, 16)

            VStack(spacing: 10) {
                ForEach(viewModel.personalRecords) { pr in
                    PRRow(pr: pr)
                }
            }
            .padding(.horizontal, 16)
        }
    }

    // MARK: - No Exercise Selected

    private var noExerciseSelectedView: some View {
        VStack(spacing: 20) {
            ZStack {
                Circle()
                    .fill(Color(hex: "#141414"))
                    .frame(width: 90, height: 90)
                Image(systemName: "chart.line.uptrend.xyaxis")
                    .font(.system(size: 36))
                    .foregroundColor(Color(white: 0.2))
            }

            VStack(spacing: 8) {
                Text("Select an Exercise")
                    .font(.system(size: 20, weight: .bold))
                    .foregroundColor(.white)
                Text("Choose an exercise above to see\nyour progress over time")
                    .font(.system(size: 14))
                    .foregroundColor(Color(white: 0.4))
                    .multilineTextAlignment(.center)
            }
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 40)
    }

    private func formatValue(_ v: Double) -> String {
        v.truncatingRemainder(dividingBy: 1) == 0 ? "\(Int(v))" : String(format: "%.1f", v)
    }
}

// MARK: - PR Row

struct PRRow: View {
    let pr: PersonalRecord

    var body: some View {
        HStack(spacing: 14) {
            ZStack {
                Circle()
                    .fill(Color(hex: "#FF6600").opacity(0.15))
                    .frame(width: 40, height: 40)
                Text("🏆")
                    .font(.system(size: 18))
            }

            VStack(alignment: .leading, spacing: 3) {
                Text(pr.recordType)
                    .font(.system(size: 15, weight: .semibold))
                    .foregroundColor(.white)
                Text(formattedDate(pr.achievedAt))
                    .font(.system(size: 12))
                    .foregroundColor(Color(white: 0.4))
            }

            Spacer()

            Text(formatValue(pr.value))
                .font(.system(size: 18, weight: .bold, design: .monospaced))
                .foregroundColor(Color(hex: "#FF6600"))
        }
        .padding(.horizontal, 14)
        .padding(.vertical, 12)
        .background(Color(hex: "#141414"))
        .clipShape(RoundedRectangle(cornerRadius: 12))
        .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color(hex: "#FF6600").opacity(0.15), lineWidth: 1))
    }

    private func formattedDate(_ date: Date) -> String {
        let fmt = DateFormatter()
        fmt.dateStyle = .medium
        return fmt.string(from: date)
    }

    private func formatValue(_ v: Double) -> String {
        v.truncatingRemainder(dividingBy: 1) == 0 ? "\(Int(v))" : String(format: "%.1f", v)
    }
}

// MARK: - Exercise Progress Picker

struct ExerciseProgressPicker: View {
    let exercises: [Exercise]
    let selected: Exercise?
    let onSelect: (Exercise) -> Void
    @Environment(\.dismiss) private var dismiss
    @State private var searchText: String = ""

    var filtered: [Exercise] {
        exercises.filter { searchText.isEmpty || $0.name.localizedCaseInsensitiveContains(searchText) }
    }

    var body: some View {
        NavigationStack {
            ZStack {
                Color(hex: "#0A0A0A").ignoresSafeArea()

                VStack(spacing: 0) {
                    HStack(spacing: 10) {
                        Image(systemName: "magnifyingglass")
                            .foregroundColor(Color(white: 0.4))
                        TextField("", text: $searchText, prompt: Text("Search exercises...")
                            .foregroundColor(Color(white: 0.3)))
                            .foregroundColor(.white)
                            .autocorrectionDisabled()
                    }
                    .padding(.horizontal, 14)
                    .padding(.vertical, 12)
                    .background(Color(hex: "#141414"))
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                    .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color(white: 0.12), lineWidth: 1))
                    .padding(.horizontal, 16)
                    .padding(.vertical, 12)

                    List(filtered) { exercise in
                        Button {
                            onSelect(exercise)
                        } label: {
                            HStack {
                                VStack(alignment: .leading, spacing: 3) {
                                    Text(exercise.name)
                                        .font(.system(size: 15, weight: .medium))
                                        .foregroundColor(.white)
                                    Text(exercise.muscleGroup)
                                        .font(.system(size: 12))
                                        .foregroundColor(Color(hex: "#0055FF").opacity(0.9))
                                }
                                Spacer()
                                if selected?.id == exercise.id {
                                    Image(systemName: "checkmark.circle.fill")
                                        .foregroundColor(Color(hex: "#0055FF"))
                                }
                            }
                            .contentShape(Rectangle())
                        }
                        .buttonStyle(PlainButtonStyle())
                        .listRowBackground(Color(hex: "#141414"))
                        .listRowSeparatorTint(Color(white: 0.08))
                    }
                    .listStyle(.plain)
                    .scrollContentBackground(.hidden)
                }
            }
            .navigationTitle("Select Exercise")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Cancel") { dismiss() }
                        .foregroundColor(Color(hex: "#0055FF"))
                }
            }
        }
        .preferredColorScheme(.dark)
    }
}

#Preview {
    NavigationStack {
        ProgressView(userId: 1)
    }
    .modelContainer(for: [], inMemory: true)
}
