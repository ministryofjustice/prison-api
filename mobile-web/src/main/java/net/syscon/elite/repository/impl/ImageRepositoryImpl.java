package net.syscon.elite.repository.impl;

import net.syscon.elite.api.model.ImageDetail;
import net.syscon.elite.repository.ImageRepository;
import net.syscon.elite.repository.mapping.Row2BeanRowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Blob;
import java.sql.SQLException;
import java.util.Optional;

@Repository
public class ImageRepositoryImpl extends RepositoryBase implements ImageRepository {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Override
	public Optional<ImageDetail> findImageDetail(final Long imageId) {
		final String sql = getQuery("FIND_IMAGE_DETAIL");
		final RowMapper<ImageDetail> imageRowMapper = Row2BeanRowMapper.makeMapping(sql, ImageDetail.class, IMAGE_SUMMARY_MAPPING);
		ImageDetail imageDetail;
		try {
			imageDetail = jdbcTemplate.queryForObject(sql, createParams("imageId", imageId), imageRowMapper);
		} catch (EmptyResultDataAccessException e) {
			imageDetail = null;
		}
		return Optional.ofNullable(imageDetail);
	}

	@Override
	public byte[] getImageContent(final Long imageId) {
		try {
			byte[] content = null;
			final String sql = getQuery("FIND_IMAGE_CONTENT");
			final Blob blob = jdbcTemplate.queryForObject(sql, createParams("imageId", imageId), Blob.class);
			if (blob != null) {
				final int length = (int) blob.length();
				content = blob.getBytes(1, length);
				blob.free();
			}
			return content;
		} catch (final DataAccessException | SQLException ex) {
			log.error(ex.getMessage(), ex);
			throw new RecoverableDataAccessException(ex.getMessage(), ex);
		}
	}
}
