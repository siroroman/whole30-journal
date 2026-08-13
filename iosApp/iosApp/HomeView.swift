import SwiftUI
import SharedKit

extension Int: @retroactive Identifiable {
    public var id: Int { self }
}

private struct SettingsNavigation: Hashable {}

struct HomeView: View {
    @State private var isLoading = true
    @State private var needsSetup = false
    @State private var editingDay: Int?
    @State private var settingsPath: [SettingsNavigation] = []
    
    private let viewModel: HomeViewModel = KoinResolver.get(HomeViewModel.self)

    var body: some View {
        Group {
            if isLoading {
                SplashView()
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
        NavigationStack(path: $settingsPath) {
            ComposeViewController {
                HomeScreenViewController(viewModel: viewModel)
            }
            .ignoresSafeArea()
            .navigationDestination(for: SettingsNavigation.self) { _ in
                SettingsView()
                    .toolbar(.hidden, for: .navigationBar)
            }
            .task {
                for await event in viewModel.outputEvents {
                    switch onEnum(of: event) {
                    case let .navigateToDayEntry(data):
                        editingDay = Int(data.dayNumber)
                    case .navigateToSettings:
                        settingsPath = [SettingsNavigation()]
                    }
                }
            }
            .sheet(item: $editingDay) { day in
                DayEntryView(dayNumber: day, onClose: { editingDay = nil })
            }
        }
    }
}
