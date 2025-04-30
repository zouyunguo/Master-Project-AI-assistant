package mp25.aiassistant;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainLayer {
    private JPanel MainPanel;
    private JTextArea toProvideYouWithTextArea;
    private JButton button1;
    private JButton button3;
    private JPanel Mainbody;

    public MainLayer() {
        Dimension fixedSize = new Dimension(800, 600);
        MainPanel.setPreferredSize(fixedSize);
        MainPanel.setMinimumSize(fixedSize);
        MainPanel.setMaximumSize(fixedSize);
    }

    public JPanel getMainPanel() {
        return MainPanel;
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}
