package com.alok.payment.paymentprocessor.bdd;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

/**
 * Cucumber Test Runner using JUnit Platform Suite.
 * This is the standard entry point for BDD test execution.
 * 
 * Follows the pattern from beneficiaries repository for consistency.
 * 
 * To run BDD tests:
 * - Maven: mvn failsafe:integration-test -Dtest=CucumberTestRunner -B
 * - CI/CD: Uses the same command in GitHub Actions workflow
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.alok.payment.paymentprocessor.bdd")
public class CucumberTestRunner {
    // Empty class - JUnit Platform discovers and executes Cucumber scenarios
    // Test scenarios defined in .feature files
    // Step definitions in PaymentProcessingSteps.java
}
