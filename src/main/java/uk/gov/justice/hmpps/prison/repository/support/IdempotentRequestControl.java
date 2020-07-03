package uk.gov.justice.hmpps.prison.repository.support;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IdempotentRequestControl {
    private String correlationId;
    private LocalDateTime createDatetime;
    private String response;
    private Integer responseStatus;
    private Status requestStatus;

    public boolean isNew() {
        return Status.NEW == requestStatus;
    }

    public boolean isPending() {
        return Status.PENDING == requestStatus;
    }

    public boolean isComplete() {
        return Status.COMPLETE == requestStatus;
    }

    public enum Status {
        NEW,
        PENDING,
        COMPLETE
    }
}
