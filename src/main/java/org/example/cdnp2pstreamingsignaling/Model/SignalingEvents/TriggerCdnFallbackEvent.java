package org.example.cdnp2pstreamingsignaling.Model.SignalingEvents;

import org.example.cdnp2pstreamingsignaling.Model.PeerNode;

import java.util.List;

public record TriggerCdnFallbackEvent(SyncReasons reason, PeerNode node) {
}
