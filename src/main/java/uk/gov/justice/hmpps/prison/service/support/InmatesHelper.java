package uk.gov.justice.hmpps.prison.service.support;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import uk.gov.justice.hmpps.prison.api.model.CategoryCodeAware;
import uk.gov.justice.hmpps.prison.api.model.OffenderSentenceDetail;
import uk.gov.justice.hmpps.prison.api.model.ReleaseDateAware;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InmatesHelper {

    public static void setReleaseDate(final List<? extends ReleaseDateAware> bookings, final List<OffenderSentenceDetail> sentenceDetails) {
        final var mapOfBookings = createMapOfSentences(sentenceDetails);
        bookings.forEach(booking -> {
            final var sentenceDet = mapOfBookings.get(booking.getBookingId());
            if (sentenceDet != null) {
                booking.setReleaseDate(sentenceDet.getSentenceDetail().getReleaseDate());
            }
        });
    }

    public static void setCategory(final List<? extends CategoryCodeAware> bookings, final List<AssessmentDto> assessmentsForBookings) {
        final var mapOfBookings = createMapOfBookings(assessmentsForBookings);
        bookings.forEach(booking -> {
            final var dtos = mapOfBookings.get(booking.getBookingId());
            if (!CollectionUtils.isEmpty(dtos)) {
                booking.setCategoryCode(deriveClassificationCode(dtos.get(0)));
            }
        });
    }

    private static Map<Long, OffenderSentenceDetail> createMapOfSentences(final List<OffenderSentenceDetail> sentenceDetails) {
        final var sentenceMap = new HashMap<Long, OffenderSentenceDetail>();
        sentenceDetails.forEach(s -> sentenceMap.put(s.getBookingId(), s));
        return sentenceMap;
    }

    public static Map<Long, List<AssessmentDto>> createMapOfBookings(final List<AssessmentDto> assessmentsForBookings) {
        // Note this grouping works on the assumption that the DB order of the assessments is preserved
        return assessmentsForBookings.stream()
                .collect(Collectors.groupingBy(AssessmentDto::getBookingId));
    }

    public static String deriveClassification(final AssessmentDto assessmentDto) {
        if (!"PEND".equalsIgnoreCase(deriveClassificationCode(assessmentDto))) {
            return StringUtils.defaultIfBlank(assessmentDto.getReviewSupLevelTypeDesc(), StringUtils.defaultIfBlank(assessmentDto.getOverridedSupLevelTypeDesc(), assessmentDto.getCalcSupLevelTypeDesc()));
        }
        return null;
    }

    public static String deriveClassificationCode(final AssessmentDto assessmentDto) {
        return StringUtils.defaultIfBlank(assessmentDto.getReviewSupLevelType(), StringUtils.defaultIfBlank(assessmentDto.getOverridedSupLevelType(), assessmentDto.getCalcSupLevelType()));
    }
}
