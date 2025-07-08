package mp25.aiassistant;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import mp25.aiassistant.chat.ChatSession;
import mp25.aiassistant.chat.SessionManager;
import mp25.aiassistant.Utils.MarkdownUtils;
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
    private JPanel sessionPanel;
    private JBList<ChatSession> sessionList;
    private JButton newSessionButton;
    private JButton renameSessionButton;
    private JButton deleteSessionButton;
    private final SessionManager sessionManager;
    private DefaultListModel<ChatSession> sessionListModel;
    private String text = "To provide you with a better and more personalized experience,"
            + "we collect anonymized usage data, such as feature interactions frequencies,"
            + "performance metrics, and error reports."
            + "This information helps us optimize the plugin and fix issues.\n\n"
            + "Your privacy is important to us—all data is aggregated and cannot be used to identify you."
            + "You can enable or disable data collection in";

    // Reference tracking
    private File selectedReferenceFile = null;

    public MainLayer() {
        sessionManager = new SessionManager();
        sessionListModel = new DefaultListModel<>();
        sessionList= new JBList<>(sessionListModel);
        sessionList.setModel(sessionListModel);
        sessionList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof ChatSession) {
                    setText(((ChatSession) value).getName());
                }
                return this;
            }
        });


        setupUI();
        //setupSessionControls();
    }

    private void setupUI() {
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
        agreeButton.addActionListener(e -> setupChatUI());
        agreeButton.addActionListener(e->setupSessionControls());
        disagreeButton.addActionListener(e -> setupChatUI());
        disagreeButton.addActionListener(e -> setupSessionControls());
    }

    private void setupSessionControls() {
        newSessionButton = new JButton("New Session");
        renameSessionButton = new JButton("Rename Session");
        deleteSessionButton = new JButton("Delete Session");
        newSessionButton.addActionListener(e -> {
            String name = Messages.showInputDialog(
                    MainPanel,
                    "Enter session name:",
                    "New Chat Session",
                    Messages.getQuestionIcon());
            if (name != null && !name.trim().isEmpty()) {
                ChatSession session = sessionManager.createNewSession(name.trim());
                updateSessionList();
                sessionList.setSelectedValue(session, true);
            }
        });

        renameSessionButton.addActionListener(e -> {
            ChatSession selected = sessionList.getSelectedValue();
            if (selected != null) {
                String newName = Messages.showInputDialog(
                        MainPanel,
                        "Enter new name:",
                        "Rename Session",
                        Messages.getQuestionIcon(),
                        selected.getName(),
                        null);
                if (newName != null && !newName.trim().isEmpty()) {
                    selected.setName(newName.trim());
                    updateSessionList();
                }
            }
        });

        deleteSessionButton.addActionListener(e -> {
            ChatSession selected = sessionList.getSelectedValue();
            if (selected != null) {
                int result = Messages.showYesNoDialog(
                        MainPanel,
                        "Are you sure you want to delete this session?",
                        "Delete Session",
                        Messages.getQuestionIcon());
                if (result == Messages.YES) {
                    sessionManager.removeSession(selected.getId());
                    updateSessionList();
                }
            }
        });

        sessionList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                ChatSession selected = sessionList.getSelectedValue();
                if (selected != null) {
                    sessionManager.setActiveSession(selected);
                    updateChatDisplay();
                }
            }
        });

        updateSessionList();
    }

    private void updateSessionList() {
        sessionListModel.clear();
        for (ChatSession session : sessionManager.getSessions()) {
            sessionListModel.addElement(session);
        }
        if (sessionManager.getActiveSession() != null) {
            sessionList.setSelectedValue(sessionManager.getActiveSession(), true);
        }
    }

    private void setupChatUI() {
        Mainbody.removeAll();
        Mainbody.setLayout(new BorderLayout());

        // Create the panel for the chats
        JPanel chatPanel = createChatPanel();
        JPanel outerPanel = new JPanel(new BorderLayout());
        outerPanel.add(chatPanel, BorderLayout.NORTH);

        // Add session panel to the top
        sessionPanel = new JPanel();
        sessionPanel.add(sessionList,BorderLayout.WEST);
        JPanel SessioncontrolPanel = new JPanel();
        SessioncontrolPanel.add(newSessionButton);
        SessioncontrolPanel.add(renameSessionButton);
        SessioncontrolPanel.add(deleteSessionButton);
        sessionPanel.add(SessioncontrolPanel,BorderLayout.EAST);
        Mainbody.add(sessionPanel, BorderLayout.NORTH);


        // put chatpanel to a scrollable pane
        JBScrollPane chatScrollPane = new JBScrollPane(outerPanel);
        chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        chatScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        setupInputPanel(chatPanel, chatScrollPane);

        Mainbody.add(chatScrollPane, BorderLayout.CENTER);
        Mainbody.revalidate();
        Mainbody.repaint();
    }

    private JPanel createChatPanel() {
        JPanel chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        chatPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        // Load existing messages if there are any
        ChatSession currentSession = sessionManager.getActiveSession();
        if (currentSession != null) {
            for (ChatSession.Message message : currentSession.getMessages()) {
                JTextArea messageArea = new JTextArea();
                messageArea.setEditable(false);
                messageArea.setLineWrap(true);
                messageArea.setWrapStyleWord(true);
                
                if (message.isUser()) {
                    messageArea.setText("User: " + message.getContent());
                    messageArea.setBackground(new Color(43, 45, 48));
                } else {
                    messageArea.setText("Assistant: " + message.getContent());
                    messageArea.setBackground(new Color(60, 63, 65));
                }
                
                messageArea.setMargin(new Insets(0, 0, 25, 25));
                messageArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                chatPanel.add(messageArea);
            }
        }
        return chatPanel;
    }

    private void setupInputPanel(JPanel chatPanel, JBScrollPane chatScrollPane) {
        JTextField inputField = new JTextField();
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BorderLayout());
        ComboBox<String> modelSelector = new ComboBox<>(new String[]{"none"}); // Default models
        modelSelector.setSelectedIndex(0);

        // Get list of models
        CompletableFuture<String[]> modelsFuture = OllamaService.getModels();
        modelsFuture.thenAccept(response -> {
            String[] models = response;
            for (int i = 0; i < models.length; i++) {
                models[i] = models[i].trim();
            }
            modelSelector.setModel(new DefaultComboBoxModel<>(models));
        }).exceptionally(ex -> {
            System.out.println("Error fetching models: " + ex.getMessage());
            return null;
        });

        // Status text
        JLabel statusLabel = new JLabel("");
        statusLabel.setForeground(Color.GRAY);

        // Reference button
        JButton addReferenceButton = new JButton();
        addReferenceButton.setIcon(AllIcons.General.Add);

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.add(addReferenceButton);
        buttonPanel.add(leftPanel, BorderLayout.WEST);

        addReferenceButton.addActionListener(action -> {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(MainPanel);
            if (result == JFileChooser.APPROVE_OPTION) {
                selectedReferenceFile = fileChooser.getSelectedFile();
                ReferenceProcessor.addReferenceFile(selectedReferenceFile);

                statusLabel.setText("Reference added: " + selectedReferenceFile.getName());
            }
        });

        // Send button
        JButton sendButton = new JButton();
        sendButton.setIcon(AllIcons.Debugger.PromptInput);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.add(statusLabel);
        rightPanel.add(modelSelector);
        rightPanel.add(sendButton);
        buttonPanel.add(rightPanel, BorderLayout.EAST);

        //callback function for sendButton
        sendButton.addActionListener(e -> {
            String userInput = inputField.getText();
            if (!userInput.isEmpty()) {
                String fullPrompt = "";
                ReferenceProcessor.InitProjectContext();
                fullPrompt = ReferenceProcessor.generateFullPrompt() + userInput;
                System.out.println("Full Prompt: " + fullPrompt);
                // Create message areas
                /*JTextArea inputArea = new JTextArea("User: " + userInput);
                inputArea.setEditable(false);
                inputArea.setLineWrap(true);
                inputArea.setWrapStyleWord(true);
                inputArea.setBackground(new Color(43, 45, 48));
                inputArea.setMargin(new Insets(0, 0, 25, 25));
                inputArea.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));

                JTextArea answerArea = new JTextArea("Assistant: ");
                answerArea.setEditable(false);
                answerArea.setLineWrap(true);
                answerArea.setBackground(new Color(60, 63, 65));
                answerArea.setWrapStyleWord(true);
                answerArea.setMargin(new Insets(0, 0, 25, 25));
                answerArea.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));*/
                JEditorPane inputArea = new JEditorPane();
                inputArea.setContentType("text/html");
                inputArea.setEditable(false);
                inputArea.setText("<html><body style='font-family: Arial; color: #FFFFFF; background-color: #2B2D30;'>"
                        +  MarkdownUtils.toHtml("User: " +userInput) + "</body></html>");
                inputArea.setBackground(new Color(43, 45, 48));
                inputArea.setMargin(new Insets(0, 0, 25, 25));
                inputArea.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

                JEditorPane answerArea = new JEditorPane();
                answerArea.setContentType("text/html");
                answerArea.setEditable(false);
                answerArea.setText("<html><body style='font-family: Arial; color: #FFFFFF; background-color: #3C3F41; white-space: normal; font-size: 16px;'>"
                        +  MarkdownUtils.toHtml("Assistant: " )+ "</body></html>");
                answerArea.setBackground(new Color(60, 63, 65));
                answerArea.setMargin(new Insets(0, 0, 25, 25));
                answerArea.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
                chatPanel.add(inputArea);
                chatPanel.add(answerArea);

                // Update UI
                sendButton.setEnabled(false);
                statusLabel.setText("Generating response...");

                // Add user message to session manager
                ChatSession currentSession = sessionManager.getActiveSession();
                if (currentSession != null) {
                    currentSession.addMessage(userInput, true);

                    // Call Ollama API
                    String selectedModel = (String) modelSelector.getSelectedItem();
                    StringBuilder responseBuilder = new StringBuilder();
                    
                    OllamaService.chatResponse(
                            selectedModel,
                            fullPrompt,
                            currentSession,
                            aiResponse -> {
                                SwingUtilities.invokeLater(() -> {
                                    // Append response to answer area
                                    responseBuilder.append(aiResponse);
                                    // 获取当前的 HTML 内容


                                    //answerArea.setText("Assistant: " + responseBuilder.toString());
                                    String currentHtml = answerArea.getText();
                                    String newText = MarkdownUtils.toHtml(responseBuilder.toString());
                                    String updatedHtml = currentHtml.replaceAll("(?s)(<body.*?>).*?(</body>)", "$1" + newText + "$2");
                                    answerArea.setText(updatedHtml);
                                    chatPanel.revalidate();
                                    chatPanel.repaint();

                                    SwingUtilities.invokeLater(() -> {
                                        JScrollBar verticalBar = chatScrollPane.getVerticalScrollBar();
                                        verticalBar.setValue(verticalBar.getMaximum());
                                    });
                                });
                            }).thenRun(() -> {
                                SwingUtilities.invokeLater(() -> {
                                    // store AI response in the session manager

                                    currentSession.addMessage(responseBuilder.toString(), false);
                                    sendButton.setEnabled(true);
                                    statusLabel.setText("");
                                    selectedReferenceFile = null;


                                });
                            }).exceptionally(ex -> {
                                SwingUtilities.invokeLater(() -> {
                                    answerArea.setText("Assistant: Error: " + ex.getMessage());
                                    sendButton.setEnabled(true);
                                    statusLabel.setText("Error occurred");
                                });
                                return null;
                            });

                    inputField.setText("");
                }
            }
        });

        bottomPanel.add(inputField, BorderLayout.NORTH);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        Mainbody.add(bottomPanel, BorderLayout.SOUTH);
    }

    private void updateChatDisplay() {
        setupChatUI();
    }

    public JPanel getMainPanel() {
        return MainPanel;
    }

    private void createUIComponents() {
        MainPanel = new JPanel();
        MainPanel.setLayout(new BorderLayout());
    }
}