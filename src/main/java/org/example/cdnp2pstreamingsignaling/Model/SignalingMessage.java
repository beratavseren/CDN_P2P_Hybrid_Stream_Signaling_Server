package org.example.cdnp2pstreamingsignaling.Model;

import lombok.Data;

@Data
public class SignalingMessage {
    private String targetSessionId;
    private SignalingMessageType type;
    private Object payload;
}
