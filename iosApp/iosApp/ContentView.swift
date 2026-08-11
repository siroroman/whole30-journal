import SwiftUI
import SharedKit

extension Int: Identifiable {
    public var id: Int { self }
}

private struct SettingsRoute: Hashable {
    let id = UUID()
}

struct ContentView: View {
    @State private var homeViewModel = KoinResolver.get(HomeViewModel.self)
    @State private var needsSetup = false
    @State private var editingDay: Int?
    @State private var path: [SettingsRoute] = []

    var body: some View {
        Group {
            if needsSetup {
                SettingsView(onDone: {})
            } else {
                NavigationStack(path: $path) {
                    HomeView(
                        viewModel: homeViewModel,
                        onEditDay: { day in editingDay = day },
                        onOpenSettings: { path = [SettingsRoute()] }
                    )
                    .toolbar(.hidden, for: .navigationBar)
                    .navigationDestination(for: SettingsRoute.self) { _ in
                        SettingsView(onDone: { path = [] })
                            .toolbar(.hidden, for: .navigationBar)
                    }
                }
                .sheet(item: $editingDay) { day in
                    DayEntryView(dayNumber: day, onClose: { editingDay = nil })
                }
            }
        }
        .task {
            for await state in homeViewModel.state {
                needsSetup = state.uiData.needsSetup
            }
        }
    }
}
