package mp25.aiassistant;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBScrollPane;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
public class MainLayer {
    private JPanel MainPanel;
    private JButton agreeButton;
    private JButton disagreeButton;
    private JPanel Mainbody;
    private JTextPane PolicyText;
    private String text = "To provide you with a better and more personalized experience,"
            + "we collect anonymized usage data, such as feature interactions frequencies,"
            + "performance metrics, and error reports."
            + "This information helps us optimize the plugin and fix issues.\n\n"
            + "Your privacy is important to us—all data is aggregated and cannot be used to identify you."
            + "You can enable or disable data collection in";
    public MainLayer() {

        StyledDocument doc = PolicyText.getStyledDocument();

        try {

            SimpleAttributeSet normalStyle = new SimpleAttributeSet();
            StyleConstants.setFontFamily(normalStyle, "Arial");
            StyleConstants.setFontSize(normalStyle, 16);
            StyleConstants.setLineSpacing(normalStyle, 10.5f);

            SimpleAttributeSet hyperlinkStyle = new SimpleAttributeSet();
            StyleConstants.setFontFamily(hyperlinkStyle, "Arial");
            StyleConstants.setFontSize(hyperlinkStyle, 16);
            StyleConstants.setForeground(hyperlinkStyle, Color.CYAN);
            StyleConstants.setUnderline(hyperlinkStyle, true);
            StyleConstants.setLineSpacing(hyperlinkStyle, 10.5f);
            doc.insertString(doc.getLength(), text, normalStyle);


            doc.insertString(doc.getLength(), " Settings", hyperlinkStyle);

            doc.insertString(doc.getLength()," at any time.\n\n" +
                    "By Clicking the Agree button below, you agree to this data collection plan. By Clicking Disagree, your usage data will not be collected and tranmitted to us. " +
                    "For more details, please review our Privacy Policy.", normalStyle);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        agreeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Mainbody.removeAll();
                Mainbody.setLayout(new BorderLayout());

                // Create the panel for the chats
                JPanel chatPanel = new JPanel();
                chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS)); // 垂直布局
                chatPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                chatPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

                // put chatpanel to a scrollable pane
                JBScrollPane chatScrollPane = new JBScrollPane(chatPanel);
                chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                chatScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

                // prompt input field
                JTextField inputField = new JTextField();
                JPanel bottomPanel = new JPanel();
                bottomPanel.setLayout(new BorderLayout());

                JPanel buttonPanel = new JPanel();
                buttonPanel.setLayout(new BorderLayout());


                String[] models = {"codellama", "gpt-4o", "deepseek"};
                ComboBox<String> modelSelector = new ComboBox<>(models);
                modelSelector.setSelectedIndex(0); // 默认选择第一个模型

                // button for adding reference
                JButton addReferenceButton = new JButton("Add Reference");
                buttonPanel.add(addReferenceButton, BorderLayout.WEST);

                // create button for sending prompt
                JButton sendButton = new JButton("Send");

                JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                rightPanel.add(modelSelector);
                rightPanel.add(sendButton);
                buttonPanel.add(rightPanel, BorderLayout.EAST);

                // actionlistener for sendbutton
                sendButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String userInput = inputField.getText();
                        if (!userInput.isEmpty()) {
                            // create the text area for the inputed prompt
                            JTextArea inputArea = new JTextArea("Userinput: " + userInput);
                            inputArea.setEditable(false);
                            inputArea.setLineWrap(true);
                            inputArea.setWrapStyleWord(true);
                            inputArea.setBackground(new Color(43, 43, 43));
                            JBScrollPane inputScrollPane = new JBScrollPane(inputArea);
                            inputScrollPane.setPreferredSize(new Dimension(400, 200)); // 设置固定大小
                            // create text area for the answer by llms
                            JTextArea answerArea = new JTextArea("answer: this is a answer example.");
                            answerArea.setEditable(false);
                            answerArea.setLineWrap(true);
                            answerArea.setBackground(new Color(60, 63, 65));
                            answerArea.setWrapStyleWord(true);
                            JBScrollPane answerScrollPane = new JBScrollPane(answerArea);
                            answerScrollPane.setPreferredSize(new Dimension(400, 200)); // 设置固定大小

                            chatPanel.add(inputScrollPane);
                            chatPanel.add(answerScrollPane);

                            // refresh chatPanel
                            chatPanel.revalidate();
                            chatPanel.repaint();

                            // scroll to the bottom
                            SwingUtilities.invokeLater(() -> {
                                JScrollBar verticalBar = chatScrollPane.getVerticalScrollBar();
                                verticalBar.setValue(verticalBar.getMaximum());
                            });

                            // clear the input field
                            inputField.setText("");
                        }
                    }
                });

                bottomPanel.add(inputField, BorderLayout.NORTH);
                bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
                Mainbody.add(chatScrollPane, BorderLayout.CENTER);
                Mainbody.add(bottomPanel, BorderLayout.SOUTH);

                // refresh Mainbody
                Mainbody.revalidate();
                Mainbody.repaint();
            }
        });
    }

    public JPanel getMainPanel() {
        return MainPanel;
    }

    private void createUIComponents() {
        MainPanel = new JPanel();
        MainPanel.setLayout(new BorderLayout());
        JButton button = new JButton("Click me");
        MainPanel.add(button);
    }
}
