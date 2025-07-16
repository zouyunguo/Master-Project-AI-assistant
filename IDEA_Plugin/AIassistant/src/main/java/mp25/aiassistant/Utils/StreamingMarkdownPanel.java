package mp25.aiassistant.Utils;

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

public class StreamingMarkdownPanel extends JPanel {
    private static final Parser parser = Parser.builder().build();
    private static final HtmlRenderer renderer = HtmlRenderer.builder().build();
    private final StringBuilder markdownBuffer = new StringBuilder();
    private final JFXPanel fxPanel = new JFXPanel();
    private  WebView webView  ;
    private final int preferredWidth;
    private final String backgroundColor;
    private final String fontColor;
    private boolean initialized = false;

    public StreamingMarkdownPanel(int width, String bgColor, String textColor) {
        this.preferredWidth = width;
        this.backgroundColor = bgColor;
        this.fontColor = textColor;

        setLayout(new BorderLayout());
        setOpaque(false);
        add(fxPanel, BorderLayout.CENTER);
        this.setPreferredSize(new Dimension(width, 50)); // 默认高度为300，可以根据需要调整
        initWebView();
    }

    private void initWebView() {
        Platform.runLater(() -> {
            webView = new WebView();
            webView.setPrefWidth(preferredWidth);
            webView.setPrefHeight(50);
            WebEngine webEngine = webView.getEngine();
            String initialHtml = wrapHtml("", backgroundColor, fontColor);
            webEngine.loadContent(initialHtml);
            fxPanel.setScene(new Scene(webView));
            initialized = true;

            webEngine.documentProperty().addListener((observable, oldDoc, newDoc) -> {
                if (newDoc != null) {
                    Platform.runLater(() -> {
                        double contentHeight = webView.getEngine().executeScript("document.body.scrollHeight").hashCode();
                        SwingUtilities.invokeLater(() -> setPreferredSize(new Dimension(preferredWidth, (int) contentHeight)));
                        revalidate();
                    });
                }
            });
        });
    }



    public void appendMarkdown(String markdownFragment) {
        if (!initialized) return;

        // 累积传入的 Markdown 字符串
        markdownBuffer.append(markdownFragment);
        System.out.println("added markdown fragment: " + markdownFragment);

        Platform.runLater(() -> {
            // 解析累积的 Markdown 内容
            Node node = parser.parse(markdownBuffer.toString());
            String html = renderer.render(node);
            // 重新加载整个页面内容
            String fullHtml = wrapHtml(html, backgroundColor, fontColor);
            webView.getEngine().loadContent(fullHtml);
            });

    }


    private static String wrapHtml(String body, String bgColor, String fontColor) {
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
                    pre {
                      background-color: #444;
                      padding: 6px;
                      border-radius: 4px;
                      white-space: pre-wrap;
                    }
                    code {
                      background-color: #444;
                      padding: 2px 4px;
                      border-radius: 3px;
                    }
                  </style>
                </head>
                <body>%s</body>
                </html>
                """.formatted(bgColor, fontColor, body);
    }
}
