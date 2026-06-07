package org.example.cdnp2pstreamingsignaling.WebSocketController;


import lombok.RequiredArgsConstructor;
import org.example.cdnp2pstreamingsignaling.Model.NodeClass;
import org.example.cdnp2pstreamingsignaling.Model.PeerNode;
import org.example.cdnp2pstreamingsignaling.Service.ClusteringService;
import org.example.cdnp2pstreamingsignaling.Service.FallbackService;
import org.example.cdnp2pstreamingsignaling.Service.RoutingService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.time.LocalDateTime;
import java.util.Scanner;

@Controller("/signalingController")
@RequiredArgsConstructor
public class SignalingController {

    private final ClusteringService clusteringService;
    private final RoutingService routingService;
    private final SimpMessagingTemplate messagingTemplate;
    private final FallbackService fallbackService;

    @MessageMapping("/metrics.update")
    public void handleMetricsUpdate(@Payload PeerNode metrics, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        metrics.setLastUpdate(LocalDateTime.now());


    }

    @EventListener
    public void handleNodeConnect(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        if (sessionId == null) {
            return;
        }

        clusteringService.handleNodeConnect(PeerNode.builder().sessionId(sessionId).lastUpdate(LocalDateTime.now()).build());
    }

    @EventListener
    public void handleNodeDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        if (sessionId == null) {
            return;
        }

        clusteringService.handleNodeDisconnectGraceful(sessionId);
    }

    @Scheduled(fixedRate = 5000)
    public void runBatchClustering() {

    }
}