package org.example.cdnp2pstreamingsignaling.Model.SignalingEvents;

import org.example.cdnp2pstreamingsignaling.Model.PeerNode;

public record NodeSyncMessage(SyncReasons reason, PeerNode nodeState, PeerNode relatedNode) {
}