package com.example.industrialdefectiveproductinspection;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;

@MapperScan("com.example.industrialdefectiveproductinspection.mapper")
@SpringBootApplication
public class IndustrialDefectiveProductInspectionApplication {

    public static void main(String[] args) {
        SpringApplication.run(IndustrialDefectiveProductInspectionApplication.class, args);
    }

}
