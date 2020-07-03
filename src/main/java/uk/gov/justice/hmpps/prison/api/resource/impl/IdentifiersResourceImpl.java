package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.OffenderIdentifier;
import uk.gov.justice.hmpps.prison.api.resource.IdentifiersResource;
import uk.gov.justice.hmpps.prison.service.InmateService;

import java.util.List;

@RestController
@RequestMapping("${api.base.path}/identifiers")
public class IdentifiersResourceImpl implements IdentifiersResource {
    private final InmateService inmateService;

    public IdentifiersResourceImpl(final InmateService inmateService) {
        this.inmateService = inmateService;
    }

    @Override
    public List<OffenderIdentifier> getOffenderIdentifiersByTypeAndValue(final String identifierType, final String identifierValue) {
        return inmateService.getOffenderIdentifiersByTypeAndValue(identifierType, identifierValue);
    }
}
