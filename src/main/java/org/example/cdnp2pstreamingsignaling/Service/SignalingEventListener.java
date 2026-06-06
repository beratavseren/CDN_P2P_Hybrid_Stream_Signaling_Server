package org.example.cdnp2pstreamingsignaling.Service;

import lombok.RequiredArgsConstructor;
import org.example.cdnp2pstreamingsignaling.Model.PeerNode;
import org.example.cdnp2pstreamingsignaling.Model.SignalingEvents.*;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SignalingEventListener {
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void onNodeJoined(NodeJoinEvent event) {
        messagingTemplate.convertAndSendToUser(event.newNode().getSessionId(), "/queue/sync", new NodeSyncMessage(SyncReasons.JOIN, event.newNode(), event.parentNode()));
    }

    @EventListener
    public void onParentChanged(ParentChangedEvent event){
        messagingTemplate.convertAndSendToUser(event.child().getSessionId(), "/queue/sync", new NodeSyncMessage(SyncReasons.PARENT_CHANGED, event.child(), event.newParent()));
    }

    @EventListener
    public void onChildAssignedEvent(ChildAssignedEvent event){
        messagingTemplate.convertAndSendToUser(event.parent().getSessionId(), "/queue/sync", new NodeSyncMessage(SyncReasons.CHILD_ASSIGNED, event.parent(), event.child()));
    }

    @EventListener
    public void onChildRemovedEvent(ChildRemovedEvent event){
        messagingTemplate.convertAndSendToUser(event.parent().getSessionId(), "/queue/sync", new NodeSyncMessage(SyncReasons.CHILD_REMOVED, event.parent(), null));
    }

    @EventListener
    public void onCdnFallbackTriggered(TriggerCdnFallbackEvent event){
        messagingTemplate.convertAndSendToUser(event.node().getSessionId(), "/queue/sync", new NodeSyncMessage(SyncReasons.CDN_TRIGGERED, event.node(), null));
    }

    @EventListener
    public void onParentDisconnected(ParentDisconnectedEvent event){
        messagingTemplate.convertAndSendToUser(event.children().getSessionId(), "/queue/sync", new NodeSyncMessage(SyncReasons.PARENT_DISCONNECTED, event.children(), null));
    }

    @EventListener
    public void onClusterChanged(ClusterChangedEvent event){
        messagingTemplate.convertAndSendToUser(event.node().getSessionId(), "/queue/sync", new NodeSyncMessage(SyncReasons.CLUSTER_CHANGED, event.node(), null));
    }
}
