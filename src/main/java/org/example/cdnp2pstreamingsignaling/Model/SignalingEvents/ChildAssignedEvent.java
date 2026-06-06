package org.example.cdnp2pstreamingsignaling.Model.SignalingEvents;

import org.example.cdnp2pstreamingsignaling.Model.PeerNode;

public record ChildAssignedEvent(SyncReasons reason, PeerNode parent, PeerNode child) {
}
