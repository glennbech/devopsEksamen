package com.example.s3rekognition.service;

import com.example.s3rekognition.model.CameraScanResponse;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

@Service
public class TrackingService implements  ApplicationListener<ApplicationReadyEvent> {
    @Autowired
    private MeterRegistry meterRegistry;
    private static final Logger logger = Logger.getLogger(TrackingService.class.getName());
    HashMap<String,CameraScanResponse> activeEmployeeMap = new HashMap<>();
    HashMap<String,CameraScanResponse> activePersonMap = new HashMap<>();
    private DistributionSummary timeSpentInBuilding;
    public void addEmployee(CameraScanResponse person){

        if (person.isEmployee()){
            activeEmployeeMap.putIfAbsent(person.getFaceId(), person);
            logger.info("Person is Employee, added to active list whit face id: " + person.getFaceId());
        }
        else {
            logger.warning("User not employee");
        }



    }
    public void addPerson(CameraScanResponse person){
        if (!person.isEmployee())
            person.setFaceId("person-"+person.getSimilarity() + person.getExternalImageId().hashCode()+ person.isEmployee());
        person.setEnter(LocalDateTime.now());
        logger.info("Person added to active list whit face id: "+person.getFaceId());
        activePersonMap.putIfAbsent(person.getFaceId(), person);
        addEmployee(person);
    }
    public String getRandomFaceId(){
        if (!activePersonMap.isEmpty()){
            var keys = activePersonMap.keySet();
            int size = keys.size();
            Random rand = new Random();
            int index = rand.nextInt(size);
            var array = keys.toArray(new String[keys.size()]);
            logger.info("Person added to active list");
            return array[index];
        }
        else
            return null;

    }
    public CameraScanResponse removePerson(String faceId){
        if (activePersonMap.isEmpty()){
            logger.info("Building is empty");
            return null;
        }
        else if (activeEmployeeMap.get(faceId) != null){
            var user= activeEmployeeMap.remove(faceId);
            user.setLeave(LocalDateTime.now());
            sendTotalTimeSpent(user);
            activePersonMap.remove(faceId);
            logger.info("Person leaving was an employee whit face id:" + user.getFaceId());
            return user;
        }
        else if (activePersonMap.get(faceId) != null){
            var user= activePersonMap.remove(faceId);
            user.setLeave(LocalDateTime.now());
            sendTotalTimeSpent(user);
            activePersonMap.remove(faceId);
            logger.info("Person left building whit face id:" + user.getFaceId());
            return  user;
        }
        else
            return null;
    }

    private void sendTotalTimeSpent(CameraScanResponse user) {
        Duration duration = Duration.between(user.getEnter(), user.getLeave());
        timeSpentInBuilding.record(duration.getSeconds());
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        Gauge.builder("person_count", activePersonMap,
                b -> b.values().size())
                .description("Number of person inside building including employees")
                .register(meterRegistry);


        Gauge.builder("employee_count", activeEmployeeMap,
                b ->  b.values().size())
                .description("Number of employee at work")
                .register(meterRegistry)
        ;

        Gauge.builder("customer_count", activePersonMap,
                        b -> b.values()
                                .stream()
                                .filter(person -> !person.isEmployee())
                                .count())
                .description("Number of non-employee customers inside building")

                .register(meterRegistry);
        timeSpentInBuilding =DistributionSummary
                .builder("user_tracking")
                .register(meterRegistry);
    }
}
