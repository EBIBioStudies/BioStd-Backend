package uk.ac.ebi.biostd.exporter.rest;

import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class NotificationController {

    @GetMapping("/api/update/partial/{fileName}")
    public String partialUpdate(@PathVariable(name = "fileName") String fileName) {
        log.info("received partial update notification at {} with file {}", Instant.now(), fileName);
        return "ok";
    }

    @GetMapping("/api/update/full/{fileName}")
    public String fullUpdate(@PathVariable(name = "fileName") String fileName) {
        log.info("received full update notification at {} with file {}", Instant.now(), fileName);
        return "ok";
    }
}
