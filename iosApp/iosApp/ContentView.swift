import SwiftUI
import Shared

/// Native shell: SwiftUI owns navigation and every control (Liquid Glass on iOS 26),
/// Compose draws the content of each screen.
struct ContentView: View {
    @State private var chat = ChatModel()
    @State private var path: [RouteWrapper] = []
    @State private var mainInsets = ChromeInsets()
    @State private var dictation = Dictation()
    @State private var isMenuOpen = false
    @State private var isAttachOpen = false
    @State private var isModelInfoOpen = false

    var body: some View {
        NavigationStack(path: $path) {
            ComposeScreenContainer(insets: mainInsets) {
                MainComposeView(bridge: chat.bridge, insets: mainInsets) { route in
                    push(route)
                }
            }
            .safeAreaInset(edge: .bottom) {
                PromptBar(
                    chat: chat,
                    dictation: dictation,
                    onAttach: { isAttachOpen = true }
                )
            }
            .navigationTitle("Bubble")
            .toolbarTitleDisplayMode(.inline)
            .toolbar { chatToolbar }
            .navigationDestination(for: RouteWrapper.self) { wrapper in
                RouteScreen(bridge: chat.bridge, route: wrapper.route) {
                    if !path.isEmpty { path.removeLast() }
                }
            }
        }
        .tint(.accentColor)
        .sheet(isPresented: $isMenuOpen) {
            MenuSheet(chat: chat) { route in
                isMenuOpen = false
                push(route)
            }
        }
        .sheet(isPresented: $isAttachOpen) {
            AttachSheet(chat: chat)
        }
        .sheet(isPresented: $isModelInfoOpen) {
            ModelInfoSheet()
        }
    }

    @ToolbarContentBuilder
    private var chatToolbar: some ToolbarContent {
        ToolbarItem(placement: .topBarLeading) {
            Button("Menu", systemImage: "line.3.horizontal") { isMenuOpen = true }
        }
        ToolbarItem(placement: .principal) {
            Button { isModelInfoOpen = true } label: {
                HStack(spacing: 6) {
                    Text("Bubble").font(.headline).foregroundStyle(.primary)
                    Text("Local").font(.subheadline.weight(.semibold)).foregroundStyle(Color.bubbleCyan)
                    Image(systemName: "chevron.down").font(.caption.weight(.semibold)).foregroundStyle(.secondary)
                }
                .padding(.horizontal, 6)
            }
            .tint(.primary)
        }
        ToolbarItemGroup(placement: .topBarTrailing) {
            Button("New conversation", systemImage: "square.and.pencil") { chat.newConversation() }
            Button("Profile", systemImage: "person.crop.circle") { push(ProfileVault.shared) }
        }
    }

    private func push(_ route: any BubbleRoute) {
        path.append(RouteWrapper(route: route))
    }
}

/// A pushed screen: Compose content with a native title and back button.
struct RouteScreen: View {
    let bridge: BubbleIosBridge
    let route: any BubbleRoute
    let onBack: () -> Void
    @State private var insets = ChromeInsets()
    @State private var isSecurityInfoOpen = false

    init(bridge: BubbleIosBridge, route: any BubbleRoute, onBack: @escaping () -> Void) {
        self.bridge = bridge
        self.route = route
        self.onBack = onBack
    }

    var body: some View {
        ComposeScreenContainer(insets: insets) {
            ScreenComposeView(bridge: bridge, route: route, insets: insets, onBack: onBack)
        }
        .navigationTitle(route.title)
        .toolbarTitleDisplayMode(.inline)
        .toolbar {
            if route is ProfileVault {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("Vault Security", systemImage: "lock.shield") { isSecurityInfoOpen = true }
                }
            }
        }
        .alert("Encrypted Local Vault", isPresented: $isSecurityInfoOpen) {
            Button("OK", role: .cancel) {}
        } message: {
            Text("Your memories, offline goals, and habits are stored only on this iPhone, within the app's private sandbox.\n\nZero cloud storage. Zero ad tracking.")
        }
    }
}

extension Color {
    /// Bubble's brand cyan (#1BB5C6), the same as `BubbleCyan` in the Compose theme.
    static let bubbleCyan = Color(red: 0x1B / 255, green: 0xB5 / 255, blue: 0xC6 / 255)
}
