package net.syscon.elite.service;


import net.syscon.elite.api.model.ImageDetail;

import java.util.Optional;

public interface ImageService {

    ImageDetail findImageDetail(Long imageId);

    Optional<byte[]> getImageContent(Long imageId, boolean fullSizeImage);

    Optional<byte[]> getImageContent(String offenderNo, boolean fullSizeImage);
}

