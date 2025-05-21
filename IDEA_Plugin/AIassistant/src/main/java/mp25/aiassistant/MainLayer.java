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
import java.io.File;
import java.util.concurrent.CompletableFuture;
import com.intellij.icons.AllIcons;
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

    // Reference tracking
    private File selectedReferenceFile = null;

    public MainLayer() {

        StyledDocument doc = PolicyText.getStyledDocument();

        try {
            agreeButton.setBackground(new Color(46, 110, 246));
            SimpleAttributeSet normalStyle = new SimpleAttributeSet();
            StyleConstants.setFontFamily(normalStyle, "Arial");
            StyleConstants.setFontSize(normalStyle, 16);
            StyleConstants.setLineSpacing(normalStyle, 10.5f);

            SimpleAttributeSet agreeStyle = new SimpleAttributeSet();
            StyleConstants.setFontFamily(agreeStyle, "Arial");
            StyleConstants.setFontSize(agreeStyle, 16);
            StyleConstants.setForeground(agreeStyle, new Color(40, 132, 46)); // 设置 "Agree" 为绿色
            StyleConstants.setBold(agreeStyle, true);

            SimpleAttributeSet disagreeStyle = new SimpleAttributeSet();
            StyleConstants.setFontFamily(disagreeStyle, "Arial");
            StyleConstants.setFontSize(disagreeStyle, 16);
            StyleConstants.setForeground(disagreeStyle, new Color(168, 28, 28)); // 设置 "Agree" 为绿色
            StyleConstants.setBold(disagreeStyle, true);

            SimpleAttributeSet hyperlinkStyle = new SimpleAttributeSet();
            StyleConstants.setFontFamily(hyperlinkStyle, "Arial");
            StyleConstants.setFontSize(hyperlinkStyle, 16);
            StyleConstants.setForeground(hyperlinkStyle,  new Color(46, 110, 246));
            StyleConstants.setUnderline(hyperlinkStyle, false);
            StyleConstants.setLineSpacing(hyperlinkStyle, 10.5f);
            doc.insertString(doc.getLength(), text, normalStyle);

            doc.insertString(doc.getLength(), " Settings", hyperlinkStyle);

            doc.insertString(doc.getLength()," at any time.\n\n", normalStyle);
            doc.insertString(doc.getLength(), "By Clicking the ", normalStyle);
            doc.insertString(doc.getLength(), "Agree", agreeStyle); // 设置 "Agree" 样式
            doc.insertString(doc.getLength(), " button below, you agree to this data collection plan. By Clicking ", normalStyle);

            doc.insertString(doc.getLength(), "Disagree", disagreeStyle); // 设置 "Disagree" 样式
            doc.insertString(doc.getLength(), ", your usage data will not be collected and tranmitted to us. For more details, please review our", normalStyle);
            doc.insertString(doc.getLength(), " Privacy Policy", hyperlinkStyle);

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
                chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
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

                // Models available in Ollama - update this list as needed
                String[] models = {"llama2", "codellama", "mistral", "gemma"};
                ComboBox<String> modelSelector = new ComboBox<>(models);
                modelSelector.setSelectedIndex(0); // Default to first model

                // Status text to show when processing
                JLabel statusLabel = new JLabel("");
                statusLabel.setForeground(Color.GRAY);

                // button for adding reference
                JButton addReferenceButton = new JButton();
                addReferenceButton.setIcon(AllIcons.General.Add);


                JPanel leftpanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                leftpanel.add (addReferenceButton);

                buttonPanel.add(leftpanel, BorderLayout.WEST);
                // Add file chooser for references
                addReferenceButton.addActionListener(e1 -> {
                    JFileChooser fileChooser = new JFileChooser();
                    int result = fileChooser.showOpenDialog(MainPanel);
                    if (result == JFileChooser.APPROVE_OPTION) {
                        selectedReferenceFile = fileChooser.getSelectedFile();
                        statusLabel.setText("Reference added: " + selectedReferenceFile.getName());
                    }
                });

                // create button for sending prompt
                JButton sendButton = new JButton();
                sendButton.setIcon(AllIcons.Debugger.PromptInput);

                JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                rightPanel.add(statusLabel);
                rightPanel.add(modelSelector);
                rightPanel.add(sendButton);
                buttonPanel.add(rightPanel, BorderLayout.EAST);

                // actionlistener for sendbutton
                sendButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String userInput = inputField.getText();
                        if (!userInput.isEmpty()) {
                            // Create composite prompt with reference if selected
                            String fullPrompt = userInput;
                            if (selectedReferenceFile != null) {
                                fullPrompt = "Reference file: " + selectedReferenceFile.getName() + "\n\n" + userInput;
                                // Note: In a real implementation, you'd need to read the file content
                                // and pass it to the LLM along with the prompt
                            }

                            // create the text area for the inputed prompt
                            JTextArea inputArea = new JTextArea("Userinput: " + userInput);
                            inputArea.setEditable(false);
                            inputArea.setLineWrap(true);
                            inputArea.setWrapStyleWord(true);
                            inputArea.setBackground(new Color(43, 45, 48));
                            inputArea.setPreferredSize(new Dimension(350, Math.max(50, 20 * userInput.length() / 40)));
                            inputArea.setMargin(new Insets(0, 0, 25, 25));
                            inputArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // 设置边框为空，确保内边距生效

                            // create placeholder text area for the answer
                            JTextArea answerArea = new JTextArea("Processing...");
                            answerArea.setEditable(false);
                            answerArea.setLineWrap(true);
                            answerArea.setBackground(new Color(60, 63, 65));
                            answerArea.setWrapStyleWord(true);
                            answerArea.setMargin(new Insets(0, 0, 25, 25));
                            answerArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // 设置边框为空，确保内边距生效



                            chatPanel.add(inputArea);
                            chatPanel.add(answerArea);
                            // Update UI to show we're processing
                            sendButton.setEnabled(false);
                            statusLabel.setText("Generating response...");


                            // Call Ollama API asynchronously
                            String selectedModel = (String) modelSelector.getSelectedItem();
                            CompletableFuture<String> futureResponse = OllamaService.generateResponse(selectedModel, fullPrompt);

                            // Handle the response when it arrives
                            futureResponse.thenAccept(response -> {
                                // Update UI on EDT
                                SwingUtilities.invokeLater(() -> {
                                    answerArea.setText(response);

                                    // Calculate a reasonable height based on content
                                    int lineCount = response.split("\n").length;
                                    int preferredHeight = Math.max(50, lineCount * 20);
                                    answerArea.setPreferredSize(new Dimension(350, preferredHeight));

                                    // Re-enable the send button
                                    sendButton.setEnabled(true);
                                    statusLabel.setText("");

                                    // Reset reference file
                                    selectedReferenceFile = null;

                                    // refresh chatPanel
                                    chatPanel.revalidate();
                                    chatPanel.repaint();

                                    // scroll to see the latest message
                                    SwingUtilities.invokeLater(() -> {
                                        JScrollBar verticalBar = chatScrollPane.getVerticalScrollBar();
                                        verticalBar.setValue(verticalBar.getMaximum());
                                    });
                                });
                            }).exceptionally(ex -> {
                                // Handle errors
                                SwingUtilities.invokeLater(() -> {
                                    answerArea.setText("Error: " + ex.getMessage());
                                    sendButton.setEnabled(true);
                                    statusLabel.setText("Error occurred");
                                });
                                return null;
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

        // Add functionality to the disagree button
        disagreeButton.addActionListener(e -> {
            // Similar to agree button but set a flag to disable data collection
            agreeButton.doClick(); // Reuse the UI setup code
            // In a real app, you would set a preference to disable data collection
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