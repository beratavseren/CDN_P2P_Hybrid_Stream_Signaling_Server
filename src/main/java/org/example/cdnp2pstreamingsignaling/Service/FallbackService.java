package org.example.cdnp2pstreamingsignaling.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class FallbackService {

    private final SimpMessagingTemplate messagingTemplate;

    public void triggerCdnFallback(String sessionId, String reason) {
        messagingTemplate.convertAndSend("/topic/sessions/" + sessionId + "/fallback",
                Map.of("action", "SWITCH_TO_CDN", "reason", reason));
    }

    public void triggerP2pRecovery(String sessionId, String reason) {
        messagingTemplate.convertAndSend("/topic/sessions/" + sessionId + "/fallback",
                Map.of("action", "ATTEMPT_P2P_RECOVERY", "reason", reason));
    }
}