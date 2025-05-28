package mp25.aiassistant.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Manages multiple chat sessions
 */
public class SessionManager {
    private final List<ChatSession> sessions;
    private ChatSession activeSession;

    public SessionManager() {
        this.sessions = new ArrayList<>();
        createNewSession("Default Session");
    }

    public ChatSession createNewSession(String name) {
        ChatSession session = new ChatSession(name);
        sessions.add(session);
        setActiveSession(session);
        return session;
    }

    public void removeSession(String sessionId) {
        sessions.removeIf(session -> session.getId().equals(sessionId));
        if (activeSession != null && activeSession.getId().equals(sessionId)) {
            activeSession = sessions.isEmpty() ? null : sessions.get(0);
        }
    }

    public List<ChatSession> getSessions() {
        return new ArrayList<>(sessions);
    }

    public Optional<ChatSession> getSession(String sessionId) {
        return sessions.stream()
                .filter(session -> session.getId().equals(sessionId))
                .findFirst();
    }

    public ChatSession getActiveSession() {
        return activeSession;
    }

    public void setActiveSession(ChatSession session) {
        if (sessions.contains(session)) {
            this.activeSession = session;
        }
    }

    public void setActiveSession(String sessionId) {
        getSession(sessionId).ifPresent(this::setActiveSession);
    }
} 