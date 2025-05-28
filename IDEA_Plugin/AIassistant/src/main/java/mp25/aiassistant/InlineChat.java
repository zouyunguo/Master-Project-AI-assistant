package mp25.aiassistant;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.Inlay;
import com.intellij.openapi.editor.InlayModel;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.CompletableFuture;

public class InlineChat extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;

        // 获取当前编辑器
        Editor editor = e.getData(com.intellij.openapi.actionSystem.CommonDataKeys.EDITOR);

        if (editor == null) return;

        InlayModel inlayModel = editor.getInlayModel();
        int offset = editor.getCaretModel().getOffset();

        // 创建输入框面板
        JPanel inputPanel = new JPanel(new BorderLayout());
        JTextField inputField = new JTextField();
        JButton sendButton = new JButton("Send");


        // 模型选择器
        String[] models = {"llama2", "codellama", "mistral", "gemma"};
        ComboBox<String> modelSelector = new ComboBox<>(models);
        modelSelector.setSelectedIndex(0);

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        inputPanel.add(modelSelector, BorderLayout.NORTH);



        // 发送按钮事件
        sendButton.addActionListener(event -> {
            String userInput = inputField.getText();
            if (!userInput.isEmpty()) {
                String selectedModel = (String) modelSelector.getSelectedItem();
                CompletableFuture<String> response = OllamaService.generateResponse(selectedModel, userInput);

                response.thenAccept(result -> {
                    SwingUtilities.invokeLater(() -> {
                        // 显示响应
                        JTextArea responseArea = new JTextArea(result);
                        responseArea.setEditable(false);
                        responseArea.setLineWrap(true);
                        responseArea.setWrapStyleWord(true);
                        JBScrollPane scrollPane = new JBScrollPane(responseArea);

                        // 替换输入框为响应内容
                        //inlayModel.addInlineElement(offset, true, scrollPane);
                        //inlay.dispose();
                    });
                });
            }
        });
    }
}