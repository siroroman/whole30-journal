import SwiftUI

struct ContentView: View {
    @State private var editingDay: Int?

    var body: some View {
        HomeView(onEditDay: { day in editingDay = day })
            .sheet(isPresented: Binding(
                get: { editingDay != nil },
                set: { isPresented in if !isPresented { editingDay = nil } }
            )) {
                if let day = editingDay {
                    DayEntryView(dayNumber: day, onClose: { editingDay = nil })
                }
            }
    }
}
