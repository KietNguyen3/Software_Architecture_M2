package com.vinabeef;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.vinabeef.alert.AlertService;
import com.vinabeef.alert.AlertView;
import com.vinabeef.edge.EdgeForwarder;
import com.vinabeef.edge.GateReaderService;
import com.vinabeef.edge.RawRead;
import com.vinabeef.edge.ReadResult;
import com.vinabeef.edge.queue.LocalEventQueue;
import com.vinabeef.edge.uplink.SiteUplink;
import com.vinabeef.movement.application.HerdSessionService;
import com.vinabeef.movement.application.HerdSessionView;
import com.vinabeef.movement.application.MovementQueryService;
import com.vinabeef.registry.api.GateDirectory;
import com.vinabeef.registry.api.GateRecord;
import com.vinabeef.shared.contract.AlertType;
import com.vinabeef.shared.contract.Direction;
import com.vinabeef.shared.contract.GateReadEvent;
import com.vinabeef.shared.contract.ReadClassification;
import com.vinabeef.shared.contract.Severity;
import com.vinabeef.shared.messaging.EventBroker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * End-to-end through the real components: gate reader, durable edge queue, relay, broker,
 * movement and counting, anomaly engine and alerts. The relay's timer is slowed down so each
 * test drives forwarding itself.
 */
@SpringBootTest(properties = "vinabeef.edge.forward-interval-ms=3600000")
@AutoConfigureMockMvc
class GatePipelineIntegrationTest {

    @Autowired GateReaderService reader;
    @Autowired EdgeForwarder forwarder;
    @Autowired SiteUplink uplink;
    @Autowired LocalEventQueue queue;
    @Autowired EventBroker broker;
    @Autowired MovementQueryService movements;
    @Autowired HerdSessionService sessions;
    @Autowired AlertService alerts;
    @Autowired GateDirectory gates;
    @Autowired MockMvc mvc;

    @AfterEach
    void restoreLinks() {
        gates.findAll().forEach(gate -> uplink.setOnline(gate.barnId(), true));
        forwarder.flush();
    }

    @Test
    void readsDuringAWanOutageAreHeldAtTheEdgeAndRecordedExactlyOnceAfterReconnect() {
        GateRecord gate = gate("DL-A01-G1");
        uplink.setOnline(gate.barnId(), false);
        long before = movements.count();

        ReadResult first = reader.handle(new RawRead("704000000000001", gate.id(), Direction.OUT, null));
        reader.handle(new RawRead("704000000000002", gate.id(), Direction.OUT, null));

        assertThat(first.delivery()).isEqualTo(ReadResult.Delivery.HELD_OFFLINE);
        forwarder.flush();
        assertThat(movements.count()).isEqualTo(before);
        assertThat(queue.pendingCount(gate.barnId())).isEqualTo(2);

        uplink.setOnline(gate.barnId(), true);
        forwarder.flush();
        assertThat(movements.count()).isEqualTo(before + 2);
        assertThat(queue.pendingCount(gate.barnId())).isZero();
        assertThat(movements.find(first.eventId())).isPresent();

        // At-least-once delivery: the same event arriving again changes nothing.
        broker.publish(new GateReadEvent(first.eventId(), first.rfidTag(), first.animalCode(), first.gate(),
                first.direction(), first.occurredAt(), first.classification(), first.whitelistVersion()));
        assertThat(movements.count()).isEqualTo(before + 2);
    }

    @Test
    void unexpectedAnimalsConfirmedByTheRegistryRaiseAlerts() {
        GateRecord gate = gate("DL-A01-G1");

        ReadResult stray = reader.handle(new RawRead("704999000000777", gate.id(), Direction.IN, null));
        ReadResult wrongBarn = reader.handle(new RawRead("704000000000004", gate.id(), Direction.IN, null));
        forwarder.flush();

        assertThat(stray.classification()).isEqualTo(ReadClassification.UNREGISTERED);
        assertThat(wrongBarn.classification()).isEqualTo(ReadClassification.WRONG_BARN);

        AlertView strayAlert = alertFor(stray.eventId());
        assertThat(strayAlert.type()).isEqualTo(AlertType.UNREGISTERED_TAG);
        assertThat(strayAlert.severity()).isEqualTo(Severity.HIGH);
        assertThat(alertFor(wrongBarn.eventId()).type()).isEqualTo(AlertType.WRONG_BARN);
    }

    @Test
    void cattleThatLeftAndDidNotReturnAreReportedMissingWhenTheSessionEnds() {
        GateRecord gate = gate("DL-A02-G1");

        reader.handle(new RawRead("704000000000004", gate.id(), Direction.OUT, null));
        reader.handle(new RawRead("704000000000005", gate.id(), Direction.OUT, null));
        reader.handle(new RawRead("704000000000004", gate.id(), Direction.IN, null));
        forwarder.flush();
        HerdSessionView closed = sessions.close(gate.barnId());

        assertThat(closed.outstandingTags()).contains("704000000000005").doesNotContain("704000000000004");
        assertThat(alerts.recent(100)).anySatisfy(alert -> {
            assertThat(alert.type()).isEqualTo(AlertType.MISSING_CATTLE);
            assertThat(alert.sourceRef()).isEqualTo("herd-session:" + closed.id());
            assertThat(alert.message()).contains("VN-0005");
        });
    }

    @Test
    void hardwareEndpointRejectsMalformedTagsWithTheStandardErrorBody() throws Exception {
        mvc.perform(post("/api/scan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rfidTag\":\"E200-ABC\",\"gateId\":1,\"direction\":\"OUT\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    private GateRecord gate(String code) {
        return gates.findAll().stream().filter(g -> g.code().equals(code)).findFirst().orElseThrow();
    }

    private AlertView alertFor(String eventId) {
        return alerts.recent(100).stream().filter(a -> eventId.equals(a.sourceRef())).findFirst().orElseThrow();
    }
}
