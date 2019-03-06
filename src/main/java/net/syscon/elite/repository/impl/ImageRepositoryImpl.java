package net.syscon.elite.repository.impl;

import net.syscon.elite.api.model.ImageDetail;
import net.syscon.elite.repository.ImageRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;

import java.sql.Blob;
import java.sql.SQLException;
import java.util.Optional;

@Repository
public class ImageRepositoryImpl extends RepositoryBase implements ImageRepository {

	@Override
	public Optional<ImageDetail> findImageDetail(final Long imageId) {
        final var sql = getQuery("FIND_IMAGE_DETAIL");
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

	@Override
	public byte[] getImageContent(final Long imageId) {
        byte[] content = null;
	    try {

            final var sql = getQuery("FIND_IMAGE_CONTENT");
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

	@Override
	public byte[] getImageContent(final String offenderNo) {
        byte[] content = null;
        try {
            final var sql = getQuery("FIND_IMAGE_CONTENT_BY_OFFENDER_NO");
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
}
