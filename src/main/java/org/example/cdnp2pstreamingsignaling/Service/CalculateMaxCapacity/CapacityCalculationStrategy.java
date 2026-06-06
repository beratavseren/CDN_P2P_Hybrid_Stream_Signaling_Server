package org.example.cdnp2pstreamingsignaling.Service.CalculateMaxCapacity;

import org.example.cdnp2pstreamingsignaling.Config.ScoringConfig;
import org.example.cdnp2pstreamingsignaling.Model.PeerNode;

public interface CapacityCalculationStrategy {
    int calculateMaxCapacity(PeerNode peerNode, ScoringConfig scoringConfig);
}
