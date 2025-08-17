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

public class CompletionPlugin implements ProjectActivity {
    private static boolean initialized = false;
    private static CompletionInlayManager inlayManager;
    private static CodeCompletionService completionService;

    @Nullable
    @Override
    public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        if (!initialized) {
            initialized = true;

            // 创建共享的管理器
            inlayManager = new CompletionInlayManager();
            completionService = new CodeCompletionService(inlayManager);

            // 获取EditorActionManager
            EditorActionManager actionManager = EditorActionManager.getInstance();

            // 替换Enter键处理器
            EditorActionHandler originalEnterHandler = actionManager.getActionHandler("EditorEnter");
            EnterKeyHandler enterKeyHandler = new EnterKeyHandler(originalEnterHandler, completionService);
            actionManager.setActionHandler("EditorEnter", enterKeyHandler);

            // 替换Tab键处理器
            EditorActionHandler originalTabHandler = actionManager.getActionHandler("EditorTab");
            TabKeyHandler tabKeyHandler = new TabKeyHandler(originalTabHandler, inlayManager);
            actionManager.setActionHandler("EditorTab", tabKeyHandler);

            EditorActionHandler originalEscHandler = actionManager.getActionHandler("EditorEscape");
            EscapeKeyHandler escapeKeyHandler = new EscapeKeyHandler(originalEscHandler, inlayManager);
            actionManager.setActionHandler("EditorEscape", escapeKeyHandler);

            // 为所有编辑器添加监听器
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