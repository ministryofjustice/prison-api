package uk.gov.justice.hmpps.prison.api.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class IdentifyingMarkDto {

    private long markId;
    private String prisonerNumber;
    private long bookingId;
    private String bodyPart;
    private String markType;
    private String side;
    private String partOrientation;
    private String comment;
    private LocalDateTime createDateTime;
    private String createdBy;

    public IdentifyingMark toIdentifyingMark() {
        return new IdentifyingMark(markId, prisonerNumber, bodyPart, markType, side, partOrientation, comment, createDateTime, createdBy);
    }
}
