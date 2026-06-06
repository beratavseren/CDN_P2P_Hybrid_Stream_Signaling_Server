package org.example.cdnp2pstreamingsignaling.Service.CalculatePenaltyScore;

import org.example.cdnp2pstreamingsignaling.Config.ScoringConfig;
import org.example.cdnp2pstreamingsignaling.Model.PeerNode;
import org.springframework.stereotype.Component;

@Component
public class CpuCoreCountPenaltyScoreCalculator implements PenaltyScoreCalculator {
    @Override
    public double calculatePenaltyScore(PeerNode peerNode, ScoringConfig scoringConfig){
        return (peerNode.getCpuCores() != null && peerNode.getCpuCores() > 0) ?
                1.0 - (peerNode.getCpuCores().doubleValue() / scoringConfig.getTargetCpuCoreCount()) * scoringConfig.getWeightCpuCoreCount() : scoringConfig.getPenaltyFatal();
    }
}