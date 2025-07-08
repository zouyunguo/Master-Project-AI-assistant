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
   /* private String getCompletionFromOllama(String context) {

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


*//*
        String fullPrompt= "You are a AI agent aiming to provide code completion function, your task is " +
                "to complete the following code block where something is missing, below is the code block to be completed\n" + context+"\n Fill in the blank to complete the code block. Your response should include only the code to replace <BLANK>, without surrounding backticks,include" +
                "the code block in ``` tags, and do not include any other text or explanation.\n" ;

*//*
        String fullPrompt= "You are a AI agent aiming to provide code completion function, your task is " +
                "to complete the following code block where something is missing, below is the code block to be completed\n" + context+"\n Fill in the blank to complete the code block. Your response should include only the code to replace <BLANK>, without surrounding backticks";
        OllamaService.generateResponse(modelList[0], fullPrompt, responseLine -> {
            System.out.println("Response: " + fullPrompt);
            //对responseLine进行处理，去除以<thinking>和</thinking>标签包裹的思考内容
            *//*Pattern pattern = Pattern.compile("```[\\s\\S]*?\\n([\\s\\S]*?)\\n```");
            Matcher matcher = pattern.matcher(responseLine);
            String codeBlock="";
            if (matcher.find()) {
                codeBlock = matcher.group(1);
                // codeBlock 就是被 ``` 包裹的内容
            }*//*
           returnString.set(responseLine);


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
                Thread.sleep(3000); // 等待1秒，确保 Ollama 响应已处理
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return returnString.get();
        });
    }*/

    private String getCompletionFromOllama(String context) {
        try {
            // 获取模型列表
            String[] models = OllamaService.getModels().get(); // 使用 get() 方法同步获取结果
            for (int i = 0; i < models.length; i++) {
                models[i] = models[i].trim(); // 清理空白
            }
            modelList = models;

            // 构建完整的提示
            String fullPrompt = "You are a AI agent aiming to provide code completion function, your task is " +
                    "to complete the following code block where something is missing, below is the code block to be completed\n" +
                    context + "\n Fill in the blank to complete the code block. Your response should include only the code to replace <BLANK>, without surrounding backticks,include the response code block in ``` tags.\n";

            // 同步生成响应
            StringBuilder responseBuilder = new StringBuilder();
            OllamaService.generateResponse(modelList[0], fullPrompt, responseBuilder::append).get(); // 使用 get() 方法同步获取结果
            Pattern pattern = Pattern.compile("```[\\s\\S]*?\\n([\\s\\S]*?)\\n```");
            Matcher matcher = pattern.matcher(responseBuilder.toString());
            String codeBlock="";
            if (matcher.find()) {
                codeBlock = matcher.group(1);
                // codeBlock 就是被 ``` 包裹的内容
            }
            // 返回响应内容
            return codeBlock;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return "";
        }
    }
}
