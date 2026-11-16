import AVFoundation
import Observation
import Speech

/// Voice dictation for the prompt bar with the Speech framework.
@Observable
final class Dictation {
    private(set) var isListening = false

    private let recognizer = SFSpeechRecognizer(locale: Locale(identifier: "es-MX"))
    private let audioEngine = AVAudioEngine()
    private var request: SFSpeechAudioBufferRecognitionRequest?
    private var task: SFSpeechRecognitionTask?

    /// Starts or stops listening. Recognized text is appended to [draft] and delivered through [onText].
    func toggle(startingFrom draft: String, onText: @escaping (String) -> Void) {
        if isListening {
            stop()
            return
        }
        SFSpeechRecognizer.requestAuthorization { status in
            guard status == .authorized else { return }
            AVAudioApplication.requestRecordPermission { granted in
                guard granted else { return }
                DispatchQueue.main.async {
                    self.start(prefix: draft, onText: onText)
                }
            }
        }
    }

    private func start(prefix: String, onText: @escaping (String) -> Void) {
        guard let recognizer, recognizer.isAvailable else { return }
        do {
            let session = AVAudioSession.sharedInstance()
            try session.setCategory(.record, mode: .measurement, options: .duckOthers)
            try session.setActive(true, options: .notifyOthersOnDeactivation)

            let request = SFSpeechAudioBufferRecognitionRequest()
            request.shouldReportPartialResults = true
            request.requiresOnDeviceRecognition = recognizer.supportsOnDeviceRecognition
            self.request = request

            let input = audioEngine.inputNode
            input.installTap(onBus: 0, bufferSize: 1024, format: input.outputFormat(forBus: 0)) { buffer, _ in
                request.append(buffer)
            }
            audioEngine.prepare()
            try audioEngine.start()
            isListening = true

            let base = prefix.isEmpty ? "" : prefix + " "
            task = recognizer.recognitionTask(with: request) { [weak self] result, error in
                if let result {
                    let text = base + result.bestTranscription.formattedString
                    DispatchQueue.main.async { onText(text) }
                }
                if error != nil || result?.isFinal == true {
                    DispatchQueue.main.async { self?.stop() }
                }
            }
        } catch {
            stop()
        }
    }

    private func stop() {
        guard isListening || task != nil else { return }
        audioEngine.stop()
        audioEngine.inputNode.removeTap(onBus: 0)
        request?.endAudio()
        task?.cancel()
        request = nil
        task = nil
        isListening = false
        try? AVAudioSession.sharedInstance().setActive(false, options: .notifyOthersOnDeactivation)
    }
}
