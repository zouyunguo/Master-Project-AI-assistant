package mp25.aiassistant.utils;

import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.editor.*;

import java.nio.charset.StandardCharsets;
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
    private static List<File> referenceFiles = new ArrayList<>();
    private static List<File> javaFiles = new ArrayList<>();
    private static List<File> otherFiles = new ArrayList<>();
    // private final project;

    public ReferenceProcessor() {

    }

    /**
     * Add a reference file to the processor
     *
     * @param file The file to add as reference
     */
    public static void addReferenceFile(File file) {
        if (file != null && file.exists() && file.isFile()) {
            referenceFiles.add(file);
        } else {
            throw new IllegalArgumentException("Invalid file: " + file);
        }
    }

    /**
     * Get all reference files
     *
     * @return List of reference files
     */
    public static List<File> getReferenceFiles() {
        return referenceFiles;
    }

    /**
     * Generate prompt text from reference files
     *
     * @return Formatted prompt with reference file contents
     */
    public static String generateReferenceFilePrompt() {
        StringBuilder promptBuilder = new StringBuilder();
        for (File file : referenceFiles) {
            try {
                promptBuilder.append("```reference file" + " " + (referenceFiles.indexOf(file) + 1) + ",\n");
                String fileName = file.getName();
                fileName = "File name: " + fileName + ",\n";
                promptBuilder.append(fileName);
                String content = "File Content:\n";

                // Read the file content using ISO_8859_1 encoding
                content += java.nio.file.Files.readString(file.toPath(), StandardCharsets.ISO_8859_1);
                promptBuilder.append(content);
            } catch (Exception e) {
                //generate a Jpanel if there is an error reading the file
                String error = "Error reading file: " + e;
                System.out.println(error);
            }

        }

        return promptBuilder.toString();
    }

    /**
     * Parse Java source files and extract structure information
     *
     * @return Formatted project structure information
     */
    public static String parseProjectSourceFiles() {
        StringBuilder promptBuilder = new StringBuilder();

        for (File file : javaFiles) {
            try {
                promptBuilder.append("\n```referenced src file " + (javaFiles.indexOf(file) + 1) + ",\n");
                String fileName = "File name: " + file.getName() + ",\n";
                promptBuilder.append(fileName);
                promptBuilder.append("File Content:\n");

                String fileContent = java.nio.file.Files.readString(file.toPath(), StandardCharsets.ISO_8859_1);
                CompilationUnit cu = new JavaParser().parse(fileContent).getResult().orElse(null);

                if (cu != null) {
                    for (ClassOrInterfaceDeclaration cls : cu.findAll(ClassOrInterfaceDeclaration.class)) {
                        promptBuilder.append("Class: ").append(cls.getName()).append("\n");
                        if (!cls.getExtendedTypes().isEmpty()) {
                            promptBuilder.append("  Extends: ");
                            cls.getExtendedTypes().forEach(ext -> promptBuilder.append(ext.getName()).append(" "));
                            promptBuilder.append("\n");
                        }
                        if (!cls.getImplementedTypes().isEmpty()) {
                            promptBuilder.append("  Implements: ");
                            cls.getImplementedTypes().forEach(impl -> promptBuilder.append(impl.getName()).append(" "));
                            promptBuilder.append("\n");
                        }
                        // Class fields
                        cls.getFields().forEach(field -> {
                            field.getVariables().forEach(var -> {
                                promptBuilder.append("  Field: ")
                                        .append(var.getType()).append(" ").append(var.getName()).append("\n");
                            });
                        });
                        // Method signatures and local variables
                        cls.getMethods().forEach(method -> {
                            promptBuilder.append("  Method: ")
                                    .append(method.getType()).append(" ")
                                    .append(method.getName()).append("(");
                            method.getParameters().forEach(param -> {
                                promptBuilder.append(param.getType()).append(" ").append(param.getName()).append(", ");
                            });
                            if (!method.getParameters().isEmpty()) {
                                promptBuilder.setLength(promptBuilder.length() - 2); // Remove trailing comma
                            }
                            promptBuilder.append(")\n");
                            // Local variables
                            method.findAll(com.github.javaparser.ast.body.VariableDeclarator.class).forEach(var -> {
                                promptBuilder.append(" Local Var: ")
                                        .append(var.getType()).append(" ").append(var.getName()).append("\n");
                            });
                        });
                    }
                }
            } catch (Exception e) {
                System.out.println("Error reading file: " + e);
            }
        }
        return promptBuilder.toString();
    }

    /*
    // Commented out for now - can be enabled if needed for other file types
    for(File file: otherFiles){
        try {
            promptBuilder.append("\n```referenced other src file"+ " " + (otherFiles.indexOf(file) + 1) + ",\n");
            String fileName = file.getName();
            fileName ="File name: " + fileName + ",\n";
            promptBuilder.append(fileName);
            String content = " File Content:\n";

            // Read the file content using ISO_8859_1 encoding
            content+=java.nio.file.Files.readString(file.toPath(), StandardCharsets.ISO_8859_1);
            promptBuilder.append(content);
        } catch (Exception e) {
            //generate a Jpanel if there is an error reading the file
            String error= "Error reading file: " + e;
            System.out.println(error);
        }
    }
    */

    /**
     * Initialize project context by scanning for Java and other files
     */
    public static void InitProjectContext() {
        Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
        if (openProjects.length > 0) {
            Project project = openProjects[0]; // Return the first open project
            if (project != null) {
                javaFiles.clear();
                otherFiles.clear();
                getFilesByDir(project.getBasePath()); // Get project root directory
            } else {
                throw new IllegalStateException("No open project found.");
            }
        }
    }

    /**
     * Recursively scan directory for files
     *
     * @param path Directory path to scan
     */
    public static void getFilesByDir(String path) {
        File dir = new File(path);

        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        getFilesByDir(file.getAbsolutePath());
                    } else if (file.isFile()) {
                        if (file.getName().endsWith(".java")) {
                            javaFiles.add(file);
                        } else {
                            otherFiles.add(file);
                        }
                    }
                }
            }
        }
    }

    /**
     * Get context around cursor position in editor
     *
     * @param editor Editor instance
     * @param contextMaxSize Maximum context size (currently unused)
     * @return Context string with cursor position marked as &lt;BLANK&gt;
     */
    public static String getContext(Editor editor, int contextMaxSize) {
        Document document = editor.getDocument();
        CaretModel caretModel = editor.getCaretModel();
        int offset = caretModel.getOffset();
        int docLength = document.getTextLength();

        int contextSize = 2000;
        int start = Math.max(0, offset - contextSize);
        int end = Math.min(docLength, offset + contextSize);

        String beforeContext = document.getText(new TextRange(start, offset));
        String afterContext = document.getText(new TextRange(offset, end));

        return beforeContext + "<BLANK>" + afterContext;
    }

    /**
     * Generate complete prompt with all context information
     *
     * @return Full formatted prompt for AI assistant
     */
    public static String generateFullPrompt() {
        StringBuilder fullPrompt = new StringBuilder();
        fullPrompt.append("You are an AI assistant that helps developers understand and work with code.\n");

        
        if (!javaFiles.isEmpty() || !otherFiles.isEmpty()) {
            fullPrompt.append("You will be provided with reference files of a project.\n");
            fullPrompt.append(parseProjectSourceFiles());
        }
        if(!referenceFiles.isEmpty()){
            fullPrompt.append("In addition, you will receive reference files that is manually added by users that may contain additional information or context in below.\n");
            fullPrompt.append(generateReferenceFilePrompt());
        }
        fullPrompt.append("\nPlease give your response based on the user's input, if the user's input seems irrelevant to the context above, then only consider user's input : ");
       // fullPrompt.append("Given the above context information, please response only in MarkDown texts which only contains the information relevant to answer user's questions, the above information is only for your reference, please do not respond them, only respond to the following question given by the user: \n");

        return fullPrompt.toString();
    }
}