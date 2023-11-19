package com.example.s3rekognition.controller;

import com.example.s3rekognition.model.CameraScanResponse;
import com.example.s3rekognition.service.PictureScannerServiceImp;
import com.example.s3rekognition.service.TrackingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.aop.TimedAspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.Random;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.DistributionSummary;

@RestController
public class CameraController {
    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    TimedAspect timer;

    @Autowired
    private PictureScannerServiceImp pictureScannerServiceImp;
    @Autowired
    private TrackingService trackingService;

    @GetMapping(value = "/scan-private-entrance-automatic", consumes = "*/*", produces = "application/json")
    @ResponseBody
    @Timed
    public ResponseEntity<CameraScanResponse>  scanPrivateEntranceAutomatic()  {

        setupScannerService();
        simulateImageUploadToPrivateEntrenceCameraFolder();

        try {
            var response = pictureScannerServiceImp.scanImageAtPrivateEntrance();
            if (response.isEmployee()){
                if (meterRegistry.counter("unauthorized_scan_attempts").count()>0)
                    meterRegistry.counter("unauthorized_scan_attempts").increment(-meterRegistry.counter("unauthorized_scan_attempts").count());
                trackingService.addPerson(response);
            }
            else{
                meterRegistry.counter("unauthorized_scan_attempts").increment(1);
            }


            return ResponseEntity.ok(response);
        }
        catch (JsonProcessingException e){
            System.out.println("WHYYYYY");
            return  ResponseEntity.internalServerError().build();
        }

    }

    @GetMapping(value = "/scan-private-entrance-manual", consumes = "*/*", produces = "application/json")
    @ResponseBody
    public ResponseEntity<CameraScanResponse>  scanPrivateEntranceManual()  {
        setupScannerService();

        try {
            var response = pictureScannerServiceImp.scanImageAtPrivateEntrance();
            trackingService.addEmployee(response);

            return ResponseEntity.ok(response);
        }
        catch (JsonProcessingException e){
            System.out.println("WHYYYYY");
            return  ResponseEntity.internalServerError().build();
        }

    }
    @GetMapping(value = "/scan-exit-manual", consumes = "*/*", produces = "application/json")
    @ResponseBody
    public ResponseEntity<?>  scanExitManual()  {
        setupScannerService();

        try {
            var response = pictureScannerServiceImp.scanImageAtExit();
            var person = trackingService.removePerson(response.getFaceId());
            if (person == null){

               return  ResponseEntity.ok("Person never enter building or missing picture");
            }
            return ResponseEntity.ok(response);
        }
        catch (JsonProcessingException e){
            System.out.println("WHYYYYY");
            return  ResponseEntity.internalServerError().build();
        }

    }
    @GetMapping(value = "/scan-exit-automatic", consumes = "*/*", produces = "application/json")
    @ResponseBody
    public ResponseEntity<?>  scanExitAutomatic()  {
        setupScannerService();
        String PersonId = trackingService.getRandomFaceId();
        var person = trackingService.removePerson(PersonId);
        // TODO: move person to this folder for scanning


        if (PersonId == null){
            return ResponseEntity.ok("Building is Empty");
        }
        try {
            simulateUploadingPictureToPublicExitCameraFolder(person);
            var response = pictureScannerServiceImp.scanImageAtExit();

            return ResponseEntity.ok(response);
        }
        catch (JsonProcessingException e){
            System.out.println("WHYYYYY");
            return  ResponseEntity.internalServerError().build();
        }

    }



    @GetMapping(value = "/scan-public-entrance-manual", consumes = "*/*", produces = "application/json")
    @ResponseBody
    public ResponseEntity<CameraScanResponse>  scanPublicEntranceManual()  {
        setupScannerService();

        try {
            var response = pictureScannerServiceImp.scanImageAtPublicEntrance();
            trackingService.addPerson(response);

            return ResponseEntity.ok(response);
        }
        catch (JsonProcessingException e){
            System.out.println("WHYYYYY");
            return  ResponseEntity.internalServerError().build();
        }

    }
    @GetMapping(value = "/scan-public-entrance-automatic", consumes = "*/*", produces = "application/json")
    @ResponseBody
    public ResponseEntity<CameraScanResponse>  scanPublicEntranceAutomatic()  {
        setupScannerService();
        simulateImageUploadToPublicCameraFolder();

        try {
            var response = pictureScannerServiceImp.scanImageAtPublicEntrance();
            trackingService.addPerson(response);

            return ResponseEntity.ok(response);
        }
        catch (JsonProcessingException e){
            System.out.println("WHYYYYY");
            return  ResponseEntity.internalServerError().build();
        }

    }

    private void simulateImageUploadToPublicCameraFolder() {
        Random rand = new Random();
        int index = rand.nextInt(99);
        // for simulating picture upladed for scanning
        if (index < 30){
            pictureScannerServiceImp.copyImageToPublicEntranceFolder("camera/employee/");
        }
        else{
            pictureScannerServiceImp.copyImageToPublicEntranceFolder("camera/pictures/");
        }
    }

    private void setupScannerService() {

    }

    private void simulateImageUploadToPrivateEntrenceCameraFolder() {
        Random rand = new Random();
        int index = rand.nextInt(99);
        // for simulating picture upladed for scanning
        if (index < 50){
            pictureScannerServiceImp.copyRandomImageForScanning("camera/employee/");
        }
        else{
            pictureScannerServiceImp.copyRandomImageForScanning("camera/pictures/");
        }
    }
    private void simulateUploadingPictureToPublicExitCameraFolder(CameraScanResponse person) {

        pictureScannerServiceImp.copyImageToPublicExitFolder(person.getExternalImageId());
    }


}
