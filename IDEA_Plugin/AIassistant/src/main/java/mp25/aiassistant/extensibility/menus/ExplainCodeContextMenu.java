package mp25.aiassistant.extensibility.menus;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import mp25.aiassistant.extensibility.IContextMenuItem;

/**
 * Example implementation of a context menu item for explaining code
 * Demonstrates how to implement the IContextMenuItem interface
 */
public class ExplainCodeContextMenu implements IContextMenuItem {
    
    private static final Logger LOG = Logger.getInstance(ExplainCodeContextMenu.class);
    private final ExplainCodeAction action;
    
    public ExplainCodeContextMenu() {
        this.action = new ExplainCodeAction();
    }
    
    @Override
    public String getId() {
        return "mp25.aiassistant.ExplainCode";
    }
    
    @Override
    public String getName() {
        return "Explain Code";
    }
    
    @Override
    public String getDescription() {
        return "Get AI explanation of the selected code";
    }
    
    @Override
    public String getTitle() {
        return "Explain Code";
    }
    
    @Override
    public String getIcon() {
        return "AllIcons.Actions.Help"; // IntelliJ built-in icon
    }
    
    @Override
    public String getWhen() {
        return "editorHasSelection"; // Only show when text is selected
    }
    
    @Override
    public String getGroup() {
        return "AI Assistant";
    }
    
    @Override
    public int getOrder() {
        return 1; // First item in the group
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
    private static class ExplainCodeAction extends AnAction {
        
        @Override
        public void actionPerformed(AnActionEvent event) {
            Project project = event.getProject();
            if (project == null) {
                Messages.showErrorDialog("No project found", "Explain Code");
                return;
            }
            
            Editor editor = event.getData(com.intellij.openapi.actionSystem.CommonDataKeys.EDITOR);
            if (editor == null) {
                Messages.showErrorDialog("No editor found", "Explain Code");
                return;
            }
            
            String selectedText = editor.getSelectionModel().getSelectedText();
            if (selectedText == null || selectedText.trim().isEmpty()) {
                Messages.showWarningDialog("Please select some code to explain", "Explain Code");
                return;
            }
            
            LOG.info("Explaining code for project: " + project.getName());
            
            // Show explanation dialog - in real implementation, this would call AI service
            String explanation = generateExplanation(selectedText);
            Messages.showInfoMessage(project, explanation, "Code Explanation");
        }
        
        @Override
        public void update(AnActionEvent event) {
            // Only enable when there's selected text
            Presentation presentation = event.getPresentation();
            Editor editor = event.getData(com.intellij.openapi.actionSystem.CommonDataKeys.EDITOR);
            boolean hasSelection = editor != null && 
                                 editor.getSelectionModel().getSelectedText() != null &&
                                 !editor.getSelectionModel().getSelectedText().trim().isEmpty();
            presentation.setEnabled(hasSelection);
        }
        
        private String generateExplanation(String code) {
            // Simple mock explanation - in real implementation, this would call AI service
            StringBuilder explanation = new StringBuilder();
            explanation.append("Code Explanation:\n\n");
            
            if (code.contains("public class")) {
                explanation.append("This appears to be a Java class definition.\n");
            } else if (code.contains("public interface")) {
                explanation.append("This appears to be a Java interface definition.\n");
            } else if (code.contains("public static void main")) {
                explanation.append("This appears to be a Java main method.\n");
            } else if (code.contains("function") || code.contains("def ")) {
                explanation.append("This appears to be a function definition.\n");
            }
            
            explanation.append("\nCode length: ").append(code.length()).append(" characters\n");
            explanation.append("Lines: ").append(code.split("\n").length).append("\n\n");
            
            explanation.append("This is a mock explanation. In the full implementation, ");
            explanation.append("this would call the AI service for detailed code analysis.");
            
            return explanation.toString();
        }
    }
}
