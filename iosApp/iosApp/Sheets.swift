import SwiftUI
import Shared

/// Native replacement for the Android side drawer.
struct MenuSheet: View {
    let chat: ChatModel
    let onOpen: (any BubbleRoute) -> Void
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            List {
                Section {
                    Button("Nueva conversación", systemImage: "square.and.pencil") {
                        chat.newConversation()
                        dismiss()
                    }
                    Button("Tiempo en pantalla y Vault", systemImage: "chart.pie") {
                        onOpen(ProfileVault.shared)
                    }
                }
                Section {
                    comingSoon("Modo de enfoque", systemImage: "moon")
                    comingSoon("Apps de bienestar", systemImage: "square.grid.2x2")
                    comingSoon("Retos", systemImage: "trophy")
                }
                if !chat.state.recentConversations.isEmpty {
                    Section("Recientes") {
                        ForEach(chat.state.recentConversations, id: \.id) { conversation in
                            Button {
                                chat.selectConversation(conversation)
                                dismiss()
                            } label: {
                                HStack {
                                    Text(conversation.title)
                                        .foregroundStyle(.primary)
                                        .lineLimit(1)
                                    Spacer()
                                    if conversation.id == chat.state.currentConversationId {
                                        Image(systemName: "checkmark").foregroundStyle(Color.accentColor)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            .navigationTitle("Bubble")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    Button("Listo", systemImage: "checkmark") { dismiss() }
                }
            }
        }
        .presentationDetents([.medium, .large])
    }

    private func comingSoon(_ title: String, systemImage: String) -> some View {
        LabeledContent {
            Text("Próximamente").font(.caption).foregroundStyle(.secondary)
        } label: {
            Label(title, systemImage: systemImage)
        }
        .foregroundStyle(.secondary)
    }
}

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
                                Text("Tiempo en pantalla de hoy")
                                Text("Agrega tu uso de hoy al mensaje").font(.caption).foregroundStyle(.secondary)
                            }
                        } icon: { Image(systemName: "hourglass") }
                    }
                    Button {
                        chat.attachOfflineGoal()
                        dismiss()
                    } label: {
                        Label {
                            VStack(alignment: .leading) {
                                Text("Meta de desconexión")
                                Text("Pide un plan para reducir 30 minutos de redes").font(.caption).foregroundStyle(.secondary)
                            }
                        } icon: { Image(systemName: "target") }
                    }
                } footer: {
                    Text("El contexto se queda en tu iPhone; solo se agrega al texto de tu mensaje.")
                }
            }
            .foregroundStyle(.primary)
            .navigationTitle("Adjuntar contexto")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cerrar", systemImage: "xmark") { dismiss() }
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
                    LabeledContent("Modelo", value: "Respuestas de prueba")
                    LabeledContent("Dónde corre", value: "En este iPhone")
                    LabeledContent("Datos enviados", value: "Ninguno")
                } footer: {
                    Text("En iPhone, Bubble usa por ahora respuestas de prueba mientras se integra un modelo local. Tus conversaciones no salen del dispositivo.")
                }
            }
            .navigationTitle("Bubble Local")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    Button("Listo", systemImage: "checkmark") { dismiss() }
                }
            }
        }
        .presentationDetents([.medium])
    }
}
