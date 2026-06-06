package org.example.cdnp2pstreamingsignaling.Service;

import lombok.RequiredArgsConstructor;
import org.example.cdnp2pstreamingsignaling.Model.RoutedMessage;
import org.example.cdnp2pstreamingsignaling.Model.SignalingMessage;
import org.example.cdnp2pstreamingsignaling.Model.SignalingMessageType;
import org.example.cdnp2pstreamingsignaling.Registry.PeerNodeRegistry;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoutingService {
    private final SimpMessagingTemplate messagingTemplate;
    private final PeerNodeRegistry peerNodeRegistry;

    public void routeSignalingMessage(String senderSessionId, SignalingMessage message){
        if (message.getTargetSessionId() == null || message.getTargetSessionId().isEmpty()){
            return;
        }

        if (peerNodeRegistry.getNode(message.getTargetSessionId()) == null){

            messagingTemplate.convertAndSendToUser(
                    senderSessionId,
                    "/queue/webrtc",
                    new RoutedMessage(senderSessionId, SignalingMessageType.TARGET_OFFLINE, "Target node not found")
            );

            return;
        }

        messagingTemplate.convertAndSendToUser(
                message.getTargetSessionId(),
                "/queue/webrtc",
                new RoutedMessage(senderSessionId, message.getType(), message.getPayload())
        );
    }
}