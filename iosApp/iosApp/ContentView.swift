import SwiftUI
import SharedKit

private struct SettingsRoute: Hashable {
    let id = UUID()
}

struct ContentView: View {
    @State private var homeViewModel = KoinResolver.get(HomeViewModel.self)
    @State private var isLoading = true
    @State private var needsSetup = false
    @State private var path: [SettingsRoute] = []

    var body: some View {
        Group {
            if isLoading {
                EmptyView()
            } else if needsSetup {
                SettingsView(onDone: {})
            } else {
                NavigationStack(path: $path) {
                    HomeView(
                        viewModel: homeViewModel,
                        onOpenSettings: { path = [SettingsRoute()] }
                    )
                    .toolbar(.hidden, for: .navigationBar)
                    .navigationDestination(for: SettingsRoute.self) { _ in
                        SettingsView(onDone: { path = [] })
                            .toolbar(.hidden, for: .navigationBar)
                    }
                }
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
