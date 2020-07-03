package net.syscon.prison.api.resource.impl;

import net.syscon.prison.api.model.OffenderIdentifier;
import net.syscon.prison.api.resource.IdentifiersResource;
import net.syscon.prison.service.InmateService;
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
