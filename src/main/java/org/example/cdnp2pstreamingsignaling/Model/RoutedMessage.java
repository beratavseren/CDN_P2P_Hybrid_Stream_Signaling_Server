package org.example.cdnp2pstreamingsignaling.Model;

public record RoutedMessage(String senderSessionId, SignalingMessageType type, Object payload) {
}
