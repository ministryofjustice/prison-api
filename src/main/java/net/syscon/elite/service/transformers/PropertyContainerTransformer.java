package net.syscon.elite.service.transformers;

import net.syscon.elite.api.model.PropertyContainer;
import net.syscon.elite.repository.jpa.model.OffenderPropertyContainer;
import net.syscon.elite.repository.jpa.transform.LocationTransformer;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Simple transform object to help reduce boiler plate mapping of entity {@link OffenderPropertyContainer} to model object {@link PropertyContainer}.
 */
public class PropertyContainerTransformer {

    public static PropertyContainer transform(final OffenderPropertyContainer propertyContainer) {
        return PropertyContainer.builder()
                .location(LocationTransformer.fromAgencyInternalLocation(propertyContainer.getInternalLocation()))
                .sealMark(propertyContainer.getSealMark())
                .build();
    }

    public static List<PropertyContainer> transform(final Collection<OffenderPropertyContainer> propertyContainers) {
        return propertyContainers.stream().map(PropertyContainerTransformer::transform).collect(Collectors.toUnmodifiableList());
    }
}
