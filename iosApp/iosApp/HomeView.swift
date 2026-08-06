import SwiftUI
import SharedKit

struct HomeView: View {
    @State private var viewModel = KoinResolver.get(HomeViewModel.self)
    let onEditDay: (Int) -> Void

    var body: some View {
        ComposeViewController {
            HomeScreenViewController(viewModel: viewModel)
        }
        .ignoresSafeArea()
        .task {
            for await event in viewModel.outputEvents {
                switch onEnum(of: event) {
                case let .navigateToDayEntry(data):
                    onEditDay(Int(data.dayNumber))
                }
            }
        }
    }
}
