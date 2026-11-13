package com.vinabeef;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * VinaBeef gate subsystem: RFID movement recording, counting and alerting (FR-03, FR-04),
 * with the master data it depends on (FR-01, FR-02).
 *
 * <p>For the demo, the edge part (Gate Reader Node) and the central part run in one process.
 * They only communicate through {@link com.vinabeef.shared.messaging.EventBroker} and the
 * message contracts in {@code com.vinabeef.shared.contract}, as they would over Kafka.
 */
@SpringBootApplication
@EnableScheduling
public class LivestockApplication {

    public static void main(String[] args) {
        SpringApplication.run(LivestockApplication.class, args);
    }
}
