package mp25.aiassistant.ai;

/**
 * Singleton provider for ModelService implementations.
 * Defaults to OllamaService, but can be swapped at runtime for testing or switching backends.
 */
public final class ModelServiceProvider {
    private static volatile ModelService INSTANCE = OllamaService.getInstance();

    private ModelServiceProvider() {}

    public static ModelService get() {
        return INSTANCE;
    }

    public static void set(ModelService service) {
        if (service != null) {
            INSTANCE = service;
        }
    }
}
