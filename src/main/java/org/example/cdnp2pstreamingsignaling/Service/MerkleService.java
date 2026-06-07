package org.example.cdnp2pstreamingsignaling.Service;

import lombok.RequiredArgsConstructor;
import org.example.cdnp2pstreamingsignaling.Model.MerkleBucket;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class MerkleService {

    private final SimpMessagingTemplate messagingTemplate;

    private final Map<Integer, MerkleBucket> hashBuckets = new ConcurrentHashMap<>();
    //deneme1

    public void collectAndVerifyHash(int sequenceId, String rootHash, String leaderSessionId, String clusterId) {
        MerkleBucket bucket = hashBuckets.computeIfAbsent(sequenceId, id -> MerkleBucket.builder()
                .sequenceId(id)
                .build());

        bucket.getVotersByRootHash().putIfAbsent(rootHash, ConcurrentHashMap.newKeySet());
        Set<String> uniqueVoters = bucket.getVotersByRootHash().get(rootHash);
        boolean isNewVote = uniqueVoters.add(leaderSessionId);
        if (!isNewVote) {
            return;
        }

        bucket.getVotesByRootHash().putIfAbsent(rootHash, new AtomicInteger(0));
        int currentVotes = bucket.getVotesByRootHash().get(rootHash).incrementAndGet();

        if (currentVotes >= 3) {
            broadcastVerifiedHash(clusterId, sequenceId, rootHash);
            hashBuckets.remove(sequenceId);
        }
    }

    private void broadcastVerifiedHash(String clusterId, int sequenceId, String rootHash) {
        String destination = "/topic/clusters/" + clusterId + "/hashes";
        Map<String, Object> payload = Map.of(
                "sequenceId", sequenceId,
                "rootHash", rootHash
        );
        messagingTemplate.convertAndSend(destination, payload);
    }
}