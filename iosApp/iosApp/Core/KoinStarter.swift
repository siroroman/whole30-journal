import SharedKit

/// Starts the shared Koin container - must run before anything touches Koin, including
/// KoinResolver, which reads from the same KoinIOS instance. Called once from
/// Whole30JournalApp.init().
enum KoinStarter {
    static func start() {
        KoinIOS.shared.doInitKoinIos {
            print("[KoinStarter] Koin started")
        }
    }
}
