package com.yanleaf.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
	@Bean
	public OpenAPI customOpenApi(){
		return new OpenAPI ().info (
				new Info ().title ("基于SpringBoot+Vue的宠物用品购物网站的设计与实现")
						.description ("集成Swagger/OpenAPI 规范的 API 文档增强工具 Knife4j，实现Java 项目接口文档的生成和管理")
						.version ("1.0.0")
						.contact (new Contact ().name ("周博"))
		);
	}
}
