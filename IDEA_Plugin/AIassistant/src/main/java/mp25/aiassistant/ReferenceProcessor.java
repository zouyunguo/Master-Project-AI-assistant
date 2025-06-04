package mp25.aiassistant;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;

import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
/**
 * File content processor for reference files
 */
public class ReferenceProcessor {

    /**
     * Process a reference file and return its content formatted for Ollama
     *
     * @param filePath Path to the reference file
     * @return Formatted content for inclusion in the prompt
     */
    private List<File> referenceFiles = new ArrayList<>();
   // private final Project project;

    public ReferenceProcessor() {

    }
    public void addReferenceFile(File file) {
        if (file != null && file.exists() && file.isFile()) {
            referenceFiles.add(file);
        } else {
            throw new IllegalArgumentException("Invalid file: " + file);
        }
    }

    public List<File> getReferenceFiles() {
        return referenceFiles;
    }

    public String generateReferenceFilePrompt(){
        StringBuilder promptBuilder = new StringBuilder();
        for (File file : referenceFiles) {
            promptBuilder.append("```reference file"+ " " + (referenceFiles.indexOf(file) + 1) + ",\n");
            String fileName = file.getName();
            fileName ="File name: " + fileName + ",\n";
            promptBuilder.append(fileName);
            String content = "File Content:\n";
            try {
                content +=java.nio.file.Files.readString(file.toPath(), StandardCharsets.ISO_8859_1);
                promptBuilder.append(content);
            } catch (Exception e) {
                //generate a Jpanel if there is an error reading the file
                String error= "Error reading file: " + e;
                System.out.println(error);
            }

        }
        return promptBuilder.toString();
    }

    public static String processReferenceFile(String filePath) {
        try {
            java.nio.file.Path path = java.nio.file.Path.of(filePath);
            String content = java.nio.file.Files.readString(path);

            // Format the reference content with a clear delimiter
            return "```reference\n" + content + "\n```\n\n";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error reading reference: " + e.getMessage();
        }
    }
}