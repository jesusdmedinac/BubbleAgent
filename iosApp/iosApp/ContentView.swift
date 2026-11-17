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
        ZStack(alignment: .leading) {
            Color.black.ignoresSafeArea()

            // 1. Drawer Navigation Pane (Left side)
            DrawerPaneView(
                chat: chat,
                onClose: {
                    withAnimation(.spring(response: 0.38, dampingFraction: 0.82)) {
                        isMenuOpen = false
                    }
                },
                onNavigate: { route in
                    withAnimation(.spring(response: 0.38, dampingFraction: 0.82)) {
                        isMenuOpen = false
                    }
                    push(route)
                }
            )
            .opacity(isMenuOpen ? 1 : 0)
            .allowsHitTesting(isMenuOpen)

            // 2. Displaced App Canvas (Main Screen & Navigation)
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
            .clipShape(RoundedRectangle(cornerRadius: isMenuOpen ? 32 : 0, style: .continuous))
            .overlay {
                if isMenuOpen {
                    RoundedRectangle(cornerRadius: 32, style: .continuous)
                        .stroke(Color.bubbleCyan, lineWidth: 2)
                }
            }
            .overlay {
                if isMenuOpen {
                    Color.black.opacity(0.18)
                        .clipShape(RoundedRectangle(cornerRadius: 32, style: .continuous))
                        .contentShape(Rectangle())
                        .onTapGesture {
                            withAnimation(.spring(response: 0.38, dampingFraction: 0.82)) {
                                isMenuOpen = false
                            }
                        }
                }
            }
            .shadow(color: isMenuOpen ? Color.black.opacity(0.65) : .clear, radius: 24, x: -8, y: 0)
            .scaleEffect(isMenuOpen ? 0.88 : 1.0, anchor: .leading)
            .offset(x: isMenuOpen ? 276 : 0)
            .gesture(
                DragGesture(minimumDistance: 25)
                    .onEnded { value in
                        if value.translation.width < -30 && isMenuOpen {
                            withAnimation(.spring(response: 0.38, dampingFraction: 0.82)) {
                                isMenuOpen = false
                            }
                        } else if value.translation.width > 40 && !isMenuOpen && path.isEmpty {
                            withAnimation(.spring(response: 0.38, dampingFraction: 0.82)) {
                                isMenuOpen = true
                            }
                        }
                    }
            )
        }
        .animation(.spring(response: 0.38, dampingFraction: 0.82), value: isMenuOpen)
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
            Button {
                withAnimation(.spring(response: 0.38, dampingFraction: 0.82)) {
                    isMenuOpen.toggle()
                }
            } label: {
                Image(systemName: "line.3.horizontal")
                    .font(.body.weight(.medium))
                    .frame(width: 36, height: 36)
                    .contentShape(.circle)
            }
            .buttonStyle(.plain)
            .glassEffect(.regular.interactive(), in: .circle)
            .accessibilityLabel("Menu")
        }
        ToolbarItem(placement: .principal) {
            Button { isModelInfoOpen = true } label: {
                HStack(spacing: 6) {
                    Text("Bubble").font(.headline).foregroundStyle(.primary)
                    Text("Local").font(.subheadline.weight(.semibold)).foregroundStyle(Color.bubbleCyan)
                    Image(systemName: "chevron.down").font(.caption.weight(.semibold)).foregroundStyle(.secondary)
                }
                .padding(.horizontal, 10)
                .frame(height: 36)
                .contentShape(.capsule)
            }
            .buttonStyle(.plain)
            .glassEffect(.regular.interactive(), in: .capsule)
        }
        ToolbarItemGroup(placement: .topBarTrailing) {
            Button {
                chat.newConversation()
            } label: {
                Image(systemName: "square.and.pencil")
                    .font(.body.weight(.medium))
                    .frame(width: 36, height: 36)
                    .contentShape(.circle)
            }
            .buttonStyle(.plain)
            .glassEffect(.regular.interactive(), in: .circle)
            .accessibilityLabel("New conversation")

            Button {
                push(ProfileVault.shared)
            } label: {
                Image(systemName: "person.crop.circle")
                    .font(.body.weight(.medium))
                    .frame(width: 36, height: 36)
                    .contentShape(.circle)
            }
            .buttonStyle(.plain)
            .glassEffect(.regular.interactive(), in: .circle)
            .accessibilityLabel("Profile")
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
    /// Accent gold (#F5A623) from the design system tokens.
    static let accentGold = Color(red: 0xF5 / 255, green: 0xA6 / 255, blue: 0x23 / 255)
}
