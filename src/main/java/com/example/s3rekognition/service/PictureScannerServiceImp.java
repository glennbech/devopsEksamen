package com.example.s3rekognition.service;

import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.*;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.example.s3rekognition.model.CameraScanResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class PictureScannerServiceImp implements PictureScannerService {
    @Autowired
    private AmazonS3 s3Client;
    @Autowired
    private  AmazonRekognition rekognitionClient;
    private final String collectionId = "users";
    @Value("${BUCKET_NAME:kandidat-id-2012}")
    private final String bucket = "kandidat-id-2012";
    private final String rawScanFolder = "camera/rawScans/";
    private final String employeeFolder = "camera/employee/";
    public PictureScannerServiceImp(){

    }
    /*
    * Used to recreate the collection on each restart for this demo
    *
    */
    @Override
    public  void createCollection(){
        ListCollectionsRequest listCollectionsRequest = new ListCollectionsRequest()
                .withMaxResults(10)
                .withNextToken(null);

        int size = rekognitionClient.listCollections(listCollectionsRequest).getCollectionIds().size();
        if (size >= 1) {
            DeleteCollectionRequest request = new DeleteCollectionRequest()
                    .withCollectionId(collectionId);
            rekognitionClient.deleteCollection(request);
        }
        rekognitionClient.createCollection(new CreateCollectionRequest().withCollectionId(collectionId));
    }
    /*
    * This is used to simulate a new image from the camera door gets uploaded to the rawScan folder for validating
    *
    */
    @Override
    public void copyRandomImageForScanning(String folder){

        ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(bucket).withPrefix(folder).withDelimiter("/");
        ListObjectsV2Result listing = s3Client.listObjectsV2(req);
        Random rand = new Random();
        int rndIndex = rand.nextInt(1,listing.getObjectSummaries().size());
        int index = 1;
        for (S3ObjectSummary summary: listing.getObjectSummaries()) {
            if (!summary.getKey().endsWith("/")){
                if (index == rndIndex){
                    s3Client.copyObject(bucket,summary.getKey(),bucket,rawScanFolder + summary.getKey().split("/")[2]);
                    return;
                }
                index++;
            }
        }
    }
    @Override
    public void addEmployeesToCollection(){
        ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(bucket).withPrefix(employeeFolder).withDelimiter("/");
        ListObjectsV2Result listing = s3Client.listObjectsV2(req);

        for (S3ObjectSummary summary: listing.getObjectSummaries()) {
            if (!summary.getKey().endsWith("/")){
                Image empImage = getImage(summary);
                IndexFacesRequest employee= new IndexFacesRequest().withImage(empImage)
                        .withQualityFilter(QualityFilter.AUTO)
                        .withMaxFaces(1)
                        .withCollectionId(collectionId)
                        .withExternalImageId(summary.getKey().split("/")[2])
                        .withDetectionAttributes("DEFAULT");
                IndexFacesResult indexFacesResult = rekognitionClient.indexFaces(employee);
                System.out.println("Results for " + summary.getKey());
                System.out.println("Faces indexed:");
                List<FaceRecord> faceRecords = indexFacesResult.getFaceRecords();
                for (FaceRecord faceRecord : faceRecords) {
                    System.out.println("  Face ID: " + faceRecord.getFace().getFaceId());
                    System.out.println("  Location:" + faceRecord.getFaceDetail().getBoundingBox().toString());
                }

                List<UnindexedFace> unindexedFaces = indexFacesResult.getUnindexedFaces();
                System.out.println("Faces not indexed:");
                for (UnindexedFace unindexedFace : unindexedFaces) {
                    System.out.println("  Location:" + unindexedFace.getFaceDetail().getBoundingBox().toString());
                    System.out.println("  Reasons:");
                    for (String reason : unindexedFace.getReasons()) {
                        System.out.println("   " + reason);
                    }
                }

            }

        }

    }
    @Override
    public CameraScanResponse imageScanningValidation() throws JsonProcessingException {
        var cameraScan = new CameraScanResponse();
        AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder.defaultClient();
        ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(bucket).withPrefix(rawScanFolder).withDelimiter("/");
        ListObjectsV2Result listing = s3Client.listObjectsV2(req);
        for (S3ObjectSummary summary: listing.getObjectSummaries()) {
            if (!summary.getKey().endsWith("/")){
                cameraScan.setEmployee(false);
                cameraScan.setFaceId("N/A");
                cameraScan.setExternalImageId(summary.getKey().split("/")[2]);
                cameraScan.setSimilarity(0);
                ObjectMapper objectMapper = new ObjectMapper();
                List<FaceMatch> faceImageMatches = getMatchingFaces(rekognitionClient, summary);
                s3Client.deleteObject(bucket,summary.getKey());

                if (faceImageMatches.isEmpty()){
                    System.out.println("IS EMPTY?");
                    return  cameraScan;
                }

                for (FaceMatch face: faceImageMatches) {

                    System.out.println(objectMapper.writerWithDefaultPrettyPrinter()
                            .writeValueAsString(face));
                    System.out.println();
                    if (face.getSimilarity() >= 90){
                        cameraScan.setEmployee(true);
                        cameraScan.setFaceId(face.getFace().getFaceId());
                        cameraScan.setExternalImageId(face.getFace().getExternalImageId());
                        cameraScan.setSimilarity(face.getSimilarity());
                        return cameraScan;

                    }
                }
                return  cameraScan;
            }
        }

        return  cameraScan;
    }

    private  List<FaceMatch> getMatchingFaces(AmazonRekognition rekognitionClient, S3ObjectSummary summary) {
        Image empImage = getImage(summary);
        SearchFacesByImageRequest searchFacesByImageRequest = buildSearchRequestForFaceMatch(empImage);
        SearchFacesByImageResult searchFacesByImageResult =
                rekognitionClient.searchFacesByImage(searchFacesByImageRequest);
        System.out.println("Faces matching largest face in image from " + summary.getKey());

        List < FaceMatch > faceImageMatches = searchFacesByImageResult.getFaceMatches();

        return faceImageMatches;
    }

    private Image getImage(S3ObjectSummary summary) {
        Image empImage = new Image()
                .withS3Object(new S3Object()
                        .withBucket(bucket)
                        .withName(summary.getKey()));
        return empImage;
    }

    private SearchFacesByImageRequest buildSearchRequestForFaceMatch(Image empImage) {
        SearchFacesByImageRequest searchFacesByImageRequest = new SearchFacesByImageRequest()
                .withCollectionId(collectionId)
                .withImage(empImage)
                .withFaceMatchThreshold(70F)
                .withMaxFaces(2);
        return searchFacesByImageRequest;
    }
}
