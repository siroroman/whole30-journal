import SwiftUI
import SharedKit

struct ContentView: View {
    @State private var exampleViewModel = KoinResolver.get(ExampleViewModel.self)
    var body: some View {
        ExampleView(viewModel: exampleViewModel)
    }
}
