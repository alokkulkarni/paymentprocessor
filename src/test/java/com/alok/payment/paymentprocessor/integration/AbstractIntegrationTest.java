package com.alok.payment.paymentprocessor.integration;

import com.alok.payment.paymentprocessor.service.AccountService;
import com.alok.payment.paymentprocessor.service.FraudService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Base class for integration tests using Testcontainers.
 * All integration tests should extend this class to get access to the PostgreSQL container.
 * Uses a singleton container pattern to avoid connection issues when Spring context is reused.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractIntegrationTest {

    protected static final PostgreSQLContainer<?> postgres;
    
    @Autowired
    private AccountService accountService;
    
    @Autowired
    private FraudService fraudService;

    static {
        postgres = new PostgreSQLContainer<>(DockerImageName.parse("ghcr.io/alokkulkarni/testcontainers-registry/testcontainers/postgres:16-alpine").asCompatibleSubstituteFor("postgres"))
                .withDatabaseName("paymentprocessor")
                .withUsername("test")
                .withPassword("test")
                .withInitScript("init.sql")
                .withReuse(true);
        postgres.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.sql.init.mode", () -> "never");
    }
    
    @BeforeEach
    void resetAccountBalances() {
        accountService.resetBalances();
        fraudService.setDeterministicMode(true);
    }
}
