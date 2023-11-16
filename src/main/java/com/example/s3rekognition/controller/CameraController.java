package com.example.s3rekognition.controller;

import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.example.s3rekognition.model.PPEResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

public class CameraController {
    private final AmazonS3 s3Client;
    private final AmazonRekognition rekognitionClient;

    public CameraController(){
        this.s3Client = AmazonS3ClientBuilder.standard().build();
        this.rekognitionClient = AmazonRekognitionClientBuilder.standard().build();
    }
    @GetMapping(value = "/entrance-camera-scan", consumes = "*/*", produces = "application/json")
    @ResponseBody
    public String  scanPersonEnter(@RequestParam String bucketName) {

        return "enter";
    }
    @GetMapping(value = "/Exit-camera-scan", consumes = "*/*", produces = "application/json")
    @ResponseBody
    public String  scanPersonExit(@RequestParam String bucketName) {
        return "leave";
    }
}
