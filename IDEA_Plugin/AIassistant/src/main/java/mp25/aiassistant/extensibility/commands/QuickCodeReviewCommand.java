package mp25.aiassistant.extensibility.commands;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import mp25.aiassistant.extensibility.IShortcutCommand;

/**
 * Example implementation of a shortcut command for quick code review
 * Demonstrates how to implement the IShortcutCommand interface
 */
public class QuickCodeReviewCommand implements IShortcutCommand {
    
    private static final Logger LOG = Logger.getInstance(QuickCodeReviewCommand.class);
    private final QuickCodeReviewAction action;
    
    public QuickCodeReviewCommand() {
        this.action = new QuickCodeReviewAction();
    }
    
    @Override
    public String getId() {
        return "mp25.aiassistant.QuickCodeReview";
    }
    
    @Override
    public String getName() {
        return "Quick Code Review";
    }
    
    @Override
    public String getDescription() {
        return "Perform a quick AI-powered code review of the current file";
    }
    
    @Override
    public String getKeybinding() {
        return "ctrl shift R";
    }
    
    @Override
    public String getCategory() {
        return "Code Quality";
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }
    
    @Override
    public void execute(AnActionEvent event) {
        action.actionPerformed(event);
    }
    
    @Override
    public AnAction getAction() {
        return action;
    }
    
    /**
     * Inner AnAction class for IntelliJ integration
     */
    private static class QuickCodeReviewAction extends AnAction {
        
        @Override
        public void actionPerformed(AnActionEvent event) {
            Project project = event.getProject();
            if (project == null) {
                Messages.showErrorDialog("No project found", "Quick Code Review");
                return;
            }
            
            Editor editor = event.getData(com.intellij.openapi.actionSystem.CommonDataKeys.EDITOR);
            if (editor == null) {
                Messages.showErrorDialog("No editor found", "Quick Code Review");
                return;
            }
            
            // Get selected text or entire file content
            String selectedText = editor.getSelectionModel().getSelectedText();
            String contentToReview = selectedText != null ? selectedText : editor.getDocument().getText();
            
            if (contentToReview.trim().isEmpty()) {
                Messages.showWarningDialog("No content to review", "Quick Code Review");
                return;
            }
            
            LOG.info("Starting quick code review for project: " + project.getName());
            
            // Show a simple dialog for now - in a real implementation, this would call AI service
            String reviewResult = performCodeReview(contentToReview);
            Messages.showInfoMessage(project, reviewResult, "Code Review Result");
        }
        
        @Override
        public void update(AnActionEvent event) {
            // Only enable when there's an active editor
            Presentation presentation = event.getPresentation();
            presentation.setEnabled(event.getData(com.intellij.openapi.actionSystem.CommonDataKeys.EDITOR) != null);
        }
        
        private String performCodeReview(String code) {
            // Simple mock review - in real implementation, this would call AI service
            StringBuilder review = new StringBuilder();
            review.append("Code Review Results:\n\n");
            
            if (code.length() > 100) {
                review.append("[OK] Code length is reasonable\n");
            } else {
                review.append("[WARNING] Code might be too short for meaningful review\n");
            }
            
            if (code.contains("TODO") || code.contains("FIXME")) {
                review.append("[WARNING] Contains TODO/FIXME comments\n");
            }
            
            if (code.contains("public class") || code.contains("public interface")) {
                review.append("[OK] Good use of public modifiers\n");
            }
            
            review.append("\nThis is a mock review. In the full implementation, ");
            review.append("this would call the AI service for comprehensive analysis.");
            
            return review.toString();
        }
    }
}
