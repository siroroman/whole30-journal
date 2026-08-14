import SwiftUI
import SharedKit

struct DayDetailView: View {
    let dayNumber: Int

    @Environment(\.dismiss) private var dismiss
    @State private var editingDay: Int?
    private let viewModel: DayDetailViewModel = KoinResolver.get(DayDetailViewModel.self)

    var body: some View {
        ComposeViewController {
            DayDetailScreenViewController(viewModel: viewModel)
        }
        .ignoresSafeArea()
        .onAppear {
            viewModel.onUiAction(uiAction: DayDetailContractUiActionOnAppear(dayNumber: Int32(dayNumber)))
        }
        .task {
            for await event in viewModel.outputEvents {
                switch onEnum(of: event) {
                case .close:
                    dismiss()
                case let .editRequested(data):
                    editingDay = Int(data.dayNumber)
                }
            }
        }
        .sheet(item: $editingDay) { day in
            DayEntryView(dayNumber: day, onClose: { editingDay = nil })
        }
    }
}
