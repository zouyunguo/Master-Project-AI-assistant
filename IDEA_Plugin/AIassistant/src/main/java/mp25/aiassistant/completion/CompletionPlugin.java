package mp25.aiassistant.completion;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.editor.actionSystem.EditorActionManager;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.editor.event.EditorFactoryListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import mp25.aiassistant.completion.managers.CompletionInlayManager;
import mp25.aiassistant.completion.services.CodeCompletionService;
import mp25.aiassistant.completion.handlers.EnterKeyHandler;
import mp25.aiassistant.completion.handlers.TabKeyHandler;
import mp25.aiassistant.completion.handlers.EscapeKeyHandler;
/**
 *  Main plugin class to initialize code completion features
 */
public class CompletionPlugin implements ProjectActivity {
    private static boolean initialized = false;
    private static CompletionInlayManager inlayManager;
    private static CodeCompletionService completionService;

    @Nullable
    @Override
    public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        if (!initialized) {
            initialized = true;

            // Create shared manager
            inlayManager = new CompletionInlayManager();
            completionService = new CodeCompletionService(inlayManager);

            // Get EditorActionManager
            EditorActionManager actionManager = EditorActionManager.getInstance();

            // Replace Enter key handler
            EditorActionHandler originalEnterHandler = actionManager.getActionHandler("EditorEnter");
            EnterKeyHandler enterKeyHandler = new EnterKeyHandler(originalEnterHandler, completionService);
            actionManager.setActionHandler("EditorEnter", enterKeyHandler);

            // Replace Tab key handler
            EditorActionHandler originalTabHandler = actionManager.getActionHandler("EditorTab");
            TabKeyHandler tabKeyHandler = new TabKeyHandler(originalTabHandler, inlayManager);
            actionManager.setActionHandler("EditorTab", tabKeyHandler);

            EditorActionHandler originalEscHandler = actionManager.getActionHandler("EditorEscape");
            EscapeKeyHandler escapeKeyHandler = new EscapeKeyHandler(originalEscHandler, inlayManager);
            actionManager.setActionHandler("EditorEscape", escapeKeyHandler);

            // Add listeners for all editors
            EditorFactory.getInstance().addEditorFactoryListener(new EditorFactoryListener() {
                @Override
                public void editorCreated(@NotNull EditorFactoryEvent event) {
                    Editor editor = event.getEditor();
                    if (editor.getProject() != null) {
                        completionService.setupListeners(editor);
                    }
                }
            }, project);
        }

        return Unit.INSTANCE;
    }
}