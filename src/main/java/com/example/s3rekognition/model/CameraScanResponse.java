package com.example.s3rekognition.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CameraScanResponse implements Serializable {
    boolean isEmployee;
    String faceId;
    String ExternalImageId;
    float Similarity;

}
