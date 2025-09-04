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
/**
 *  Manager for handling completion preview inlays
 */
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


        // Other lines display as block elements
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

    // Custom renderer for displaying gray text
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
            // Try to get appropriate gray color from editor color scheme
            TextAttributes attributes = EditorColorsManager.getInstance()
                    .getGlobalScheme()
                    .getAttributes(EditorColors.FOLDED_TEXT_ATTRIBUTES);

            if (attributes != null && attributes.getForegroundColor() != null) {
                return attributes.getForegroundColor();
            }

            // Use semi-transparent gray, similar to GitHub Copilot effect
            boolean isDarkTheme = JBColor.isBright();
            if (isDarkTheme) {
                return new JBColor(new Color(150, 150, 150), new Color(100, 100, 100));
            } else {
                return new JBColor(new Color(120, 120, 120), new Color(150, 150, 150));
            }
        }
    }
}
