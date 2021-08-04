package uk.gov.justice.hmpps.prison.service.transformers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import oracle.sql.RAW;
import oracle.sql.STRUCT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.justice.hmpps.prison.api.model.OffenderEvent;
import uk.gov.justice.hmpps.prison.service.xtag.Xtag;
import uk.gov.justice.hmpps.prison.service.xtag.XtagContent;
import uk.gov.justice.hmpps.prison.service.xtag.XtagEventNonJpa;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@Slf4j
@Component
public class OffenderEventsTransformer {

    private static final Map<String, String> INCIDENT_TABLE_MAP = Map.of(
        "incident_cases", "CASES",
        "incident_case_parties", "PARTIES",
        "incident_case_responses", "RESPONSES",
        "incident_case_requirements", "REQUIREMENTS"
    );

    private final TypesTransformer typesTransformer;
    private final ObjectMapper objectMapper;

    @Autowired
    public OffenderEventsTransformer(final TypesTransformer typesTransformer) {
        this.typesTransformer = typesTransformer;
        this.objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
            .registerModules(new Jdk8Module(), new JavaTimeModule());
    }

    public static LocalDateTime xtagFudgedTimestampOf(final LocalDateTime xtagEnqueueTime) {
        final var london = ZoneId.of("Europe/London");
        if (london.getRules().isDaylightSavings(xtagEnqueueTime.atZone(london).toInstant())) {
            return xtagEnqueueTime;
        }
        return xtagEnqueueTime.minusHours(1L);
    }

    public static String externalMovementEventOf(final Xtag xtag) {
        final var del = Optional.ofNullable(xtag.getContent().getP_record_deleted()).orElse("");
        return switch (del) {
            case "N" -> "EXTERNAL_MOVEMENT_RECORD-INSERTED";
            case "Y" -> "EXTERNAL_MOVEMENT_RECORD-DELETED";
            default -> "EXTERNAL_MOVEMENT_RECORD-UPDATED";
        };
    }

    public OffenderEvent offenderEventOf(final uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderEvent offenderEvent) {
        return Optional.ofNullable(offenderEvent)
            .map(event -> OffenderEvent.builder()
                .caseNoteId(caseNoteIdOf(event))
                .eventId(event.getEventId().toString())
                .eventDatetime(typesTransformer.localDateTimeOf(event.getEventTimestamp()))
                .eventType(caseNoteEventTypeOf(event))
                .rootOffenderId(event.getRootOffenderId())
                .offenderIdDisplay(event.getOffenderIdDisplay())
                .agencyLocationId(event.getAgencyLocId())
                .build()).orElse(null);
    }

    public OffenderEvent offenderEventOf(final XtagEventNonJpa xtagEvent) {
        final var s = xtagEvent.getUserData();
        try {
            return getOffenderEvent(s, xtagEvent.getEnqTime());
        } catch (final SQLException e) {
            log.error("Failed to convert STRUCT {} to OffenderEvent: {}", s, e.getMessage());
            return null;
        }
    }

    private OffenderEvent getOffenderEvent(final STRUCT s, final Timestamp enqTime) throws SQLException {
        final var maybeStruct = Arrays.stream(s.getOracleAttributes()).filter(STRUCT.class::isInstance).findFirst();
        final var maybeRaw = Arrays.stream(s.getOracleAttributes()).filter(RAW.class::isInstance).findFirst();

        Optional<String> maybeType = Optional.empty();
        {
            final var struct = maybeStruct.filter(STRUCT.class::isInstance).map(STRUCT.class::cast);

            if (struct.isPresent()) {
                final var attributes = struct.get().getAttributes();
                if (attributes.length >= 2) {
                    maybeType = Optional.ofNullable(attributes[1].toString());
                }
            }
        }

        final var maybeMap = maybeRaw.flatMap(d -> {
            try {
                return Optional.ofNullable(deserialize(d.getBytes()));
            } catch (final IOException | ClassNotFoundException e) {
                log.error("Failed to derive Map from Datum {} : {}", d, e.getMessage());
                return Optional.empty();
            }
        });

        return offenderEventOf(Xtag.builder()
            .eventType(maybeType.orElse("?"))
            .nomisTimestamp(xtagFudgedTimestampOf(enqTime.toLocalDateTime()))
            .content(maybeMap.map(this::xtagContentOf).orElse(null))
            .build());
    }

    public XtagContent xtagContentOf(final Map<String, String> map) {
        try {
            final var stringValue = objectMapper.writeValueAsString(map);
            return objectMapper.readValue(stringValue, XtagContent.class);
        } catch (final IOException e) {
            log.error("Failed to deserialize Map {} into XtagContent: {}", map.toString(), e.getMessage());
            return null;
        }
    }

    private Map<String, String> deserialize(final byte[] bytes) throws IOException, ClassNotFoundException {

        if (bytes == null) {
            log.warn("No bytes to deserialize!");
            return null;
        }

        final var o = new ObjectInputStream(new ByteArrayInputStream(bytes)).readObject();

        if (o instanceof Map) {
            return (Map<String, String>) o;
        } else {
            log.warn("Can't deserialize bytes into Map. Deserialized is of type {}", o.getClass().toString());
        }

        return null;
    }

    public OffenderEvent offenderEventOf(final Xtag xtag) {
        if (xtag == null || xtag.getEventType() == null) {
            log.warn("Bad xtag: {}", xtag);
            return null;
        }

        log.debug("Processing Xtag {}...", xtag);

        try {
            final OffenderEvent offenderEvent;
            switch (xtag.getEventType()) {
                case "P8_RESULT" -> offenderEvent = riskScoreEventOf(xtag);
                case "A3_RESULT" -> offenderEvent = offenderSanctionEventOf(xtag);
                case "P1_RESULT", "BOOK_UPD_OASYS" -> offenderEvent = bookingNumberEventOf(xtag);
                case "OFF_HEALTH_PROB_INS" -> offenderEvent = maternityStatusInsertedEventOf(xtag);
                case "OFF_HEALTH_PROB_UPD" -> offenderEvent = maternityStatusUpdatedEventOf(xtag);
                case "OFF_RECEP_OASYS" -> offenderEvent = offenderMovementReceptionEventOf(xtag);
                case "OFF_DISCH_OASYS" -> offenderEvent = offenderMovementDischargeEventOf(xtag);
                case "M1_RESULT", "M1_UPD_RESULT" -> offenderEvent = externalMovementRecordEventOf(xtag);
                case "OFF_UPD_OASYS" -> offenderEvent = !Strings.isNullOrEmpty(xtag.getContent().getP_offender_book_id()) ?
                    offenderBookingChangedEventOf(xtag) :
                    offenderDetailsChangedEventOf(xtag);
                case "ADDR_USG_INS" -> offenderEvent = addressUsageInsertedEventOf(xtag);
                case "ADDR_USG_UPD" -> offenderEvent = xtag.getContent().getP_address_deleted().equals("Y") ?
                    addressUsageDeletedEventOf(xtag) :
                    addressUsageUpdatedEventOf(xtag);
                case "P4_RESULT" -> offenderEvent = offenderAliasChangedEventOf(xtag);
                case "P2_RESULT" -> offenderEvent = offenderUpdatedEventOf(xtag);
                case "OFF_BKB_INS" -> offenderEvent = offenderBookingInsertedEventOf(xtag);
                case "OFF_BKB_UPD" -> offenderEvent = offenderBookingReassignedEventOf(xtag);
                case "OFF_CONT_PER_INS" -> offenderEvent = contactPersonInsertedEventOf(xtag);
                case "OFF_CONT_PER_UPD" -> offenderEvent = xtag.getContent().getP_address_deleted().equals("Y") ?
                    contactPersonDeletedEventOf(xtag) :
                    contactPersonUpdatedEventOf(xtag);
                case "OFF_EDUCATION_INS" -> offenderEvent = educationLevelInsertedEventOf(xtag);
                case "OFF_EDUCATION_UPD" -> offenderEvent = educationLevelUpdatedEventOf(xtag);
                case "OFF_EDUCATION_DEL" -> offenderEvent = educationLevelDeletedEventOf(xtag);
                case "P3_RESULT" -> offenderEvent = (xtag.getContent().getP_identifier_type().equals("NOMISP3")) ?
                    offenderBookingInsertedEventOf(xtag) :
                    !Strings.isNullOrEmpty(xtag.getContent().getP_identifier_value()) ?
                        offenderIdentifierInsertedEventOf(xtag) :
                        offenderIdentifierDeletedEventOf(xtag);
                case "S1_RESULT" -> offenderEvent = !Strings.isNullOrEmpty(xtag.getContent().getP_imprison_status_seq()) ?
                    imprisonmentStatusChangedEventOf(xtag) :
                    !Strings.isNullOrEmpty(xtag.getContent().getP_assessment_seq()) ?
                        assessmentChangedEventOf(xtag) :
                        !Strings.isNullOrEmpty(xtag.getContent().getP_alert_date()) ?
                            alertUpdatedEventOf(xtag) :
                            alertInsertedEventOf(xtag);
                case "OFF_ALERT_INSERT" -> offenderEvent = alertInsertedEventOf(xtag);
                case "OFF_ALERT_UPDATE" -> offenderEvent = alertUpdatedEventOf(xtag);
                case "OFF_ALERT_DELETE", "S1_DEL_RESULT" -> offenderEvent = alertDeletedEventOf(xtag);
                case "INCIDENT-INSERTED" -> offenderEvent = incidentInsertedEventOf(xtag);
                case "INCIDENT-UPDATED" -> offenderEvent = incidentUpdatedEventOf(xtag);
                case "OFF_IMP_STAT_OASYS" -> offenderEvent = imprisonmentStatusChangedEventOf(xtag);
                case "OFF_PROF_DETAIL_INS" -> offenderEvent = offenderProfileDetailInsertedEventOf(xtag);
                case "OFF_PROF_DETAIL_UPD" -> offenderEvent = offenderProfileUpdatedEventOf(xtag);
                case "S2_RESULT" -> offenderEvent = sentenceDatesChangedEventOf(xtag);
                case "A2_CALLBACK" -> offenderEvent = hearingDateChangedEventOf(xtag);
                case "A2_RESULT" -> offenderEvent = "Y".equals(xtag.getContent().getP_delete_flag()) ?
                    hearingResultDeletedEventOf(xtag) :
                    hearingResultChangedEventOf(xtag);
                case "PHONES_INS" -> offenderEvent = phoneInsertedEventOf(xtag);
                case "PHONES_UPD" -> offenderEvent = phoneUpdatedEventOf(xtag);
                case "PHONES_DEL" -> offenderEvent = phoneDeletedEventOf(xtag);
                case "OFF_EMPLOYMENTS_INS" -> offenderEvent = offenderEmploymentInsertedEventOf(xtag);
                case "OFF_EMPLOYMENTS_UPD" -> offenderEvent = offenderEmploymentUpdatedEventOf(xtag);
                case "OFF_EMPLOYMENTS_DEL" -> offenderEvent = offenderEmploymentDeletedEventOf(xtag);
                case "D5_RESULT" -> offenderEvent = hdcConditionChanged(xtag);
                case "D4_RESULT" -> offenderEvent = hdcFineInserted(xtag);
                case "ADDR_INS" -> offenderEvent = personAddressInserted(xtag);
                case "ADDR_UPD" -> {
                    if (xtag.getContent().getP_owner_class().equals("PER")) {
                        offenderEvent = xtag.getContent().getP_address_deleted().equals("N") ?
                            personAddressUpdatedEventOf(xtag) :
                            personAddressDeletedEventOf(xtag);
                    } else if (xtag.getContent().getP_owner_class().equals("OFF")) {
                        offenderEvent = xtag.getContent().getP_address_deleted().equals("N") ?
                            offenderAddressUpdatedEventOf(xtag) :
                            offenderAddressDeletedEventOf(xtag);
                    } else {
                        offenderEvent = xtag.getContent().getP_address_deleted().equals("N") ?
                            addressUpdatedEventOf(xtag) :
                            addressDeletedEventOf(xtag);
                    }
                }
                case "OFF_SENT_OASYS" -> offenderEvent = sentenceCalculationDateChangedEventOf(xtag);
                case "C_NOTIFICATION" -> offenderEvent = courtSentenceChangedEventOf(xtag);
                case "IEDT_OUT" -> offenderEvent = offenderTransferOutOfLidsEventOf(xtag);
                case "BED_ASSIGNMENT_HISTORY-INSERTED" -> offenderEvent = offenderBedAssignmentEventOf(xtag);
                case "CONFIRMED_RELEASE_DATE-CHANGED" -> offenderEvent = confirmedReleaseDateOf(xtag);
                case "OFFENDER-INSERTED", "OFFENDER-UPDATED", "OFFENDER-DELETED" -> offenderEvent = offenderUpdatedOf(xtag);
                default -> offenderEvent = OffenderEvent.builder()
                    .eventType(xtag.getEventType())
                    .eventDatetime(xtag.getNomisTimestamp())
                    .build();
            }
            return offenderEvent;
        } catch (final Throwable t) {
            log.error("Caught throwable {} {}", t.getMessage(), t.getStackTrace());
            throw t;
        }
    }

    private OffenderEvent offenderTransferOutOfLidsEventOf(final Xtag xtag) {
        return OffenderEvent.builder()
            .eventType("OFFENDER_TRANSFER-OUT_OF_LIDS")
            .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
            .eventDatetime(xtag.getNomisTimestamp())
            .nomisEventType(xtag.getEventType())
            .build();
    }

    private OffenderEvent offenderBedAssignmentEventOf(final Xtag xtag) {
        return OffenderEvent.builder()
            .eventType("BED_ASSIGNMENT_HISTORY-INSERTED")
            .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
            .bedAssignmentSeq(integerOf(xtag.getContent().getP_bed_assign_seq()))
            .livingUnitId(longOf(xtag.getContent().getP_living_unit_id()))
            .eventDatetime(xtag.getNomisTimestamp())
            .nomisEventType(xtag.getEventType())
            .build();
    }

    private OffenderEvent courtSentenceChangedEventOf(final Xtag xtag) {
        return OffenderEvent.builder()
            .eventType("COURT_SENTENCE-CHANGED")
            .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
            .eventDatetime(xtag.getNomisTimestamp())
            .nomisEventType(xtag.getEventType())
            .build();
    }

    private OffenderEvent alertDeletedEventOf(final Xtag xtag) {
        return OffenderEvent.builder()
            .eventType("ALERT-DELETED")
            .offenderId(longOf(xtag.getContent().getP_offender_id()))
            .rootOffenderId(longOf(xtag.getContent().getP_root_offender_id()))
            .alertDateTime(localDateTimeOf(xtag.getContent().getP_alert_date(), xtag.getContent().getP_alert_time()))
            .alertType(xtag.getContent().getP_alert_type())
            .alertCode(xtag.getContent().getP_alert_code())
            .expiryDateTime(localDateTimeOf(xtag.getContent().getP_expiry_date(), xtag.getContent().getP_expiry_time()))
            .eventDatetime(xtag.getNomisTimestamp())
            .nomisEventType(xtag.getEventType())
            .build();
    }

    private OffenderEvent personAddressUpdatedEventOf(final Xtag xtag) {
        return OffenderEvent.builder()
            .eventType("PERSON_ADDRESS-UPDATED")
            .addressId(longOf(xtag.getContent().getP_address_id()))
            .ownerId(longOf(xtag.getContent().getP_owner_id()))
            .ownerClass(xtag.getContent().getP_owner_class())
            .addressEndDate(localDateOf(xtag.getContent().getP_address_end_date()))
            .primaryAddressFlag(xtag.getContent().getP_primary_addr_flag())
            .mailAddressFlag(xtag.getContent().getP_mail_addr_flag())
            .personId(longOf(xtag.getContent().getP_person_id()))
            .eventDatetime(xtag.getNomisTimestamp())
            .nomisEventType(xtag.getEventType())
            .build();
    }

    private OffenderEvent offenderAddressUpdatedEventOf(final Xtag xtag) {
        return OffenderEvent.builder()
            .eventType("OFFENDER_ADDRESS-UPDATED")
            .addressId(longOf(xtag.getContent().getP_address_id()))
            .ownerId(longOf(xtag.getContent().getP_owner_id()))
            .ownerClass(xtag.getContent().getP_owner_class())
            .addressEndDate(localDateOf(xtag.getContent().getP_address_end_date()))
            .primaryAddressFlag(xtag.getContent().getP_primary_addr_flag())
            .mailAddressFlag(xtag.getContent().getP_mail_addr_flag())
            .personId(longOf(xtag.getContent().getP_person_id()))
            .eventDatetime(xtag.getNomisTimestamp())
            .nomisEventType(xtag.getEventType())
            .build();
    }

    private OffenderEvent addressUpdatedEventOf(final Xtag xtag) {
        return OffenderEvent.builder()
            .eventType("ADDRESS-UPDATED")
            .addressId(longOf(xtag.getContent().getP_address_id()))
            .ownerId(longOf(xtag.getContent().getP_owner_id()))
            .ownerClass(xtag.getContent().getP_owner_class())
            .addressEndDate(localDateOf(xtag.getContent().getP_address_end_date()))
            .primaryAddressFlag(xtag.getContent().getP_primary_addr_flag())
            .mailAddressFlag(xtag.getContent().getP_mail_addr_flag())
            .personId(longOf(xtag.getContent().getP_person_id()))
            .eventDatetime(xtag.getNomisTimestamp())
            .nomisEventType(xtag.getEventType())
            .build();
    }

    private OffenderEvent personAddressDeletedEventOf(final Xtag xtag) {
        return OffenderEvent.builder()
            .eventType("PERSON_ADDRESS-DELETED")
            .addressId(longOf(xtag.getContent().getP_address_id()))
            .ownerId(longOf(xtag.getContent().getP_owner_id()))
            .ownerClass(xtag.getContent().getP_owner_class())
            .addressEndDate(localDateOf(xtag.getContent().getP_address_end_date()))
            .primaryAddressFlag(xtag.getContent().getP_primary_addr_flag())
            .mailAddressFlag(xtag.getContent().getP_mail_addr_flag())
            .personId(longOf(xtag.getContent().getP_person_id()))
            .eventDatetime(xtag.getNomisTimestamp())
            .nomisEventType(xtag.getEventType())
            .build();
    }

    private OffenderEvent offenderAddressDeletedEventOf(final Xtag xtag) {
        return OffenderEvent.builder()
            .eventType("OFFENDER_ADDRESS-DELETED")
            .addressId(longOf(xtag.getContent().getP_address_id()))
            .ownerId(longOf(xtag.getContent().getP_owner_id()))
            .ownerClass(xtag.getContent().getP_owner_class())
            .addressEndDate(localDateOf(xtag.getContent().getP_address_end_date()))
            .primaryAddressFlag(xtag.getContent().getP_primary_addr_flag())
            .mailAddressFlag(xtag.getContent().getP_mail_addr_flag())
            .personId(longOf(xtag.getContent().getP_person_id()))
            .eventDatetime(xtag.getNomisTimestamp())
            .nomisEventType(xtag.getEventType())
            .build();
    }

    private OffenderEvent addressDeletedEventOf(final Xtag xtag) {
        return OffenderEvent.builder()
            .eventType("ADDRESS-DELETED")
            .addressId(longOf(xtag.getContent().getP_address_id()))
            .ownerId(longOf(xtag.getContent().getP_owner_id()))
            .ownerClass(xtag.getContent().getP_owner_class())
            .addressEndDate(localDateOf(xtag.getContent().getP_address_end_date()))
            .primaryAddressFlag(xtag.getContent().getP_primary_addr_flag())
            .mailAddressFlag(xtag.getContent().getP_mail_addr_flag())
            .personId(longOf(xtag.getContent().getP_person_id()))
            .eventDatetime(xtag.getNomisTimestamp())
            .nomisEventType(xtag.getEventType())
            .build();
    }

    private OffenderEvent personAddressInserted(final Xtag xtag) {
        return OffenderEvent.builder()
            .eventType("PERSON_ADDRESS-INSERTED")
            .rootOffenderId(longOf(xtag.getContent().getP_root_offender_id()))
            .addressId(longOf(xtag.getContent().getP_address_id()))
            .ownerId(longOf(xtag.getContent().getP_owner_id()))
            .ownerClass(xtag.getContent().getP_owner_class())
            .addressEndDate(localDateOf(xtag.getContent().getP_address_end_date()))
            .primaryAddressFlag(xtag.getContent().getP_primary_addr_flag())
            .mailAddressFlag(xtag.getContent().getP_mail_addr_flag())
            .personId(longOf(xtag.getContent().getP_person_id()))
            .eventDatetime(xtag.getNomisTimestamp())
            .nomisEventType(xtag.getEventType())
            .build();
    }

    private OffenderEvent hdcFineInserted(final Xtag xtag) {
        return OffenderEvent.builder()
            .eventType("HDC_FINE-INSERTED")
            .eventDatetime(xtag.getNomisTimestamp())
            .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
            .sentenceSeq(longOf(xtag.getContent().getP_sentence_seq()))
            .nomisEventType(xtag.getEventType())
            .build();
    }

    private OffenderEvent hdcConditionChanged(final Xtag xtag) {
        return OffenderEvent.builder()
            .eventType("HDC_CONDITION-CHANGED")
            .eventDatetime(xtag.getNomisTimestamp())
            .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
            .sentenceSeq(longOf(xtag.getContent().getP_sentence_seq()))
            .conditionCode(xtag.getContent().getP_condition_code())
            .offenderSentenceConditionId(longOf(xtag.getContent().getP_offender_sent_calculation_id()))
            .nomisEventType(xtag.getEventType())
            .build();
    }

    private OffenderEvent offenderEmploymentInsertedEventOf(final Xtag xtag) {
        return OffenderEvent.builder()
            .eventType("OFFENDER_EMPLOYMENT-INSERTED")
            .eventDatetime(xtag.getNomisTimestamp())
            .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
            .nomisEventType(xtag.getEventType())
            .build();
    }

    private OffenderEvent offenderEmploymentUpdatedEventOf(final Xtag xtag) {
        return OffenderEvent.builder()
            .eventType("OFFENDER_EMPLOYMENT-UPDATED")
            .eventDatetime(xtag.getNomisTimestamp())
            .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
            .nomisEventType(xtag.getEventType())
            .build();
    }

    private OffenderEvent offenderEmploymentDeletedEventOf(final Xtag xtag) {
        return OffenderEvent.builder()
            .eventType("OFFENDER_EMPLOYMENT-DELETED")
            .eventDatetime(xtag.getNomisTimestamp())
            .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
            .nomisEventType(xtag.getEventType())
            .build();
    }

    private OffenderEvent phoneInsertedEventOf(final Xtag xtag) {
        return OffenderEvent.builder()
            .eventType("PHONE-INSERTED")
            .ownerId(longOf(xtag.getContent().getP_owner_id()))
            .ownerClass(xtag.getContent().getP_owner_class())
            .eventDatetime(xtag.getNomisTimestamp())
            .nomisEventType(xtag.getEventType())
            .build();
    }

    private OffenderEvent phoneUpdatedEventOf(final Xtag xtag) {
        return OffenderEvent.builder()
            .eventType("PHONE-UPDATED")
            .ownerId(longOf(xtag.getContent().getP_owner_id()))
            .ownerClass(xtag.getContent().getP_owner_class())
            .eventDatetime(xtag.getNomisTimestamp())
            .nomisEventType(xtag.getEventType())
            .build();
    }

    private OffenderEvent phoneDeletedEventOf(final Xtag xtag) {
        return OffenderEvent.builder()
            .eventType("PHONE-DELETED")
            .ownerId(longOf(xtag.getContent().getP_owner_id()))
            .ownerClass(xtag.getContent().getP_owner_class())
            .eventDatetime(xtag.getNomisTimestamp())
            .nomisEventType(xtag.getEventType())
            .build();
    }

    private OffenderEvent hearingResultChangedEventOf(final Xtag xtag) {
        return OffenderEvent.builder()
            .eventType("HEARING_RESULT-CHANGED")
            .oicHearingId(longOf(xtag.getContent().getP_oic_hearing_id()))
            .resultSeq(longOf(xtag.getContent().getP_result_seq()))
            .agencyIncidentId(longOf(xtag.getContent().getP_agency_incident_id()))
            .chargeSeq(longOf(xtag.getContent().getP_charge_seq()))
            .eventDatetime(xtag.getNomisTimestamp())
            .nomisEventType(xtag.getEventType())
            .build();
    }

    private OffenderEvent hearingResultDeletedEventOf(final Xtag xtag) {
        return OffenderEvent.builder()
            .eventType("HEARING_RESULT-DELETED")
            .oicHearingId(longOf(xtag.getContent().getP_oic_hearing_id()))
            .resultSeq(longOf(xtag.getContent().getP_result_seq()))
            .agencyIncidentId(longOf(xtag.getContent().getP_agency_incident_id()))
            .chargeSeq(longOf(xtag.getContent().getP_charge_seq()))
            .oicOffenceId(longOf(xtag.getContent().getP_oic_offence_id()))
            .pleaFindingCode(xtag.getContent().getP_plea_finding_code())
            .findingCode(xtag.getContent().getP_finding_code())
            .eventDatetime(xtag.getNomisTimestamp())
            .nomisEventType(xtag.getEventType())
            .build();
    }

    private OffenderEvent hearingDateChangedEventOf(final Xtag xtag) {
        return OffenderEvent.builder()
            .eventType("HEARING_DATE-CHANGED")
            .oicHearingId(longOf(xtag.getContent().getP_oic_hearing_id()))
            .eventDatetime(xtag.getNomisTimestamp())
            .nomisEventType(xtag.getEventType())
            .build();
    }

    private OffenderEvent sentenceCalculationDateChangedEventOf(final Xtag xtag) {
        return OffenderEvent.builder()
            .eventType("SENTENCE_CALCULATION_DATES-CHANGED")
            .eventDatetime(xtag.getNomisTimestamp())
            .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
            .nomisEventType(xtag.getEventType())
            .build();
    }

    private OffenderEvent sentenceDatesChangedEventOf(final Xtag xtag) {
        return OffenderEvent.builder()
            .eventType("SENTENCE_DATES-CHANGED")
            .eventDatetime(xtag.getNomisTimestamp())
            .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
            .sentenceCalculationId(longOf(xtag.getContent().getP_offender_sent_calculation_id()))
            .nomisEventType(xtag.getEventType())
            .build();
    }

    private OffenderEvent offenderProfileUpdatedEventOf(final Xtag xtag) {
        return OffenderEvent.builder()
            .eventType("OFFENDER_PROFILE_DETAILS-UPDATED")
            .eventDatetime(xtag.getNomisTimestamp())
            .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
            .nomisEventType(xtag.getEventType())
            .build();
    }

    private OffenderEvent offenderProfileDetailInsertedEventOf(final Xtag xtag) {
        return OffenderEvent.builder()
            .eventType("OFFENDER_PROFILE_DETAILS-INSERTED")
            .eventDatetime(xtag.getNomisTimestamp())
            .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
            .nomisEventType(xtag.getEventType())
            .build();
    }

    private OffenderEvent alertInsertedEventOf(final Xtag xtag) {
        return OffenderEvent.builder()
            .eventType("ALERT-INSERTED")
            .eventDatetime(xtag.getNomisTimestamp())
            .rootOffenderId(longOf(xtag.getContent().getP_root_offender_id()))
            .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
            .alertSeq(longOf(xtag.getContent().getP_alert_seq()))
            .alertDateTime(localDateTimeOf(xtag.getContent().getP_alert_date(), xtag.getContent().getP_alert_time()))
            .alertType(xtag.getContent().getP_alert_type())
            .alertCode(xtag.getContent().getP_alert_code())
            .nomisEventType(xtag.getEventType())
            .build();
    }

    private OffenderEvent alertUpdatedEventOf(final Xtag xtag) {
        return OffenderEvent.builder()
            .eventType("ALERT-UPDATED")
            .eventDatetime(xtag.getNomisTimestamp())
            .rootOffenderId(longOf(xtag.getContent().getP_root_offender_id()))
            .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
            .alertSeq(longOf(xtag.getContent().getP_alert_seq()))
            .alertDateTime(localDateTimeOf(xtag.getContent().getP_old_alert_date(), xtag.getContent().getP_old_alert_time()))
            .alertType(xtag.getContent().getP_alert_type())
            .alertCode(xtag.getContent().getP_alert_code())
            .nomisEventType(xtag.getEventType())
            .build();
    }

    private OffenderEvent assessmentChangedEventOf(final Xtag xtag) {
        return OffenderEvent.builder()
            .eventType("ASSESSMENT-CHANGED")
            .eventDatetime(xtag.getNomisTimestamp())
            .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
            .assessmentSeq(longOf(xtag.getContent().getP_assessment_seq()))
            .nomisEventType(xtag.getEventType())
            .build();
    }

    private OffenderEvent imprisonmentStatusChangedEventOf(final Xtag xtag) {
        return OffenderEvent.builder()
            .eventType("IMPRISONMENT_STATUS-CHANGED")
            .eventDatetime(xtag.getNomisTimestamp())
            .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
            .imprisonmentStatusSeq(longOf(xtag.getContent().getP_imprison_status_seq()))
            .nomisEventType(xtag.getEventType())
            .build();
    }

    private OffenderEvent incidentInsertedEventOf(final Xtag xtag) {
        return OffenderEvent.builder()
            .eventType("INCIDENT-INSERTED")
            .eventDatetime(xtag.getNomisTimestamp())
            .incidentCaseId(longOf(xtag.getContent().getP_incident_case_id()))
            .nomisEventType(xtag.getEventType())
            .build();
    }

    private OffenderEvent incidentUpdatedEventOf(final Xtag xtag) {
        return OffenderEvent.builder()
            .eventType("INCIDENT-" + (xtag.getContent().getP_delete_flag().equals("N") ? "CHANGED-" : "DELETED-")
                + INCIDENT_TABLE_MAP.get(xtag.getContent().getP_table_name()))
            .eventDatetime(xtag.getNomisTimestamp())
            .incidentCaseId(longOf(xtag.getContent().getP_incident_case_id()))
            .incidentPartySeq(longOf(xtag.getContent().getP_party_seq()))
            .incidentRequirementSeq(longOf(xtag.getContent().getP_requirement_seq()))
            .incidentQuestionSeq(longOf(xtag.getContent().getP_question_seq()))
            .incidentResponseSeq(longOf(xtag.getContent().getP_response_seq()))
            .nomisEventType(xtag.getEventType())
            .build();
    }

    private OffenderEvent offenderIdentifierInsertedEventOf(final Xtag xtag) {
        return OffenderEvent.builder()
            .eventType("OFFENDER_IDENTIFIER-INSERTED")
            .eventDatetime(xtag.getNomisTimestamp())
            .offenderId(longOf(xtag.getContent().getP_offender_id()))
            .rootOffenderId(longOf(xtag.getContent().getP_root_offender_id()))
            .identifierType(xtag.getContent().getP_identifier_type())
            .identifierValue(xtag.getContent().getP_identifier_value())
            .nomisEventType(xtag.getEventType())
            .build();
    }

    private OffenderEvent offenderIdentifierDeletedEventOf(final Xtag xtag) {
        return OffenderEvent.builder()
            .eventType("OFFENDER_IDENTIFIER-DELETED")
            .eventDatetime(xtag.getNomisTimestamp())
            .offenderId(longOf(xtag.getContent().getP_offender_id()))
            .rootOffenderId(longOf(xtag.getContent().getP_root_offender_id()))
            .identifierType(xtag.getContent().getP_identifier_type())
            .nomisEventType(xtag.getEventType())
            .build();
    }

    private OffenderEvent educationLevelInsertedEventOf(final Xtag xtag) {
        return OffenderEvent.builder()
            .eventType("EDUCATION_LEVEL-INSERTED")
            .eventDatetime(xtag.getNomisTimestamp())
            .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
            .nomisEventType(xtag.getEventType())
            .build();
    }

    private OffenderEvent educationLevelUpdatedEventOf(final Xtag xtag) {
        return OffenderEvent.builder()
            .eventType("EDUCATION_LEVEL-UPDATED")
            .eventDatetime(xtag.getNomisTimestamp())
            .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
            .nomisEventType(xtag.getEventType())
            .build();
    }

    private OffenderEvent educationLevelDeletedEventOf(final Xtag xtag) {
        return OffenderEvent.builder()
            .eventType("EDUCATION_LEVEL-DELETED")
            .eventDatetime(xtag.getNomisTimestamp())
            .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
            .nomisEventType(xtag.getEventType())
            .build();
    }

    private OffenderEvent contactPersonInsertedEventOf(final Xtag xtag) {
        return OffenderEvent.builder()
            .eventType("CONTACT_PERSON-INSERTED")
            .eventDatetime(xtag.getNomisTimestamp())
            .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
            .personId(longOf(xtag.getContent().getP_person_id()))
            .nomisEventType(xtag.getEventType())
            .build();
    }

    private OffenderEvent contactPersonUpdatedEventOf(final Xtag xtag) {
        return OffenderEvent.builder()
            .eventType("CONTACT_PERSON-UPDATED")
            .eventDatetime(xtag.getNomisTimestamp())
            .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
            .personId(longOf(xtag.getContent().getP_person_id()))
            .nomisEventType(xtag.getEventType())
            .build();
    }

    private OffenderEvent contactPersonDeletedEventOf(final Xtag xtag) {
        return OffenderEvent.builder()
            .eventType("CONTACT_PERSON-DELETED")
            .eventDatetime(xtag.getNomisTimestamp())
            .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
            .personId(longOf(xtag.getContent().getP_person_id()))
            .nomisEventType(xtag.getEventType())
            .build();
    }

    private OffenderEvent offenderUpdatedEventOf(final Xtag xtag) {
        return OffenderEvent.builder()
            .eventType("OFFENDER-UPDATED")
            .eventDatetime(xtag.getNomisTimestamp())
            .offenderId(longOf(xtag.getContent().getP_offender_id()))
            .rootOffenderId(longOf(xtag.getContent().getP_root_offender_id()))
            .nomisEventType(xtag.getEventType())
            .build();
    }

    private OffenderEvent offenderAliasChangedEventOf(final Xtag xtag) {
        return OffenderEvent.builder()
            .eventType("OFFENDER_ALIAS-CHANGED")
            .eventDatetime(xtag.getNomisTimestamp())
            .offenderId(longOf(xtag.getContent().getP_offender_id()))
            .rootOffenderId(longOf(xtag.getContent().getP_root_offender_id()))
            .aliasOffenderId(longOf(xtag.getContent().getP_alias_offender_id()))
            .nomisEventType(xtag.getEventType())
            .build();
    }

    private OffenderEvent addressUsageInsertedEventOf(final Xtag xtag) {
        return OffenderEvent.builder()
            .eventType("ADDRESS_USAGE-INSERTED")
            .eventDatetime(xtag.getNomisTimestamp())
            .addressId(longOf(xtag.getContent().getP_address_id()))
            .addressUsage(xtag.getContent().getP_address_usage())
            .nomisEventType(xtag.getEventType())
            .build();
    }

    private OffenderEvent addressUsageUpdatedEventOf(final Xtag xtag) {
        return OffenderEvent.builder()
            .eventType("ADDRESS_USAGE-UPDATED")
            .eventDatetime(xtag.getNomisTimestamp())
            .addressId(longOf(xtag.getContent().getP_address_id()))
            .addressUsage(xtag.getContent().getP_address_usage())
            .nomisEventType(xtag.getEventType())
            .build();
    }

    private OffenderEvent addressUsageDeletedEventOf(final Xtag xtag) {
        return OffenderEvent.builder()
            .eventType("ADDRESS_USAGE-DELETED")
            .eventDatetime(xtag.getNomisTimestamp())
            .addressId(longOf(xtag.getContent().getP_address_id()))
            .addressUsage(xtag.getContent().getP_address_usage())
            .nomisEventType(xtag.getEventType())
            .build();
    }

    private OffenderEvent offenderDetailsChangedEventOf(final Xtag xtag) {
        return OffenderEvent.builder()
            .eventType("OFFENDER_DETAILS-CHANGED")
            .eventDatetime(xtag.getNomisTimestamp())
            .offenderId(longOf(xtag.getContent().getP_offender_id()))
            .rootOffenderId(longOf(xtag.getContent().getP_root_offender_id()))
            .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
            .nomisEventType(xtag.getEventType())
            .build();
    }

    private OffenderEvent offenderBookingInsertedEventOf(final Xtag xtag) {
        return OffenderEvent.builder()
            .eventType("OFFENDER_BOOKING-INSERTED")
            .eventDatetime(xtag.getNomisTimestamp())
            .offenderId(longOf(xtag.getContent().getP_offender_id()))
            .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
            .identifierType(xtag.getContent().getP_identifier_type())
            .nomisEventType(xtag.getEventType())
            .build();
    }

    private OffenderEvent offenderBookingChangedEventOf(final Xtag xtag) {
        return OffenderEvent.builder()
            .eventType("OFFENDER_BOOKING-CHANGED")
            .eventDatetime(xtag.getNomisTimestamp())
            .offenderId(longOf(xtag.getContent().getP_offender_id()))
            .rootOffenderId(longOf(xtag.getContent().getP_root_offender_id()))
            .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
            .nomisEventType(xtag.getEventType())
            .build();
    }

    private OffenderEvent offenderBookingReassignedEventOf(final Xtag xtag) {
        return OffenderEvent.builder()
            .eventType("OFFENDER_BOOKING-REASSIGNED")
            .eventDatetime(xtag.getNomisTimestamp())
            .offenderId(longOf(xtag.getContent().getP_offender_id()))
            .previousOffenderId(longOf(xtag.getContent().getP_old_offender_id()))
            .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
            .nomisEventType(xtag.getEventType())
            .build();
    }

    public OffenderEvent externalMovementRecordEventOf(final Xtag xtag) {
        return OffenderEvent.builder()
            .eventType(externalMovementEventOf(xtag))
            .eventDatetime(xtag.getNomisTimestamp())
            .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
            .movementSeq(longOf(xtag.getContent().getP_movement_seq()))
            .movementDateTime(localDateTimeOf(xtag.getContent().getP_movement_date(), xtag.getContent().getP_movement_time()))
            .movementType(xtag.getContent().getP_movement_type())
            .movementReasonCode(xtag.getContent().getP_movement_reason_code())
            .directionCode(xtag.getContent().getP_direction_code())
            .escortCode(xtag.getContent().getP_escort_code())
            .fromAgencyLocationId(xtag.getContent().getP_from_agy_loc_id())
            .toAgencyLocationId(xtag.getContent().getP_to_agy_loc_id())
            .nomisEventType(xtag.getEventType())
            .build();
    }

    private OffenderEvent offenderMovementDischargeEventOf(final Xtag xtag) {
        return OffenderEvent.builder()
            .eventType("OFFENDER_MOVEMENT-DISCHARGE")
            .eventDatetime(xtag.getNomisTimestamp())
            .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
            .movementSeq(longOf(xtag.getContent().getP_movement_seq()))
            .nomisEventType(xtag.getEventType())
            .build();
    }

    private OffenderEvent offenderMovementReceptionEventOf(final Xtag xtag) {
        return OffenderEvent.builder()
            .eventType("OFFENDER_MOVEMENT-RECEPTION")
            .eventDatetime(xtag.getNomisTimestamp())
            .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
            .movementSeq(longOf(xtag.getContent().getP_movement_seq()))
            .nomisEventType(xtag.getEventType())
            .build();
    }

    private OffenderEvent maternityStatusInsertedEventOf(final Xtag xtag) {
        return OffenderEvent.builder()
            .eventType("MATERNITY_STATUS-INSERTED")
            .eventDatetime(xtag.getNomisTimestamp())
            .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
            .nomisEventType(xtag.getEventType())
            .build();
    }

    private OffenderEvent maternityStatusUpdatedEventOf(final Xtag xtag) {
        return OffenderEvent.builder()
            .eventType("MATERNITY_STATUS-UPDATED")
            .eventDatetime(xtag.getNomisTimestamp())
            .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
            .nomisEventType(xtag.getEventType())
            .build();
    }

    private OffenderEvent riskScoreEventOf(final Xtag xtag) {
        return OffenderEvent.builder()
            .eventType("RISK_SCORE-" + (xtag.getContent().getP_delete_flag().equals("N") ? "CHANGED" : "DELETED"))
            .eventDatetime(xtag.getNomisTimestamp())
            .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
            .riskPredictorId(longOf(xtag.getContent().getP_offender_risk_predictor_id()))
            .nomisEventType(xtag.getEventType())
            .build();
    }

    private OffenderEvent offenderSanctionEventOf(final Xtag xtag) {
        return OffenderEvent.builder()
            .eventType("OFFENDER_SANCTION-CHANGED")
            .eventDatetime(xtag.getNomisTimestamp())
            .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
            .sanctionSeq(longOf(xtag.getContent().getP_sanction_seq()))
            .nomisEventType(xtag.getEventType())
            .build();
    }

    private OffenderEvent bookingNumberEventOf(final Xtag xtag) {
        return OffenderEvent.builder()
            .eventType("BOOKING_NUMBER-CHANGED")
            .eventDatetime(xtag.getNomisTimestamp())
            .offenderId(longOf(xtag.getContent().getP_offender_id()))
            .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
            .bookingNumber(xtag.getContent().getP_new_prison_num())
            .previousBookingNumber(Optional.ofNullable(xtag.getContent().getP_old_prison_num())
                .orElse(Optional.ofNullable(xtag.getContent().getP_old_prision_num())
                    .orElse(xtag.getContent().getP_old_prison_number())))
            .nomisEventType(xtag.getEventType())
            .build();
    }

    private OffenderEvent confirmedReleaseDateOf(final Xtag xtag) {
        return OffenderEvent.builder()
            .eventType(xtag.getEventType())
            .eventDatetime(xtag.getNomisTimestamp())
            .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
            .build();
    }

    private OffenderEvent offenderUpdatedOf(final Xtag xtag) {
        return OffenderEvent.builder()
            .eventType(xtag.getEventType())
            .eventDatetime(xtag.getNomisTimestamp())
            .offenderId(longOf(xtag.getContent().getP_offender_id()))
            .offenderIdDisplay(xtag.getContent().getP_offender_id_display())
            .build();
    }

    public String caseNoteEventTypeOf(final uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderEvent event) {
        if (event.getEventType().equalsIgnoreCase("CASE_NOTE")) {
            final var eventData = event.getEventData();
            final var typePattern = Pattern.compile("(?<=\\btype.{0,4}\\bcode.{0,4})(\\w+)");
            final var typeMatcher = typePattern.matcher(eventData);

            final var subtypePattern = Pattern.compile("(?<=\\bsub_type.{0,4}\\bcode.{0,4})(\\w+)");
            final var subtypeMatcher = subtypePattern.matcher(eventData);

            if (typeMatcher.find() && subtypeMatcher.find()) {
                return String.format("%s-%s", typeMatcher.group(), subtypeMatcher.group());
            }
        }

        return event.getEventType();
    }

    public Long caseNoteIdOf(final uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderEvent event) {
        if (event.getEventType().equalsIgnoreCase("CASE_NOTE")) {
            final var eventData = event.getEventData();
            final var typePattern = Pattern.compile("(?<=\\bcase_note.{0,4}\\bid.{0,4})(\\w+)");
            final var typeMatcher = typePattern.matcher(eventData);

            return typeMatcher.find() ? longOf(typeMatcher.group()) : null;
        }
        return null;
    }

    private Long longOf(final String num) {
        return Optional.ofNullable(num).map(Long::valueOf).orElse(null);
    }

    private Integer integerOf(final String num) {
        return Optional.ofNullable(num).map(Integer::valueOf).orElse(null);
    }

    public static LocalDate localDateOf(final String date) {
        final var pattern = "[yyyy-MM-dd HH:mm:ss][yyyy-MM-dd][dd-MMM-yyyy][dd-MMM-yy]";
        try {
            return Optional.ofNullable(date)
                .map(d -> LocalDate.parse(d, new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern(pattern).toFormatter()))
                .orElse(null);
        } catch (final DateTimeParseException dtpe) {
            log.error("Unable to parse {} into a LocalDate using pattern {}", date, pattern);
        }
        return null;
    }

    public static LocalTime localTimeOf(final String dateTime) {
        final var pattern = "[yyyy-MM-dd ]HH:mm:ss";
        try {
            return Optional.ofNullable(dateTime)
                .map(d -> LocalTime.parse(d, DateTimeFormatter.ofPattern(pattern)))
                .orElse(null);
        } catch (final DateTimeParseException dtpe) {
            log.error("Unable to parse {} into a LocalTime using pattern {}", dateTime, pattern);
        }
        return null;
    }

    public static LocalDateTime localDateTimeOf(final String date, final String time) {

        final var maybeLocalDate = Optional.ofNullable(localDateOf(date));
        final var maybeLocalTime = Optional.ofNullable((localTimeOf(time)));

        return maybeLocalDate
            .map(ld -> maybeLocalTime.map(lt -> lt.atDate(ld)).orElse(ld.atStartOfDay()))
            .orElse(null);
    }
}
