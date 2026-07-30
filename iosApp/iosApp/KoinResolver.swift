import SharedKit

/// Resolves instances by type from the shared Koin container. Kept independent (no dependency on
/// any feature module) so it never has to know about specific ViewModel types - see ARCHITECTURE.md
/// for the per-VM Swift factory wrappers built on top of this.
enum KoinResolver {
    private static var koin: Koin_coreKoin {
        KoinIOS.shared.getKoinApplication().koin
    }

    static func get<T: AnyObject>(_ type: T.Type) -> T {
        guard let result = koin.get(objCClass: type) as? T else {
            fatalError("KMP dependency not found: \(type) - is it registered in a Koin module?")
        }
        return result
    }
}
