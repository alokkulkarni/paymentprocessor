package com.alok.payment.paymentprocessor.bdd;

import com.alok.payment.paymentprocessor.service.AccountService;
import com.alok.payment.paymentprocessor.service.FraudService;
import io.cucumber.java.Before;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Cucumber Spring configuration that integrates Cucumber with Spring Boot and Testcontainers.
 * This configuration is shared across all BDD tests.
 * 
 * Uses @ServiceConnection for automatic Spring Boot datasource configuration.
 * Follows the pattern from beneficiaries repository.
 */
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
public class CucumberSpringConfiguration {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("ghcr.io/alokkulkarni/testcontainers-registry/testcontainers/postgres:16-alpine").asCompatibleSubstituteFor("postgres"))
            .withDatabaseName("paymentprocessor")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("init.sql");
    
    @Autowired
    private AccountService accountService;
    
    @Autowired
    private FraudService fraudService;
    
    @Before
    public void resetAccountBalances() {
        accountService.resetBalances();
        fraudService.setDeterministicMode(true);
    }
}
