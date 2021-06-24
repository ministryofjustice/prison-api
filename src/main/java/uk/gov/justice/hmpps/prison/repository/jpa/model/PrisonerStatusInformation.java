package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.Getter;
import org.hibernate.annotations.Subselect;
import org.springframework.data.annotation.Immutable;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDate;

@Entity
@Subselect(
                "SELECT O.OFFENDER_ID_DISPLAY                                                                     NOMS_ID, " +
                "       OB.AGY_LOC_ID                                                                             ESTABLISHMENT_CODE, " +
                "       AL.DESCRIPTION                                                                            ESTABLISHMENT_NAME, " +
                "       OB.OFFENDER_BOOK_ID                                                                       BOOKING_ID, " +
                "       O.FIRST_NAME                                                                              GIVEN_NAME1, " +
                "       CONCAT(O.MIDDLE_NAME, " +
                "              CASE WHEN MIDDLE_NAME_2 IS NOT NULL THEN CONCAT(' ', O.MIDDLE_NAME_2) ELSE '' END) GIVEN_NAME2, " +
                "       O.LAST_NAME                                                                               LAST_NAME, " +
                "       OB.REQUEST_NAME AS                                                                        REQUESTED_NAME, " +
                "       O.BIRTH_DATE                                                                              DATE_OF_BIRTH, " +
                "       RCS.DESCRIPTION                                                                           GENDER, " +
                "       english_speaking.english_speaking_flag                                                    ENGLISH_SPEAKING_FLAG, " +
                "       AIL.DESCRIPTION                                                                           CELL_LOCATION, " +
                "       OB.BOOKING_BEGIN_DATE                                                                     BOOKING_BEGIN_DATE, " +
                "       dt_admission.admission_date                                                               ADMISSION_DATE, " +
                "       OB.ACTIVE_FLAG                                                                            ACTIVE_FLAG, " +
                "       OB.in_out_status                                                                          IN_OUT_STATUS, " +
                "       IST.BAND_CODE                                                                             BAND_CODE, " +
                "       OIS.IMPRISONMENT_STATUS                                                                   IMPRISONMENT_STATUS " +
                " FROM OFFENDER_BOOKINGS OB " +
                "          INNER JOIN OFFENDERS O ON OB.OFFENDER_ID = O.OFFENDER_ID AND OB.BOOKING_SEQ = 1 " +
                "          INNER JOIN AGENCY_LOCATIONS AL ON OB.AGY_LOC_ID = AL.AGY_LOC_ID " +
                "          LEFT JOIN AGENCY_INTERNAL_LOCATIONS AIL ON OB.LIVING_UNIT_ID = AIL.INTERNAL_LOCATION_ID " +
                "          LEFT JOIN OFFENDER_IMPRISON_STATUSES OIS ON OIS.OFFENDER_BOOK_ID = OB.OFFENDER_BOOK_ID AND OIS.LATEST_STATUS = 'Y' " +
                "          LEFT JOIN IMPRISONMENT_STATUSES IST ON IST.IMPRISONMENT_STATUS = OIS.IMPRISONMENT_STATUS " +
                "          LEFT JOIN REFERENCE_CODES RCS ON O.SEX_CODE = RCS.CODE AND RCS.DOMAIN = 'SEX' " +
                "          LEFT JOIN ( " +
                "     SELECT DISTINCT offender_book_id, 'Y' AS english_speaking_flag FROM offender_languages " +
                "     WHERE language_code = 'ENG' " +
                "       AND speak_skill IN ('Y', 'A', 'G', 'D')) english_speaking on english_speaking.OFFENDER_BOOK_ID = OB.OFFENDER_BOOK_ID " +
                "          LEFT JOIN ( " +
                "     SELECT m.offender_book_id, " +
                "            Max(m.movement_time) AS admission_date " +
                "     FROM offender_external_movements m, " +
                "          offender_bookings h " +
                "     WHERE h.booking_status = 'O' " +
                "       AND h.active_flag = 'Y' " +
                "       AND m.direction_code = 'IN' " +
                "       AND m.movement_type = 'ADM' " +
                "       AND m.to_agy_loc_id = h.agy_loc_id " +
                "       AND h.offender_book_id = m.offender_book_id " +
                "     GROUP BY m.offender_book_id) dt_admission on dt_admission.OFFENDER_BOOK_ID = ob.OFFENDER_BOOK_ID ")
@Immutable
@Getter
public class PrisonerStatusInformation {
    @Id
    private String nomsId;
    private String establishmentCode;
    private String establishmentName;
    private Long bookingId;
    private String givenName1;
    private String givenName2;
    private String lastName;
    private String requestedName;
    private LocalDate dateOfBirth;
    private String gender;
    private String englishSpeakingFlag;
    private String cellLocation;
    private LocalDate bookingBeginDate;
    private LocalDate admissionDate;
    private String activeFlag;
    private String inOutStatus;
    private String bandCode;
    private String imprisonmentStatus;
}
