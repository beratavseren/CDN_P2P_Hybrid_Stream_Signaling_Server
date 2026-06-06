package org.example.cdnp2pstreamingsignaling.Model.SignalingEvents;

import org.example.cdnp2pstreamingsignaling.Model.PeerNode;

public record ClusterChangedEvent(SyncReasons reason, PeerNode node) {
}
