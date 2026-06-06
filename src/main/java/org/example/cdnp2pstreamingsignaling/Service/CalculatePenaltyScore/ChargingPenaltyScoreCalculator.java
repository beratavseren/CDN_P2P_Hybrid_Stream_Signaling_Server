package org.example.cdnp2pstreamingsignaling.Service.CalculatePenaltyScore;

import org.example.cdnp2pstreamingsignaling.Config.ScoringConfig;
import org.example.cdnp2pstreamingsignaling.Model.PeerNode;
import org.springframework.stereotype.Component;

@Component
public class ChargingPenaltyScoreCalculator implements PenaltyScoreCalculator {
    @Override
    public double calculatePenaltyScore(PeerNode peerNode, ScoringConfig scoringConfig){
        double penaltyPoint = 0.0;

        if (peerNode.getCharging() != null && !peerNode.getCharging()) {
            penaltyPoint += (peerNode.getDeviceType() == null) ?
                    scoringConfig.getPenaltyFatal() : peerNode.getDeviceType().equals("MOBILE") ?
                    scoringConfig.getPenaltyFatal() : 0.0;
            if (peerNode.getBatteryLevel() != null && peerNode.getBatteryLevel() > 0.0) {
                if (peerNode.getBatteryLevel() < 30.0) {
                    penaltyPoint += scoringConfig.getPenaltyFatal();
                }else {
                    penaltyPoint += 1.0 - (peerNode.getBatteryLevel() / scoringConfig.getTargetBattery()) * scoringConfig.getWeightBattery();
                }
            }else {
                penaltyPoint += scoringConfig.getPenaltyFatal();
            }
        }

        return penaltyPoint;
    }
}