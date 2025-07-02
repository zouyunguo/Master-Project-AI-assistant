package mp25.aiassistant;

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
import com.intellij.openapi.util.TextRange;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        // 模型选择器

        modelSelector.setSelectedIndex(0);
        //get list of models available from OllamaService.getModels()
        CompletableFuture<String[]> modelsFuture = OllamaService.getModels();
        modelsFuture.thenAccept(response -> {
            // Parse the response and return an array of model names
            String[] models = response;
            for (int i = 0; i < models.length; i++) {
                models[i] = models[i].trim(); // Clean up whitespace
            }
            modelSelector.setModel(new DefaultComboBoxModel<>(models));
        }).exceptionally(ex -> {
            // Handle errors
            SwingUtilities.invokeLater(() -> {
                System.out.println("Error fetching models: " + ex.getMessage());
            });
            return null;
        });


        inputPanel.add(rightPanel, BorderLayout.SOUTH);
        rightPanel.add(modelSelector);
        rightPanel.add(sendButton);
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor == null) return;
        // 获取光标位置
        CaretModel caretModel = editor.getCaretModel();
        LogicalPosition caretPos = caretModel.getLogicalPosition();

        // 创建悬浮输入框
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
                String Context=ReferenceProcessor.getContext(editor,1000);
                String beforeContext= Context.split("<Cursor>")[0];
                String afterContext= Context.split("<Cursor>")[1];
                String fullPrompt= "You are an AI agent helping with coding, your mission is to insert proper code snippet based on the input task at the current cursor based on the context before and after the cursor. The input task is:" + userInput +"\n\n below is the context before the current cursor:\n"+beforeContext+"\n and then is the context after the current cursor:\n"+afterContext+" .Return your code in a code block based on the input task and context, which starts with ```LanguageName\\n and ends with ```\n";
                System.out.println("Full Prompt: " + fullPrompt);
                if (!userInput.isEmpty()) {
                    // 禁用按钮以防止重复点击
                    sendButton.setEnabled(false);
                    String selectedModel = (String) modelSelector.getSelectedItem();
                    boolean onThinkFinished = false;
                    // 调用 OllamaService.generateResponse
                    OllamaService.generateResponse(selectedModel, fullPrompt, responseLine -> {
                        //对responseLine进行处理，去除以<thinking>和</thinking>标签包裹的思考内容
                        System.out.println("Response: " + responseLine);
                        Pattern pattern = Pattern.compile("```[\\s\\S]*?\\n([\\s\\S]*?)\\n```");
                        Matcher matcher = pattern.matcher(responseLine);
                        String codeBlock="";
                        if (matcher.find()) {
                             codeBlock = matcher.group(1);
                            // codeBlock 就是被 ``` 包裹的内容
                        }
                        final String finalCodeBlock = codeBlock;
                        // 在事件调度线程中更新 UI
                        ApplicationManager.getApplication().invokeLater(() -> {
                            WriteCommandAction.runWriteCommandAction(editor.getProject(), () -> {
                                // 获取 Editor 的 Document 对象

                                // 在光标位置插入返回结果
                                document.insertString(offset+Chatoffset, finalCodeBlock);
                                Chatoffset+= finalCodeBlock.length();
                                // 关闭弹出窗口
                                popup.cancel();

                                sendButton.setEnabled(true);
                            });
                        });
                    }).exceptionally(ex -> {
                        // 处理异常
                        SwingUtilities.invokeLater(() -> {
                            System.out.println("Error: " + ex.getMessage());
                            sendButton.setEnabled(true);
                        });
                        return null;
                    });

                    // 清空输入框
                    inputField.setText("");
                }
            }
        });
        // 创建悬浮框
        return popup;
    }
}


