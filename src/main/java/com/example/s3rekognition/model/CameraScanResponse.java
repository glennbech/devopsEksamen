package com.example.s3rekognition.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

import java.io.Serializable;

import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CameraScanResponse implements Serializable {
    boolean isEmployee;
    String faceId;
    String externalImageId;
    float similarity;
    LocalDateTime enter;
    LocalDateTime leave;



}
