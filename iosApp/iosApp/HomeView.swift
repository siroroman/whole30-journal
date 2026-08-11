import SwiftUI
import SharedKit

extension Int: @retroactive Identifiable {
    public var id: Int { self }
}

struct HomeView: View {
    private let viewModel: HomeViewModel = KoinResolver.get(HomeViewModel.self)
    @State private var isLoading = true
    @State private var needsSetup = false
    @State private var editingDay: Int?
    let onOpenSettings: () -> Void

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
        ComposeViewController {
            HomeScreenViewController(viewModel: viewModel)
        }
        .ignoresSafeArea()
        .task {
            for await event in viewModel.outputEvents {
                switch onEnum(of: event) {
                case let .navigateToDayEntry(data):
                    editingDay = Int(data.dayNumber)
                case .navigateToSettings:
                    onOpenSettings()
                }
            }
        }
        .sheet(item: $editingDay) { day in
            DayEntryView(dayNumber: day, onClose: { editingDay = nil })
        }
    }
}
