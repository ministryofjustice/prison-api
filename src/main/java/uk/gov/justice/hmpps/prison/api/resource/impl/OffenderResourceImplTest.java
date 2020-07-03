package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.justice.hmpps.prison.api.resource.OffenderAssessmentResource;
import uk.gov.justice.hmpps.prison.api.support.CategoryInformationType;
import uk.gov.justice.hmpps.prison.service.InmateService;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class OffenderResourceImplTest {
    @Mock
    private InmateService inmateService;
    private OffenderAssessmentResource offenderAssessmentResource;

    @Before
    public void setUp() {
        offenderAssessmentResource = new OffenderAssessmentResourceImpl(inmateService);
    }

    @Test(expected = HttpClientErrorException.class)
    public void getOffenderCategorisationsInvalidType() {
        offenderAssessmentResource.getOffenderCategorisations("LEI", "INVALID_CAT_TYPE", null);
    }

    @Test()
    public void getOffenderCategorisations() {
        offenderAssessmentResource.getOffenderCategorisations("LEI", CategoryInformationType.CATEGORISED.name(), null);
        verify(inmateService).getCategory("LEI", CategoryInformationType.CATEGORISED, null);
    }
}
