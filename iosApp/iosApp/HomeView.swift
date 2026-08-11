import SwiftUI
import SharedKit

extension Int: @retroactive Identifiable {
    public var id: Int { self }
}

struct HomeView: View {
    let viewModel: HomeViewModel
    let onOpenSettings: () -> Void
    @State private var editingDay: Int?

    var body: some View {
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
