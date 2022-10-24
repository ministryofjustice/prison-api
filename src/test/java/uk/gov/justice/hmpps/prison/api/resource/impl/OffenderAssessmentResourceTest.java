package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.justice.hmpps.prison.api.resource.OffenderAssessmentResource;
import uk.gov.justice.hmpps.prison.api.support.CategoryInformationType;
import uk.gov.justice.hmpps.prison.service.InmateService;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class OffenderAssessmentResourceTest {
    @Mock
    private InmateService inmateService;
    private OffenderAssessmentResource offenderAssessmentResource;

    @BeforeEach
    public void setUp() {
        offenderAssessmentResource = new OffenderAssessmentResource(inmateService, null);
    }

    @Test
    public void getOffenderCategorisationsInvalidType() {
        assertThatThrownBy(() -> offenderAssessmentResource.getOffenderCategorisations("LEI", "INVALID_CAT_TYPE", null))
                .isInstanceOf(HttpClientErrorException.class);
    }

    @Test()
    public void getOffenderCategorisations() {
        offenderAssessmentResource.getOffenderCategorisations("LEI", CategoryInformationType.CATEGORISED.name(), null);
        verify(inmateService).getCategory("LEI", CategoryInformationType.CATEGORISED, null);
    }
}
