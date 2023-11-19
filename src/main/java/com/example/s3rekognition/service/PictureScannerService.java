package com.example.s3rekognition.service;

import com.example.s3rekognition.model.CameraScanResponse;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface PictureScannerService {
    /*
     * Used to recreate the collection on each restart for this demo
     *
     */
    void createEmployeeCollection();

    /*
     * This is used to simulate a new image from the camera door gets uploaded to the rawScan folder for validating
     *
     */
    void copyRandomImageForScanning(String folder);
    void addEmployeesToCollection();
    CameraScanResponse imageScanningValidationByCameraLocation(String cameraLocation) throws JsonProcessingException;
}
