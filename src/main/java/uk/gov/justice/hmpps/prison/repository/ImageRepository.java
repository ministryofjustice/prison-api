package uk.gov.justice.hmpps.prison.repository;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.api.model.ImageDetail;
import uk.gov.justice.hmpps.prison.repository.mapping.StandardBeanPropertyRowMapper;
import uk.gov.justice.hmpps.prison.repository.sql.ImageRepositorySql;

import java.sql.Blob;
import java.sql.SQLException;
import java.util.Optional;

import static java.lang.String.format;

@Repository
public class ImageRepository extends RepositoryBase {

    public final static StandardBeanPropertyRowMapper<ImageDetail> IMAGE_DETAIL_MAPPER = new StandardBeanPropertyRowMapper<>(ImageDetail.class);

    public Optional<ImageDetail> findImageDetail(final Long imageId) {
        final var sql = ImageRepositorySql.FIND_IMAGE_DETAIL.getSql();
        ImageDetail imageDetail;
        try {
            imageDetail = jdbcTemplate.queryForObject(sql,
                    createParams("imageId", imageId),
                    IMAGE_DETAIL_MAPPER);
        } catch (final EmptyResultDataAccessException e) {
            imageDetail = null;
        }
        return Optional.ofNullable(imageDetail);
    }


    public byte[] getImageContent(final Long imageId, final boolean fullSizeImage) {
        byte[] content = null;
        try {

            final var sql = getImageContextWithSize(fullSizeImage, ImageRepositorySql.FIND_IMAGE_CONTENT);
            final var blob = jdbcTemplate.queryForObject(sql, createParams("imageId", imageId), Blob.class);
            if (blob != null) {
                final var length = (int) blob.length();
                content = blob.getBytes(1, length);
                blob.free();
            }
        } catch (final DataAccessException | SQLException ex) {
            content = null;
        }
        return content;
    }


    public byte[] getImageContent(final String offenderNo, final boolean fullSizeImage) {
        byte[] content = null;
        try {
            final var sql = getImageContextWithSize(fullSizeImage, ImageRepositorySql.FIND_IMAGE_CONTENT_BY_OFFENDER_NO);
            final var blob = jdbcTemplate.queryForObject(sql, createParams("offenderNo", offenderNo), Blob.class);
            if (blob != null) {
                final var length = (int) blob.length();
                content = blob.getBytes(1, length);
                blob.free();
            }
        } catch (final DataAccessException | SQLException ex) {
            content = null;
        }
        return content;
    }

    private String getImageContextWithSize(final boolean fullSizeImage, ImageRepositorySql imageContentQuery) {
        return format(imageContentQuery.getSql(), fullSizeImage ? "FULL_SIZE_IMAGE" : "THUMBNAIL_IMAGE");
    }

}
