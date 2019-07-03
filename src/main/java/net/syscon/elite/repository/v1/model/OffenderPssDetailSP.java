package net.syscon.elite.repository.v1.model;

import lombok.*;

import java.util.Calendar;

@Data
@ToString(of = {"id","eventTimestamp"})
@Builder
public class OffenderPssDetailSP {

    private Long id;
    private Calendar eventTimestamp;
    private String prisonId;
    private Long rootOffenderId;
    private String nomsId;
    private String singleOffenderId;
    private String eventType;
    private String eventData;
}
