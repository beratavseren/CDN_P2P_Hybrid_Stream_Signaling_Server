package org.example.cdnp2pstreamingsignaling.Service.RoleAssignment;

import org.example.cdnp2pstreamingsignaling.Config.ScoringConfig;
import org.example.cdnp2pstreamingsignaling.Model.NodeClass;
import org.example.cdnp2pstreamingsignaling.Model.PeerNode;

public interface RoleAssignmentStrategy {
    NodeClass assignRole(PeerNode peerNode, ScoringConfig scoringConfig);
}
