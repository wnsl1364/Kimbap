package com.kimbap.kbs;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan({"com.kimbap.kbs.standard.mapper",
			 "com.kimbap.kbs.simjaejine.mapper",
			 "com.kimbap.kbs.order.mapper",})
@SpringBootApplication
public class KbsApplication {

	public static void main(String[] args) {
		SpringApplication.run(KbsApplication.class, args);
	}

}
