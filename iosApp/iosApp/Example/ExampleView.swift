import SharedKit
import SwiftUI

struct ExampleView: View {
    let viewModel: ExampleViewModel

    var body: some View {
        ComposeViewController {
            ExampleScreenViewController(viewModel: viewModel)
        }
        .onAppear {
            viewModel.onUiAction(uiAction: ExampleContractUiActionOnAppear())
        }
        .ignoresSafeArea(edges: .bottom)
    }
}
