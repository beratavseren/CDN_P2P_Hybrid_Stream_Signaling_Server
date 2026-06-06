package org.example.cdnp2pstreamingsignaling.Model;

import lombok.Builder;
import lombok.Data;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Data
@Builder
public class MerkleBucket {
    private int sequenceId;
    @Builder.Default
    private Map<String, AtomicInteger> votesByRootHash = new ConcurrentHashMap<>();
    @Builder.Default
    private Map<String, Set<String>> votersByRootHash = new ConcurrentHashMap<>();
}
