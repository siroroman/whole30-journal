import SwiftUI
import UIKit

/// Restores the swipe-from-the-left-edge back gesture on a `NavigationStack`.
///
/// UIKit disables `interactivePopGestureRecognizer` whenever the navigation bar is hidden, and every
/// destination here hides it because the shared Compose screens draw their own top bar. Attaching
/// this to the stack's root content installs a delegate that re-enables the gesture for the whole
/// stack, for as long as the stack exists.
struct InteractivePopGesture: UIViewControllerRepresentable {
    final class Coordinator: NSObject, UIGestureRecognizerDelegate {
        weak var navigationController: UINavigationController?

        func gestureRecognizerShouldBegin(_ gestureRecognizer: UIGestureRecognizer) -> Bool {
            (navigationController?.viewControllers.count ?? 0) > 1
        }

        // The Compose screen underneath installs its own recognizers; without this the edge pan and
        // Compose's touch handling cancel each other out instead of both being allowed to start.
        func gestureRecognizer(
            _ gestureRecognizer: UIGestureRecognizer,
            shouldRecognizeSimultaneouslyWith otherGestureRecognizer: UIGestureRecognizer
        ) -> Bool {
            true
        }
    }

    func makeCoordinator() -> Coordinator {
        Coordinator()
    }

    func makeUIViewController(context: Context) -> UIViewController {
        Enabler(coordinator: context.coordinator)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }

    private final class Enabler: UIViewController {
        private let coordinator: Coordinator

        init(coordinator: Coordinator) {
            self.coordinator = coordinator
            super.init(nibName: nil, bundle: nil)
        }

        @available(*, unavailable)
        required init?(coder: NSCoder) {
            fatalError("init(coder:) has not been implemented")
        }

        override func viewDidAppear(_ animated: Bool) {
            super.viewDidAppear(animated)
            guard let navigationController else { return }
            coordinator.navigationController = navigationController
            navigationController.interactivePopGestureRecognizer?.delegate = coordinator
        }
    }
}

extension View {
    /// Attach to a `NavigationStack`'s root content - see `InteractivePopGesture`.
    func interactivePopGestureEnabled() -> some View {
        background(InteractivePopGesture().frame(width: 0, height: 0).allowsHitTesting(false))
    }
}
