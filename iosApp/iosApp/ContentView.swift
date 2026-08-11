import SwiftUI
import SharedKit

struct ContentView: View {
    @State private var showingSettings = false

    var body: some View {
        HomeView(onOpenSettings: { showingSettings = true })
            .sheet(isPresented: $showingSettings) {
                SettingsView()
            }
    }
}
