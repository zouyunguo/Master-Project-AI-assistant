package mp25.aiassistant.completion.managers;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorCustomElementRenderer;
import com.intellij.openapi.editor.Inlay;
import com.intellij.openapi.editor.InlayModel;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorFontType;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CompletionInlayManager {
    private final List<Inlay<?>> activeInlays = new ArrayList<>();
    private String currentCompletion = null;
    private int completionOffset = -1;

    public void showCompletionPreview(Editor editor, String completion) {
        clearPreview(editor);

        if (completion == null || completion.isEmpty()) {
            return;
        }


        currentCompletion = completion;
        System.out.println(completion);
        completionOffset =  editor.getCaretModel().getOffset();
        InlayModel inlayModel = editor.getInlayModel();
        String[] lines = completion.split("\n", -1);

        // 第一行显示在当前行的inline位置
/*        if (lines.length > 0 && !lines[0].isEmpty()) {
            Inlay<?> inlay = inlayModel.addInlineElement(
                    completionOffset,
                    new CompletionRenderer(lines[0])
            );
            if (inlay != null) {
                activeInlays.add(inlay);
            }

        }*/

        // 其余行显示为block elements
        for (int i = 0; i < lines.length; i++) {
            Inlay<?> inlay = inlayModel.addBlockElement(
                    completionOffset,
                    true,
                    false,
                    1,
                    new CompletionRenderer(lines[i])
            );
            if (inlay != null) {
                activeInlays.add(inlay);
            }
        }



    }

    public void clearPreview(Editor editor) {
        for (Inlay<?> inlay : activeInlays) {
            inlay.dispose();
        }
        activeInlays.clear();
        currentCompletion = null;
        completionOffset = -1;
    }

    public String getCurrentCompletion() {
        return currentCompletion;
    }

    public boolean hasActiveCompletion() {
        return currentCompletion != null && !activeInlays.isEmpty();
    }

    // 自定义渲染器，用于显示灰色文本
    private static class CompletionRenderer implements EditorCustomElementRenderer {
        private final String text;

        CompletionRenderer(String text) {
            this.text = text;
        }

        @Override
        public int calcWidthInPixels(@NotNull Inlay inlay) {
            return inlay.getEditor().getContentComponent()
                    .getFontMetrics(getFont(inlay.getEditor()))
                    .stringWidth(text);
        }

        @Override
        public void paint(@NotNull Inlay inlay, @NotNull Graphics g, @NotNull Rectangle targetRegion, @NotNull TextAttributes textAttributes) {
            Editor editor = inlay.getEditor();
            g.setFont(getFont(editor));
            g.setColor(getGrayColor());
            g.drawString(text, targetRegion.x, targetRegion.y + editor.getAscent());
        }

        private Font getFont(Editor editor) {
            return editor.getColorsScheme().getFont(EditorFontType.PLAIN);
        }

        private Color getGrayColor() {
            // 尝试从编辑器配色方案中获取合适的灰色
            TextAttributes attributes = EditorColorsManager.getInstance()
                    .getGlobalScheme()
                    .getAttributes(EditorColors.FOLDED_TEXT_ATTRIBUTES);

            if (attributes != null && attributes.getForegroundColor() != null) {
                return attributes.getForegroundColor();
            }

            // 使用半透明灰色，类似GitHub Copilot的效果
            boolean isDarkTheme = JBColor.isBright();
            if (isDarkTheme) {
                return new JBColor(new Color(150, 150, 150), new Color(100, 100, 100));
            } else {
                return new JBColor(new Color(120, 120, 120), new Color(150, 150, 150));
            }
        }
    }
}
