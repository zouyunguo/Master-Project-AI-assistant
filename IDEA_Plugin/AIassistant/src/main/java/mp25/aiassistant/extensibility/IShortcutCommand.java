package mp25.aiassistant.extensibility;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Interface for extensible shortcut commands
 * Defines the standard structure for all shortcut-based features
 */
public interface IShortcutCommand {
    
    /**
     * Unique identifier for the command
     */
    String getId();
    
    /**
     * Display name for the command
     */
    String getName();
    
    /**
     * Description of what the command does
     */
    String getDescription();
    
    /**
     * Keyboard shortcut (e.g., "ctrl shift R")
     */
    String getKeybinding();
    
    /**
     * Category for grouping commands
     */
    String getCategory();
    
    /**
     * Whether the command is currently enabled
     */
    boolean isEnabled();
    
    /**
     * Execute the command action
     */
    void execute(AnActionEvent event);
    
    /**
     * Get the underlying AnAction for IntelliJ integration
     */
    AnAction getAction();
}
