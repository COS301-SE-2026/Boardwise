package com.boardwise.backend;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Disabled("Requires MongoDB connection") // Unit tests don't need actual connection
class BackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
