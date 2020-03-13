package net.syscon.elite.service.support;

import net.syscon.elite.api.model.Location;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Utility class containing methods for processing of {@link net.syscon.elite.api.model.Location} objects.
 */
public class LocationProcessor {
    /**
     * List of the abbreviations that should remain in all capitals after location description processing
     */
    public static final List<String> ABBREVIATIONS = List.of("HMP", "YOI", "VCC", "CSC", "CSU", "CASU", "MCASU", "MDT", "VDT", "OMU", "ITQ", "SPU", "CES", "UK", "ROTL", "SOTP", "IMB", "RAPT", "PICTA", "HCC", "AIC", "BICS", "IPSO", "IAG", "IPD", "PACT", "PIPE", "DART", "VP");

    /**
     * Strips agency id from description if agency id is used as prefix for description. If either description or agency
     * id are {@code null}, agency id is not stripped and unaltered description is returned.
     *
     * @param description the location description.
     * @param agencyId    the location agency id.
     * @return description with agency id removed (or unaltered description if description is not prefixed with agency
     * id or agency id is {@code null}.
     */
    public static String stripAgencyId(final String description, final String agencyId) {
        if (StringUtils.isBlank(agencyId)) {
            return description;
        }

        return RegExUtils.replaceFirst(description, StringUtils.trimToEmpty(agencyId) + "-", "");
    }

    /**
     * Processes provided locations, returning list of new <i>processed</i> location objects. The following processing
     * takes place:
     * <ul>
     *     <li>sets location prefix to location description if location prefix not already set</li>
     *     <li>strips location agency id from location description if location agency id is used as prefix for description</li>
     * </ul>
     *
     * @param locations location to process.
     * @return new location representing processed input location.
     * @throws {@code NullPointerException} if no location provided for processing.
     */
    public static List<Location> processLocations(final List<Location> locations) {
        return processLocations(locations, false);
    }

    /**
     * Processes provided locations, returning list of new <i>processed</i> location objects. The following processing
     * takes place:
     * <ul>
     *     <li>sets location prefix to location description if location prefix not already set</li>
     *     <li>strips location agency id from location description if location agency id is used as prefix for description</li>
     * </ul>
     *
     * @param locations             locations to process.
     * @param preferUserDescription if {@code true}, the location's user description will be used as the location's
     *                              description if it is set. Stripping of agency id from location description will not
     *                              occur if this is {@code true} and user description has been set.
     * @return new location representing processed input location.
     * @throws {@code NullPointerException} if no location provided for processing.
     */
    public static List<Location> processLocations(final List<Location> locations, final boolean preferUserDescription) {
        System.out.println(locations);
        Objects.requireNonNull(locations);

        return locations.stream().map(loc -> processLocation(loc, preferUserDescription)).collect(Collectors.toList());
    }

    /**
     * Processes provided location, returning new <i>processed</i> location object. The following processing takes place:
     * <ul>
     *     <li>sets location prefix to location description if location prefix not already set</li>
     *     <li>strips location agency id from location description if location agency id is used as prefix for description</li>
     * </ul>
     *
     * @param location location to process.
     * @return new location representing processed input location.
     * @throws {@code NullPointerException} if no location provided for processing.
     */
    public static Location processLocation(final Location location) {
        return processLocation(location, false);
    }

    /**
     * Processes provided location, returning new <i>processed</i> location object. The following processing takes place:
     * <ul>
     *     <li>sets location prefix to location description if location prefix not already set</li>
     *     <li>strips location agency id from location description if location agency id is used as prefix for description</li>
     * </ul>
     *
     * @param location              location to process.
     * @param preferUserDescription if {@code true}, the location's user description will be used as the location's
     *                              description if it is set. Stripping of agency id from location description will not
     *                              occur if this is {@code true} and user description has been set.
     * @return new location representing processed input location.
     * @throws {@code NullPointerException} if no location provided for processing.
     */
    public static Location processLocation(final Location location, final boolean preferUserDescription) {
        Objects.requireNonNull(location);

        final var newLocationPrefix = StringUtils.defaultIfBlank(location.getLocationPrefix(), location.getDescription());

        final String newDescription = newDescription(location, preferUserDescription);

        return Location.builder()
                .agencyId(location.getAgencyId())
                .currentOccupancy(location.getCurrentOccupancy())
                .description(newDescription)
                .locationId(location.getLocationId())
                .locationPrefix(newLocationPrefix)
                .locationType(location.getLocationType())
                .operationalCapacity(location.getOperationalCapacity())
                .parentLocationId(location.getParentLocationId())
                .userDescription(formatLocation(location.getUserDescription()))
                .internalLocationCode(location.getInternalLocationCode())
                .build();
    }

    private static String newDescription(Location location, boolean preferUserDescription) {
        if (preferUserDescription && StringUtils.isNotBlank(location.getUserDescription())) {
            return location.getUserDescription();
        } else {
            return stripAgencyId(location.getDescription(), location.getAgencyId());
        }
    }

    /**
     *
     * @param locationDescription string to convert
     * @return new location with correct titlecase
     *
     */
    public static String formatLocation(final String locationDescription) {
        // Handle the possibility of the userDescription being empty
        if (locationDescription == null) {
            return null;
        }
        var description = WordUtils.capitalizeFully(locationDescription);
        // Using word boundaries to find the right string ensures we catch the strings
        // wherever they appear in the description, while also avoiding replacing
        // the letter sequence should it appear in the middle of a word
        // e.g. this will not match 'mosaic' even though AIC is one of the abbreviations
        Pattern pattern = Pattern.compile("\\b(" + String.join("|", ABBREVIATIONS) + ")\\b", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(description);

        // There could be more than one abbreviation in a string,
        // e.g. HMP Moorland VCC Room 1
        // By using the string buffer and the appendReplacement method
        // we ensure that all the matching groups are replaced accordingly
        StringBuffer stringBuffer = new StringBuffer();
        while (matcher.find()) {
            var matched = matcher.group(1);
            matcher.appendReplacement(stringBuffer, matched.toUpperCase());
        }
        matcher.appendTail(stringBuffer);
        return stringBuffer.toString();
    }
}
