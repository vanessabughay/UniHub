package com.unihub.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.unihub.backend.config.TestPropertiesConfig;

@SpringBootTest
@ActiveProfiles("test") // opcional, mas bom já deixar
@Import({ TestMailConfiguration.class, TestPropertiesConfig.class })
@TestPropertySource(properties = {
        "spring.mail.username=test",
        "spring.mail.password=test"
})
class BackendApplicationTests {

    @Test
    void contextLoads() {
    }
}
