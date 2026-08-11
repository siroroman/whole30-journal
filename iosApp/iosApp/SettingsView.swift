import SwiftUI
import SharedKit

struct SettingsView: View {
    @Environment(\.dismiss) var dismiss
    private let viewModel: SettingsViewModel = KoinResolver.get(SettingsViewModel.self)

    var body: some View {
        ComposeViewController {
            SettingsScreenViewController(viewModel: viewModel)
        }
        .ignoresSafeArea()
        .task {
            for await event in viewModel.outputEvents {
                switch onEnum(of: event) {
                    case .saved, .cancelled:
                    dismiss()
                }
            }
        }
    }
}
