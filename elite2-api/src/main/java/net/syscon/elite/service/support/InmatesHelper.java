package net.syscon.elite.service.support;

import net.syscon.elite.api.model.OffenderBooking;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InmatesHelper {

    public static void setCategory(List<OffenderBooking> bookings, List<AssessmentDto> assessmentsForBookings) {
        final Map<Long, List<AssessmentDto>> mapOfBookings = createMapOfBookings(assessmentsForBookings);
        bookings.forEach(booking -> {
            final List<AssessmentDto> dtos = mapOfBookings.get(booking.getBookingId());
            if (!CollectionUtils.isEmpty(dtos)) {
                booking.setCategoryCode(deriveClassificationCode(dtos.get(0)));
            }
        });
    }

    public static Map<Long, List<AssessmentDto>> createMapOfBookings(List<AssessmentDto> assessmentsForBookings) {
        // Note this grouping works on the assumption that the DB order of the assessments is preserved
        return assessmentsForBookings.stream()
                .collect(Collectors.groupingBy(AssessmentDto::getBookingId));
    }

    public static String deriveClassification(AssessmentDto assessmentDto) {
        if (!"PEND".equalsIgnoreCase(deriveClassificationCode(assessmentDto))) {
            return StringUtils.defaultIfBlank(assessmentDto.getReviewSupLevelTypeDesc(), StringUtils.defaultIfBlank(assessmentDto.getOverridedSupLevelTypeDesc(), assessmentDto.getCalcSupLevelTypeDesc()));
        }
        return null;
    }

    public static String deriveClassificationCode(AssessmentDto assessmentDto) {
        return StringUtils.defaultIfBlank(assessmentDto.getReviewSupLevelType(), StringUtils.defaultIfBlank(assessmentDto.getOverridedSupLevelType(), assessmentDto.getCalcSupLevelType()));
    }
}
