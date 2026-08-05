import SwiftUI
import SharedKit

struct HomeView: View {
    @State private var viewModel = KoinResolver.get(HomeViewModel.self)

    var body: some View {
        ComposeViewController {
            HomeScreenViewController(viewModel: viewModel)
        }
        .ignoresSafeArea()
    }
}
