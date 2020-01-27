package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.OffenderIdentifier;
import net.syscon.elite.api.resource.IdentifiersResource;
import net.syscon.elite.service.InmateService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
