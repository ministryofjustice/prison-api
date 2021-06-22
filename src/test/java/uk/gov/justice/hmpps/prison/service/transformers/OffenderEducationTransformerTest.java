package uk.gov.justice.hmpps.prison.service.transformers;

import org.junit.Test;
import uk.gov.justice.hmpps.prison.repository.jpa.model.EducationLevel;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderEducation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderEducation.PK;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderEducationAddress;
import uk.gov.justice.hmpps.prison.repository.jpa.model.StudyArea;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class OffenderEducationTransformerTest {

    private final OffenderEducationTransformer transformer = new OffenderEducationTransformer();

    @Test
    public void convert() {
        final var address = OffenderEducationAddress.builder()
            .addressId(1L)
            .build();

        final var offenderEducation = OffenderEducation
            .builder()
            .id(new PK(1L, 2L))
            .startDate(LocalDate.now().minusDays(5))
            .endDate(LocalDate.now())
            .studyArea(new StudyArea("GEN", "General Studies"))
            .educationLevel(new EducationLevel("DEG", "Degree Level or Higher"))
            .numberOfYears(1)
            .graduationYear("2018")
            .comment("Good student")
            .school("School of economics")
            .isSpecialEducation(false)
            .addresses(List.of(address))
            .build();

        final var actual = transformer.convert(offenderEducation);

        assertThat(actual.getBookingId()).isEqualTo(offenderEducation.getId().getBookingId());
        assertThat(actual.getStartDate()).isEqualTo(offenderEducation.getStartDate());
        assertThat(actual.getEndDate()).isEqualTo(offenderEducation.getEndDate());
        assertThat(actual.getStudyArea()).isEqualTo(offenderEducation.getStudyArea().getDescription());
        assertThat(actual.getEducationLevel()).isEqualTo(offenderEducation.getEducationLevel().getDescription());
        assertThat(actual.getNumberOfYears()).isEqualTo(offenderEducation.getNumberOfYears());
        assertThat(actual.getGraduationYear()).isEqualTo(offenderEducation.getGraduationYear());
        assertThat(actual.getComment()).isEqualTo(offenderEducation.getComment());
        assertThat(actual.getSchool()).isEqualTo(offenderEducation.getSchool());
        assertThat(actual.getIsSpecialEducation()).isEqualTo(offenderEducation.getIsSpecialEducation());

        assertThat(actual.getAddresses()).hasSize(1);
        assertThat(actual.getAddresses().get(0).getAddressId()).isEqualTo(address.getAddressId());
    }

    @Test
    public void convertWithMissingNestedFields() {

        final var offenderEducation = OffenderEducation
            .builder()
            .id(new PK(1L, 2L))
            .startDate(LocalDate.now().minusDays(5))
            .endDate(LocalDate.now())
            .numberOfYears(1)
            .graduationYear("2018")
            .comment("Good student")
            .school("School of economics")
            .isSpecialEducation(false)
            .build();

        final var actual = transformer.convert(offenderEducation);

        assertThat(actual.getBookingId()).isEqualTo(offenderEducation.getId().getBookingId());
        assertThat(actual.getStartDate()).isEqualTo(offenderEducation.getStartDate());
        assertThat(actual.getEndDate()).isEqualTo(offenderEducation.getEndDate());
        assertThat(actual.getNumberOfYears()).isEqualTo(offenderEducation.getNumberOfYears());
        assertThat(actual.getGraduationYear()).isEqualTo(offenderEducation.getGraduationYear());
        assertThat(actual.getComment()).isEqualTo(offenderEducation.getComment());
        assertThat(actual.getSchool()).isEqualTo(offenderEducation.getSchool());
        assertThat(actual.getIsSpecialEducation()).isEqualTo(offenderEducation.getIsSpecialEducation());

        assertThat(actual.getAddresses()).hasSize(0);
    }
}