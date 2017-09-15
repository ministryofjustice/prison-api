package net.syscon.elite.service;


import net.syscon.elite.v2.api.model.ImageDetail;

public interface ImageService {

	ImageDetail findImageDetail(Long imageId);
	byte[] getImageContent(Long imageId);


}

