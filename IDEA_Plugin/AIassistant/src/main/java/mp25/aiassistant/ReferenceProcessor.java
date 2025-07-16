package mp25.aiassistant;

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
    private static  List<File> javaFiles = new ArrayList<>();
    private static List<File> otherFiles = new ArrayList<>();
   // private final Project project;

    public ReferenceProcessor() {

    }



    public static void addReferenceFile(File file) {
        if (file != null && file.exists() && file.isFile()) {
            referenceFiles.add(file);
        } else {
            throw new IllegalArgumentException("Invalid file: " + file);
        }
    }

    public static List<File> getReferenceFiles() {
        return referenceFiles;
    }

    public static String generateReferenceFilePrompt(){
        StringBuilder promptBuilder = new StringBuilder();
        for (File file : referenceFiles) {
            try {
            promptBuilder.append("```reference file"+ " " + (referenceFiles.indexOf(file) + 1) + ",\n");
            String fileName = file.getName();
            fileName ="File name: " + fileName + ",\n";
            promptBuilder.append(fileName);
            String content = "File Content:\n";

                // Read the file content using ISO_8859_1 encoding
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
                        // 类字段
                        cls.getFields().forEach(field -> {
                            field.getVariables().forEach(var -> {
                                promptBuilder.append("  Field: ")
                                        .append(var.getType()).append(" ").append(var.getName()).append("\n");
                            });
                        });
                        // 方法签名和局部变量
                        cls.getMethods().forEach(method -> {
                            promptBuilder.append("  Method: ")
                                    .append(method.getType()).append(" ")
                                    .append(method.getName()).append("(");
                            method.getParameters().forEach(param -> {
                                promptBuilder.append(param.getType()).append(" ").append(param.getName()).append(", ");
                            });
                            if (!method.getParameters().isEmpty()) {
                                promptBuilder.setLength(promptBuilder.length() - 2); // 去掉最后的逗号
                            }
                            promptBuilder.append(")\n");
                            // 局部变量
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
        /*for(File file: otherFiles){
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
        }*/

    // get the project directory
    public static void InitProjectContext() {
        Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
        if (openProjects.length > 0) {
            Project project = openProjects[0]; // 返回第一个打开的项目
            if (project != null) {
                javaFiles.clear();
                otherFiles.clear();
                getFilesByDir(project.getBasePath()) ; // 获取项目的根目录
            } else {
                throw new IllegalStateException("No open project found.");
            }
        }

    }


    public static void getFilesByDir(String path) {
        File dir = new File(path);

        if (dir.exists() && dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                if (file.isDirectory()) {
                    getFilesByDir(file.getAbsolutePath());
                } else if (file.isFile()) {
                    if( file.getName().endsWith(".java")){
                        javaFiles.add(file);
                    }
                    else {
                        otherFiles.add(file);
                    }
                }
            }
        }
    }

    public static String getContext(Editor editor,int contextMaxSize) {
        Document document = editor.getDocument();
        CaretModel caretModel = editor.getCaretModel();
        int offset = caretModel.getOffset();
        int docLength = document.getTextLength();

        int contextSize = 2000;
        int start = Math.max(0, offset - contextSize);
        int end = Math.min(docLength, offset + contextSize);

        String beforeContext = document.getText(new TextRange(start, offset));
        String afterContext = document.getText(new TextRange(offset, end));

        return beforeContext+"<BLANK>"+afterContext;
    }

    public static String generateFullPrompt(){
        StringBuilder fullPrompt = new StringBuilder();
        fullPrompt.append("You are an AI assistant that helps developers understand and work with code.\n");
        fullPrompt.append("You will be provided with reference files of a project.\n");
        if(!javaFiles.isEmpty()|| !otherFiles.isEmpty()) {
            fullPrompt.append(parseProjectSourceFiles());
        }

        fullPrompt.append("In addition, you will receive reference files that is manually added by users that may contain additional information or context in below.\n");
        fullPrompt.append(generateReferenceFilePrompt());
        fullPrompt.append("given the above context information, please response only in MarkDown texts which only contains the information relevant to answer user's questions, the above information is only for your reference, please do not respond them, only respond to the following question given by the user  : \n");


        return fullPrompt.toString();
    }




}