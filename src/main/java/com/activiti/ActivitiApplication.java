package com.activiti;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@MapperScan("com.activiti.mapper")
@EnableSwagger2
@SpringBootApplication
public class ActivitiApplication {

	public static void main(String[] args) {

		SpringApplication.run(ActivitiApplication.class, args);
		System.out.println("你喔号哦较好皮带上");
	}
}
