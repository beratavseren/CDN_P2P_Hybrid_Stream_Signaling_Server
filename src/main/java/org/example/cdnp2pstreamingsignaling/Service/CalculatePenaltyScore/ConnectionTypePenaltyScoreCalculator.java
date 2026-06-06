package org.example.cdnp2pstreamingsignaling.Service.CalculatePenaltyScore;

import org.example.cdnp2pstreamingsignaling.Config.ScoringConfig;
import org.example.cdnp2pstreamingsignaling.Model.PeerNode;
import org.springframework.stereotype.Component;

@Component
public class ConnectionTypePenaltyScoreCalculator implements PenaltyScoreCalculator {
    @Override
    public double calculatePenaltyScore(PeerNode peerNode, ScoringConfig scoringConfig){
        double penaltyPoint = 0.0;

        if (peerNode.getConnectionType() != null) {
            if (peerNode.getConnectionType().equals("CELLULAR")){
                penaltyPoint += scoringConfig.getPenaltyFatal();
            }
        }

        return penaltyPoint;
    }
}