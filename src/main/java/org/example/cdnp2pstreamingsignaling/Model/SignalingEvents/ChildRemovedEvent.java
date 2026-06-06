package org.example.cdnp2pstreamingsignaling.Model.SignalingEvents;

import org.example.cdnp2pstreamingsignaling.Model.PeerNode;

public record ChildRemovedEvent(SyncReasons reason, PeerNode parent) {
}
