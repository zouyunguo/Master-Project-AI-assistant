package mp25.aiassistant.completion;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TabKeyHandler extends EditorActionHandler {
    private final EditorActionHandler originalHandler;
    private final CompletionInlayManager inlayManager;

    public TabKeyHandler(EditorActionHandler originalHandler, CompletionInlayManager inlayManager) {
        this.originalHandler = originalHandler;
        this.inlayManager = inlayManager;
    }

    @Override
    protected void doExecute(@NotNull Editor editor, @Nullable Caret caret, DataContext dataContext) {
        // 如果有活动的补全预览，接受它
        if (inlayManager.hasActiveCompletion()) {
            String completion = inlayManager.getCurrentCompletion();
            Project project = editor.getProject();

            if (completion != null && project != null) {
                // 清除预览
                inlayManager.clearPreview(editor);

                // 插入补全内容
                WriteCommandAction.runWriteCommandAction(project, () -> {
                    int offset = editor.getCaretModel().getOffset();

                    editor.getDocument().insertString(offset, completion);
                    editor.getCaretModel().moveToOffset(offset + completion.length());
                });

                return;
            }
        }

        // 否则执行原始的Tab功能
        if (originalHandler != null) {
            originalHandler.execute(editor, caret, dataContext);
        }
    }
}