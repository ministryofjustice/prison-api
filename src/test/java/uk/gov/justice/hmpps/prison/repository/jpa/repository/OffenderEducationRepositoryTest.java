package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Country;
import uk.gov.justice.hmpps.prison.repository.jpa.model.EducationLevel;
import uk.gov.justice.hmpps.prison.repository.jpa.model.EducationSchedule;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderEducation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderEducation.PK;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderEducationAddress;
import uk.gov.justice.hmpps.prison.repository.jpa.model.StudyArea;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
public class OffenderEducationRepositoryTest {

    private final String offenderNumber = "G8346GA";
    private final int pageSize = 1;

    @Autowired
    private OffenderEducationRepository repository;


    private final OffenderEducationAddress address = OffenderEducationAddress.builder()
        .addressId(2349706L)
        .noFixedAddressFlag("N")
        .primaryFlag("N")
        .country(new Country("ENG", "England"))
        .build();


    final OffenderEducation education1 = OffenderEducation
        .builder()
        .id(new PK(584215L, 1L))
        .startDate(LocalDate.of(2009, 12, 21))
        .studyArea(new StudyArea("GEN", "General Studies"))
        .educationLevel(new EducationLevel("DEGREE", "Degree Level or Higher"))
        .comment("Good student")
        .school("School of economics")
        .isSpecialEducation(true)
        .schedule(new EducationSchedule("PART", "Part Time"))
        .addresses(List.of(address))
        .build();

    private final OffenderEducation education2 = OffenderEducation
        .builder()
        .id(new PK(584215L, 2L))
        .startDate(LocalDate.of(2016, 2, 10))
        .comment("Needs more focus")
        .school("moj education")
        .schedule(new EducationSchedule("NK", "Not Known"))
        .isSpecialEducation(false)
        .build();


    @Test
    void testExpectedNumberOfEducationsAreReturned() {
        final var educations = repository.findAllByNomisId(offenderNumber, Pageable.unpaged());

        assertThat(educations).hasSize(2);

        assertThat(education1).isEqualTo(educations.getContent().get(0));
        assertThat(education2).isEqualTo(educations.getContent().get(1));
    }

    @Test
    public void pagedFindAllByNomisIdPage1() {
        final var educations = repository.findAllByNomisId(offenderNumber, PageRequest.of(0, pageSize));

        assertThat(educations.getTotalElements()).isEqualTo(2);
        assertThat(educations).hasSize(1);
        assertThat(education1).isEqualTo(educations.getContent().get(0));
    }


    @Test
    public void pagedFindAllByNomisIdPage2() {
        final var educations = repository.findAllByNomisId(offenderNumber, PageRequest.of(1, pageSize));

        assertThat(educations.getTotalElements()).isEqualTo(2);
        assertThat(educations).hasSize(1);
        assertThat(education2).isEqualTo(educations.getContent().get(0));
    }

    @Test
    void bulk_testExpectedNumberOfEducationsAreReturned() {
        final var educations = repository.findAllByNomisIdIn(List.of(offenderNumber));

        assertThat(educations).hasSize(2);

        assertThat(education1).isEqualTo(educations.get(0));
        assertThat(education2).isEqualTo(educations.get(1));
    }

}
