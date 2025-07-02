package mp25.aiassistant.completion;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.gson.Gson;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.project.Project;
import mp25.aiassistant.OllamaService;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.StringReader;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeCompletionService {
    private static final int CONTEXT_CHARS = 1000;
    String[] modelList = new String[]{"none"};
    private final CompletionInlayManager inlayManager;

    public CodeCompletionService(CompletionInlayManager inlayManager) {
        this.inlayManager = inlayManager;

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

        CompletableFuture<String[]> modelsFuture = OllamaService.getModels();
        modelsFuture.thenAccept(response -> {
            // Parse the response and return an array of model names
            String[] models = response;
            for (int i = 0; i < models.length; i++) {
                models[i] = models[i].trim(); // Clean up whitespace
            }
            modelList=models;
        }).exceptionally(ex -> {
            // Handle errors
            SwingUtilities.invokeLater(() -> {
                System.out.println("Error fetching models: " + ex.getMessage());
            });
            return null;
        });
        AtomicReference<String> returnString= new AtomicReference<>("");
        String fullPrompt= "You are a AI agent aiming to provide auto-completion for software development, your task is to provide a code completion based on the following context:\n" + context + "\nPlease only output code, no other texts to explain your work, return your code which starts with ```LanguageName\\n and ends with ```\n";
        OllamaService.generateResponse(modelList[0], fullPrompt, responseLine -> {
            //对responseLine进行处理，去除以<thinking>和</thinking>标签包裹的思考内容
            System.out.println("Response: " + responseLine);
            Pattern pattern = Pattern.compile("```[\\s\\S]*?\\n([\\s\\S]*?)\\n```");
            Matcher matcher = pattern.matcher(responseLine);
            String codeBlock="";
            if (matcher.find()) {
                codeBlock = matcher.group(1);
                // codeBlock 就是被 ``` 包裹的内容
            }
           returnString.set(codeBlock);

        }).exceptionally(ex -> {
            // 处理异常
            SwingUtilities.invokeLater(() -> {
                System.out.println("Error: " + ex.getMessage());

            });
            return null;
        });
        return ReadAction.compute(() -> {
            // 等待异步操作完成
            try {
                Thread.sleep(1000); // 等待1秒，确保 Ollama 响应已处理
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return returnString.get();
        });
    }
}
