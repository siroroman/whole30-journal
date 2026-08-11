import SwiftUI
import SharedKit

struct SettingsView: View {
    let viewModel: SettingsViewModel

    var body: some View {
        ComposeViewController {
            SettingsScreenViewController(viewModel: viewModel)
        }
        .ignoresSafeArea()
    }
}
