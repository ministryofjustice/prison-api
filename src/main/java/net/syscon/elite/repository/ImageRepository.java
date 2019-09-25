package net.syscon.elite.repository;


import net.syscon.elite.api.model.ImageDetail;
import net.syscon.elite.repository.mapping.StandardBeanPropertyRowMapper;

import java.util.Optional;

public interface ImageRepository {

    StandardBeanPropertyRowMapper<ImageDetail> IMAGE_DETAIL_MAPPER = new StandardBeanPropertyRowMapper<>(ImageDetail.class);

    Optional<ImageDetail> findImageDetail(Long imageId);

    byte[] getImageContent(Long imageId, boolean fullSizeImage);

    byte[] getImageContent(final String offenderNo, boolean fullSizeImage);
}

