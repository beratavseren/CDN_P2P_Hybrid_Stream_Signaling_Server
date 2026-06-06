package org.example.cdnp2pstreamingsignaling.Service.RoleAssignment;

import org.example.cdnp2pstreamingsignaling.Config.ScoringConfig;
import org.example.cdnp2pstreamingsignaling.Model.NodeClass;
import org.example.cdnp2pstreamingsignaling.Model.PeerNode;
import org.springframework.stereotype.Component;

@Component
public class DefaultRoleAssignmentStategy implements RoleAssignmentStrategy{
    @Override
    public NodeClass assignRole(PeerNode peerNode, ScoringConfig scoringConfig){
        if (peerNode.getPenaltyPoint() < scoringConfig.getLeaderThreshold()) {
            return NodeClass.LEADER;
        } else if (peerNode.getPenaltyPoint() < scoringConfig.getHelperThreshold()) {
            return NodeClass.HELPER;
        } else {
            return NodeClass.PASSIVE;
        }
    }
}
