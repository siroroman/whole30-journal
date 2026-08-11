import SwiftUI
import SharedKit

struct DayEntryView: View {
    let dayNumber: Int
    let onClose: () -> Void

    private let viewModel: DayEntryViewModel = KoinResolver.get(DayEntryViewModel.self)

    var body: some View {
        ComposeViewController {
            DayEntryScreenViewController(viewModel: viewModel)
        }
        .ignoresSafeArea()
        .onAppear {
            viewModel.onUiAction(uiAction: DayEntryContractUiActionOnAppear(dayNumber: Int32(dayNumber)))
        }
        .task {
            for await event in viewModel.outputEvents {
                switch onEnum(of: event) {
                case .close:
                    onClose()
                }
            }
        }
    }
}
