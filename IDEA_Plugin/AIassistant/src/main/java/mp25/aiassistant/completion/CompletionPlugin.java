package mp25.aiassistant.completion;

import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.editor.actionSystem.EditorActionManager;
import com.intellij.openapi.editor.actions.EnterAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CompletionPlugin implements ProjectActivity {
    private static boolean initialized = false;

    @Nullable
    @Override
    public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        if (!initialized) {
            initialized = true;

            // 获取EditorActionManager
            EditorActionManager actionManager = EditorActionManager.getInstance();

            // 获取原始的Enter动作处理器
            EditorActionHandler originalEnterHandler = actionManager.getActionHandler("EditorEnter");

            // 创建我们的处理器
            EnterKeyHandler enterKeyHandler = new EnterKeyHandler(originalEnterHandler);

            // 替换Enter动作的处理器
            actionManager.setActionHandler("EditorEnter", enterKeyHandler);
        }

        return Unit.INSTANCE;
    }
}