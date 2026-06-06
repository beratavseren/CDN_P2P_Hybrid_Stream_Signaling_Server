package org.example.cdnp2pstreamingsignaling.Model.SignalingEvents;

import org.example.cdnp2pstreamingsignaling.Model.PeerNode;

public record NodeJoinEvent(SyncReasons reason ,PeerNode newNode, PeerNode parentNode) {
}
