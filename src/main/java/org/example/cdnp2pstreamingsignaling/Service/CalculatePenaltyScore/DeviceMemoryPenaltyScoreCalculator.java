package org.example.cdnp2pstreamingsignaling.Service.CalculatePenaltyScore;

import org.example.cdnp2pstreamingsignaling.Config.ScoringConfig;
import org.example.cdnp2pstreamingsignaling.Model.PeerNode;
import org.springframework.stereotype.Component;

@Component
public class DeviceMemoryPenaltyScoreCalculator implements PenaltyScoreCalculator {
    @Override
    public double calculatePenaltyScore(PeerNode peerNode, ScoringConfig scoringConfig){
        double penaltyPoint = 0.0;

        if (peerNode.getDeviceMemory() != null && peerNode.getDeviceMemory() > 0.0) {
            penaltyPoint += 1.0 - (peerNode.getDeviceMemory()/scoringConfig.getTargetDeviceMemory()) * scoringConfig.getWeightDeviceMemory();
        }

        return penaltyPoint;
    }
}