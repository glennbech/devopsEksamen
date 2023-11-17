package com.example.s3rekognition.controller;

import com.example.s3rekognition.model.CameraScanResponse;
import com.example.s3rekognition.service.PictureScannerServiceImp;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;


import java.util.Random;

@RestController
public class CameraController {


    @Autowired
   private PictureScannerServiceImp pictureScannerServiceImp;

    @GetMapping(value = "/entrance-camera-scan", consumes = "*/*", produces = "application/json")
    @ResponseBody
    public ResponseEntity<CameraScanResponse>  scanPersonEnter()  {
        pictureScannerServiceImp.createCollection();
        pictureScannerServiceImp.addEmployeesToCollection();
        Random rand = new Random();
        int index = rand.nextInt(99);

        // 70/30 chance it adds a employee or a non employee
        if (index < 30){
            pictureScannerServiceImp.copyRandomImageForScanning("camera/employee/");
        }
        else{
            pictureScannerServiceImp.copyRandomImageForScanning("camera/pictures/");
        }

        //SCAN PICTURE

        try {
            var response = pictureScannerServiceImp.imageScanningValidation();
            return ResponseEntity.ok(response);
        }
        catch (JsonProcessingException e){
            System.out.println("WHYYYYY");
            return  ResponseEntity.internalServerError().build();
        }

    }
    @GetMapping(value = "/Exit-camera-scan", consumes = "*/*", produces = "application/json")
    @ResponseBody
    public String  scanPersonExit(@RequestParam String bucketName) {
        return "leave";
    }
}
