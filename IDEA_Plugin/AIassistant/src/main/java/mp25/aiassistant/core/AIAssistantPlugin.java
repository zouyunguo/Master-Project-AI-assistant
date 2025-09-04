package mp25.aiassistant.core;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import mp25.aiassistant.extensibility.DefaultExtensibleFeatures;
import mp25.aiassistant.extensibility.ExtensibleFeatureManager;

import javax.swing.*;

/**
 * Main AI Assistant Plugin class
 * Integrates the extensible feature system
 */
public class AIAssistantPlugin implements StartupActivity {
    
    private JTextField textField1;
    
    @Override
    public void runActivity(Project project) {
        // Initialize the extensible feature system
        initializeExtensibleFeatures();
        
        System.out.println("AI Assistant Plugin initialized for project: " + project.getName());
    }
    
    /**
     * Initialize all extensible features
     */
    private void initializeExtensibleFeatures() {
        try {
            // Initialize extensible features system
            ExtensibleFeatureManager.initialize();

            // Register default features
            DefaultExtensibleFeatures.registerDefaultFeatures();
            
            // Log successful initialization
            var manager = ExtensibleFeatureManager.getInstance();
            var counts = manager.getFeatureCounts();
            System.out.println("[SUCCESS] Extensible features initialized successfully:");
            System.out.println("   - " + counts.get("shortcutCommands") + " shortcut commands");
            System.out.println("   - " + counts.get("contextMenuItems") + " context menu items");
            
            // Validate features
            var issues = manager.validateFeatures();
            if (!issues.isEmpty()) {
                System.out.println("[WARNING] Feature validation issues found:");
                for (String issue : issues) {
                    System.out.println("   - " + issue);
                }
            }
            
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to initialize extensible features: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Cleanup method for plugin shutdown
     */
    public void cleanup() {
        try {
            // Unregister all features
            DefaultExtensibleFeatures.unregisterDefaultFeatures();
            
            // Cleanup the manager
            var manager = ExtensibleFeatureManager.getInstance();
            manager.cleanup();
            
            System.out.println("[SUCCESS] Extensible features cleaned up successfully");

        } catch (Exception e) {
            System.err.println("[ERROR] Failed to cleanup extensible features: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
