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
import mp25.aiassistant.ReferenceProcessor;
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
    private static final int CONTEXT_CHARS = 500;
    String[] modelList = new String[]{"none"};
    private final CompletionInlayManager inlayManager;

    public CodeCompletionService(CompletionInlayManager inlayManager) {
        this.inlayManager = inlayManager;

    }

    public void requestCompletion(Editor editor, Project project) {
        String context = ReferenceProcessor.getContext(editor,CONTEXT_CHARS);
        if ( !context.isEmpty()) {
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


        String fullPrompt= "You are a AI agent aiming to provide auto-completion function, your task is " +
                "to provide a proper code snippet to be inserted at the current cursor position where some code is missing, you have to give your answer based " +
                "on the context before and after cursor position, below is the context:\n" + context+"\n Inside the context above there is a <Cursor> tag which indicates the current cursor position," +
                " the place where <Cursor> tag is located usually missed something and it is the place where the user wants to insert the code to complete the missing part, " +
                "\nPlease only output the code snippet which starts with ```LanguageName\\n and ends with ```\n";


        System.out.println(fullPrompt);
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
