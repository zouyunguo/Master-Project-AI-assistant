package mp25.aiassistant.completion;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EscapeKeyHandler extends EditorActionHandler {
    private final EditorActionHandler originalHandler;
    private final CompletionInlayManager inlayManager;

    public EscapeKeyHandler(EditorActionHandler originalHandler, CompletionInlayManager inlayManager) {
        this.originalHandler = originalHandler;
        this.inlayManager = inlayManager;
    }

    @Override
    protected void doExecute(@NotNull Editor editor, @Nullable Caret caret, DataContext dataContext) {
        // 如果有活动的补全预览，清除它
        if (inlayManager.hasActiveCompletion()) {
            inlayManager.clearPreview(editor);
            //保证 IDE 状态一致，仍然执行原始处理器
            if (originalHandler != null) {
                originalHandler.execute(editor, caret, dataContext);
            }
            return;
        }

        // 否则执行原始的ESC功能
        if (originalHandler != null) {
            originalHandler.execute(editor, caret, dataContext);
        }
    }
}