package net.syscon.elite.service;


import net.syscon.elite.api.model.ImageDetail;
import net.syscon.elite.repository.jpa.model.OffenderImage;

import java.util.List;

public interface ImageService {

    List<OffenderImage> findOffenderImagesFor(String offenderNumber);

    ImageDetail findImageDetail(Long imageId);

    byte[] getImageContent(Long imageId, boolean fullSizeImage);

    byte[] getImageContent(String offenderNo, boolean fullSizeImage);
}

