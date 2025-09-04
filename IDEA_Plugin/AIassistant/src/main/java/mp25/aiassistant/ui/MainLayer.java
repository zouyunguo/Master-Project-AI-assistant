package mp25.aiassistant.ui;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import mp25.aiassistant.utils.ReferenceProcessor;
import mp25.aiassistant.utils.markdown.StreamingMarkdownPanel;
import mp25.aiassistant.chat.ChatSession;
import mp25.aiassistant.chat.SessionManager;
import mp25.aiassistant.ai.OllamaService;
import javax.swing.*;
import java.awt.*;
import javafx.application.Platform;
import java.io.File;
import java.util.concurrent.CompletableFuture;
import com.intellij.icons.AllIcons;

/**
 * MainLayer
 * Entry panel for the plugin AFTER removing the privacy / terms screen.
 * Responsibilities:
 * 1. Manage chat sessions (create / rename / delete / switch)
 * 2. Render conversation history for the active session
 * 3. Provide input area + model selector + reference file attachment
 * 4. Stream model responses incrementally into markdown panels
 */
public class MainLayer {
    // Root container returned to ToolWindow
    private JPanel MainPanel;
    // Central body area that switches from (now removed) policy -> chat UI
    private JPanel Mainbody;
    // Panel containing session list + control buttons
    private JPanel sessionPanel;
    // List of chat sessions (left side)
    private JBList<ChatSession> sessionList;
    // Session management buttons
    private JButton newSessionButton;
    private JButton renameSessionButton;
    private JButton deleteSessionButton;
    // Session manager encapsulates persistence (in-memory for now)
    private final SessionManager sessionManager;
    // Backing model for session JList
    private DefaultListModel<ChatSession> sessionListModel;

    // Tracks an optional attached reference file for prompt enrichment
    private File selectedReferenceFile = null;

    /**
     * Constructor: builds UI programmatically (no .form usage anymore)
     */
    public MainLayer() {
        MainPanel = new JPanel(new BorderLayout());
        Mainbody = new JPanel();
        Mainbody.setLayout(new BorderLayout());
        MainPanel.add(Mainbody, BorderLayout.CENTER);

        sessionManager = new SessionManager();
        sessionListModel = new DefaultListModel<>();
        sessionList = new JBList<>(sessionListModel);
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

        // Initialize session area + main chat UI directly
        setupSessionControls();
        setupChatUI();
    }

    /**
     * Build buttons + listeners for session CRUD and selection.
     * Keeps JList model in sync with SessionManager.
     */
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

        // When user selects another session, rebuild the chat UI
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

    /**
     * Refresh the session list model and keep current selection focused.
     */
    private void updateSessionList() {
        sessionListModel.clear();
        for (ChatSession session : sessionManager.getSessions()) {
            sessionListModel.addElement(session);
        }
        if (sessionManager.getActiveSession() != null) {
            sessionList.setSelectedValue(sessionManager.getActiveSession(), true);
        }
    }

    /**
     * Reconstruct the main chat layout:
     *  - session panel (top area)
     *  - scrollable chat history
     *  - input panel (bottom)
     */
    private void setupChatUI() {
        Mainbody.removeAll();
        Mainbody.setLayout(new BorderLayout());

        JPanel chatPanel = createChatPanel(); // Holds rendered messages
        JPanel outerPanel = new JPanel(new BorderLayout());
        outerPanel.add(chatPanel, BorderLayout.NORTH); // NORTH ensures dynamic height shrink-wrap

        // Build session control bar
        sessionPanel = new JPanel();
        sessionPanel.add(sessionList, BorderLayout.WEST); // Simple layout (FlowLayout fallback)
        JPanel SessioncontrolPanel = new JPanel();
        SessioncontrolPanel.add(newSessionButton);
        SessioncontrolPanel.add(renameSessionButton);
        SessioncontrolPanel.add(deleteSessionButton);
        sessionPanel.add(SessioncontrolPanel, BorderLayout.EAST);
        Mainbody.add(sessionPanel, BorderLayout.NORTH);

        // Scroll container for chat history
        JBScrollPane chatScrollPane = new JBScrollPane(outerPanel);
        chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        chatScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // Input + controls
        setupInputPanel(chatPanel, chatScrollPane);

        Mainbody.add(chatScrollPane, BorderLayout.CENTER);
        Mainbody.revalidate();
        Mainbody.repaint();
    }

    /**
     * Create a panel that lists existing messages for the active session.
     * (Legacy simple JTextArea approach; streaming markdown used for new messages.)
     */
    private JPanel createChatPanel() {
        JPanel chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        chatPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

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

    /**
     * Build bottom input panel including:
     *  - Text field for user input
     *  - Model selector (populated async)
     *  - Reference file attach button
     *  - Send button (streams response)
     * Streaming logic:
     *  - Immediately adds two StreamingMarkdownPanel components (user + assistant)
     *  - Disables send during request; re-enables when streaming completes
     */
    private void setupInputPanel(JPanel chatPanel, JBScrollPane chatScrollPane) {
        JTextField inputField = new JTextField();
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BorderLayout());
        ComboBox<String> modelSelector = new ComboBox<>(new String[]{"none"});
        modelSelector.setSelectedIndex(0);

        // Async load available models
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

        JLabel statusLabel = new JLabel("");
        statusLabel.setForeground(Color.GRAY);

        // Reference file button (adds context to prompt)
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

        // Send (executes streaming chat completion)
        JButton sendButton = new JButton();
        sendButton.setIcon(AllIcons.Debugger.PromptInput);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.add(statusLabel);
        rightPanel.add(modelSelector);
        rightPanel.add(sendButton);
        buttonPanel.add(rightPanel, BorderLayout.EAST);

        sendButton.addActionListener(e -> {
            String userInput = inputField.getText();
            if (!userInput.isEmpty()) {
                String fullPrompt = "";
                // Build project context + user question
                ReferenceProcessor.InitProjectContext();
                fullPrompt = ReferenceProcessor.generateFullPrompt() + userInput;
                System.out.println("Full Prompt: " + fullPrompt);

                // Two streaming panels: one for user, one for assistant streaming reply
                StreamingMarkdownPanel answerArea = new StreamingMarkdownPanel(700, "#3C3F41", "#FFFFFF");
                StreamingMarkdownPanel inputArea = new StreamingMarkdownPanel(700, "#2B2D30", "#FFFFFF");
                chatPanel.add(inputArea);
                chatPanel.add(answerArea);
                Platform.runLater(() -> {
                    inputArea.appendMarkdown("User: " + userInput);
                    answerArea.appendMarkdown("Assistant: \n");
                });

                sendButton.setEnabled(false);
                statusLabel.setText("Generating response...");

                ChatSession currentSession = sessionManager.getActiveSession();
                if (currentSession != null) {
                    currentSession.addMessage(userInput, true);

                    String selectedModel = (String) modelSelector.getSelectedItem();
                    StringBuilder responseBuilder = new StringBuilder();

                    // Streaming callback: append chunks as they arrive
                    OllamaService.chatResponse(
                            selectedModel,
                            fullPrompt,
                            currentSession,
                            aiResponse -> {
                                SwingUtilities.invokeLater(() -> {
                                    responseBuilder.append(aiResponse);
                                    Platform.runLater(() -> {
                                        answerArea.appendMarkdown(aiResponse);
                                    });
                                    // Auto-scroll to bottom
                                    SwingUtilities.invokeLater(() -> {
                                        JScrollBar verticalBar = chatScrollPane.getVerticalScrollBar();
                                        verticalBar.setValue(verticalBar.getMaximum());
                                    });
                                });
                            }).thenRun(() -> {
                                // Completion: persist assistant message + reset state
                                SwingUtilities.invokeLater(() -> {
                                    System.out.println("AI Response: " + responseBuilder.toString());
                                    currentSession.addMessage(responseBuilder.toString(), false);
                                    sendButton.setEnabled(true);
                                    statusLabel.setText("");
                                    selectedReferenceFile = null;
                                });
                            }).exceptionally(ex -> {
                                // Error path: re-enable send + show lightweight status
                                SwingUtilities.invokeLater(() -> {
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

    /**
     * Rebuild chat UI for newly selected session (simple refresh strategy).
     */
    private void updateChatDisplay() {
        setupChatUI();
    }

    /**
     * Exposed to ToolWindow factory for embedding.
     */
    public JPanel getMainPanel() {
        return MainPanel;
    }
}

