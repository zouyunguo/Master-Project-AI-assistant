package mp25.aiassistant.completion.handlers;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import mp25.aiassistant.completion.managers.CompletionInlayManager;
/**
 *  Key handler for Escape key to clear completion preview
 */
public class EscapeKeyHandler extends EditorActionHandler {
    private final EditorActionHandler originalHandler;
    private final CompletionInlayManager inlayManager;

    public EscapeKeyHandler(EditorActionHandler originalHandler, CompletionInlayManager inlayManager) {
        this.originalHandler = originalHandler;
        this.inlayManager = inlayManager;
    }

    @Override
    protected void doExecute(@NotNull Editor editor, @Nullable Caret caret, DataContext dataContext) {
        // If there's an active completion preview, clear it
        if (inlayManager.hasActiveCompletion()) {
            inlayManager.clearPreview(editor);
            // To ensure IDE state consistency, still execute original handler
            if (originalHandler != null) {
                originalHandler.execute(editor, caret, dataContext);
            }
            return;
        }

        // Otherwise execute original ESC functionality
        if (originalHandler != null) {
            originalHandler.execute(editor, caret, dataContext);
        }
    }
}