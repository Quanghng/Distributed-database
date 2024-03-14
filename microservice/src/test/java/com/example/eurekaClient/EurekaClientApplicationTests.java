package com.example.eurekaClient;

import com.example.eurekaClient.db.Database;
import com.example.eurekaClient.service.RequestService;
import com.example.eurekaClient.service.MicroserviceService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import({MicroserviceService.class, RequestService.class, Database.class})
class EurekaClientApplicationTests {

	@Test
	void contextLoads() {
	}

}
