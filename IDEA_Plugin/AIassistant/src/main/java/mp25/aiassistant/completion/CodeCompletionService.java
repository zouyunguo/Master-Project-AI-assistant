package mp25.aiassistant.completion;

import com.github.javaparser.JavaParser;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
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
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

public class CodeCompletionService {
    private static final String OLLAMA_API_URL = "http://localhost:11434/api/generate";
    private static final int CONTEXT_CHARS = 1000;

    private final CompletionInlayManager inlayManager;
    private final HttpClient httpClient;
    private final Gson gson;

    public CodeCompletionService(CompletionInlayManager inlayManager) {
        this.inlayManager = inlayManager;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.gson = new Gson();
    }

    public void requestCompletion(Editor editor, Project project) {
        String context = ReadAction.compute(() -> getEditorContextByAST(editor));
        if (context != null && !context.isEmpty()) {
            ApplicationManager.getApplication().executeOnPooledThread(() -> {
                String completion = getCompletionFromOllama(context);
                if (completion != null && !completion.isEmpty()) {
                    ApplicationManager.getApplication().invokeLater(() -> {
                        if (!project.isDisposed()) {
                            inlayManager.showCompletionPreview(editor, completion);
                        }
                    });
                }
            });
        }
    }

    public void setupListeners(Editor editor) {
        editor.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void documentChanged(@NotNull DocumentEvent event) {
                inlayManager.clearPreview(editor);
            }
        });

        editor.getCaretModel().addCaretListener(new CaretListener() {
            @Override
            public void caretPositionChanged(@NotNull CaretEvent event) {
                if (event.getOldPosition().line != event.getNewPosition().line ||
                        Math.abs(event.getOldPosition().column - event.getNewPosition().column) > 1) {
                    inlayManager.clearPreview(editor);
                }
            }
        });
    }

    /**
     * 使用 JavaParser 提取光标所在方法的源码
     */
    private String getEditorContextByAST(Editor editor) {
        String code = editor.getDocument().getText();
        int offset = editor.getCaretModel().getOffset();

        try {
            CompilationUnit cu = StaticJavaParser.parse(new StringReader(code));
            Optional<MethodDeclaration> methodOpt = findMethodAtOffset(cu, offset);
            if (methodOpt.isPresent()) {
                MethodDeclaration method = methodOpt.get();
                return method.toString();
            } else {
                return fallbackContext(code, offset);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return fallbackContext(code, offset);
        }
    }

    /**
     * 递归查找包含 offset 的最内层方法声明
     */
    private Optional<MethodDeclaration> findMethodAtOffset(Node node, int offset) {
        for (Node child : node.getChildNodes()) {
            Optional<MethodDeclaration> result = findMethodAtOffset(child, offset);
            if (result.isPresent()) return result;
        }

        if (node instanceof MethodDeclaration) {
            MethodDeclaration method = (MethodDeclaration) node;
            int begin = method.getBegin().map(p -> p.column).orElse(-1);
            int end = method.getEnd().map(p -> p.column).orElse(-1);
            if (begin != -1 && end != -1 && offset >= begin && offset <= end) {
                return Optional.of(method);
            }
        }
        return Optional.empty();
    }

    /**
     * 如果 AST 提取失败，使用字符截断兜底
     */
    private String fallbackContext(String code, int offset) {
        int start = Math.max(0, offset - CONTEXT_CHARS);
        int end = Math.min(code.length(), offset + CONTEXT_CHARS);
        return code.substring(start, offset) + "<cursor>" + code.substring(offset, end);
    }

    /**
     * 调用 Ollama 本地模型生成补全
     */
    private String getCompletionFromOllama(String context) {
        JsonObject body = new JsonObject();
        body.addProperty("model", "qwen2.5-coder:1.5b");
        body.addProperty("prompt", context);
        body.addProperty("stream", false);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OLLAMA_API_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject result = gson.fromJson(response.body(), JsonObject.class);
            return result.get("response").getAsString();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }
}
