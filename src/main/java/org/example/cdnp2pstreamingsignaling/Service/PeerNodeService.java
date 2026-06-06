package org.example.cdnp2pstreamingsignaling.Service;

import lombok.RequiredArgsConstructor;
import org.example.cdnp2pstreamingsignaling.Config.ScoringConfig;
import org.example.cdnp2pstreamingsignaling.Model.NodeClass;
import org.example.cdnp2pstreamingsignaling.Model.PeerNode;
import org.example.cdnp2pstreamingsignaling.Service.CalculateMaxCapacity.CapacityCalculationStrategy;
import org.example.cdnp2pstreamingsignaling.Service.CalculatePenaltyScore.PenaltyScoreCalculator;
import org.example.cdnp2pstreamingsignaling.Service.RoleAssignment.RoleAssignmentStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PeerNodeService {

    private final ScoringConfig scoringConfig;

    private final List<PenaltyScoreCalculator> penaltyScoreCalculators;
    private final RoleAssignmentStrategy roleAssignmentStrategy;
    private final CapacityCalculationStrategy capacityCalculationStrategy;

    public PeerNode handlePeerNode(PeerNode peerNode){
        peerNode.setPenaltyPoint(calculatePenaltyPoint(peerNode));
        peerNode.setCurrentRole(roleAssignmentStrategy.assignRole(peerNode, scoringConfig));
        peerNode.setMaxCapacity(capacityCalculationStrategy.calculateMaxCapacity(peerNode, scoringConfig));

        return peerNode;
    }


    private Double calculatePenaltyPoint(PeerNode peerNode){

        double penaltyPoint = 0.0;

        for (PenaltyScoreCalculator penaltyScoreCalculator : penaltyScoreCalculators) {
            penaltyPoint += penaltyScoreCalculator.calculatePenaltyScore(peerNode, scoringConfig);
        }

        return penaltyPoint;
    }














}
