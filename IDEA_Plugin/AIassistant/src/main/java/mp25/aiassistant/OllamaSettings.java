package mp25.aiassistant;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
        name = "mp25.aiassistant.OllamaSettings",
        storages = {@Storage("OllamaAISettings.xml")}
)
public class OllamaSettings implements PersistentStateComponent<OllamaSettings> {
    public String ollamaServerUrl = "http://localhost:11434";
    public String defaultModel = "codellama";
    public boolean streamResponses = false;

    public static OllamaSettings getInstance() {
        return ServiceManager.getService(OllamaSettings.class);
    }

    @Nullable
    @Override
    public OllamaSettings getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull OllamaSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}