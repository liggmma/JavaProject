package luxdine.example.luxdine.service.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Secure session manager for AI chatbot
 * Prevents session ID guessing and PII exposure
 */
@Component
public class SecureSessionManager {

    private static final Logger logger = LoggerFactory.getLogger(SecureSessionManager.class);
    private static final SecureRandom secureRandom = new SecureRandom();

    // Map userId to secure sessionId (hashed)
    private final Map<Long, String> userSessionMap = new ConcurrentHashMap<>();

    // Map sessionId to userId (for reverse lookup)
    private final Map<String, Long> sessionUserMap = new ConcurrentHashMap<>();

    /**
     * Generate or retrieve secure session ID for a user
     * @param userId User ID (can be null for guests)
     * @return Secure session ID (UUID or hashed)
     */
    public String getOrCreateSecureSessionId(Long userId) {
        if (userId == null) {
            // Guest user - generate random UUID
            return generateGuestSessionId();
        }

        // Check if user already has a session
        String existingSession = userSessionMap.get(userId);
        if (existingSession != null) {
            return existingSession;
        }

        // Create new secure session for user
        String secureSessionId = generateUserSessionId(userId);
        userSessionMap.put(userId, secureSessionId);
        sessionUserMap.put(secureSessionId, userId);

        logger.debug("Created secure session for userId: {}", userId);
        return secureSessionId;
    }

    /**
     * Get userId from session ID
     * @param sessionId Session ID
     * @return User ID or null if not found/guest
     */
    public Long getUserIdFromSession(String sessionId) {
        return sessionUserMap.get(sessionId);
    }

    /**
     * Check if session belongs to a logged-in user
     * @param sessionId Session ID
     * @return true if user is logged in
     */
    public boolean isAuthenticatedSession(String sessionId) {
        return sessionUserMap.containsKey(sessionId);
    }

    /**
     * Clear user session
     * @param userId User ID
     */
    public void clearUserSession(Long userId) {
        String sessionId = userSessionMap.remove(userId);
        if (sessionId != null) {
            sessionUserMap.remove(sessionId);
            logger.debug("Cleared session for userId: {}", userId);
        }
    }

    /**
     * Generate guest session ID
     * @return Random UUID-based session ID
     */
    private String generateGuestSessionId() {
        return "guest-" + UUID.randomUUID().toString();
    }

    /**
     * Generate secure session ID for user
     * Uses SHA-256 hash of userId + random salt to prevent guessing
     * @param userId User ID
     * @return Hashed session ID
     */
    private String generateUserSessionId(Long userId) {
        try {
            // Generate random salt
            byte[] salt = new byte[16];
            secureRandom.nextBytes(salt);

            // Hash userId with salt
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt);
            digest.update(String.valueOf(userId).getBytes());
            digest.update(String.valueOf(System.currentTimeMillis()).getBytes());

            byte[] hash = digest.digest();

            // Return base64-encoded hash
            return "user-" + Base64.getUrlEncoder().withoutPadding().encodeToString(hash);

        } catch (NoSuchAlgorithmException e) {
            logger.error("SHA-256 algorithm not available, falling back to UUID", e);
            return "user-" + UUID.randomUUID().toString();
        }
    }

    /**
     * Get session count for monitoring
     * @return Number of active sessions
     */
    public int getActiveSessionCount() {
        return sessionUserMap.size();
    }

    /**
     * Cleanup all sessions (for testing/maintenance)
     */
    public void clearAllSessions() {
        userSessionMap.clear();
        sessionUserMap.clear();
        logger.info("Cleared all sessions");
    }
}
