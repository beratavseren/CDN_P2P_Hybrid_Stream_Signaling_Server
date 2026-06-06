package org.example.cdnp2pstreamingsignaling.Model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bytedeco.javacpp.annotation.Optional;
import org.example.cdnp2pstreamingsignaling.Config.ScoringConfig;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PeerNode {
    private String sessionId;

    private String publicIp;
    private Double latitude;
    private Double longitude;
    private String deviceType;
    private Integer cpuCores;
    private Double uploadBandwidth;
    private String networkCode;

    private NodeClass currentRole;
    private String clusterId;
    private LocalDateTime lastUpdate;

    private int maxCapacity;

    private String parentSessionId;
    @Builder.Default
    private Set<String> childSessionIds = ConcurrentHashMap.newKeySet();


    private Double batteryLevel;
    private Boolean charging;
    private String connectionType;
    private Integer deviceMemory;

    private Double penaltyPoint;

    public PeerNode(String SessionId, String publicIp,
                    Double latitude, Double longitude,
                    String deviceType, Integer cpuCores,
                    Double uploadBandwidth, Double batteryLevel,
                    Boolean charging, String connectionType,
                    Integer deviceMemory, ScoringConfig scoringConfig){
        this.sessionId=SessionId;
        this.publicIp=publicIp;
        this.latitude=latitude;
        this.longitude=longitude;
        this.deviceType=deviceType;
        this.cpuCores=cpuCores;
        this.uploadBandwidth=uploadBandwidth;
        this.batteryLevel=batteryLevel;
        this.charging=charging;
        this.connectionType=connectionType;
        this.deviceMemory=deviceMemory;
    }

    public synchronized boolean addChildSessionId(String childSessionId){
        if (this.maxCapacity > this.childSessionIds.size())
        {
            this.childSessionIds.add(childSessionId);
            return true;
        }
        return false;
    }

    public void removeChildSessionId(String childSessionId){
        childSessionIds.remove(childSessionId);
    }
}