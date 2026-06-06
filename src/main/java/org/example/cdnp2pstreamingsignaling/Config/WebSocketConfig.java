package org.example.cdnp2pstreamingsignaling.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // İstemcilerin dinleyeceği kanallar (Örn: /topic/hashes, /topic/clusters)
        config.enableSimpleBroker("/topic");

        // İstemcilerin sunucuya mesaj yollayacağı ön ek (Örn: /app/metrics)
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // İstemcinin (React) ilk bağlantıyı kuracağı endpoint
        registry.addEndpoint("/ws-signaling")
                .setAllowedOriginPatterns("*") // Geliştirme aşamasında CORS esnekliği
                .withSockJS(); // Fallback desteği [cite: 250]
    }
}