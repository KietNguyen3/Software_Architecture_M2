package com.vinabeef.edge.web;

import com.vinabeef.edge.GateReaderService;
import com.vinabeef.edge.RawRead;
import com.vinabeef.edge.ReadResult;
import com.vinabeef.shared.contract.Direction;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Hardware-facing endpoint of the Gate Reader Node ({@code POST /api/scan} in the M1 interface table). */
@RestController
public class GateReaderController {

    public record ScanRequest(@NotBlank String rfidTag, @NotNull Long gateId, Direction direction, Instant timestamp) {
    }

    private final GateReaderService reader;

    public GateReaderController(GateReaderService reader) {
        this.reader = reader;
    }

    /** 202 Accepted: the read is stored at the edge; central processing happens asynchronously. */
    @PostMapping("/api/scan")
    public ResponseEntity<ReadResult> scan(@Valid @RequestBody ScanRequest request) {
        ReadResult result = reader.handle(
                new RawRead(request.rfidTag(), request.gateId(), request.direction(), request.timestamp()));
        return ResponseEntity.accepted().body(result);
    }
}
