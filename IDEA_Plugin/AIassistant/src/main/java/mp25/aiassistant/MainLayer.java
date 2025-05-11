package mp25.aiassistant;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.TextRange;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class MainLayer {
    private JPanel MainPanel;
    private JTextArea inputTextArea;
    private JTextArea outputTextArea;
    private JButton sendButton;
    private JButton clearButton;
    private JComboBox<String> modelSelector;
    private JPanel Mainbody;

    private OllamaClient ollamaClient;
    private Project project;

    public MainLayer() {
        // initialize
        initializeComponents();

        // get Ollama settings
        OllamaSettings settings = OllamaSettings.getInstance();
        ollamaClient = new OllamaClient(settings.ollamaServerUrl, settings.defaultModel);

        // get current project
        Project[] projects = ProjectManager.getInstance().getOpenProjects();
        if (projects.length > 0) {
            project = projects[0];
        }

        // add editor factory listener
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendPromptToOllama();
            }
        });

        // add clear button action
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                inputTextArea.setText("");
                outputTextArea.setText("");
            }
        });

        // add model selector action
        modelSelector.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedModel = (String) modelSelector.getSelectedItem();
                if (selectedModel != null && !selectedModel.isEmpty()) {
                    ollamaClient.setCurrentModel(selectedModel);
                }
            }
        });

        // set default model
        Dimension fixedSize = new Dimension(800, 600);
        MainPanel.setPreferredSize(fixedSize);
        MainPanel.setMinimumSize(fixedSize);
        MainPanel.setMaximumSize(fixedSize);
    }

    private void initializeComponents() {
        // If these components were not created through a form file, manual initialization is required
        if (inputTextArea == null) {
            inputTextArea = new JTextArea();
            inputTextArea.setLineWrap(true);
            inputTextArea.setWrapStyleWord(true);
        }

        if (outputTextArea == null) {
            outputTextArea = new JTextArea();
            outputTextArea.setLineWrap(true);
            outputTextArea.setWrapStyleWord(true);
            outputTextArea.setEditable(false);
        }

        if (sendButton == null) {
            sendButton = new JButton("send");
        }

        if (clearButton == null) {
            clearButton = new JButton("clear");
        }

        if (modelSelector == null) {
            modelSelector = new JComboBox<>(new String[]{"codellama", "llama2", "gemma", "mistral"});
        }

        if (Mainbody == null) {
            Mainbody = new JPanel(new BorderLayout());

            // Create input area
            JPanel inputPanel = new JPanel(new BorderLayout());
            inputPanel.add(new JScrollPane(inputTextArea), BorderLayout.CENTER);

            // Create button panel
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.add(modelSelector);
            buttonPanel.add(sendButton);
            buttonPanel.add(clearButton);
            inputPanel.add(buttonPanel, BorderLayout.SOUTH);

            // Create output area
            JPanel outputPanel = new JPanel(new BorderLayout());
            outputPanel.add(new JScrollPane(outputTextArea), BorderLayout.CENTER);

            // Create split pane
            JSplitPane splitPane = new JSplitPane(
                    JSplitPane.VERTICAL_SPLIT,
                    inputPanel,
                    outputPanel
            );
            splitPane.setResizeWeight(0.3);

            Mainbody.add(splitPane, BorderLayout.CENTER);
        }

        if (MainPanel == null) {
            MainPanel = new JPanel(new BorderLayout());
            MainPanel.add(Mainbody, BorderLayout.CENTER);
        }
    }

    private void sendPromptToOllama() {
        String prompt = inputTextArea.getText();
        if (prompt.isEmpty()) {
            return;
        }

        // disable the send button and show processing message
        sendButton.setEnabled(false);
        outputTextArea.setText("Processing request ..");

        // Retrieve the current editor content as context
        String editorContext = getCurrentEditorContent();

        // Build prompt words
        final String fullPrompt;
        if (editorContext != null && !editorContext.isEmpty()) {
//            fullPrompt = "The following is the context of the currently edited code:\n\n```\n" + editorContext + "\n```\n\nUser issue: " + prompt;
            fullPrompt = "User issue:\n\n```\n" + prompt;
        } else {
            fullPrompt = prompt;
        }

        // Using backend threads to handle API requests
        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                try {
                    if (!ollamaClient.isServerRunning()) {
                        return "Unable to connect to Ollama server. Please ensure that Ollama is started and running.";
                    }
                    return ollamaClient.generateCompletion(fullPrompt);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                    return "Request failed: " + e.getMessage();
                }
            }

            @Override
            protected void done() {
                try {
                    String response = get();
                    outputTextArea.setText(response);
                } catch (Exception e) {
                    outputTextArea.setText("An error occurred while processing the request: " + e.getMessage());
                } finally {
                    sendButton.setEnabled(true);
                }
            }
        };

        worker.execute();
    }

    private String getCurrentEditorContent() {
        if (project == null) {
            return null;
        }

        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (editor == null) {
            return null;
        }

        Document document = editor.getDocument();
        return document.getText();
    }

    public JPanel getMainPanel() {
        return MainPanel;
    }
}