package org.example.cdnp2pstreamingsignaling.WebSocketController;

import lombok.RequiredArgsConstructor;
import org.example.cdnp2pstreamingsignaling.Model.SignalingMessage;
import org.example.cdnp2pstreamingsignaling.Service.RoutingService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
public class RoutingController {
    private final RoutingService routingService;

    @MessageMapping("/signaling.route")
    public void handleSignalingMessage(@Payload SignalingMessage message, SimpMessageHeaderAccessor headerAccessor){
        String senderSessionId = headerAccessor.getSessionId();

        if (senderSessionId != null) {
            routingService.routeSignalingMessage(senderSessionId, message);
        }
    }
}
