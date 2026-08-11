import SwiftUI
import SharedKit

extension Int: Identifiable {
    public var id: Int { self }
}

struct HomeView: View {
    private let viewModel = KoinResolver.get(HomeViewModel.self)
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
                }
            }
        }
        .sheet(item: $editingDay) { day in
            DayEntryView(dayNumber: day, onClose: { editingDay = nil })
        }
    }
}
