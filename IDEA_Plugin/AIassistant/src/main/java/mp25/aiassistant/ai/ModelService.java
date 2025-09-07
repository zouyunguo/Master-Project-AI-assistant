package mp25.aiassistant.ai;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import mp25.aiassistant.chat.ChatSession;

/**
 * Abstract interface for LLM services, defining core capabilities to allow swapping implementations
 * (e.g., Ollama, OpenAI, or other local/remote providers).
 */
public interface ModelService {
    /**
     * Streaming chat API with conversation context.
     */
    CompletableFuture<Void> chatResponse(String model, String prompt, ChatSession session, Consumer<String> onResponse);

    /**
     * Generate a full single-shot response (non-streaming).
     */
    CompletableFuture<Void> generateResponse(String model, String prompt, Consumer<String> onResponse);

    /**
     * Retrieve the list of available model identifiers.
     */
    CompletableFuture<String[]> getModels();
}
