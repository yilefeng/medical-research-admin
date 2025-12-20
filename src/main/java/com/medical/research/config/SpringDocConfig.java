package com.medical.research.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc/Swagger配置
 */
@Configuration
public class SpringDocConfig {

    @Bean
    public OpenAPI medicalResearchOpenAPI() {
        // JWT安全配置
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .name("Authorization")
                .in(SecurityScheme.In.HEADER);

        return new OpenAPI()
                // 接口文档基本信息
                .info(new Info()
                        .title("医疗科研管理系统API文档")
                        .description("医疗科研管理系统，包含实验方案、科研数据、统计报告、数据源、用户权限等核心模块")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("研发团队")
                                .email("dev@medical-research.com")
                                .url("http://localhost:8080/api/swagger-ui.html"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                // 全局JWT认证
                .addSecurityItem(new SecurityRequirement().addList("Authorization"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("Authorization", securityScheme));
    }
}