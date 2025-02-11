package uk.gov.justice.hmpps.prison.repository;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.api.model.IdentifyingMarkDto;
import uk.gov.justice.hmpps.prison.api.model.IdentifyingMarkImageDto;
import uk.gov.justice.hmpps.prison.repository.mapping.FieldMapper;
import uk.gov.justice.hmpps.prison.repository.mapping.Row2BeanRowMapper;
import uk.gov.justice.hmpps.prison.repository.sql.IdentifyingMarksRepositorySql;
import java.util.List;
import java.util.Map;

@Repository
@Slf4j
public class IdentifyingMarksRepository extends RepositoryBase {

    private final Map<String, FieldMapper> identifyingMarkMapping = new ImmutableMap.Builder<String, FieldMapper>()
        .put("OFFENDER_ID_DISPLAY", new FieldMapper("prisonerNumber"))
        .put("ID_MARK_SEQ", new FieldMapper("markId"))
        .put("OFFENDER_BOOK_ID", new FieldMapper("bookingId"))
        .put("BODY_PART_CODE", new FieldMapper("bodyPart"))
        .put("MARK_TYPE", new FieldMapper("markType"))
        .put("SIDE_CODE", new FieldMapper("side"))
        .put("PART_ORIENTATION_CODE", new FieldMapper("partOrientation"))
        .put("COMMENT_TEXT", new FieldMapper("comment"))
        .put("CREATE_DATETIME", new FieldMapper("createDateTime"))
        .put("CREATE_USER_ID", new FieldMapper("createdBy"))
        .build();

    private final Map<String, FieldMapper> markImageMapping = new ImmutableMap.Builder<String, FieldMapper>()
        .put("OFFENDER_IMAGE_ID", new FieldMapper("id"))
        .build();

    public List<IdentifyingMarkDto> findIdentifyingMarksForLatestBooking(final String offenderId) {
        final var sql = IdentifyingMarksRepositorySql.FIND_IDENTIFYING_MARKS_FOR_LATEST_BOOKING.getSql();

        final var identifyingMarkRowMapper =
            Row2BeanRowMapper.makeMapping(IdentifyingMarkDto.class, identifyingMarkMapping);

        return jdbcTemplate.query(
            sql,
            createParams("offenderId", offenderId),
            identifyingMarkRowMapper
        );
    }

    public List<IdentifyingMarkImageDto> findImageIdsForIdentifyingMark(final long bookingId, final int markSequenceId) {
        final var sql = IdentifyingMarksRepositorySql.FIND_IMAGE_IDS_FOR_IDENTIFYING_MARK.getSql();

        final var markImageMapper =
            Row2BeanRowMapper.makeMapping(IdentifyingMarkImageDto.class, markImageMapping);

        return jdbcTemplate.query(
            sql,
            createParams("bookingId", bookingId, "markSeqId", markSequenceId),
            markImageMapper
        );
    }
}
