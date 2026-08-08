import SwiftUI

extension Int: Identifiable {
    public var id: Int { self }
}

struct ContentView: View {
    @State private var editingDay: Int?

    var body: some View {
        HomeView(onEditDay: { day in editingDay = day })
            .sheet(item: $editingDay) { day in
                DayEntryView(dayNumber: day, onClose: { editingDay = nil })
            }
    }
}
