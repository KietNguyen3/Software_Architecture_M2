package com.vinabeef.shared.messaging;

import java.util.Objects;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Demo stand-in for Kafka: delivers events to in-process subscribers through Spring's event bus.
 * Delivery is synchronous, so a subscriber failure surfaces to the producer as a missing
 * acknowledgement, the same way a rejected Kafka send would.
 */
@Component
class InProcessEventBroker implements EventBroker {

    private final ApplicationEventPublisher publisher;

    InProcessEventBroker(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void publish(Object event) {
        publisher.publishEvent(Objects.requireNonNull(event, "event"));
    }
}
