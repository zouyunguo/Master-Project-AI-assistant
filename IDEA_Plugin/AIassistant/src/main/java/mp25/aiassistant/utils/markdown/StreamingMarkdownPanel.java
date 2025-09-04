package mp25.aiassistant.utils.markdown;

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
        this.setPreferredSize(new Dimension(width, 50)); // Default height is 50, can be adjusted as needed
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

        // Accumulate incoming Markdown strings
        markdownBuffer.append(markdownFragment);
        System.out.println("added markdown fragment: " + markdownFragment);

        Platform.runLater(() -> {
            // Parse accumulated Markdown content
            Node node = parser.parse(markdownBuffer.toString());
            String html = renderer.render(node);
            // Reload entire page content
            String fullHtml = wrapHtml(html, backgroundColor, fontColor);
            webView.getEngine().loadContent(fullHtml);
            });

    }


    private static String wrapHtml(String body, String bgColor, String fontColor) {
        return """
        <html>
        <head>
          <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.8.0/styles/atom-one-dark.min.css">
          <script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.8.0/highlight.min.js"></script>
          <script>hljs.highlightAll();</script>
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
              background-color: #2B2D30;
              padding: 6px;
              border-radius: 4px;
              white-space: pre-wrap;
            }
            code {
              background-color: #2B2D30;
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
