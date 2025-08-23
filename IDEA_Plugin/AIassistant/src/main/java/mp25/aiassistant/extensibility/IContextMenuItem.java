package mp25.aiassistant.extensibility;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Interface for extensible context menu items
 * Defines the standard structure for all context menu-based features
 */
public interface IContextMenuItem {
    
    /**
     * Unique identifier for the menu item
     */
    String getId();
    
    /**
     * Display name for the menu item
     */
    String getName();
    
     /**
     * Description of what the menu item does
     */
    String getDescription();
    
    /**
     * Title shown in the context menu
     */
    String getTitle();
    
    /**
     * Icon for the menu item (can be null)
     */
    String getIcon();
    
    /**
     * Condition when this item should be visible
     * e.g., "editorHasSelection", "fileType:java"
     */
    String getWhen();
    
    /**
     * Group for organizing menu items
     */
    String getGroup();
    
    /**
     * Order within the group (lower numbers appear first)
     */
    int getOrder();
    
    /**
     * Whether the menu item is currently enabled
     */
    boolean isEnabled();
    
    /**
     * Execute the menu item action
     */
    void execute(AnActionEvent event);
    
    /**
     * Get the underlying AnAction for IntelliJ integration
     */
    AnAction getAction();
}
