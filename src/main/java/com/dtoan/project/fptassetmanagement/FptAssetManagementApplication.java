package com.dtoan.project.fptassetmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FptAssetManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(FptAssetManagementApplication.class, args);
    }

}
