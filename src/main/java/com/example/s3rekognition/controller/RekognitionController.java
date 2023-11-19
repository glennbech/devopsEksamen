package com.example.s3rekognition.controller;

import com.example.s3rekognition.model.PPEResponse;
import com.example.s3rekognition.service.PPEScannerService;
import io.micrometer.core.annotation.Timed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;




@RestController
public class RekognitionController implements ApplicationListener<ApplicationReadyEvent> {
    @Autowired
    private PPEScannerService ppeScannerService;



    /**
     * This endpoint takes an S3 bucket name in as an argument, scans all the
     * Files in the bucket for Protective Gear Violations.
     * <p>
     *
     * @param bucketName
     * @return
     */
    @GetMapping(value = "/scan-ppe", consumes = "*/*", produces = "application/json")
    @ResponseBody
    @Timed
    public ResponseEntity<PPEResponse> scanForPPE(@RequestParam String bucketName) {

        var images = ppeScannerService.getAllImagesInBucket(bucketName);
        ppeScannerService.setBodyPart("FACE_COVER");
        PPEResponse ppeResponse = ppeScannerService.scanImagesToFindViolations(bucketName, images);
        return ResponseEntity.ok(ppeResponse);
    }
    @GetMapping(value = "/scan-custom-ppe", consumes = "*/*", produces = "application/json")
    @ResponseBody
    public ResponseEntity<PPEResponse> scanForCustomPPE(@RequestParam String bucketName,@RequestParam String ppe) {

        var images = ppeScannerService.getAllImagesInBucket(bucketName);
        ppeScannerService.setBodyPart(ppe);
        PPEResponse ppeResponse = ppeScannerService.scanImagesToFindViolations(bucketName, images);
        return ResponseEntity.ok(ppeResponse);
    }
    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {

    }
}
