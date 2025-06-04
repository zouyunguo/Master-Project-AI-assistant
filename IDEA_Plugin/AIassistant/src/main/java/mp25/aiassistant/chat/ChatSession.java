package mp25.aiassistant.chat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a single chat session with message history
 */
public class ChatSession {
    private final String id;
    private String name;
    private final LocalDateTime createdAt;
    private final List<Message> messages;

    public ChatSession(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.createdAt = LocalDateTime.now();
        this.messages = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void addMessage(String content, boolean isUser) {
        messages.add(new Message(content, isUser));
    }

    public static class Message {
        private final String content;
        private final boolean isUser;
        private final LocalDateTime timestamp;

        public Message(String content, boolean isUser) {
            this.content = content;
            this.isUser = isUser;
            this.timestamp = LocalDateTime.now();
        }

        public String getContent() {
            return content;
        }

        public boolean isUser() {
            return isUser;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }
    }
} 