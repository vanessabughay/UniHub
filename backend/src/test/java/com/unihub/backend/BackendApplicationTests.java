package com.unihub.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@Import(TestMailConfiguration.class)
@TestPropertySource(properties = {
        "spring.mail.username=test",
        "spring.mail.password=test"
})
class BackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
