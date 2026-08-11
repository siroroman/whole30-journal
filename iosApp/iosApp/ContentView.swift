import SwiftUI
import SharedKit

private struct SettingsRoute: Hashable {
    let id = UUID()
}

struct ContentView: View {
    @State private var path: [SettingsRoute] = []

    var body: some View {
        NavigationStack(path: $path) {
            HomeView(onOpenSettings: { path = [SettingsRoute()] })
                .toolbar(.hidden, for: .navigationBar)
                .navigationDestination(for: SettingsRoute.self) { _ in
                    SettingsView()
                        .toolbar(.hidden, for: .navigationBar)
                }
        }
    }
}
