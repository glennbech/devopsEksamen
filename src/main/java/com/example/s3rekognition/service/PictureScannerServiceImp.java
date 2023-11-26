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
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.LongTaskTimer;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class PictureScannerServiceImp implements PictureScannerService, ApplicationListener<ApplicationReadyEvent> {
    @Autowired
    private AmazonS3 s3Client;

    @Autowired
    private MeterRegistry meterRegistry;
    @Autowired
    private  AmazonRekognition rekognitionClient;
    private final String collectionId = "users";
    @Value("${BUCKET_NAME:kandidat-id-2012}")
    private final String bucket = "kandidat-id-2012";
    private final String privateEntrance = "camera/private/";
    private final String employeeFolder = "camera/employee/";
    private final String publicEntrance = "camera/entrance/";
    private final String publicExit = "camera/exit/";
    private static final Logger logger = Logger.getLogger(PictureScannerServiceImp.class.getName());
    public PictureScannerServiceImp(){

    }
    /*
    * Used to recreate the collection on each restart for this demo
    *
    */
    @Override
    public  void createEmployeeCollection(){
        ListCollectionsRequest listCollectionsRequest = new ListCollectionsRequest()
                .withMaxResults(100)
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
        var list = listing.getObjectSummaries().stream().filter(i ->!i.getKey().endsWith("/") && !i.getKey().endsWith(".txt")).collect(Collectors.toList());

        int rndIndex = rand.nextInt(0,list.size());
        int index = 0;
        for (S3ObjectSummary summary: list) {
            if (index == rndIndex ){
                s3Client.copyObject(bucket,summary.getKey(),bucket, privateEntrance + summary.getKey().split("/")[2]);
                return;
            }
            index++;
        }
    }
    public void copyImageToPublicEntranceFolder(String source){
        copyImageToDestinationFolder(source,publicEntrance);
    }

    public void copyImageToPublicExitFolder(String name){

        s3Client.copyObject(bucket,"camera/pictures/"+name,bucket,publicExit + name);
    }
    public void copyImageToDestinationFolder(String source,String destination ){
        ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(bucket).withPrefix(source).withDelimiter("/");
        ListObjectsV2Result listing = s3Client.listObjectsV2(req);
        var items = listing.getObjectSummaries().stream().filter(i ->!i.getKey().endsWith("/") && !i.getKey().endsWith(".txt")).collect(Collectors.toList());
        Random rand = new Random();
        int rndIndex = rand.nextInt(0,listing.getObjectSummaries().size());
        int index = 0;
        for (S3ObjectSummary summary: items) {

            if (!summary.getKey().endsWith("/")){
                if (index == rndIndex){
                    s3Client.copyObject(bucket,summary.getKey(),bucket,destination + summary.getKey().split("/")[2]);
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
        var list = listing.getObjectSummaries().stream().filter(i ->!i.getKey().endsWith("/") && !i.getKey().endsWith(".txt")).collect(Collectors.toList());

        for (S3ObjectSummary summary: list) {
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
                    // TODO: SEND MAIL IF IMAGE DID NOT WORK
                    System.out.println("  Location:" + unindexedFace.getFaceDetail().getBoundingBox().toString());
                    System.out.println("  Reasons:");
                    for (String reason : unindexedFace.getReasons()) {
                        System.out.println("   " + reason);
                    }
                }

            }

        }

    }

    public CameraScanResponse scanImageAtPrivateEntrance() throws JsonProcessingException {
        LongTaskTimer imageS3Timer = LongTaskTimer
                .builder("scanImageAtPrivateEntranceTimer")
                .register(meterRegistry);
        var imageloadTimer = imageS3Timer.start();
        var resp= imageScanningValidationByCameraLocation(privateEntrance);
        meterRegistry.timer("scan_image_at_private_entrance_timer").record(imageloadTimer.stop(), TimeUnit.NANOSECONDS);

        return  resp;
    }
    public CameraScanResponse scanImageAtExit() throws JsonProcessingException {
        return imageScanningValidationByCameraLocation(publicExit);
    }
    public CameraScanResponse scanImageAtPublicEntrance() throws JsonProcessingException {
        return imageScanningValidationByCameraLocation(publicEntrance);
    }
    @Override
    public CameraScanResponse imageScanningValidationByCameraLocation(String cameraLocation) throws JsonProcessingException {
        LongTaskTimer imageS3Timer = LongTaskTimer
                .builder("imageScanningValidationByCameraLocationTimer")
                .register(meterRegistry);
        var imageloadTimer = imageS3Timer.start();
        List<CameraScanResponse> allMatches = new ArrayList<>();
        AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder.defaultClient();
        ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(bucket).withPrefix(cameraLocation).withDelimiter("/");
        ListObjectsV2Result listing = s3Client.listObjectsV2(req);
        var list = listing.getObjectSummaries().stream().filter(i ->!i.getKey().endsWith("/") && !i.getKey().endsWith(".txt")).collect(Collectors.toList());

        meterRegistry.timer("image_retrive_timer").record(imageloadTimer.stop(), TimeUnit.NANOSECONDS);
        var imageLoopTimer = imageS3Timer.start();
        for (S3ObjectSummary summary: list) {
            if (!summary.getKey().endsWith("/") || !summary.getKey().endsWith(".txt")){

                ObjectMapper objectMapper = new ObjectMapper();

                List<FaceMatch> faceImageMatches = getMatchingFaces(rekognitionClient, summary);


                s3Client.deleteObject(bucket,summary.getKey());
                if (faceImageMatches.isEmpty()){
                    var cameraScan = new CameraScanResponse();
                    cameraScan.setEmployee(false);
                    cameraScan.setFaceId("N/A");
                    cameraScan.setExternalImageId(summary.getKey().split("/")[2]);
                    cameraScan.setSimilarity(0);
                    System.out.println("IS EMPTY?");

                    return  cameraScan;
                }

                for (FaceMatch face: faceImageMatches) {

                    System.out.println(objectMapper.writerWithDefaultPrettyPrinter()
                            .writeValueAsString(face));
                    System.out.println();
                    if (face.getSimilarity() >= 90){
                        var cameraScan = new CameraScanResponse();

                        cameraScan.setEmployee(true);
                        cameraScan.setFaceId(face.getFace().getFaceId());
                        cameraScan.setExternalImageId(face.getFace().getExternalImageId());
                        cameraScan.setSimilarity(face.getSimilarity());
                        allMatches.add(cameraScan);
                    }else{
                        var cameraScan = new CameraScanResponse();
                        cameraScan.setEmployee(false);
                        cameraScan.setFaceId(face.getFace().getFaceId());
                        cameraScan.setExternalImageId(face.getFace().getExternalImageId());
                        cameraScan.setSimilarity(face.getSimilarity());
                        allMatches.add(cameraScan);
                    }
                }

            }

        }

        meterRegistry.timer("Image_loop_timer").record(imageLoopTimer.stop(), TimeUnit.NANOSECONDS);
        if (allMatches.size() == 1)
            return  allMatches.stream().findFirst().orElse(new CameraScanResponse());
        else{

            return allMatches.stream().max(Comparator.comparing(i->i.getSimilarity())).orElse(new CameraScanResponse());

        }
    }
    @Timed
    private  List<FaceMatch> getMatchingFaces(AmazonRekognition rekognitionClient, S3ObjectSummary summary) {
        LongTaskTimer imageS3Timer = LongTaskTimer
                .builder("getMatchingFacesTimer")
                .register(meterRegistry);
        var getMatchingFacesTimer = imageS3Timer.start();
        Image empImage = getImage(summary);
        SearchFacesByImageRequest searchFacesByImageRequest = buildSearchRequestForFaceMatch(empImage);
        SearchFacesByImageResult searchFacesByImageResult =
                rekognitionClient.searchFacesByImage(searchFacesByImageRequest);
        System.out.println("Faces matching largest face in image from " + summary.getKey());

        List < FaceMatch > faceImageMatches = searchFacesByImageResult.getFaceMatches();
        meterRegistry.timer("get_matching_faces_timer").record(getMatchingFacesTimer.stop(), TimeUnit.NANOSECONDS);

        return faceImageMatches;
    }

    private Image getImage(S3ObjectSummary summary) {
        LongTaskTimer imageS3Timer = LongTaskTimer
                .builder("getImageTimer")
                .register(meterRegistry);
        var getImageTimer = imageS3Timer.start();
        Image empImage = new Image()
                .withS3Object(new S3Object()
                        .withBucket(bucket)
                        .withName(summary.getKey()));
        meterRegistry.timer("get_image_timer").record(getImageTimer.stop(), TimeUnit.NANOSECONDS);

        return empImage;
    }

    private SearchFacesByImageRequest buildSearchRequestForFaceMatch(Image empImage) {
        LongTaskTimer imageS3Timer = LongTaskTimer
                .builder("buildSearchRequestForFaceMatch")
                .register(meterRegistry);
        var buildSearchRequestForFaceMatchTimer = imageS3Timer.start();
        SearchFacesByImageRequest searchFacesByImageRequest = new SearchFacesByImageRequest()
                .withCollectionId(collectionId)
                .withImage(empImage)
                .withFaceMatchThreshold(70F)
                .withMaxFaces(2);
        meterRegistry.timer("get_matching_faces_timer").record(buildSearchRequestForFaceMatchTimer.stop(), TimeUnit.NANOSECONDS);
        return searchFacesByImageRequest;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        this.createEmployeeCollection();
        this.addEmployeesToCollection();
    }
}
