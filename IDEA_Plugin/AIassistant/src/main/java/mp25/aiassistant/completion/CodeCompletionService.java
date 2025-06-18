package mp25.aiassistant.completion;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.project.Project;
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
    private final CompletionInlayManager inlayManager;

    public CodeCompletionService(CompletionInlayManager inlayManager) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.gson = new Gson();
        this.inlayManager = inlayManager;
    }

    public void requestCompletion(Editor editor, Project project) {
        String context = ReadAction.compute(() -> getEditorContext(editor));
        if (context != null) {
            // 在后台线程中执行网络请求
            ApplicationManager.getApplication().executeOnPooledThread(() -> {
                String completion = getCompletionFromOllama(context);
                if (completion != null && !completion.isEmpty()) {
                    ApplicationManager.getApplication().invokeLater(() -> {
                        if (!project.isDisposed()) {
                            // 显示灰色预览而不是弹出窗口
                            inlayManager.showCompletionPreview(editor, completion);
                        }
                    });
                }
            });
        }
    }

    // 添加监听器以在用户输入或移动光标时清除预览
    public void setupListeners(Editor editor) {
        // 文档变化监听器
        editor.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void documentChanged(@NotNull DocumentEvent event) {
                inlayManager.clearPreview(editor);
            }
        });

        // 光标移动监听器
        editor.getCaretModel().addCaretListener(new CaretListener() {
            @Override
            public void caretPositionChanged(@NotNull CaretEvent event) {
                // 只在光标实际移动时清除预览
                if (event.getOldPosition().line != event.getNewPosition().line ||
                        Math.abs(event.getOldPosition().column - event.getNewPosition().column) > 1) {
                    inlayManager.clearPreview(editor);
                }
            }
        });
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
}