package org.example.cdnp2pstreamingsignaling.RestController;

import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController("/api/speedTest")
public class SpeedTestController {
    @PostMapping("/upload")
    public ResponseEntity<?> startSpeedTest(@RequestBody byte[] dummyPayload){
        return ResponseEntity.ok().build();
    }
}
