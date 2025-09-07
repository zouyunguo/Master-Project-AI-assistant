package mp25.aiassistant.state;

/**
 * Singleton to store the globally selected model from the chat UI.
 */
public class ModelSelectionManager {
    private static final ModelSelectionManager INSTANCE = new ModelSelectionManager();

    // Volatile for safe read-after-write across EDT/background threads
    private volatile String selectedModel = "";

    private ModelSelectionManager() {}

    public static ModelSelectionManager getInstance() {
        return INSTANCE;
    }

    public String getSelectedModel() {
        return selectedModel;
    }

    public void setSelectedModel(String model) {
        if (model != null) {
            this.selectedModel = model.trim();
        }
    }
}

