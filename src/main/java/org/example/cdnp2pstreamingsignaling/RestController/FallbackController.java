package org.example.cdnp2pstreamingsignaling.RestController;

import lombok.RequiredArgsConstructor;
import org.example.cdnp2pstreamingsignaling.Service.FallbackService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/fallback")
@RequiredArgsConstructor
public class FallbackController {

    private final FallbackService fallbackService;

    @PostMapping("/{sessionId}")
    public ResponseEntity<Map<String, Object>> triggerFallback(@PathVariable String sessionId,
                                                               @RequestBody(required = false) Map<String, Object> request) {
        String reason = request != null && request.get("reason") != null
                ? String.valueOf(request.get("reason"))
                : "P2P link dropped";

        fallbackService.triggerCdnFallback(sessionId, reason);
        return ResponseEntity.ok(Map.of(
                "sessionId", sessionId,
                "action", "SWITCH_TO_CDN",
                "status", "TRIGGERED"
        ));
    }
}
