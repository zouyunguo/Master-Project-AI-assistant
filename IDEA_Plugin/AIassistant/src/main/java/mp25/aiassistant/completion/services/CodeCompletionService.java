package mp25.aiassistant.completion.services;


import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.project.Project;
import mp25.aiassistant.utils.ReferenceProcessor;
import mp25.aiassistant.completion.managers.CompletionInlayManager;
import org.jetbrains.annotations.NotNull;
import mp25.aiassistant.state.ModelSelectionManager;
import mp25.aiassistant.ai.ModelServiceProvider;


import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 *  Service to handle code completion requests and responses
 */
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
     * Call model service to generate completion
     */
    private String getCompletionFromOllama(String context) {
        try {
            // Get model list
            String[] models = ModelServiceProvider.get().getModels().get(); // synchronous get
            for (int i = 0; i < models.length; i++) {
                models[i] = models[i].trim(); // Clean whitespace
            }
            modelList = models;

            // Resolve selected model from global state, fallback to first available
            String selectedModel = ModelSelectionManager.getInstance().getSelectedModel();
            if (selectedModel == null || selectedModel.isEmpty()) {
                selectedModel = models.length > 0 ? models[0] : "";
            }
            if (selectedModel == null || selectedModel.isEmpty()) {
                System.out.println("No model available to generate completion.");
                return "";
            }

            // Build complete prompt
            String fullPrompt = "You are a AI agent aiming to provide code completion function, your task is " +
                    "to complete the following code block where something is missing, below is the code block to be completed\n" +
                    context + "\n Fill in the blank to complete the code block. Your response should include only the code to replace <BLANK>, without surrounding backticks,include the response code block in ``` tags.\n";

            // Synchronously generate response
            StringBuilder responseBuilder = new StringBuilder();
            ModelServiceProvider.get().generateResponse(selectedModel, fullPrompt, responseBuilder::append).get();
            Pattern pattern = Pattern.compile("```[\\s\\S]*?\\n([\\s\\S]*?)\\n```" );
            Matcher matcher = pattern.matcher(responseBuilder.toString());
            String codeBlock="";
            if (matcher.find()) {
                codeBlock = matcher.group(1);
            }
            // Return response content
            return codeBlock;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return "";
        }
    }
}
