import SwiftUI
import SharedKit

private final class DayEntryViewModelHolder: ObservableObject {
    let viewModel: DayEntryViewModel

    init(viewModel: DayEntryViewModel) {
        self.viewModel = viewModel
    }

    deinit {
        let viewModel = self.viewModel
        Task { @MainActor in viewModel.clearScope() }
    }
}

struct DayEntryView: View {
    let dayNumber: Int
    let onClose: () -> Void
    @StateObject private var holder = DayEntryViewModelHolder(
        viewModel: KoinResolver.get(DayEntryViewModel.self)
    )

    private var viewModel: DayEntryViewModel { holder.viewModel }

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
