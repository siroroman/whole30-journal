import SwiftUI
import SharedKit

private enum HomeRoute: Hashable {
    case settings
    case dayDetail(Int)
    case dayEntry(Int)
}

struct HomeView: View {
    @State private var isLoading = true
    @State private var needsSetup = false
    @State private var path: [HomeRoute] = []

    private let viewModel: HomeViewModel = KoinResolver.get(HomeViewModel.self)

    var body: some View {
        Group {
            if isLoading {
                ProgressView()
            } else if needsSetup {
                SettingsView()
            } else {
                homeContent
            }
        }
        .task {
            for await state in viewModel.state {
                if isLoading != state.isLoading {
                    isLoading = state.isLoading
                }
                if needsSetup != state.uiData.needsSetup {
                    needsSetup = state.uiData.needsSetup
                }
            }
        }
    }

    private var homeContent: some View {
        NavigationStack(path: $path) {
            ComposeViewController {
                HomeScreenViewController(viewModel: viewModel)
            }
            .ignoresSafeArea()
            .interactivePopGestureEnabled()
            .navigationDestination(for: HomeRoute.self) { route in
                switch route {
                case .settings:
                    SettingsView()
                        .toolbar(.hidden, for: .navigationBar)
                case let .dayDetail(dayNumber):
                    DayDetailView(dayNumber: dayNumber, onEditRequested: { path.append(.dayEntry($0)) })
                        .toolbar(.hidden, for: .navigationBar)
                case let .dayEntry(dayNumber):
                    DayEntryView(dayNumber: dayNumber)
                        .toolbar(.hidden, for: .navigationBar)
                }
            }
            .task {
                for await event in viewModel.outputEvents {
                    switch onEnum(of: event) {
                    case let .navigateToDayEntry(data):
                        path.append(.dayEntry(Int(data.dayNumber)))
                    case let .navigateToDayDetail(data):
                        path.append(.dayDetail(Int(data.dayNumber)))
                    case .navigateToSettings:
                        path = [.settings]
                    }
                }
            }
        }
    }
}
