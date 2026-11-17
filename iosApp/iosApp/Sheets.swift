import SwiftUI
import Shared

/// Native version of the "attach context" sheet.
struct AttachSheet: View {
    let chat: ChatModel
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            List {
                Section {
                    Button {
                        chat.attachScreenTime()
                        dismiss()
                    } label: {
                        Label {
                            VStack(alignment: .leading) {
                                Text("Today's Screen Time")
                                Text("Attach today's usage to message").font(.caption).foregroundStyle(.secondary)
                            }
                        } icon: { Image(systemName: "hourglass") }
                    }
                    Button {
                        chat.attachOfflineGoal()
                        dismiss()
                    } label: {
                        Label {
                            VStack(alignment: .leading) {
                                Text("Disconnect Goal")
                                Text("Request a plan to reclaim 30 min from social media").font(.caption).foregroundStyle(.secondary)
                            }
                        } icon: { Image(systemName: "target") }
                    }
                } footer: {
                    Text("Context stays on your iPhone; it is only added to your message draft.")
                }
            }
            .foregroundStyle(.primary)
            .navigationTitle("Attach Context")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Close", systemImage: "xmark") { dismiss() }
                }
            }
        }
        .presentationDetents([.medium])
    }
}

/// What powers the assistant on this device.
struct ModelInfoSheet: View {
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            List {
                Section {
                    LabeledContent("Model", value: "Demo offline engine")
                    LabeledContent("Runs on", value: "On this iPhone")
                    LabeledContent("Data sent", value: "None")
                } footer: {
                    Text("On iPhone, Bubble currently uses demo offline responses while an on-device model is integrated. Your conversations never leave the device.")
                }
            }
            .navigationTitle("Bubble Local")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    Button("Done", systemImage: "checkmark") { dismiss() }
                }
            }
        }
        .presentationDetents([.medium])
    }
}
