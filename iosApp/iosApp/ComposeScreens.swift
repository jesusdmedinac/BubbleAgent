import SwiftUI
import Shared

/// `NavigationStack` needs hashable values; a UUID keeps two pushes of the same route distinct.
struct RouteWrapper: Hashable, Identifiable {
    let id = UUID()
    let route: any BubbleRoute

    static func == (lhs: RouteWrapper, rhs: RouteWrapper) -> Bool { lhs.id == rhs.id }
    func hash(into hasher: inout Hasher) { hasher.combine(id) }
}

/// The chat drawn by Compose, without its own bars.
struct MainComposeView: UIViewControllerRepresentable {
    let bridge: BubbleIosBridge
    let insets: ChromeInsets
    let onNavigate: (any BubbleRoute) -> Void

    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController(bridge: bridge, insets: insets, onNavigate: onNavigate)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

/// Any other screen pushed on the stack, drawn by Compose.
struct ScreenComposeView: UIViewControllerRepresentable {
    let bridge: BubbleIosBridge
    let route: any BubbleRoute
    let insets: ChromeInsets
    let onBack: () -> Void

    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.ScreenViewController(bridge: bridge, route: route, insets: insets, onBack: onBack)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

/// Compose draws edge to edge under the glass bars; this tells it how much room they take.
struct ComposeScreenContainer<Content: View>: View {
    let insets: ChromeInsets
    @ViewBuilder let content: () -> Content

    var body: some View {
        ZStack {
            Color.black.ignoresSafeArea()
            Color.clear
                .onGeometryChange(for: EdgeInsets.self) { $0.safeAreaInsets } action: { safeArea in
                    insets.update(top: safeArea.top, bottom: safeArea.bottom)
                }
            content().ignoresSafeArea()
        }
    }
}
