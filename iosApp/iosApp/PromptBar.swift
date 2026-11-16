import SwiftUI

/// Floating Liquid Glass prompt bar: attach, text field, and dictation or send.
struct PromptBar: View {
    @Bindable var chat: ChatModel
    let dictation: Dictation
    let onAttach: () -> Void
    @FocusState private var isFocused: Bool

    var body: some View {
        GlassEffectContainer(spacing: 10) {
            HStack(alignment: .bottom, spacing: 10) {
                Button(action: onAttach) {
                    Image(systemName: "plus")
                        .font(.title3.weight(.medium))
                        .frame(width: 48, height: 48)
                        .contentShape(.circle)
                }
                .buttonStyle(.plain)
                .glassEffect(.regular.interactive(), in: .circle)
                .accessibilityLabel("Attach context")

                HStack(alignment: .bottom, spacing: 8) {
                    TextField("Ask Bubble...", text: $chat.draft, axis: .vertical)
                        .lineLimit(1...5)
                        .focused($isFocused)
                        .padding(.vertical, 14)
                        .submitLabel(.send)
                        .onSubmit(chat.send)
                    trailingButton
                        .padding(.bottom, 4)
                }
                .padding(.leading, 18)
                .padding(.trailing, 4)
                .frame(minHeight: 48)
                .glassEffect(.regular.interactive(), in: .rect(cornerRadius: 24))
            }
        }
        .padding(.horizontal, 16)
        .padding(.bottom, 8)
        .animation(.smooth, value: chat.canSend)
        .animation(.smooth, value: dictation.isListening)
    }

    @ViewBuilder
    private var trailingButton: some View {
        if chat.state.isGenerating {
            ProgressView()
                .frame(width: 40, height: 40)
        } else if chat.canSend && !dictation.isListening {
            Button {
                chat.send()
            } label: {
                Image(systemName: "arrow.up")
                    .font(.body.weight(.bold))
                    .frame(width: 40, height: 40)
                    .contentShape(.circle)
            }
            .buttonStyle(.glassProminent)
            .buttonBorderShape(.circle)
            .accessibilityLabel("Send")
        } else {
            Button {
                dictation.toggle(startingFrom: chat.draft) { text in chat.draft = text }
            } label: {
                Image(systemName: dictation.isListening ? "stop.fill" : "mic")
                    .font(.body.weight(.semibold))
                    .foregroundStyle(dictation.isListening ? Color.accentColor : .primary)
                    .symbolEffect(.pulse, isActive: dictation.isListening)
                    .frame(width: 40, height: 40)
                    .contentShape(.circle)
            }
            .buttonStyle(.plain)
            .accessibilityLabel(dictation.isListening ? "Stop dictation" : "Dictate")
        }
    }
}
