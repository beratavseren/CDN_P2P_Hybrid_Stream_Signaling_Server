package org.example.cdnp2pstreamingsignaling.Service.CalculateMaxCapacity;

import org.example.cdnp2pstreamingsignaling.Config.ScoringConfig;
import org.example.cdnp2pstreamingsignaling.Model.NodeClass;
import org.example.cdnp2pstreamingsignaling.Model.PeerNode;
import org.example.cdnp2pstreamingsignaling.Registry.NetworkAvaregeReader;
import org.springframework.stereotype.Component;

@Component
public class DefaultCapacityCalculationStrategy implements CapacityCalculationStrategy{
    @Override
    public int calculateMaxCapacity(PeerNode peerNode, ScoringConfig scoringConfig) {
        if (!peerNode.getCurrentRole().equals(NodeClass.PASSIVE)) {
            return (int) (peerNode.getUploadBandwidth() * 0.7 / scoringConfig.getStreamBitrateMbps());
        }else {
            peerNode.getChildSessionIds().clear();
            return 0;
        }
    }
}
