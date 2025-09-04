package mp25.aiassistant.completion.handlers;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import mp25.aiassistant.completion.services.CodeCompletionService;
/**
 *  Key handler for Enter key to trigger code completion
 */
public class EnterKeyHandler extends EditorActionHandler {
    private final EditorActionHandler originalHandler;
    private final CodeCompletionService completionService;

    public EnterKeyHandler(EditorActionHandler originalHandler, CodeCompletionService completionService) {
        this.originalHandler = originalHandler;
        this.completionService = completionService;
    }

    @Override
    protected void doExecute(@NotNull Editor editor, @Nullable Caret caret, DataContext dataContext) {
        // Execute original enter operation first
        if (originalHandler != null) {
            originalHandler.execute(editor, caret, dataContext);
        }

        // Then trigger code completion
        Project project = editor.getProject();
        if (project != null && !project.isDisposed()) {
            // Delay execution a bit to ensure enter has been inserted
            ApplicationManager.getApplication().invokeLater(() -> {
                completionService.requestCompletion(editor, project);

            });
        }
    }
}