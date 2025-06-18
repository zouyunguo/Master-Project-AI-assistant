package mp25.aiassistant.completion;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EnterKeyHandler extends EditorActionHandler {
    private final EditorActionHandler originalHandler;
    private final CodeCompletionService completionService;

    public EnterKeyHandler(EditorActionHandler originalHandler, CodeCompletionService completionService) {
        this.originalHandler = originalHandler;
        this.completionService = completionService;
    }

    @Override
    protected void doExecute(@NotNull Editor editor, @Nullable Caret caret, DataContext dataContext) {
        // 先执行原始的回车操作
        if (originalHandler != null) {
            originalHandler.execute(editor, caret, dataContext);
        }

        // 然后触发代码补全
        Project project = editor.getProject();
        if (project != null && !project.isDisposed()) {
            // 延迟一点执行，确保回车已经插入
            ApplicationManager.getApplication().invokeLater(() -> {
                completionService.requestCompletion(editor, project);
            });
        }
    }
}