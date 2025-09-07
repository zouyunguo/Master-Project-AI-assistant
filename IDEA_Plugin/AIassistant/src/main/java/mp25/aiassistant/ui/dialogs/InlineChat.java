package mp25.aiassistant.ui.dialogs;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import mp25.aiassistant.state.ModelSelectionManager;
import mp25.aiassistant.utils.ReferenceProcessor;
import mp25.aiassistant.ai.ModelServiceProvider;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 *  Inline chat input box for code generation
 */
public class InlineChat extends AnAction {
    JPanel inputPanel = new JPanel(new BorderLayout());
    JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    JTextField inputField = new JTextField();
    JButton sendButton = new JButton();
    ComboBox<String> modelSelector = new ComboBox<>(new String[]{"none"}); // Default models
    int Chatoffset = 0;
    public InlineChat(){


    }
    @Override
    public void actionPerformed(AnActionEvent e) {
        inputPanel.add(inputField, BorderLayout.NORTH);
        sendButton.setIcon(AllIcons.Debugger.PromptInput);
        // Model selector

        modelSelector.setSelectedIndex(0);
        //get list of models available from ModelServiceProvider.get().getModels()
        CompletableFuture<String[]> modelsFuture = ModelServiceProvider.get().getModels();
        modelsFuture.thenAccept(response -> {
            // Parse the response and return an array of model names
            String[] models = response;
            for (int i = 0; i < models.length; i++) {
                models[i] = models[i].trim(); // Clean up whitespace
            }
            modelSelector.setModel(new DefaultComboBoxModel<>(models));
            // Set default global selected model to first available
            if (models.length > 0) {
                String first = models[0];
                ModelSelectionManager.getInstance().setSelectedModel(first);
                SwingUtilities.invokeLater(() -> modelSelector.setSelectedItem(first));
            }
        }).exceptionally(ex -> {
            // Handle errors
            SwingUtilities.invokeLater(() -> {
                System.out.println("Error fetching models: " + ex.getMessage());
            });
            return null;
        });

        // Keep global state in sync when user changes model
        modelSelector.addActionListener(evt -> {
            Object sel = modelSelector.getSelectedItem();
            if (sel != null) {
                ModelSelectionManager.getInstance().setSelectedModel(sel.toString());
            }
        });


        inputPanel.add(rightPanel, BorderLayout.SOUTH);
        rightPanel.add(modelSelector);
        rightPanel.add(sendButton);
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor == null) return;
        // Get cursor position
        CaretModel caretModel = editor.getCaretModel();
        LogicalPosition caretPos = caretModel.getLogicalPosition();

        // Create floating input box
        createInputPopup(editor, caretPos).showInBestPositionFor(editor);
    }

    private JBPopup createInputPopup(Editor editor, LogicalPosition position) {
        Chatoffset = 0;
        JBPopup popup=JBPopupFactory.getInstance().createComponentPopupBuilder(inputPanel,inputField)
                .setFocusable(true)
                .setRequestFocus(true)
                .setCancelOnClickOutside(true)
                .setCancelKeyEnabled(true)
                .createPopup();
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String userInput = inputField.getText();
                Document document = editor.getDocument();
                CaretModel caretModel = editor.getCaretModel();
                int offset = caretModel.getOffset();
                String Context= ReferenceProcessor.getContext(editor,1000);

                String fullPrompt = "You are a AI agent aiming to provide code completion function, your task is " +
                        "to complete the following code block where something is missing based on the input task given by users, below is the input task:"+userInput+"\n Then is the code block to be completed\n" +
                        Context + "\n Fill in the blank to complete the code block. Your response should include only the code to replace <BLANK>, without surrounding backticks,include the response code block in ``` tags.\n";

                System.out.println("Full Prompt: " + fullPrompt);
                if (!userInput.isEmpty()) {
                    // Disable button to prevent duplicate clicks
                    sendButton.setEnabled(false);
                    String selectedModel = (String) modelSelector.getSelectedItem();
                    // Sync global state before sending
                    if (selectedModel != null) {
                        ModelSelectionManager.getInstance().setSelectedModel(selectedModel);
                    }
                    boolean onThinkFinished = false;
                    // Call ModelServiceProvider.generateResponse
                    ModelServiceProvider.get().generateResponse(selectedModel, fullPrompt, responseLine -> {
                        // Process responseLine, remove thinking content wrapped in <thinking> and </thinking> tags
                        System.out.println("Response: " + responseLine);
                        Pattern pattern = Pattern.compile("```[\\s\\S]*?\\n([\\s\\S]*?)\\n```");
                        Matcher matcher = pattern.matcher(responseLine);
                        String codeBlock="";
                        if (matcher.find()) {
                             codeBlock = matcher.group(1);
                            // codeBlock is the content wrapped by ```
                        }
                        final String finalCodeBlock = codeBlock;
                        // Update UI in event dispatch thread
                        ApplicationManager.getApplication().invokeLater(() -> {
                            WriteCommandAction.runWriteCommandAction(editor.getProject(), () -> {
                                // Get Editor's Document object

                                // Insert return result at cursor position
                                document.insertString(offset+Chatoffset, finalCodeBlock);
                                Chatoffset+= finalCodeBlock.length();
                                // Close popup window
                                popup.cancel();

                                sendButton.setEnabled(true);
                            });
                        });
                    }).exceptionally(ex -> {
                        // Handle exceptions
                        SwingUtilities.invokeLater(() -> {
                            System.out.println("Error: " + ex.getMessage());
                            sendButton.setEnabled(true);
                        });
                        return null;
                    });

                    // Clear input field
                    inputField.setText("");
                }
            }
        });
        // Create floating box
        return popup;
    }
}
