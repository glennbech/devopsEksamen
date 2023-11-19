package com.example.s3rekognition.service;

import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.model.*;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.example.s3rekognition.controller.RekognitionController;
import com.example.s3rekognition.model.PPEClassificationResponse;
import com.example.s3rekognition.model.PPEResponse;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class PPEScannerServiceImp implements PPEScannerService, ApplicationListener<ApplicationReadyEvent> {
    @Autowired
    private AmazonS3 s3Client;
    @Autowired
    private AmazonRekognition rekognitionClient;
    @Autowired
    private MeterRegistry meterRegistry;
    private String activeBodyCover;
    private String activeBody;
    private static final Logger logger = Logger.getLogger(PPEScannerServiceImp.class.getName());


    @Override
    public PPEResponse scanImagesToFindViolations(String bucketName, List<S3ObjectSummary> images) {
        // This will hold all of our classifications
        List<PPEClassificationResponse> classificationResponses = new ArrayList<>();

        // Iterate over each object and scan for PPE
        for (S3ObjectSummary image : images) {
            logger.info("scanning " + image.getKey());

            DetectProtectiveEquipmentResult result = detectFaceCoverProtectiveEquipment(bucketName, image);

            // If any person on an image lacks PPE on the face, it's a violation of regulations
            boolean violation = isViolation(result);

            logger.info("scanning " + image.getKey() + ", violation result " + violation);
            // Categorize the current image as a violation or not.
            int personCount = result.getPersons().size();
            PPEClassificationResponse classification = new PPEClassificationResponse(image.getKey(), personCount, violation);
            classificationResponses.add(classification);
        }
        PPEResponse ppeResponse = new PPEResponse(bucketName, classificationResponses);
        return ppeResponse;
    }
    @Override
    public List<S3ObjectSummary> getAllImagesInBucket(String bucketName){
        // List all objects in the S3 bucket
        ListObjectsV2Result imageList = s3Client.listObjectsV2(bucketName);
        List<S3ObjectSummary> images = imageList.getObjectSummaries().stream().filter(f->f.getKey().endsWith(".jpeg")|| f.getKey().endsWith(".jpg")).collect(Collectors.toList());
        // This is all the images in the bucket
        logger.info("gets here");
        return  images;
    }

    @Override
    public Void setBodyPart(String bodyPart) {
        bodyPart = bodyPart.toUpperCase();
        //FACE | HEAD | LEFT_HAND | RIGHT_HAND
        //"FACE_COVER | HAND_COVER | HEAD_COVER"
        switch (bodyPart){
            case "FACE_COVER":{
                activeBodyCover = bodyPart;
                activeBody = "FACE";
                break;
            }
            case "HEAD":{
                activeBodyCover = bodyPart;
                activeBody = "HEAD";
                break;
            }
            case "LEFT_HAND":{
                activeBodyCover = bodyPart;
                activeBody = "LEFT_HAND";
                break;
            }
            case "RIGHT_HAND":{
                activeBodyCover = bodyPart;
                activeBody = "RIGHT_HAND";
                break;
            }
            default:{
                activeBodyCover = bodyPart;
                activeBody = "FACE";
                break;
            }
        }
        return null;
    }

    /**
     * Detects if the image has a protective gear violation for the FACE bodypart-
     * It does so by iterating over all persons in a picture, and then again over
     * each body part of the person. If the body part is a FACE and there is no
     * protective gear on it, a violation is recorded for the picture.
     *
     * @param result
     * @return
     */
    private boolean isViolation(DetectProtectiveEquipmentResult result) {
        return result.getPersons().stream()
                .flatMap(p -> p.getBodyParts().stream())
                .anyMatch(bodyPart -> bodyPart.getName().equals(activeBody)
                        && bodyPart.getEquipmentDetections().isEmpty());
    }
    @Override
    public DetectProtectiveEquipmentResult detectFaceCoverProtectiveEquipment(String bucketName, S3ObjectSummary image) {
        // This is where the magic happens, use AWS rekognition to detect PPE
        DetectProtectiveEquipmentRequest request = new DetectProtectiveEquipmentRequest()
                .withImage(new Image()
                        .withS3Object(new S3Object()
                                .withBucket(bucketName)
                                .withName(image.getKey())))
                .withSummarizationAttributes(new ProtectiveEquipmentSummarizationAttributes()
                        .withMinConfidence(80f)
                        .withRequiredEquipmentTypes(activeBodyCover));

        DetectProtectiveEquipmentResult result = rekognitionClient.detectProtectiveEquipment(request);
        return result;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {

    }
}
