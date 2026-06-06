package org.example.cdnp2pstreamingsignaling.Config;

import lombok.Data;
import org.example.cdnp2pstreamingsignaling.Registry.PeerNodeRegistry;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "p2p.scoring")
public class ScoringConfig {
    // Ağırlıklar (Toplamı veya dengesi sana kalmış, sistemin önem sırası)
    private double weightUploadBandwidth = 1000.0;
    private double weightCpuCoreCount = 500.0;
    private double weightBattery = 300.0;
    private double weightDeviceMemory = 200.0;
    private double penaltyFatal = 5000.0; // Mobil veya Hücresel için ölümcül ceza

    // İdeal Hedefler (Bu hedefe ulaşan 0 ceza yer)
    private double targetUploadBandwidth = 25.0;
    private int targetCpuCoreCount = 8;
    private double targetBattery = 100.0;
    private Double targetDeviceMemory = 8.0; // GB

    private Double StreamBitrateMbps = 2.5;

    private Double leaderThreshold = 100.0;
    private Double helperThreshold = 250.0;
}