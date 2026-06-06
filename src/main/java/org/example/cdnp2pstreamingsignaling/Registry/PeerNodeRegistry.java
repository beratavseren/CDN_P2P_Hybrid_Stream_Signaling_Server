package org.example.cdnp2pstreamingsignaling.Registry;

import org.example.cdnp2pstreamingsignaling.Model.PeerNode;
import org.example.cdnp2pstreamingsignaling.Model.RegionStats;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PeerNodeRegistry implements NetworkAvaregeReader{
    // sessionId -> PeerNode
    private final Map<String, PeerNode> activeNodes = new ConcurrentHashMap<>();
    // clusterId(Leader's sessionId) -> Set of sessionIds
    private final Map<String, Set<String>> activeClusters = new ConcurrentHashMap<>();
    //networkCode -> regionStats
    private final Map<String, RegionStats> networkStats = new ConcurrentHashMap<>();



    public void addNode(PeerNode peerNode){
        this.activeNodes.put(peerNode.getSessionId(), peerNode);

        addPeerNodeToCluster(peerNode);

        addPeerNodeToNetworkStats(peerNode);

        updateNetworkAvaregeOnJoin(peerNode);
    }

    public void updateNodeClusterIndex(String sessionId, String oldClusterId, String newClusterId) {

        if (oldClusterId != null && activeClusters.containsKey(oldClusterId)) {
            Set<String> oldMembers = activeClusters.get(oldClusterId);
            if (oldMembers != null) {
                oldMembers.remove(sessionId);
                if (oldMembers.isEmpty()) {
                    activeClusters.remove(oldClusterId);
                }
            }
        }

        if (newClusterId != null) {
            this.activeClusters.computeIfAbsent(newClusterId, k -> ConcurrentHashMap.newKeySet())
                    .add(sessionId);
        }
    }

    public void deleteNode(String sessionId){
        PeerNode peerNode = activeNodes.remove(sessionId);

        if (peerNode != null && peerNode.getClusterId() != null){
            Set<String> members = activeClusters.get(peerNode.getClusterId());
            if (members != null){
                members.remove(peerNode.getSessionId());
                if (members.isEmpty()){
                    activeClusters.remove(peerNode.getClusterId());
                }
            }
        }


    }

    public PeerNode getNode(String sessionId){
        return activeNodes.get(sessionId);
    }

    public List<PeerNode> getNodeChildren(String sessionId){
        Set<String> children = activeNodes.get(sessionId).getChildSessionIds();

        return children.stream().map(activeNodes::get).toList();
    }

    public Set<String> getActiveClusterIds(){
        return Collections.unmodifiableSet(activeClusters.keySet());
    }

    public Set<String> getActiveClusterMembers(String clusterId){

        if (clusterId == null){
            return Collections.emptySet();
        }

        Set<String> members = activeClusters.get(clusterId);

        if (members == null || members.isEmpty()){
            return Collections.emptySet();
        }

        return Collections.unmodifiableSet(members);
    }

    public void addPeerNodeToCluster(PeerNode peerNode){
        if (peerNode.getClusterId() != null){
            this.activeClusters.computeIfAbsent(peerNode.getClusterId(), k -> ConcurrentHashMap.newKeySet())
                    .add(peerNode.getSessionId());
        }
    }

    private void addPeerNodeToNetworkStats(PeerNode peerNode){
        if (peerNode.getNetworkCode() != null){
            this.networkStats.computeIfAbsent(peerNode.getNetworkCode(), k -> new RegionStats());
        }
    }

    public void shutDownCluster(String clusterId){
        activeClusters.remove(clusterId);
    }

    private void updateNetworkAvaregeOnJoin(PeerNode peerNode){
        double uploadBandwith = peerNode.getUploadBandwidth() == null || peerNode.getUploadBandwidth() == 0 ? 0.0 : peerNode.getUploadBandwidth();
        networkStats.get(peerNode.getNetworkCode()).adduser(uploadBandwith);
    }

    private void updateNetworkAvaregeOnLeave(PeerNode peerNode){
        double uploadBandwith = peerNode.getUploadBandwidth() == null || peerNode.getUploadBandwidth() == 0 ? 0.0 : peerNode.getUploadBandwidth();
        networkStats.get(peerNode.getNetworkCode()).deleteUser(uploadBandwith);
    }

    @Override
    public double getNetworkAvarege(String networkCode){
        RegionStats regionStats = networkStats.get(networkCode);
        if (regionStats != null){
            return regionStats.getAvarege();
        }
        return 0.0;
    }
}
