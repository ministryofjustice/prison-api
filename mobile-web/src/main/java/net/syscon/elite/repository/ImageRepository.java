package net.syscon.elite.repository;


import jersey.repackaged.com.google.common.collect.ImmutableMap;
import net.syscon.elite.api.model.ImageDetail;
import net.syscon.elite.repository.mapping.FieldMapper;
import net.syscon.util.DateTimeConverter;

import java.util.Map;
import java.util.Optional;

public interface ImageRepository {

	Map<String, FieldMapper> IMAGE_SUMMARY_MAPPING = new ImmutableMap.Builder<String, FieldMapper>()
			.put("IMAGE_ID",            new FieldMapper("imageId"))
			.put("CAPTURE_DATE",        new FieldMapper("captureDate", DateTimeConverter::toISO8601LocalDate))
			.put("IMAGE_VIEW_TYPE",     new FieldMapper("imageView"))
			.put("ORIENTATION_TYPE",    new FieldMapper("imageOrientation"))
			.put("IMAGE_OBJECT_TYPE",   new FieldMapper("imageType"))
			.put("IMAGE_OBJECT_ID",     new FieldMapper("objectId")).build();

	Optional<ImageDetail> findImageDetail(Long imageId);
	byte[] getImageContent(Long imageId);


}

