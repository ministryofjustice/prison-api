package uk.gov.justice.hmpps.prison.service;

import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Country;
import uk.gov.justice.hmpps.prison.repository.jpa.model.EducationLevel;
import uk.gov.justice.hmpps.prison.repository.jpa.model.EducationSchedule;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderEducation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderEducation.PK;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderEducationAddress;
import uk.gov.justice.hmpps.prison.repository.jpa.model.StudyArea;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderEducationRepository;
import uk.gov.justice.hmpps.prison.service.transformers.OffenderEducationTransformer;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OffenderEducationServiceTest {

    private final int BATCH_SIZE = 1;

    private final String nomisId = "abc";

    private final PageRequest pageRequest = PageRequest.of(0, 10);

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

    @Mock
    private OffenderEducationTransformer transformer;

    @Mock
    private OffenderEducationRepository repository;


    private OffenderEducationService service;


    @BeforeEach
    void setup() {
        service = new OffenderEducationService(repository, transformer, BATCH_SIZE);
    }

    @Test
    public void getOffenderEducations() {
        final var educations = List.of(education1, education2);

        when(repository.findAllByNomisId(nomisId, pageRequest)).thenReturn(new PageImpl<>(educations));

        service.getOffenderEducations(nomisId, pageRequest);

        verify(transformer, times(1)).convert(education1);
        verify(transformer, times(1)).convert(education2);
    }

    @Test
    public void getOffenderEducationsInBulk_inBatchesOfOne() {
        final var educations = List.of(education1, education2);

        when(repository.findAllByNomisIdIn(List.of(nomisId))).thenReturn(educations);
        when(repository.findAllByNomisIdIn(List.of("ABC123"))).thenReturn(Collections.emptyList());

        service.getOffenderEducations(List.of(nomisId, "ABC123"));

        verify(repository).findAllByNomisIdIn(List.of(nomisId));
        verify(repository).findAllByNomisIdIn(List.of("ABC123"));
        verify(transformer, times(1)).convert(education1);
        verify(transformer, times(1)).convert(education2);
    }
}
