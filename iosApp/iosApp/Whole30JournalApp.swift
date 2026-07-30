import SwiftUI

@main
struct Whole30JournalApp: App {
    init() {
        KoinStarter.start()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
