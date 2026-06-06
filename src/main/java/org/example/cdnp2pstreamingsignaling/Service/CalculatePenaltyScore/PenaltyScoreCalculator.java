package org.example.cdnp2pstreamingsignaling.Service.CalculatePenaltyScore;

import org.example.cdnp2pstreamingsignaling.Config.ScoringConfig;
import org.example.cdnp2pstreamingsignaling.Model.PeerNode;

public interface PenaltyScoreCalculator {
    double calculatePenaltyScore(PeerNode peerNode, ScoringConfig scoringConfig);
}
