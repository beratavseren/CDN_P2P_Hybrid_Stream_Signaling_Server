package org.example.cdnp2pstreamingsignaling.Service;


import lombok.RequiredArgsConstructor;
import org.example.cdnp2pstreamingsignaling.Config.ScoringConfig;
import org.example.cdnp2pstreamingsignaling.Model.NodeClass;
import org.example.cdnp2pstreamingsignaling.Model.PeerNode;
import org.example.cdnp2pstreamingsignaling.Model.SignalingEvents.*;
import org.example.cdnp2pstreamingsignaling.Registry.PeerNodeRegistry;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClusteringService {

    //DENEME3

    private final PeerNodeRegistry peerNodeRegistry;
    private final PeerNodeService peerNodeService;
    private final ApplicationEventPublisher eventPublisher;
    private final ScoringConfig scoringConfig;

    public void handleNodeDisconnect(String sessionId, boolean isGraceful){
        if (isGraceful){
            handleNodeDisconnectGraceful(sessionId);
        }else {
            handleNodeDisconnectUngraceful(sessionId);
        }
    }

    public void handleNodeDisconnectGraceful(String sessionId) {
        PeerNode leavingNode = peerNodeRegistry.getNode(sessionId);

        if (leavingNode == null) return;

        if (leavingNode.getParentSessionId() != null && !leavingNode.getParentSessionId().equals("CDN_ROOT")) {

            PeerNode parentNode = peerNodeRegistry.getNode(leavingNode.getParentSessionId());

            if (parentNode != null){
                parentNode.removeChildSessionId(sessionId);
                eventPublisher.publishEvent(new ChildRemovedEvent(SyncReasons.CHILD_REMOVED, parentNode));
            }
        }

        handleDisconnectByRole(leavingNode);

        peerNodeRegistry.deleteNode(leavingNode.getSessionId());
    }

    private void handleDisconnectByRole(PeerNode leavingNode){
        List<PeerNode> childrens = peerNodeRegistry.getNodeChildren(leavingNode.getSessionId());

        if (leavingNode.getCurrentRole() == NodeClass.LEADER){
            //all childrens are helpers
            handleLeaderDisconnectGraceful(leavingNode, childrens);

        }else if (leavingNode.getCurrentRole() == NodeClass.HELPER){

            for (PeerNode child : childrens) {
                child.setParentSessionId(null);
                eventPublisher.publishEvent(new ParentDisconnectedEvent(SyncReasons.PARENT_DISCONNECTED, child));
                handleHelperChildrenOnGracefulDisconnect(child);
            }
        }
    }

    //all childrens are helper
    private void handleLeaderDisconnectGraceful(PeerNode leavingNode, List<PeerNode> helperChildrens){

        PeerNode newLeader = chooseNewLeaderAmongHelpersInCluster(helperChildrens);

        if (newLeader  == null){
            shutDownCluster(leavingNode.getClusterId(), helperChildrens);
        }else {
            helperChildrens.remove(newLeader);
            recoverCluster(leavingNode.getClusterId(), newLeader, helperChildrens);
        }

    }


    //todo: lider parent bulunamayan helperların sürekli cdn de kalmasını engelleyecek bir mekanizma yaz mümkünse state barındırmayan bir mekanizma olsun mesela eğer cdn e bağlı lider olmayan node varsa yeni bir lider geldiğinde onu yeni lidere yamamaya çalışmak gibi
    private void shutDownCluster(String clusterId, List<PeerNode> clusterHelperNodes){
        clusterHelperNodes.forEach(helperNode -> {
            helperNode.setClusterId("ORPHAN_" + helperNode.getSessionId());
            helperNode.setParentSessionId("CDN_ROOT");

            eventPublisher.publishEvent(new ParentDisconnectedEvent(SyncReasons.PARENT_DISCONNECTED, helperNode));

            peerNodeRegistry.getNodeChildren(helperNode.getSessionId()).forEach(child -> {
                child.setClusterId("ORPHAN_" + helperNode.getSessionId());
                eventPublisher.publishEvent(new ClusterChangedEvent(SyncReasons.CLUSTER_CHANGED, child));
            });
        });

        clusterHelperNodes.forEach(helperNode ->{
            PeerNode newLeaderParent = assignClusterAndParentGreedy(helperNode);
            if (newLeaderParent != null){
                eventPublisher.publishEvent(new ChildAssignedEvent(SyncReasons.CHILD_ASSIGNED, newLeaderParent, helperNode));
                eventPublisher.publishEvent(new ParentChangedEvent(SyncReasons.PARENT_CHANGED, helperNode, newLeaderParent));

                peerNodeRegistry.getNodeChildren(helperNode.getSessionId()).forEach(child -> {
                    child.setClusterId(newLeaderParent.getClusterId());
                    eventPublisher.publishEvent(new ClusterChangedEvent(SyncReasons.CLUSTER_CHANGED, child));
                });
            }
        });

        peerNodeRegistry.shutDownCluster(clusterId);
    }

    private void recoverCluster(String clusterId, PeerNode newLeader, List<PeerNode> clusterHelperNodes){
        peerNodeRegistry.shutDownCluster(clusterId);

        newLeader.setClusterId(newLeader.getSessionId());
        newLeader.setParentSessionId("CDN_ROOT");
        newLeader.setCurrentRole(NodeClass.LEADER);
        eventPublisher.publishEvent(new NodePromotedToLeader(SyncReasons.ROLE_CHANGED, newLeader));
        peerNodeRegistry.addPeerNodeToCluster(newLeader);

        List<PeerNode> newLeadersOldPassiveChildrens = peerNodeRegistry.getNodeChildren(newLeader.getSessionId());

        for (PeerNode child : newLeadersOldPassiveChildrens) {
            newLeader.removeChildSessionId(child.getSessionId());
        }

        // yeni liderin pasif çocuklarını diğer helperlara dağıtmaya çalış
        for (PeerNode helperNode : clusterHelperNodes) {

            int helperNodeAvaliableCapacity = helperNode.getMaxCapacity() - helperNode.getChildSessionIds().size();

            if (helperNodeAvaliableCapacity <= 0){
                continue;
            }

            for (int i = 0; i < helperNodeAvaliableCapacity && i < newLeadersOldPassiveChildrens.size(); i++) {

                PeerNode passiveChild = newLeadersOldPassiveChildrens.get(i);

                if (helperNode.addChildSessionId(passiveChild.getSessionId())){
                    passiveChild.setClusterId(newLeader.getSessionId());
                    passiveChild.setParentSessionId(helperNode.getSessionId());
                    eventPublisher.publishEvent(new ChildAssignedEvent(SyncReasons.CHILD_ASSIGNED, helperNode, passiveChild));
                    eventPublisher.publishEvent(new ParentChangedEvent(SyncReasons.PARENT_CHANGED, passiveChild, helperNode));
                    newLeadersOldPassiveChildrens.remove(passiveChild);
                    helperNodeAvaliableCapacity--;
                    i--;
                }
            }
        }

        // helperlar yeni liderin eski pasif çocuklarını almaya güçleri yetmiyorsa ya da sadece bir kısmını alabiliyorlars kalan pasifleri başka clustera yama
        if (!newLeadersOldPassiveChildrens.isEmpty()) {

            for(PeerNode oldPassiveChild : newLeadersOldPassiveChildrens){
                oldPassiveChild.setClusterId("ORPHAN_" + oldPassiveChild.getSessionId());
                oldPassiveChild.setParentSessionId("CDN_ROOT");
                eventPublisher.publishEvent(new ParentDisconnectedEvent(SyncReasons.PARENT_DISCONNECTED, oldPassiveChild));
                PeerNode oldPassiveChildNewParent = assignClusterAndParentGreedy(oldPassiveChild);

                if (oldPassiveChildNewParent != null){
                    oldPassiveChild.setClusterId(oldPassiveChildNewParent.getClusterId());
                    oldPassiveChild.setParentSessionId(oldPassiveChildNewParent.getSessionId());
                    eventPublisher.publishEvent(new ParentChangedEvent(SyncReasons.PARENT_CHANGED, oldPassiveChild, oldPassiveChildNewParent));
                    eventPublisher.publishEvent(new ChildAssignedEvent(SyncReasons.CHILD_ASSIGNED, oldPassiveChildNewParent, oldPassiveChild));
                }
            }
        }

        // helperları lidere bağla.
        for (int i = 0; i < clusterHelperNodes.size(); i++) {
            PeerNode helperNode = clusterHelperNodes.get(i);
            if (newLeader.addChildSessionId(helperNode.getSessionId())){
                helperNode.setClusterId(newLeader.getSessionId());
                helperNode.setParentSessionId(newLeader.getSessionId());
                peerNodeRegistry.addPeerNodeToCluster(helperNode);
                eventPublisher.publishEvent(new ChildAssignedEvent(SyncReasons.CHILD_ASSIGNED, newLeader, helperNode));
                eventPublisher.publishEvent(new ParentChangedEvent(SyncReasons.PARENT_CHANGED, helperNode, newLeader));
                clusterHelperNodes.remove(helperNode);
                i--;
            }
        }

        //yeni liderin tüm helperları almaya gücü yetmiyorsa eğer kalan helperları başka bir clustera yama
        if (!clusterHelperNodes.isEmpty()){
            for (int i = 0; i < clusterHelperNodes.size(); i++) {

                PeerNode helperNode = clusterHelperNodes.get(i);

                PeerNode helpersNewParent = assignClusterAndParentGreedy(helperNode);

                //todo: buraya bir göz gezdir sorun olabilir.
                if (helpersNewParent == null){
                    continue;
                }

                if (helpersNewParent.addChildSessionId(helperNode.getSessionId())){
                    helperNode.setClusterId(helpersNewParent.getClusterId());
                    helperNode.setParentSessionId(helpersNewParent.getSessionId());
                    peerNodeRegistry.addPeerNodeToCluster(helperNode);
                    eventPublisher.publishEvent(new ParentChangedEvent(SyncReasons.PARENT_CHANGED, helperNode, helpersNewParent));
                    eventPublisher.publishEvent(new ChildAssignedEvent(SyncReasons.CHILD_ASSIGNED, helpersNewParent, helperNode));
                    clusterHelperNodes.remove(helperNode);
                    i--;
                }
            }
        }

        //todo: kalan helperNodeları ve dolayısıyla altındaki pasifleri de bir yerlere yamamaya çalışacak bir mekanizma yaz
        if (!clusterHelperNodes.isEmpty()) {
            makeNewClusterWithHelpers(clusterHelperNodes);
        }

    }

    private void makeNewClusterWithHelpers(List<PeerNode> helperNodes){

    }

    private PeerNode findLeastPointNode(List<PeerNode> candidateHelperNodes){
        PeerNode leastPointNode = candidateHelperNodes.get(0);

        for (PeerNode candidateHelperNode : candidateHelperNodes) {
            if (candidateHelperNode.getPenaltyPoint()<leastPointNode.getPenaltyPoint()){
                leastPointNode = candidateHelperNode;
            }
        }

        return leastPointNode;
    }

    //returns new leader of cluster if cant find returns null
    private PeerNode chooseNewLeaderAmongHelpersInCluster(List<PeerNode> candidateHelperNodes) {
        PeerNode leastPointNode = candidateHelperNodes.get(0);

        for (PeerNode candidateHelperNode : candidateHelperNodes) {
            if (candidateHelperNode.getPenaltyPoint()<leastPointNode.getPenaltyPoint()){
                leastPointNode = candidateHelperNode;
            }
        }

        if ((leastPointNode.getPenaltyPoint() - scoringConfig.getLeaderThreshold())/100 < 0.5){
            return leastPointNode;
        }

        return null;
    }

    //leaving node is helper
    private void handleHelperChildrenOnGracefulDisconnect(PeerNode peerNode){
        PeerNode newParent = assignNewParentInCluster(peerNode.getClusterId(), peerNode);


        if (newParent == null) {
            peerNode.setClusterId("ORPHAN_" + peerNode.getSessionId());
            newParent = assignClusterAndParentGreedy(peerNode);
        }

        eventPublisher.publishEvent(new ParentChangedEvent(SyncReasons.PARENT_CHANGED, peerNode, newParent));

        if (newParent != null) {
            eventPublisher.publishEvent(new ChildAssignedEvent(SyncReasons.CHILD_ASSIGNED, newParent, peerNode));
        }
    }

    // returns new parent node in specific cluster
    private PeerNode assignNewParentInCluster(String clusterId, PeerNode peerNode){
        Set<String> clusterMemberSessionIds = peerNodeRegistry.getActiveClusterMembers(clusterId);
        PeerNode newParent = null;

        if (clusterMemberSessionIds.isEmpty()) return newParent;

        //get cluster members as PeerNode
        Set<PeerNode> clusterMembers = clusterMemberSessionIds.stream().map(peerNodeRegistry::getNode).collect(Collectors.toSet());

        //filter except helpers
        clusterMembers = clusterMembers.stream().filter(member -> member.getCurrentRole() == NodeClass.HELPER).collect(Collectors.toSet());


        for (PeerNode member : clusterMembers) {
            if (member.addChildSessionId(peerNode.getSessionId())){
                newParent = member;
                peerNode.setParentSessionId(member.getSessionId());
                return newParent;
            }
        }
        return newParent;
    }

    public void handleNodeDisconnectUngraceful(String sessionId) {
        PeerNode peerNode = peerNodeRegistry.getNode(sessionId);

    }


    public void handleNodeConnect(PeerNode peerNode) {
        peerNode = peerNodeService.handlePeerNode(peerNode);
        PeerNode parentNode = null;

        if (peerNode.getCurrentRole() == NodeClass.LEADER){
            peerNode.setClusterId(peerNode.getSessionId());
            peerNode.setParentSessionId("CDN_ROOT");
        }else {
            parentNode = assignClusterAndParentGreedy(peerNode);
        }

        peerNodeRegistry.addNode(peerNode);

        eventPublisher.publishEvent(new NodeJoinEvent(SyncReasons.JOIN, peerNode, parentNode));

        if (parentNode != null) {
            eventPublisher.publishEvent(new ChildAssignedEvent(SyncReasons.CHILD_ASSIGNED, parentNode, peerNode));
        }
    }

    //returns parent node if cant find returns null
    private PeerNode assignClusterAndParentGreedy(PeerNode peerNode){
        Set<String> clusters = findPotentialClusters(peerNode);
        return assignParentAndCluster(peerNode, clusters);
    }

    private Set<String> findPotentialClusters(PeerNode peerNode){
        Set<String> activeClusters = peerNodeRegistry.getActiveClusterIds();
        Set<String> potentialClusters = activeClusters.stream()
                .filter(clusterId -> {
                    PeerNode leaderNode = peerNodeRegistry.getNode(clusterId);

                    if (leaderNode == null) {
                        return false;
                    }

                    return Objects.equals(leaderNode.getNetworkCode(), peerNode.getNetworkCode());
                }).collect(Collectors.toSet());

        if (!potentialClusters.isEmpty()){
            return potentialClusters;
        }else {
            return activeClusters;
        }
    }


    //returns parent node if cant find returns null
    //todo: helpers should be assign as leaders child and passives should be assign as helpers child
    //todo: should assign random or else clusters will be full by order one cluster is full when other cluster just have leader
    private PeerNode assignParentAndCluster(PeerNode peerNode, Set<String> potentialClusters){
        for (String potentialClusterId : potentialClusters) {
            Set<String> memberSessionIds = peerNodeRegistry.getActiveClusterMembers(potentialClusterId);

            for(String memberSessionId : memberSessionIds){
                PeerNode potentialParentNode = peerNodeRegistry.getNode(memberSessionId);

                if (potentialParentNode == null) continue;

                if(potentialParentNode.addChildSessionId(peerNode.getSessionId())){
                    peerNode.setParentSessionId(memberSessionId);
                    peerNode.setClusterId(potentialClusterId);

                    return potentialParentNode;
                }
            }
        }

        peerNode.setParentSessionId("CDN_ROOT");
        peerNode.setClusterId("ORPHAN_" + peerNode.getSessionId());
        return null;
    }
}