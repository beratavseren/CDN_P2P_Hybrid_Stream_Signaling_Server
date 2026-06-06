package org.example.cdnp2pstreamingsignaling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.Serializable;

@SpringBootApplication
@EnableScheduling
public class CdnP2PStreamingSignalingApplication {

	public static void main(String[] args) {
		SpringApplication.run(CdnP2PStreamingSignalingApplication.class, args);
	}

}
