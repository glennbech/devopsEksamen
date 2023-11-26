package com.example.s3rekognition.configuration;

import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.example.s3rekognition.service.PictureScannerServiceImp;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {
    private static PictureScannerServiceImp pictureScannerServiceImp;
    @Bean
    AmazonS3 amazonS3Service(){
        return AmazonS3ClientBuilder.standard().build();
    }

    @Bean
    AmazonRekognition amazonRekognitionService(){
        return AmazonRekognitionClientBuilder.standard().build();
    }


}
