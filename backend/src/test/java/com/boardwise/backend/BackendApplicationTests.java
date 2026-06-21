package com.boardwise.backend;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/*
 * FIX 1 — The bare @SpringBootTest had no MongoDB connection, causing the context
 * to fail immediately because Spring tries to create a MongoClient from the placeholder
 * value in application.properties. A @Container with @ServiceConnection overrides the
 * MongoDB URI automatically before the context starts.
 *
 * FIX 2 — ListingService uses @Value("${r2.bucket-listings}") (mapped from the env var
 * R2_BUCKET_LISTINGS). Without this property, the context fails to initialise the bean.
 * The same applies to the other R2 and JWT properties already used by LockManagerIntegrationTest.
 */
@SpringBootTest(properties = {
		"r2.access-key=test-access-key",
		"r2.secret-key=test-secret-key",
		"r2.account-id=test-account-id",
		"r2.bucket-listings=test-listings-bucket",
		"r2.bucket-profiles=test-profiles-bucket",
		"jwt.secret=test-secret-key-that-is-long-enough-for-hmac",
		"r2.dev-url=http://localhost:9000"
})
@Testcontainers
class BackendApplicationTests {

	@Container
	@ServiceConnection
	static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:latest");

	@Test
	void contextLoads() {
	}
}