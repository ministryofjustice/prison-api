package net.syscon.elite.persistence.impl;

import jersey.repackaged.com.google.common.collect.ImmutableMap;
import net.syscon.elite.persistence.ImageRepository;
import net.syscon.elite.persistence.mapping.FieldMapper;
import net.syscon.elite.persistence.mapping.Row2BeanRowMapper;
import net.syscon.elite.web.api.model.ImageDetail;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Blob;
import java.sql.SQLException;
import java.util.Map;

@Repository
public class ImageRepositoryImpl extends RepositoryBase implements ImageRepository {



	private final Map<String, FieldMapper> imageSummaryMapping = new ImmutableMap.Builder<String, FieldMapper>()
		.put("IMAGE_ID",            new FieldMapper("imageId"))
		.put("CAPTURE_DATE",        new FieldMapper("captureDate"))
		.put("IMAGE_VIEW_TYPE",     new FieldMapper("imageView"))
		.put("ORIENTATION_TYPE",    new FieldMapper("imageOrientation"))
		.put("IMAGE_OBJECT_TYPE",   new FieldMapper("imageType"))
		.put("IMAGE_OBJECT_ID",     new FieldMapper("objectId")).build();


	@Override
	public ImageDetail findImageDetail(Long imageId) {
		String sql = getQuery("FIND_IMAGE_DETAIL");
		RowMapper<ImageDetail> imageRowMapper = Row2BeanRowMapper.makeMapping(sql, ImageDetail.class, imageSummaryMapping);
		return jdbcTemplate.queryForObject(sql, createParams("imageId", imageId), imageRowMapper);
	}

	@Override
	public byte[] getImageContent(Long imageId) {
		try {
			byte content[] = null;
			String sql = getQuery("FIND_IMAGE_CONTENT");
			Blob blob = jdbcTemplate.queryForObject(sql, createParams("imageId", imageId), Blob.class);
			if (blob != null) {
				int length = (int) blob.length();
				content = blob.getBytes(1, length);
				blob.free();
			}
			return content;
		} catch (SQLException ex) {
			throw new RecoverableDataAccessException(ex.getMessage(), ex);
		}
	}
}


