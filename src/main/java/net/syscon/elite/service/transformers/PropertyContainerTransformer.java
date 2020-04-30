package net.syscon.elite.service.transformers;

import net.syscon.elite.api.model.CourtCase;
import net.syscon.elite.api.model.CourtHearing;
import net.syscon.elite.api.model.Location;
import net.syscon.elite.api.model.PropertyContainer;
import net.syscon.elite.repository.jpa.model.CaseStatus;
import net.syscon.elite.repository.jpa.model.LegalCaseType;
import net.syscon.elite.repository.jpa.model.OffenderCourtCase;
import net.syscon.elite.repository.jpa.model.OffenderPropertyContainer;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Simple transform object to help reduce boiler plate mapping of entity {@link OffenderCourtCase} to model object {@link CourtCase}.
 */
public class PropertyContainerTransformer {

    public static PropertyContainer transform(final OffenderPropertyContainer propertyContainer) {
        return PropertyContainer.builder()
                .location(Location.builder()
                        .locationId(propertyContainer.getInternalLocation().getLocationId())
                        .description(propertyContainer.getInternalLocation().getDescription())
                        .build())
                .sealMark(propertyContainer.getSealMark())
                .build();
    }

    public static List<PropertyContainer> transform(final Collection<OffenderPropertyContainer> propertyContainers) {
        return propertyContainers.stream().map(PropertyContainerTransformer::transform).collect(Collectors.toUnmodifiableList());
    }
}
