package com.example.s3rekognition.configuration;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class TimedConfig {
    @Autowired
    MeterRegistry registry;
    @Bean
    public TimedAspect timedAspect() {
        return new TimedAspect(registry);
    }

}