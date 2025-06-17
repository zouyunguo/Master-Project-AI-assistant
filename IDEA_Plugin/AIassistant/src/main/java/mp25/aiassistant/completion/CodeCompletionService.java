package mp25.aiassistant.completion;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.command.WriteCommandAction;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class CodeCompletionService {
    private static final String OLLAMA_API_URL = "http://localhost:11434/api/generate";
    private static final int CONTEXT_CHARS = 1000;
    private final HttpClient httpClient;
    private final Gson gson;

    public CodeCompletionService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.gson = new Gson();
    }


    public void requestCompletion(Editor editor, Project project) {
        String context = ReadAction.compute(() -> getEditorContext(editor));
        if (context != null) {
            String completion = getCompletionFromOllama(context);
            if (completion != null && !completion.isEmpty()) {
                ApplicationManager.getApplication().invokeLater(() -> {
                    if (!project.isDisposed()) {
                        showCompletionPopup(editor, project, completion);
                    }
                });
            }
        }
    }

    private String getEditorContext(Editor editor) {
        if (!ApplicationManager.getApplication().isReadAccessAllowed()) {
            return null;
        }
        
        int offset = editor.getCaretModel().getOffset();
        String text = editor.getDocument().getText();
        
        int startOffset = Math.max(0, offset - CONTEXT_CHARS);
        int endOffset = Math.min(text.length(), offset + CONTEXT_CHARS);
        
        return text.substring(startOffset, offset) + "<cursor>" + text.substring(offset, endOffset);
    }
    // 


    private String getCompletionFromOllama(String context) {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", "qwen2.5-coder:1.5b");
        requestBody.addProperty("prompt", context);
        requestBody.addProperty("stream", false);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OLLAMA_API_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);
            return jsonResponse.get("response").getAsString();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void showCompletionPopup(Editor editor, Project project, String completion) {
        BaseListPopupStep<String> popupStep = new BaseListPopupStep<>("Code Completion", completion) {
            @Override
            public PopupStep<?> onChosen(String selectedValue, boolean finalChoice) {
                insertCompletion(editor, project, selectedValue);
                return FINAL_CHOICE;
            }
        };

        JBPopupFactory.getInstance()
                .createListPopup(popupStep)
                .showInBestPositionFor(editor);
    }

    private void insertCompletion(Editor editor, Project project, String completion) {
        WriteCommandAction.runWriteCommandAction(project, () -> {
            int offset = editor.getCaretModel().getOffset();
            editor.getDocument().insertString(offset, completion);
        });
    }
} 