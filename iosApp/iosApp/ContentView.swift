import SwiftUI
import SharedKit

private struct SettingsRoute: Hashable {
    let id = UUID()
}

struct ContentView: View {
    private let homeViewModel = KoinResolver.get(HomeViewModel.self)
    private let settingsViewModel = KoinResolver.get(SettingsViewModel.self)
    @State private var isLoading = true
    @State private var needsSetup = false
    @State private var path: [SettingsRoute] = []

    var body: some View {
        NavigationStack(path: $path) {
            Group {
                if isLoading {
                    ProgressView()
                } else if needsSetup {
                    SettingsView(viewModel: settingsViewModel)
                } else {
                    HomeView(
                        viewModel: homeViewModel,
                        onOpenSettings: { path = [SettingsRoute()] }
                    )
                }
            }
            .toolbar(.hidden, for: .navigationBar)
            .navigationDestination(for: SettingsRoute.self) { _ in
                SettingsView(viewModel: settingsViewModel)
                    .toolbar(.hidden, for: .navigationBar)
            }
        }
        .task {
            for await state in homeViewModel.state {
                if isLoading != state.isLoading {
                    isLoading = state.isLoading
                }
                if needsSetup != state.uiData.needsSetup {
                    needsSetup = state.uiData.needsSetup
                }
            }
        }
    }
}
