package org.example.cdnp2pstreamingsignaling.Service.CalculatePenaltyScore;

import org.example.cdnp2pstreamingsignaling.Config.ScoringConfig;
import org.example.cdnp2pstreamingsignaling.Model.PeerNode;
import org.example.cdnp2pstreamingsignaling.Registry.NetworkAvaregeReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UploadBandwithPenaltyScoreCalculator implements PenaltyScoreCalculator {

    @Autowired
    NetworkAvaregeReader networkAvaregeReader;

    @Override
    public double calculatePenaltyScore(PeerNode peerNode, ScoringConfig scoringConfig) {
        if (peerNode.getUploadBandwidth() == null || peerNode.getUploadBandwidth() <= 0) {
            return scoringConfig.getPenaltyFatal();
        }

        double regionAverage = networkAvaregeReader.getNetworkAvarege(peerNode.getNetworkCode());

        double absoluteMinimum = scoringConfig.getStreamBitrateMbps() * 1.5;
        double dynamicTarget = Math.max(absoluteMinimum, regionAverage * 1.5);

        double bandwidthDeficit = Math.max(0.0, 1.0 - (peerNode.getUploadBandwidth() / dynamicTarget));

        return bandwidthDeficit * scoringConfig.getWeightUploadBandwidth();
    }
}