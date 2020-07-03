package uk.gov.justice.hmpps.prison.service.transformers;

import uk.gov.justice.hmpps.prison.api.model.PropertyContainer;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderPropertyContainer;
import uk.gov.justice.hmpps.prison.repository.jpa.transform.LocationTransformer;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Simple transform object to help reduce boiler plate mapping of entity {@link OffenderPropertyContainer} to model object {@link PropertyContainer}.
 */
public class PropertyContainerTransformer {

    public static PropertyContainer transform(final OffenderPropertyContainer propertyContainer) {
        final var containerType = propertyContainer.getContainerType() != null ? propertyContainer.getContainerType().getDescription() : null;
        return PropertyContainer.builder()
                .location(LocationTransformer.fromAgencyInternalLocation(propertyContainer.getInternalLocation()))
                .sealMark(propertyContainer.getSealMark())
                .containerType(containerType)
                .build();
    }

    public static List<PropertyContainer> transform(final Collection<OffenderPropertyContainer> propertyContainers) {
        return propertyContainers.stream().map(PropertyContainerTransformer::transform).collect(Collectors.toUnmodifiableList());
    }
}
