package mp25.aiassistant.extensibility;

import mp25.aiassistant.extensibility.commands.QuickCodeReviewCommand;
import mp25.aiassistant.extensibility.menus.ExplainCodeContextMenu;
import mp25.aiassistant.extensibility.menus.OptimizeCodeContextMenu;

/**
 * Default extensible features registration
 * Registers all predefined shortcut commands and context menu items
 * Similar to DefaultExtensibleFeatures.ts in VS Code version
 */
public class DefaultExtensibleFeatures {
    
    private static final ExtensibleFeatureManager manager = ExtensibleFeatureManager.getInstance();
    
    /**
     * Register all default extensible features
     * This method should be called during plugin initialization
     */
    public static void registerDefaultFeatures() {
        registerShortcutCommands();
        registerContextMenuItems();
        
        // Log registration summary
        var counts = manager.getFeatureCounts();
        System.out.println("Registered " + counts.get("totalFeatures") + " default features:");
        System.out.println("  - " + counts.get("shortcutCommands") + " shortcut commands");
        System.out.println("  - " + counts.get("contextMenuItems") + " context menu items");
    }
    
    /**
     * Register all default shortcut commands
     */
    private static void registerShortcutCommands() {
        // Quick Code Review Command
        QuickCodeReviewCommand quickReviewCommand = new QuickCodeReviewCommand();
        manager.registerShortcutCommand(quickReviewCommand);
        
        // Add more shortcut commands here as needed
        // Example:
        // manager.registerShortcutCommand(new GenerateTestCommand());
        // manager.registerShortcutCommand(new FormatCodeCommand());
    }
    
    /**
     * Register all default context menu items
     */
    private static void registerContextMenuItems() {
        // Explain Code Context Menu
        ExplainCodeContextMenu explainCodeMenu = new ExplainCodeContextMenu();
        manager.registerContextMenuItem(explainCodeMenu);
        
        // Optimize Code Context Menu
        OptimizeCodeContextMenu optimizeCodeMenu = new OptimizeCodeContextMenu();
        manager.registerContextMenuItem(optimizeCodeMenu);
        
        // Add more context menu items here as needed
        // Example:
        // manager.registerContextMenuItem(new GenerateDocumentationMenu());
        // manager.registerContextMenuItem(new SuggestRefactoringMenu());
    }
    
    /**
     * Unregister all default features
     * This method should be called during plugin cleanup
     */
    public static void unregisterDefaultFeatures() {
        // Get all registered feature IDs and unregister them
        var allFeatureIds = manager.getAllFeatureIds();
        for (String featureId : allFeatureIds) {
            if (featureId.contains("mp25.aiassistant.")) {
                // Only unregister our plugin's features
                if (featureId.contains("QuickCodeReview")) {
                    manager.unregisterShortcutCommand(featureId);
                } else if (featureId.contains("ExplainCode") || featureId.contains("OptimizeCode")) {
                    manager.unregisterContextMenuItem(featureId);
                }
            }
        }
        
        System.out.println("Unregistered all default features");
    }
    
    /**
     * Get the feature manager instance
     */
    public static ExtensibleFeatureManager getManager() {
        return manager;
    }
}
