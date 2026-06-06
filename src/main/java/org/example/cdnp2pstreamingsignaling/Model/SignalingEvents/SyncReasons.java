package org.example.cdnp2pstreamingsignaling.Model.SignalingEvents;

public enum SyncReasons {
    JOIN,
    LEAVE,
    CHILD_ASSIGNED,
    PARENT_CHANGED,
    CHILD_REMOVED,
    PARENT_DISCONNECTED,
    ROLE_CHANGED,
    CLUSTER_CHANGED,
    CDN_TRIGGERED,
    CDN_TRIGGERED_PARENT_NOT_FOUND,
    CDN_TRIGGERED_PARENT_DISCONNECTED,
}
