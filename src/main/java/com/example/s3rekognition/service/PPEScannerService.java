package com.example.s3rekognition.service;

import com.amazonaws.services.rekognition.model.DetectProtectiveEquipmentResult;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.example.s3rekognition.model.PPEResponse;

import java.util.List;

public interface PPEScannerService {
    PPEResponse scanImagesToFindViolations(String bucketName, List<S3ObjectSummary> images);

    List<S3ObjectSummary> getAllImagesInBucket(String bucketName);
    Void setBodyPart(String BodyPart);

    DetectProtectiveEquipmentResult detectFaceCoverProtectiveEquipment(String bucketName, S3ObjectSummary image);
}
