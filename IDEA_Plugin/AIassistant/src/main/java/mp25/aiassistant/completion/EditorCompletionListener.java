package mp25.aiassistant.completion;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class EditorCompletionListener implements CaretListener {
    private static final long DEBOUNCE_DELAY_MS = 1000;
    private final CodeCompletionService completionService;
    private final ScheduledExecutorService scheduler;
    private ScheduledFuture<?> scheduledTask;

    public EditorCompletionListener() {
        this.completionService = new CodeCompletionService(new CompletionInlayManager());
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void caretPositionChanged(@NotNull CaretEvent event) {
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
        }

        Editor editor = event.getEditor();
        Project project = editor.getProject();

        if (project == null) {
            return;
        }

        scheduledTask = scheduler.schedule(
            () -> ApplicationManager.getApplication().invokeLater(() -> {
                if (!project.isDisposed()) {
                    completionService.requestCompletion(editor, project);
                }
            }),
            DEBOUNCE_DELAY_MS,
            TimeUnit.MILLISECONDS
        );
    }

    public void dispose() {
        if (!scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }
} 