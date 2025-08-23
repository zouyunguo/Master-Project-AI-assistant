package mp25.aiassistant.extensibility;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Unified manager for all extensible features
 * Manages shortcut commands and context menu items in a centralized way
 */
public class ExtensibleFeatureManager {
    
    private static final Logger LOG = Logger.getInstance(ExtensibleFeatureManager.class);
    private static ExtensibleFeatureManager instance;
    
    private final Map<String, IShortcutCommand> shortcutCommands = new ConcurrentHashMap<>();
    private final Map<String, IContextMenuItem> contextMenuItems = new ConcurrentHashMap<>();
    private final Map<String, Boolean> featureStates = new ConcurrentHashMap<>();
    
    private ExtensibleFeatureManager() {}
    
    /**
     * Get the singleton instance
     */
    public static synchronized ExtensibleFeatureManager getInstance() {
        if (instance == null) {
            instance = new ExtensibleFeatureManager();
        }
        return instance;
    }
    
    // ==================== Shortcut Commands Management ====================
    
    /**
     * Register a new shortcut command
     */
    public void registerShortcutCommand(IShortcutCommand command) {
        if (command == null || command.getId() == null) {
            LOG.warn("Attempted to register null or invalid shortcut command");
            return;
        }
        
        shortcutCommands.put(command.getId(), command);
        featureStates.put(command.getId(), command.isEnabled());
        LOG.info("Registered shortcut command: " + command.getName());
    }
    
    /**
     * Unregister a shortcut command
     */
    public void unregisterShortcutCommand(String commandId) {
        IShortcutCommand removed = shortcutCommands.remove(commandId);
        if (removed != null) {
            featureStates.remove(commandId);
            LOG.info("Unregistered shortcut command: " + removed.getName());
        }
    }
    
    /**
     * Get all registered shortcut commands
     */
    public Collection<IShortcutCommand> getAllShortcutCommands() {
        return shortcutCommands.values();
    }
    
    /**
     * Get shortcut command by ID
     */
    public IShortcutCommand getShortcutCommand(String commandId) {
        return shortcutCommands.get(commandId);
    }
    
    // ==================== Context Menu Items Management ====================
    
    /**
     * Register a new context menu item
     */
    public void registerContextMenuItem(IContextMenuItem item) {
        if (item == null || item.getId() == null) {
            LOG.warn("Attempted to register null or invalid context menu item");
            return;
        }
        
        contextMenuItems.put(item.getId(), item);
        featureStates.put(item.getId(), item.isEnabled());
        LOG.info("Registered context menu item: " + item.getName());
    }
    
    /**
     * Unregister a context menu item
     */
    public void unregisterContextMenuItem(String itemId) {
        IContextMenuItem removed = contextMenuItems.remove(itemId);
        if (removed != null) {
            featureStates.remove(itemId);
            LOG.info("Unregistered context menu item: " + removed.getName());
        }
    }
    
    /**
     * Get all registered context menu items
     */
    public Collection<IContextMenuItem> getAllContextMenuItems() {
        return contextMenuItems.values();
    }
    
    /**
     * Get context menu items for a specific group
     */
    public List<IContextMenuItem> getContextMenuItemsByGroup(String group) {
        return contextMenuItems.values().stream()
                .filter(item -> group.equals(item.getGroup()))
                .sorted(Comparator.comparingInt(IContextMenuItem::getOrder))
                .toList();
    }
    
    /**
     * Get context menu item by ID
     */
    public IContextMenuItem getContextMenuItem(String itemId) {
        return contextMenuItems.get(itemId);
    }
    
    // ==================== Feature State Management ====================
    
    /**
     * Toggle a feature on/off
     */
    public void toggleFeature(String featureId) {
        Boolean currentState = featureStates.get(featureId);
        if (currentState != null) {
            boolean newState = !currentState;
            featureStates.put(featureId, newState);
            LOG.info("Toggled feature " + featureId + " to: " + newState);
        }
    }
    
    /**
     * Check if a feature is enabled
     */
    public boolean isFeatureEnabled(String featureId) {
        return featureStates.getOrDefault(featureId, false);
    }
    
    /**
     * Enable/disable a feature
     */
    public void setFeatureEnabled(String featureId, boolean enabled) {
        featureStates.put(featureId, enabled);
        LOG.info("Set feature " + featureId + " to: " + enabled);
    }
    
    // ==================== Utility Methods ====================
    
    /**
     * Get all registered feature IDs
     */
    public Set<String> getAllFeatureIds() {
        Set<String> allIds = new HashSet<>();
        allIds.addAll(shortcutCommands.keySet());
        allIds.addAll(contextMenuItems.keySet());
        return allIds;
    }
    
    /**
     * Get feature count statistics
     */
    public Map<String, Integer> getFeatureCounts() {
        Map<String, Integer> counts = new HashMap<>();
        counts.put("shortcutCommands", shortcutCommands.size());
        counts.put("contextMenuItems", contextMenuItems.size());
        counts.put("totalFeatures", shortcutCommands.size() + contextMenuItems.size());
        return counts;
    }
    
    /**
     * Clean up all registered features
     */
    public void cleanup() {
        shortcutCommands.clear();
        contextMenuItems.clear();
        featureStates.clear();
        LOG.info("Cleaned up all registered features");
    }
    
    /**
     * Validate feature configuration
     */
    public List<String> validateFeatures() {
        List<String> issues = new ArrayList<>();
        
        // Check for duplicate IDs
        Set<String> allIds = getAllFeatureIds();
        if (allIds.size() != shortcutCommands.size() + contextMenuItems.size()) {
            issues.add("Duplicate feature IDs detected");
        }
        
        // Check for invalid shortcuts
        for (IShortcutCommand cmd : shortcutCommands.values()) {
            if (cmd.getKeybinding() == null || cmd.getKeybinding().trim().isEmpty()) {
                issues.add("Shortcut command " + cmd.getId() + " has no keybinding");
            }
        }
        
        return issues;
    }
}
