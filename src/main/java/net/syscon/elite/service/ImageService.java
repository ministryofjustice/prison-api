package net.syscon.elite.service;

import net.syscon.elite.api.model.ImageDetail;

import java.util.List;

public interface ImageService {

    List<ImageDetail> findOffenderImagesFor(String offenderNumber);

    ImageDetail findImageDetail(Long imageId);

    byte[] getImageContent(Long imageId, boolean fullSizeImage);

    byte[] getImageContent(String offenderNo, boolean fullSizeImage);
}

