import Observation
import Shared

/// Swift side of the shared chat: mirrors the Kotlin `ChatUiState` and owns the prompt draft.
@Observable
final class ChatModel {
    let bridge = BubbleIosBridge(appGraph: IosAppGraph.shared)
    private(set) var state: ChatUiState
    var draft = ""

    init() {
        state = bridge.currentState
        bridge.watch { [weak self] state in
            self?.state = state
        }
    }

    deinit {
        bridge.close()
    }

    var canSend: Bool {
        !draft.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty && !state.isGenerating
    }

    func send() {
        guard canSend else { return }
        bridge.send(text: draft)
        draft = ""
    }

    func newConversation() {
        bridge.doNewConversation()
    }

    func selectConversation(_ conversation: Conversation) {
        bridge.selectConversation(id: conversation.id)
    }

    func attachScreenTime() {
        draft += bridge.screenTimeContext()
    }

    func attachOfflineGoal() {
        draft = bridge.offlineGoalPrompt
    }
}
