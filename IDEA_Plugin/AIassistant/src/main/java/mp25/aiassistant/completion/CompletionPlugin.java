package mp25.aiassistant.completion;

import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CompletionPlugin implements ProjectActivity {
    @Nullable
    @Override
    public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        EditorCompletionListener listener = new EditorCompletionListener();
        EditorFactory.getInstance().getEventMulticaster().addCaretListener(listener, project);
        return Unit.INSTANCE; // 表示协程执行完成
    }
}
