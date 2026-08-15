import SwiftUI
import SharedKit

struct DayDetailView: View {
    let dayNumber: Int
    let onEditRequested: (Int) -> Void

    @Environment(\.dismiss) private var dismiss
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
                    onEditRequested(Int(data.dayNumber))
                }
            }
        }
    }
}
