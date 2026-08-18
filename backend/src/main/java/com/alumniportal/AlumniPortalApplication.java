package com.alumniportal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AlumniPortalApplication {
    public static void main(String[] args) {
        SpringApplication.run(AlumniPortalApplication.class, args);
    }
}
