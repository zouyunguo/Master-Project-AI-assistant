package mp25.aiassistant.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
/**
 *  Implementations of a sidebar tool window in IntelliJ IDEA
 *  This class creates and manages the content of the sidebar window
 */
public class SideBarWindow implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        // This method is called to create the content of the tool window.
        // You can add components to the tool window here.
        // For example, you can create a panel and add it to the tool window.
        // toolWindow.getComponent().add(createPanel());
        MainLayer mainlayer =new MainLayer();
        JPanel panel =mainlayer.getMainPanel();
        panel.setPreferredSize(new Dimension(80, 60));
        Content content=ContentFactory.getInstance().createContent(panel,"",false);
        toolWindow.getContentManager().addContent(content);
    }

}
