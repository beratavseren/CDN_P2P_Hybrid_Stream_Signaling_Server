package org.example.cdnp2pstreamingsignaling.Model.SignalingEvents;

import org.example.cdnp2pstreamingsignaling.Model.PeerNode;

public record ParentChangedEvent(SyncReasons reason, PeerNode child, PeerNode newParent) {
}
