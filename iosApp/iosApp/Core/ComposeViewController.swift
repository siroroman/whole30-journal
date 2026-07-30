import SwiftUI

/// Wraps a Kotlin-side `ComposeUIViewController { ... }` factory so shared Compose Multiplatform
/// screens can be embedded directly inside SwiftUI - see ARCHITECTURE.md.
struct ComposeViewController: UIViewControllerRepresentable {
    private let makeScreenViewController: () -> UIViewController

    init(makeScreenViewController: @escaping () -> UIViewController) {
        self.makeScreenViewController = makeScreenViewController
    }

    func makeUIViewController(context: Context) -> UIViewController {
        makeScreenViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }
}
