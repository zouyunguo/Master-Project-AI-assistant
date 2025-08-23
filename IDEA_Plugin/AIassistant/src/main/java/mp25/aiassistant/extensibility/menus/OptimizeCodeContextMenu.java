package mp25.aiassistant.extensibility.menus;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import mp25.aiassistant.extensibility.IContextMenuItem;

/**
 * Example implementation of a context menu item for code optimization
 * Demonstrates conditional display based on file type
 */
public class OptimizeCodeContextMenu implements IContextMenuItem {
    
    private static final Logger LOG = Logger.getInstance(OptimizeCodeContextMenu.class);
    private final OptimizeCodeAction action;
    
    public OptimizeCodeContextMenu() {
        this.action = new OptimizeCodeAction();
    }
    
    @Override
    public String getId() {
        return "mp25.aiassistant.OptimizeCode";
    }
    
    @Override
    public String getName() {
        return "Optimize Code";
    }
    
    @Override
    public String getDescription() {
        return "Get AI suggestions for code optimization";
    }
    
    @Override
    public String getTitle() {
        return "Optimize Code";
    }
    
    @Override
    public String getIcon() {
        return "AllIcons.Actions.Refactor"; // IntelliJ built-in icon
    }
    
    @Override
    public String getWhen() {
        return "fileType:java,fileType:kt,fileType:py,fileType:js,fileType:ts"; // Only for specific file types
    }
    
    @Override
    public String getGroup() {
        return "AI Assistant";
    }
    
    @Override
    public int getOrder() {
        return 2; // Second item in the group
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
    private static class OptimizeCodeAction extends AnAction {
        
        @Override
        public void actionPerformed(AnActionEvent event) {
            Project project = event.getProject();
            if (project == null) {
                Messages.showErrorDialog("No project found", "Optimize Code");
                return;
            }
            
            Editor editor = event.getData(com.intellij.openapi.actionSystem.CommonDataKeys.EDITOR);
            if (editor == null) {
                Messages.showErrorDialog("No editor found", "Optimize Code");
                return;
            }
            
            VirtualFile file = event.getData(com.intellij.openapi.actionSystem.CommonDataKeys.VIRTUAL_FILE);
            if (file == null) {
                Messages.showErrorDialog("No file found", "Optimize Code");
                return;
            }
            
            String fileContent = editor.getDocument().getText();
            if (fileContent.trim().isEmpty()) {
                Messages.showWarningDialog("File is empty, nothing to optimize", "Optimize Code");
                return;
            }
            
            LOG.info("Optimizing code for file: " + file.getName());
            
            // Show optimization suggestions - in real implementation, this would call AI service
            String optimizationResult = generateOptimizationSuggestions(fileContent, file.getExtension());
            Messages.showInfoMessage(project, optimizationResult, "Code Optimization");
        }
        
        @Override
        public void update(AnActionEvent event) {
            // Only enable for supported file types
            Presentation presentation = event.getPresentation();
            VirtualFile file = event.getData(com.intellij.openapi.actionSystem.CommonDataKeys.VIRTUAL_FILE);
            boolean supportedFileType = isSupportedFileType(file);
            presentation.setEnabled(supportedFileType);
        }
        
        private boolean isSupportedFileType(VirtualFile file) {
            if (file == null) return false;
            String extension = file.getExtension();
            if (extension == null) return false;
            
            return extension.equalsIgnoreCase("java") ||
                   extension.equalsIgnoreCase("kt") ||
                   extension.equalsIgnoreCase("py") ||
                   extension.equalsIgnoreCase("js") ||
                   extension.equalsIgnoreCase("ts");
        }
        
        private String generateOptimizationSuggestions(String code, String fileExtension) {
            // Simple mock optimization - in real implementation, this would call AI service
            StringBuilder suggestions = new StringBuilder();
            suggestions.append("Code Optimization Suggestions:\n\n");
            
            suggestions.append("File type: ").append(fileExtension).append("\n\n");
            
            if (code.contains("public class")) {
                suggestions.append("✓ Consider using interfaces for better abstraction\n");
            }
            
            if (code.contains("TODO") || code.contains("FIXME")) {
                suggestions.append("⚠ Address TODO/FIXME comments for better code quality\n");
            }
            
            if (code.contains("import java.util.*")) {
                suggestions.append("⚠ Avoid wildcard imports, import specific classes\n");
            }
            
            if (code.length() > 500) {
                suggestions.append("⚠ Consider breaking large classes into smaller ones\n");
            }
            
            if (code.contains("System.out.println")) {
                suggestions.append("⚠ Use proper logging framework instead of System.out\n");
            }
            
            suggestions.append("\nThis is a mock optimization. In the full implementation, ");
            suggestions.append("this would call the AI service for comprehensive analysis.");
            
            return suggestions.toString();
        }
    }
}
