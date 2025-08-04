package com.kimbap.kbs;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan({"com.kimbap.kbs.standard.mapper",
			 "com.kimbap.kbs.simjaejine.mapper",
			 "com.kimbap.kbs.order.mapper",
			 "com.kimbap.kbs.materials.mapper",
			 "com.kimbap.kbs.production.mapper",
			 "com.kimbap.kbs.product.mapper",
			 "com.kimbap.kbs.common.mapper",
			 "com.kimbap.kbs.payment.mapper",
			 "com.kimbap.kbs.dashboard.mapper",
		   "com.kimbap.kbs.distribution.mapper"})
@SpringBootApplication
public class KbsApplication {

	public static void main(String[] args) {
		SpringApplication.run(KbsApplication.class, args);
	}

}
