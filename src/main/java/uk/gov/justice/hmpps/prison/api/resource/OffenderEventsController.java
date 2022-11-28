package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.OffenderEvent;
import uk.gov.justice.hmpps.prison.service.OffenderEventsService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Slf4j
@RestController
@Validated
@AllArgsConstructor
@Tag(name = "events")
public class OffenderEventsController {

    private final OffenderEventsService offenderEventsService;

    @GetMapping(value = "${api.base.path}/events", produces = "application/json")
    @ResponseBody
    @Operation(summary = "Get events", description = """
        **from** and **to** query params are optional.
        An awful lot of events occur every day. To guard against unintentionally heavy queries, the following rules are applied:
        If **both** are absent, scope will be limited to 24 hours starting from midnight yesterday.
        If **to** is present but **from** is absent, **from** will be defaulted to 24 hours before **to**.
        If **from** is present but **to** is absent, **to** will be defaulted to 24 hours after **from**.""")
    public ResponseEntity<List<OffenderEvent>> getEvents(@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final @RequestParam("from") Optional<LocalDateTime> maybeFrom,
                                                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final @RequestParam("to") Optional<LocalDateTime> maybeTo,
                                                         final @RequestParam("type") Optional<Set<String>> maybeTypeFilter,
                                                         final @RequestParam("sortBy") Optional<SortTypes> maybeSortBy) {
        return offenderEventsService.getEvents(maybeFrom, maybeTo, maybeTypeFilter, maybeSortBy)
                .map(events -> new ResponseEntity<>(events, HttpStatus.OK))
                .orElse(new ResponseEntity<>(NOT_FOUND));
    }

    public enum SortTypes {
        TIMESTAMP_ASC,
        TIMESTAMP_DESC
    }
}
