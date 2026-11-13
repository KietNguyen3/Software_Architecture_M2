package com.vinabeef.shared.messaging;

/**
 * Port for the event broker that connects the components of the gate subsystem
 * (Kafka in the M1 design, AD-1).
 *
 * <p>Producers depend only on this abstraction, so the in-process adapter used for the demo
 * can be replaced by a Kafka adapter without changing any producer (DIP, OCP).
 */
public interface EventBroker {

    /**
     * Publishes an event. Returning normally is the broker acknowledgement; an exception means
     * the event was not accepted and the producer must keep it and retry.
     */
    void publish(Object event);
}
