package mp25.aiassistant.components;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import javax.swing.*;
import java.awt.*;

public class MarkdownBubblePanel extends JPanel {
    private static final Parser parser = Parser.builder().build();
    private static final HtmlRenderer renderer = HtmlRenderer.builder().build();

    private final JFXPanel fxPanel = new JFXPanel();
    private final int preferredWidth;
    private final String backgroundColor;
    private final String fontColor;

    public MarkdownBubblePanel(String markdown, int width, String bgColor, String textColor) {
        this.preferredWidth = width;
        this.backgroundColor = bgColor;
        this.fontColor = textColor;

        setLayout(new BorderLayout());
        setOpaque(false);
        add(fxPanel, BorderLayout.CENTER);

        // 渲染 JavaFX 内容
        renderMarkdown(markdown);
    }

    private void renderMarkdown(String markdown) {
        Platform.runLater(() -> {
            Node document = parser.parse(markdown);
            String html = renderer.render(document);

            WebView webView = new WebView();
            webView.setPrefWidth(preferredWidth);

            WebEngine engine = webView.getEngine();
            engine.loadContent(wrapHtml(html, backgroundColor, fontColor));

            // 延迟计算内容高度并设置 WebView 高度
            engine.documentProperty().addListener((obs, oldDoc, newDoc) -> {
                if (newDoc != null) {
                    // 使用 JS 获取 scrollHeight 设置 JavaFX WebView 高度
                    Platform.runLater(() -> {
                        Object result = engine.executeScript("document.body.scrollHeight");
                        if (result instanceof Number height) {
                            webView.setPrefHeight(height.doubleValue());
                            fxPanel.setPreferredSize(new Dimension(preferredWidth, (int) height.doubleValue()));
                            fxPanel.revalidate();
                        }
                    });
                }
            });

            Scene scene = new Scene(webView);
            fxPanel.setScene(scene);
        });
    }

    private String wrapHtml(String body, String bgColor, String fontColor) {
        return """
                <html>
                <head>
                  <style>
                    html, body {
                      margin: 0;
                      padding: 10px;
                      font-family: Arial, sans-serif;
                      font-size: 14px;
                      line-height: 1.4;
                      background-color: %s;
                      color: %s;
                    }
                    h1, h2, h3, h4 {
                      margin: 6px 0;
                      color: %s;
                    }
                    p {
                      margin: 4px 0;
                    }
                    pre {
                      margin: 6px 0;
                      padding: 6px;
                      background-color: #444;
                      border-radius: 4px;
                      white-space: pre-wrap;
                    }
                    code {
                      background-color: #444;
                      padding: 2px 4px;
                      border-radius: 3px;
                    }
                    ul, ol {
                      margin: 4px 0 4px 20px;
                    }
                  </style>
                </head>
                <body>%s</body>
                </html>
                """.formatted(bgColor, fontColor, fontColor, body);
    }
}
