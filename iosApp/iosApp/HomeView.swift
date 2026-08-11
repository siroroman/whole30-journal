import SwiftUI
import SharedKit

struct HomeView: View {
    let viewModel: HomeViewModel
    let onEditDay: (Int) -> Void
    let onOpenSettings: () -> Void

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
                case .navigateToSettings:
                    onOpenSettings()
                }
            }
        }
    }
}
