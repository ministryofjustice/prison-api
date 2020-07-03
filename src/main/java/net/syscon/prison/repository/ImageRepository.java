package net.syscon.prison.repository;


import net.syscon.prison.api.model.ImageDetail;
import net.syscon.prison.repository.mapping.StandardBeanPropertyRowMapper;

import java.util.Optional;

public interface ImageRepository {

    StandardBeanPropertyRowMapper<ImageDetail> IMAGE_DETAIL_MAPPER = new StandardBeanPropertyRowMapper<>(ImageDetail.class);

    Optional<ImageDetail> findImageDetail(Long imageId);

    byte[] getImageContent(Long imageId, boolean fullSizeImage);

    byte[] getImageContent(final String offenderNo, boolean fullSizeImage);
}

