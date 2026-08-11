import SwiftUI
import SharedKit

private final class SettingsViewModelHolder {
    let viewModel: SettingsViewModel

    init(viewModel: SettingsViewModel) {
        self.viewModel = viewModel
    }

    deinit {
        let viewModel = self.viewModel
        Task { @MainActor in viewModel.clearScope() }
    }
}

struct SettingsView: View {
    let onDone: () -> Void
    @State private var holder = SettingsViewModelHolder(
        viewModel: KoinResolver.get(SettingsViewModel.self)
    )

    private var viewModel: SettingsViewModel { holder.viewModel }

    var body: some View {
        ComposeViewController {
            SettingsScreenViewController(viewModel: viewModel)
        }
        .ignoresSafeArea()
        .task {
            for await event in viewModel.outputEvents {
                switch onEnum(of: event) {
                case .saved, .cancelled:
                    onDone()
                }
            }
        }
    }
}
