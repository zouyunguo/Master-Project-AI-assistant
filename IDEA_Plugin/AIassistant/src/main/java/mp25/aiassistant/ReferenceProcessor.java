package mp25.aiassistant;

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