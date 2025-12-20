package com.medical.research;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
/**
 * @Auther: yilefeng
 * @Date: 2025/12/17 10:11
 * @Description:
 */
@SpringBootApplication
@MapperScan("com.medical.research.mapper")
public class MedicalResearchApplication {
    public static void main(String[] args) {
        SpringApplication.run(MedicalResearchApplication.class, args);
    }
}
