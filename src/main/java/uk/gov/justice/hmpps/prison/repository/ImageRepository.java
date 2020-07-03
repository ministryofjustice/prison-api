package uk.gov.justice.hmpps.prison.repository;


import uk.gov.justice.hmpps.prison.api.model.ImageDetail;
import uk.gov.justice.hmpps.prison.repository.mapping.StandardBeanPropertyRowMapper;

import java.util.Optional;

public interface ImageRepository {

    StandardBeanPropertyRowMapper<ImageDetail> IMAGE_DETAIL_MAPPER = new StandardBeanPropertyRowMapper<>(ImageDetail.class);

    Optional<ImageDetail> findImageDetail(Long imageId);

    byte[] getImageContent(Long imageId, boolean fullSizeImage);

    byte[] getImageContent(final String offenderNo, boolean fullSizeImage);
}

