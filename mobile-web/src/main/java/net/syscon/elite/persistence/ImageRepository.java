package net.syscon.elite.persistence;


import net.syscon.elite.api.model.ImageDetail;

import java.util.Optional;

public interface ImageRepository {

	Optional<ImageDetail> findImageDetail(Long imageId);
	byte[] getImageContent(Long imageId);


}

