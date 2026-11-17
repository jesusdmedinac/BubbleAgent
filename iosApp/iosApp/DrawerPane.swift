import SwiftUI
import Shared

/// Drawer Navigation Pane based on `bubble-system.pen` (Screen / HIG - 04 Bubble Menu Drawer).
/// Sits behind the displaced app canvas on the left.
struct DrawerPaneView: View {
    let chat: ChatModel
    let onClose: () -> Void
    let onNavigate: (any BubbleRoute) -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            // Upper Group
            VStack(alignment: .leading, spacing: 14) {
                headerRow
                newConversationButton
                navigationLinksGroup
                recentsSection
            }

            Spacer(minLength: 16)

            // Bottom Profile / Settings Group
            profileBottomRow
        }
        .padding(.horizontal, 14)
        .padding(.top, 10)
        .padding(.bottom, 8)
        .frame(width: 260, alignment: .leading)
        .frame(maxHeight: .infinity)
        .safeAreaPadding(.vertical)
    }

    // MARK: - Header (Brand + Close)
    private var headerRow: some View {
        HStack(alignment: .center) {
            HStack(spacing: 8) {
                Circle()
                    .fill(Color.bubbleCyan)
                    .frame(width: 10, height: 10)
                    .shadow(color: Color.bubbleCyan.opacity(0.8), radius: 6)

                Text("Bubble")
                    .font(.system(size: 22, weight: .bold))
                    .foregroundStyle(.primary)
            }

            Spacer()

            // Close button (34x34 Circle with Liquid Glass)
            Button(action: onClose) {
                Image(systemName: "xmark")
                    .font(.system(size: 13, weight: .semibold))
                    .foregroundStyle(.secondary)
                    .frame(width: 34, height: 34)
                    .contentShape(.circle)
            }
            .buttonStyle(.plain)
            .glassEffect(.regular.interactive(), in: .circle)
            .accessibilityLabel("Close Menu")
        }
    }

    // MARK: - New Conversation
    private var newConversationButton: some View {
        Button {
            chat.newConversation()
            onClose()
        } label: {
            HStack(spacing: 8) {
                Image(systemName: "plus")
                    .font(.system(size: 14, weight: .bold))
                    .foregroundStyle(Color.bubbleCyan)

                Text("New conversation")
                    .font(.system(size: 13, weight: .semibold))
                    .foregroundStyle(.primary)

                Spacer()
            }
            .padding(.horizontal, 14)
            .frame(height: 42)
            .contentShape(Capsule())
        }
        .buttonStyle(.plain)
        .glassEffect(.regular.interactive(), in: .capsule)
    }

    // MARK: - Navigation Links
    private var navigationLinksGroup: some View {
        VStack(spacing: 10) {
            DrawerNavigationLinkRow(
                iconName: "target",
                title: "Focus mode",
                badge: { AnyView(ComingSoonBadge()) },
                action: nil
            )

            DrawerNavigationLinkRow(
                iconName: "square.grid.2x2",
                title: "Apps",
                badge: { AnyView(ProBadge()) },
                action: nil
            )

            DrawerNavigationLinkRow(
                iconName: "trophy",
                title: "Challenges",
                badge: { AnyView(ComingSoonBadge()) },
                action: nil
            )

            DrawerNavigationLinkRow(
                iconName: "chart.pie",
                title: "Screen time",
                badge: {
                    AnyView(
                        Image(systemName: "chevron.right")
                            .font(.system(size: 11, weight: .semibold))
                            .foregroundStyle(.secondary)
                    )
                },
                action: {
                    onClose()
                    onNavigate(ProfileVault.shared)
                }
            )
        }
        .padding(.vertical, 2)
    }

    // MARK: - Recents
    private var recentsSection: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text("Recent")
                .font(.system(size: 11, weight: .bold))
                .foregroundStyle(Color(white: 0.55))
                .padding(.top, 4)

            if !chat.state.recentConversations.isEmpty {
                VStack(spacing: 4) {
                    ForEach(chat.state.recentConversations, id: \.id) { conv in
                        let isSelected = conv.id == chat.state.currentConversationId
                        Button {
                            chat.selectConversation(conv)
                            onClose()
                        } label: {
                            HStack(spacing: 6) {
                                if isSelected {
                                    Circle()
                                        .fill(Color.bubbleCyan)
                                        .frame(width: 5, height: 5)
                                }
                                Text(conv.title.isEmpty ? "Conversation" : conv.title)
                                    .font(.system(size: 12, weight: isSelected ? .semibold : .regular))
                                    .foregroundStyle(isSelected ? Color.bubbleCyan : Color(white: 0.75))
                                    .lineLimit(1)
                                Spacer()
                            }
                            .padding(.vertical, 5)
                            .padding(.horizontal, 6)
                            .background(
                                RoundedRectangle(cornerRadius: 6, style: .continuous)
                                    .fill(isSelected ? Color.bubbleCyan.opacity(0.12) : Color.clear)
                            )
                        }
                        .buttonStyle(.plain)
                    }
                }
            } else if !chat.state.recentQueries.isEmpty {
                VStack(alignment: .leading, spacing: 5) {
                    ForEach(chat.state.recentQueries, id: \.self) { query in
                        sampleQueryButton(query)
                    }
                }
            } else {
                VStack(alignment: .leading, spacing: 5) {
                    sampleQueryButton("4-7-8 breathing exercise for focus")
                    sampleQueryButton("Overcoming phone check anxiety")
                    sampleQueryButton("Digital detox bedtime routine")
                }
            }
        }
    }

    private func sampleQueryButton(_ query: String) -> some View {
        Button {
            chat.draft = query
            onClose()
        } label: {
            Text(query)
                .font(.system(size: 12))
                .foregroundStyle(Color(white: 0.65))
                .lineLimit(1)
                .padding(.vertical, 3)
                .padding(.horizontal, 4)
        }
        .buttonStyle(.plain)
    }

    // MARK: - Profile Footer
    private var profileBottomRow: some View {
        HStack(alignment: .center) {
            Button {
                onClose()
                onNavigate(ProfileVault.shared)
            } label: {
                HStack(spacing: 8) {
                    ZStack {
                        Image(systemName: "person.fill")
                            .font(.system(size: 14))
                            .foregroundStyle(.primary)
                    }
                    .frame(width: 34, height: 34)
                    .contentShape(.circle)
                    .glassEffect(.regular.interactive(), in: .circle)

                    VStack(alignment: .leading, spacing: 2) {
                        Text(chat.state.userName.isEmpty ? "Jesús Daniel" : chat.state.userName)
                            .font(.system(size: 12, weight: .semibold))
                            .foregroundStyle(.primary)

                        Text("Pro Account")
                            .font(.system(size: 10, weight: .medium))
                            .foregroundStyle(Color.bubbleCyan)
                    }
                }
            }
            .buttonStyle(.plain)

            Spacer()

            Button {
                onClose()
                onNavigate(ProfileVault.shared)
            } label: {
                Image(systemName: "gearshape")
                    .font(.system(size: 15, weight: .regular))
                    .foregroundStyle(.secondary)
                    .frame(width: 34, height: 34)
                    .contentShape(.circle)
            }
            .buttonStyle(.plain)
            .glassEffect(.regular.interactive(), in: .circle)
            .accessibilityLabel("Settings")
        }
        .padding(.top, 6)
    }
}

/// A row in the navigation drawer
struct DrawerNavigationLinkRow: View {
    let iconName: String
    let title: String
    let badge: () -> AnyView
    let action: (() -> Void)?

    var body: some View {
        Button {
            action?()
        } label: {
            HStack(spacing: 10) {
                Image(systemName: iconName)
                    .font(.system(size: 15, weight: .medium))
                    .foregroundStyle(action != nil ? .primary : .secondary)
                    .frame(width: 20, height: 20)

                Text(title)
                    .font(.system(size: 13, weight: .medium))
                    .foregroundStyle(action != nil ? .primary : .secondary)

                Spacer()

                badge()
            }
            .padding(.vertical, 3)
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .disabled(action == nil)
    }
}

/// "Coming soon" pill badge
struct ComingSoonBadge: View {
    var body: some View {
        Text("Coming soon")
            .font(.system(size: 9, weight: .semibold))
            .foregroundStyle(Color.bubbleCyan)
            .padding(.horizontal, 6)
            .padding(.vertical, 2)
            .background(
                RoundedRectangle(cornerRadius: 6, style: .continuous)
                    .fill(Color.bubbleCyan.opacity(0.12))
            )
            .overlay(
                RoundedRectangle(cornerRadius: 6, style: .continuous)
                    .stroke(Color.bubbleCyan.opacity(0.20), lineWidth: 1)
            )
    }
}

/// "PRO" gold chip badge
struct ProBadge: View {
    var body: some View {
        Text("PRO")
            .font(.system(size: 9, weight: .bold))
            .foregroundStyle(Color.accentGold)
            .padding(.horizontal, 6)
            .padding(.vertical, 2)
            .background(
                RoundedRectangle(cornerRadius: 6, style: .continuous)
                    .fill(Color.accentGold.opacity(0.20))
            )
    }
}
